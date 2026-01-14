package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.remote.websocket.WebSocketManager
import com.mytheclipse.quizbattle.data.repository.DataModels.LeaderboardEntry
import com.mytheclipse.quizbattle.data.repository.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for Online Leaderboard screen
 */
data class OnlineLeaderboardState(
    val leaderboard: List<LeaderboardEntry> = emptyList(),
    val userRank: Int = 0,
    val totalPlayers: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    /** Check if leaderboard has data */
    val hasData: Boolean get() = leaderboard.isNotEmpty()
    
    /** Get top 3 players */
    val topThree: List<LeaderboardEntry>
        get() = leaderboard.take(3)
    
    /** Get remaining players after top 3 */
    val remainingPlayers: List<LeaderboardEntry>
        get() = leaderboard.drop(3)
    
    /** Check if user is in top 10 */
    val isUserInTopTen: Boolean get() = userRank in 1..10
}

/**
 * One-time events for Online Leaderboard screen
 */
sealed class OnlineLeaderboardEvent {
    data object DataLoaded : OnlineLeaderboardEvent()
    data class ShowError(val message: String) : OnlineLeaderboardEvent()
}

/**
 * ViewModel for Online Leaderboard functionality
 */
class OnlineLeaderboardViewModel(application: Application) : AndroidViewModel(application) {
    
    // region Dependencies
    private val tokenRepository = TokenRepository(application)
    private val leaderboardRepository = OnlineLeaderboardRepository()
    private val webSocketManager = WebSocketManager.getInstance()
    // endregion
    
    // region State
    private val _state = MutableStateFlow(OnlineLeaderboardState())
    val state: StateFlow<OnlineLeaderboardState> = _state.asStateFlow()
    
    private val _events = MutableSharedFlow<OnlineLeaderboardEvent>()
    val events: SharedFlow<OnlineLeaderboardEvent> = _events.asSharedFlow()
    // endregion
    
    // region Exception Handler
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logError("Coroutine error", throwable)
        updateState { copy(isLoading = false, error = throwable.message) }
    }
    // endregion
    
    init {
        initializeLeaderboard()
    }
    
    // region Public Actions
    
    fun loadGlobalLeaderboard() {
        launchSafely {
            setLoading(true)
            val userId = getUserIdOrReturn() ?: return@launchSafely
            leaderboardRepository.syncGlobalLeaderboard(userId)
        }
    }
    
    fun refresh() {
        loadGlobalLeaderboard()
    }
    
    fun clearError() {
        updateState { copy(error = null) }
    }
    
    // endregion
    
    // region Private Methods
    
    private fun initializeLeaderboard() {
        connectWebSocket()
        loadGlobalLeaderboard()
        observeLeaderboardEvents()
    }
    
    private fun connectWebSocket() {
        launchSafely {
            val userId = getUserIdOrReturn() ?: return@launchSafely
            val token = tokenRepository.getToken() ?: return@launchSafely
            val username = tokenRepository.getUserName() ?: DEFAULT_USERNAME
            val deviceId = tokenRepository.getOrCreateDeviceId()
            
            webSocketManager.connect(userId, token, username, deviceId)
        }
    }
    
    private fun observeLeaderboardEvents() {
        launchSafely {
            leaderboardRepository.observeLeaderboardEvents().collect { event ->
                handleLeaderboardEvent(event)
            }
        }
    }
    
    private fun handleLeaderboardEvent(event: LeaderboardEvent) {
        when (event) {
            is LeaderboardEvent.GlobalData -> handleGlobalData(event)
            else -> logDebug("Unhandled leaderboard event: $event")
        }
    }
    
    private fun handleGlobalData(event: LeaderboardEvent.GlobalData) {
        updateState {
            copy(
                leaderboard = event.entries,
                userRank = event.userRank,
                totalPlayers = event.totalPlayers,
                isLoading = false
            )
        }
        emitEvent(OnlineLeaderboardEvent.DataLoaded)
    }
    
    private suspend fun getUserIdOrReturn(): String? {
        return tokenRepository.getUserId().also {
            if (it == null) {
                logError("User ID not available")
                updateState { copy(isLoading = false) }
            }
        }
    }
    
    // endregion
    
    // region Utility Methods
    
    private inline fun updateState(update: OnlineLeaderboardState.() -> OnlineLeaderboardState) {
        _state.update { it.update() }
    }
    
    private fun setLoading(isLoading: Boolean) {
        updateState { copy(isLoading = isLoading, error = null) }
    }
    
    private fun launchSafely(block: suspend () -> Unit) {
        viewModelScope.launch(exceptionHandler) { block() }
    }
    
    private fun emitEvent(event: OnlineLeaderboardEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
    
    private fun logDebug(message: String) {
        Log.d(TAG, message)
    }
    
    private fun logError(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }
    
    // endregion
    
    companion object {
        private const val TAG = "OnlineLeaderboardVM"
        private const val DEFAULT_USERNAME = "User"
    }
}
