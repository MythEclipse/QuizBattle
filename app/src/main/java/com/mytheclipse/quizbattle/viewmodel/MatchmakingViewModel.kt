package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.remote.websocket.WebSocketManager
import com.mytheclipse.quizbattle.data.repository.MatchmakingEvent
import com.mytheclipse.quizbattle.data.repository.MatchmakingRepository
import com.mytheclipse.quizbattle.data.repository.TokenRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for Matchmaking screen
 */
data class MatchmakingState(
    val isSearching: Boolean = false,
    val queuePosition: Int = 0,
    val estimatedWaitTime: Int = 0,
    val matchFound: MatchFoundData? = null,
    val confirmRequest: ConfirmRequestData? = null,
    val confirmStatus: ConfirmStatusData? = null,
    val searchStartTime: Long? = null,
    val error: String? = null
) {
    /** Calculate elapsed search time in seconds */
    val elapsedSearchTimeSeconds: Long
        get() = searchStartTime?.let { 
            (System.currentTimeMillis() - it) / 1000 
        } ?: 0
    
    /** Check if match requires confirmation */
    val needsConfirmation: Boolean get() = confirmRequest != null
    
    /** Check if match is ready to start */
    val isMatchReady: Boolean get() = matchFound != null
}

/**
 * Data class for match found information
 */
data class MatchFoundData(
    val matchId: String,
    val opponentName: String,
    val opponentLevel: Int,
    val opponentAvatar: String?,
    val difficulty: String,
    val category: String
)

/**
 * Data class for match confirmation request
 */
data class ConfirmRequestData(
    val matchId: String,
    val opponentName: String,
    val opponentLevel: Int,
    val opponentPoints: Int,
    val opponentAvatar: String?,
    val difficulty: String,
    val category: String,
    val totalQuestions: Int,
    val timePerQuestion: Int,
    val expiresIn: Long
) {
    /** Check if confirmation has expired */
    val isExpired: Boolean
        get() = System.currentTimeMillis() > expiresIn
}

/**
 * Data class for confirmation status
 */
data class ConfirmStatusData(
    val matchId: String,
    val status: String,
    val confirmedCount: Int,
    val totalPlayers: Int
) {
    val isBothConfirmed: Boolean get() = status == STATUS_BOTH_CONFIRMED
    val isRejected: Boolean get() = status == STATUS_REJECTED
    val isTimeout: Boolean get() = status == STATUS_TIMEOUT
    
    companion object {
        const val STATUS_BOTH_CONFIRMED = "both_confirmed"
        const val STATUS_REJECTED = "rejected"
        const val STATUS_TIMEOUT = "timeout"
    }
}

/**
 * One-time events for Matchmaking screen
 */
sealed class MatchmakingUiEvent {
    data class MatchReady(val matchId: String) : MatchmakingUiEvent()
    data class ShowError(val message: String) : MatchmakingUiEvent()
    data object SearchCancelled : MatchmakingUiEvent()
    data object ConfirmationExpired : MatchmakingUiEvent()
}

/**
 * ViewModel for Matchmaking functionality
 */
class MatchmakingViewModel(application: Application) : AndroidViewModel(application) {
    
    // region Dependencies
    private val tokenRepository = TokenRepository(application)
    private val matchmakingRepository = MatchmakingRepository()
    private val webSocketManager = WebSocketManager.getInstance()
    // endregion
    
    // region State
    private val _state = MutableStateFlow(MatchmakingState())
    val state: StateFlow<MatchmakingState> = _state.asStateFlow()
    
    private val _events = MutableSharedFlow<MatchmakingUiEvent>()
    val events: SharedFlow<MatchmakingUiEvent> = _events.asSharedFlow()
    
    /** Cached search parameters for reconnection */
    private var lastDifficulty: String? = null
    private var lastCategory: String? = null
    // endregion
    
    // region Exception Handler
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logError("Coroutine error", throwable)
        updateState { copy(error = throwable.message) }
    }
    // endregion
    
    init {
        observeMatchmakingEvents()
        observeConnectionState()
    }
    
    // region Public Actions
    
    fun connectWebSocket() {
        launchSafely {
            val userId = getUserIdOrReturn() ?: return@launchSafely
            val token = tokenRepository.getToken() ?: return@launchSafely
            val username = tokenRepository.getUserName() ?: DEFAULT_USERNAME
            val deviceId = tokenRepository.getOrCreateDeviceId()
            webSocketManager.connect(userId, token, username, deviceId)
        }
    }
    
    fun findMatch(difficulty: String? = null, category: String? = null) {
        lastDifficulty = difficulty
        lastCategory = category
        
        launchSafely {
            val userId = getUserIdOrReturn() ?: return@launchSafely
            matchmakingRepository.findMatch(userId, MATCH_MODE_CASUAL, difficulty, category)
            updateState {
                copy(isSearching = true, searchStartTime = System.currentTimeMillis())
            }
        }
    }
    
    fun confirmMatch(matchId: String, accept: Boolean) {
        launchSafely {
            val userId = getUserIdOrReturn() ?: return@launchSafely
            matchmakingRepository.confirmMatch(userId, matchId, accept)
            
            if (!accept) {
                clearConfirmState()
            }
        }
    }
    
    fun cancelMatchmaking() {
        launchSafely {
            val userId = getUserIdOrReturn() ?: return@launchSafely
            matchmakingRepository.cancelMatchmaking(userId)
            resetState()
            emitEvent(MatchmakingUiEvent.SearchCancelled)
        }
    }
    
    fun clearMatchFound() {
        updateState { copy(matchFound = null) }
    }
    
    fun clearConfirmRequest() {
        clearConfirmState()
    }
    
    fun clearError() {
        updateState { copy(error = null) }
    }
    
    // endregion
    
    // region Private Methods
    
    private fun observeConnectionState() {
        launchSafely {
            webSocketManager.connectionState.collect { connectionState ->
                handleConnectionStateChange(connectionState)
            }
        }
    }
    
    private suspend fun handleConnectionStateChange(connectionState: WebSocketManager.ConnectionState) {
        if (connectionState !is WebSocketManager.ConnectionState.Connected) return
        if (!_state.value.isSearching) return
        
        val userId = getUserIdOrReturn() ?: return
        
        // Add delay to ensure auth message is sent first
        delay(RECONNECT_DELAY)
        
        // Verify still searching after delay
        if (_state.value.isSearching) {
            logDebug("Reconnected - resuming matchmaking search")
            matchmakingRepository.findMatch(userId, MATCH_MODE_CASUAL, lastDifficulty, lastCategory)
        }
    }
    
    private fun observeMatchmakingEvents() {
        launchSafely {
            matchmakingRepository.observeMatchmakingEvents().collect { event ->
                handleMatchmakingEvent(event)
            }
        }
    }
    
    private fun handleMatchmakingEvent(event: MatchmakingEvent) {
        when (event) {
            is MatchmakingEvent.Searching -> handleSearching(event)
            is MatchmakingEvent.MatchFound -> handleMatchFound(event)
            is MatchmakingEvent.ConfirmRequest -> handleConfirmRequest(event)
            is MatchmakingEvent.ConfirmStatus -> handleConfirmStatus(event)
            is MatchmakingEvent.Cancelled -> handleCancelled()
            else -> logDebug("Unhandled matchmaking event: $event")
        }
    }
    
    private fun handleSearching(event: MatchmakingEvent.Searching) {
        updateState {
            copy(
                isSearching = true,
                queuePosition = event.queuePosition,
                estimatedWaitTime = event.estimatedWaitTime
            )
        }
    }
    
    private fun handleMatchFound(event: MatchmakingEvent.MatchFound) {
        val matchData = MatchFoundData(
            matchId = event.matchId,
            opponentName = event.opponentName,
            opponentLevel = event.opponentLevel,
            opponentAvatar = event.opponentAvatar,
            difficulty = event.difficulty,
            category = event.category
        )
        
        updateState {
            copy(
                isSearching = false,
                searchStartTime = null,
                matchFound = matchData
            )
        }
        
        emitEvent(MatchmakingUiEvent.MatchReady(event.matchId))
    }
    
    private fun handleConfirmRequest(event: MatchmakingEvent.ConfirmRequest) {
        val confirmData = ConfirmRequestData(
            matchId = event.matchId,
            opponentName = event.opponentName,
            opponentLevel = event.opponentLevel,
            opponentPoints = event.opponentPoints,
            opponentAvatar = event.opponentAvatar,
            difficulty = event.difficulty,
            category = event.category,
            totalQuestions = event.totalQuestions,
            timePerQuestion = event.timePerQuestion,
            expiresIn = event.expiresIn
        )
        
        updateState {
            copy(
                isSearching = false,
                searchStartTime = null,
                confirmRequest = confirmData
            )
        }
    }
    
    private fun handleConfirmStatus(event: MatchmakingEvent.ConfirmStatus) {
        val statusData = ConfirmStatusData(
            matchId = event.matchId,
            status = event.status,
            confirmedCount = event.confirmedCount,
            totalPlayers = event.totalPlayers
        )
        
        updateState { copy(confirmStatus = statusData) }
        
        when {
            statusData.isBothConfirmed -> handleBothConfirmed()
            statusData.isRejected -> handleRejected()
            statusData.isTimeout -> handleTimeout()
        }
    }
    
    private fun handleBothConfirmed() {
        val confirmData = _state.value.confirmRequest ?: return
        
        val matchData = MatchFoundData(
            matchId = confirmData.matchId,
            opponentName = confirmData.opponentName,
            opponentLevel = confirmData.opponentLevel,
            opponentAvatar = confirmData.opponentAvatar,
            difficulty = confirmData.difficulty,
            category = confirmData.category
        )
        
        updateState {
            copy(
                confirmRequest = null,
                confirmStatus = null,
                matchFound = matchData
            )
        }
        
        emitEvent(MatchmakingUiEvent.MatchReady(confirmData.matchId))
    }
    
    private fun handleRejected() {
        updateState {
            copy(
                confirmRequest = null,
                confirmStatus = null,
                error = ERROR_MATCH_REJECTED
            )
        }
        emitEvent(MatchmakingUiEvent.ShowError(ERROR_MATCH_REJECTED))
    }
    
    private fun handleTimeout() {
        updateState {
            copy(
                confirmRequest = null,
                confirmStatus = null,
                error = ERROR_CONFIRM_TIMEOUT
            )
        }
        emitEvent(MatchmakingUiEvent.ConfirmationExpired)
    }
    
    private fun handleCancelled() {
        resetState()
    }
    
    private suspend fun getUserIdOrReturn(): String? {
        return tokenRepository.getUserId().also {
            if (it == null) logError("User ID not available")
        }
    }
    
    private fun clearConfirmState() {
        updateState { copy(confirmRequest = null, confirmStatus = null) }
    }
    
    private fun resetState() {
        _state.value = MatchmakingState()
    }
    
    // endregion
    
    // region Utility Methods
    
    private inline fun updateState(update: MatchmakingState.() -> MatchmakingState) {
        _state.update { it.update() }
    }
    
    private fun launchSafely(block: suspend () -> Unit) {
        viewModelScope.launch(exceptionHandler) { block() }
    }
    
    private fun emitEvent(event: MatchmakingUiEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
    
    private fun logDebug(message: String) {
        Log.d(TAG, message)
    }
    
    private fun logError(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }
    
    // endregion
    
    override fun onCleared() {
        super.onCleared()
        // Don't disconnect WebSocket - it's a singleton shared across activities
        // and needs to stay connected for the game to receive messages
    }
    
    companion object {
        private const val TAG = "MatchmakingViewModel"
        
        // Constants
        private const val MATCH_MODE_CASUAL = "casual"
        private const val DEFAULT_USERNAME = "User"
        private const val RECONNECT_DELAY = 500L
        
        // Error messages
        private const val ERROR_MATCH_REJECTED = "Lawan menolak pertandingan"
        private const val ERROR_CONFIRM_TIMEOUT = "Waktu konfirmasi habis"
    }
}
