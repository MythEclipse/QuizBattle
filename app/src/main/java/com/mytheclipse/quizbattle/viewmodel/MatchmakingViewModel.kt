package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.remote.websocket.WebSocketManager
import com.mytheclipse.quizbattle.data.repository.MatchmakingEvent
import com.mytheclipse.quizbattle.data.repository.MatchmakingRepository
import com.mytheclipse.quizbattle.data.repository.TokenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MatchmakingState(
    val isSearching: Boolean = false,
    val queuePosition: Int = 0,
    val estimatedWaitTime: Int = 0,
    val matchFound: MatchFoundData? = null,
    val confirmRequest: ConfirmRequestData? = null,
    val confirmStatus: ConfirmStatusData? = null,
    val searchStartTime: Long? = null,
    val error: String? = null
)

data class MatchFoundData(
    val matchId: String,
    val opponentName: String,
    val opponentLevel: Int,
    val opponentAvatar: String?,
    val difficulty: String,
    val category: String
)

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
)

data class ConfirmStatusData(
    val matchId: String,
    val status: String,
    val confirmedCount: Int,
    val totalPlayers: Int
)

class MatchmakingViewModel(application: Application) : AndroidViewModel(application) {
    
    private val tokenRepository = TokenRepository(application)
    private val matchmakingRepository = MatchmakingRepository()
    private val webSocketManager = WebSocketManager.getInstance()
    
    private val _state = MutableStateFlow(MatchmakingState())
    val state: StateFlow<MatchmakingState> = _state.asStateFlow()
    
    init {
        observeMatchmakingEvents()
        observeConnectionState()
    }

    private var lastDifficulty: String? = null
    private var lastCategory: String? = null
    
    private fun observeConnectionState() {
        viewModelScope.launch {
            webSocketManager.connectionState.collect { connectionState ->
                if (connectionState is WebSocketManager.ConnectionState.Connected) {
                     // Check if we were searching. If so, resend the find request.
                     if (_state.value.isSearching) {
                         val userId = tokenRepository.getUserId()
                         if (userId != null) {
                             // Add a small delay to ensure auth message is sent first
                             kotlinx.coroutines.delay(500)
                             // CRITICAL: Check isSearching AGAIN to ensure user didn't cancel during delay
                             if (_state.value.isSearching) {
                                 matchmakingRepository.findMatch(userId, "casual", lastDifficulty, lastCategory)
                             }
                         }
                     }
                }
            }
        }
    }
    
    private fun observeMatchmakingEvents() {
        viewModelScope.launch {
            matchmakingRepository.observeMatchmakingEvents().collect { event ->
                when (event) {
                    is MatchmakingEvent.Searching -> {
                        _state.value = _state.value.copy(
                            isSearching = true,
                            queuePosition = event.queuePosition,
                            estimatedWaitTime = event.estimatedWaitTime
                        )
                    }
                    is MatchmakingEvent.MatchFound -> {
                        _state.value = _state.value.copy(
                            isSearching = false,
                            searchStartTime = null, // Clear timer
                            matchFound = MatchFoundData(
                                matchId = event.matchId,
                                opponentName = event.opponentName,
                                opponentLevel = event.opponentLevel,
                                opponentAvatar = event.opponentAvatar,
                                difficulty = event.difficulty,
                                category = event.category
                            )
                        )
                    }
                    is MatchmakingEvent.ConfirmRequest -> {
                        _state.value = _state.value.copy(
                            isSearching = false,
                            searchStartTime = null,
                            confirmRequest = ConfirmRequestData(
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
                        )
                    }
                    is MatchmakingEvent.ConfirmStatus -> {
                        _state.value = _state.value.copy(
                            confirmStatus = ConfirmStatusData(
                                matchId = event.matchId,
                                status = event.status,
                                confirmedCount = event.confirmedCount,
                                totalPlayers = event.totalPlayers
                            )
                        )
                        
                        // If both players confirmed, clear confirm request and set match found
                        if (event.status == "both_confirmed") {
                            val confirmData = _state.value.confirmRequest
                            if (confirmData != null) {
                                _state.value = _state.value.copy(
                                    confirmRequest = null,
                                    confirmStatus = null,
                                    matchFound = MatchFoundData(
                                        matchId = confirmData.matchId,
                                        opponentName = confirmData.opponentName,
                                        opponentLevel = confirmData.opponentLevel,
                                        opponentAvatar = confirmData.opponentAvatar,
                                        difficulty = confirmData.difficulty,
                                        category = confirmData.category
                                    )
                                )
                            }
                        } else if (event.status == "rejected" || event.status == "timeout") {
                            // Clear confirm state if rejected or timed out
                            _state.value = _state.value.copy(
                                confirmRequest = null,
                                confirmStatus = null,
                                error = if (event.status == "rejected") "Lawan menolak pertandingan" else "Waktu konfirmasi habis"
                            )
                        }
                    }
                    is MatchmakingEvent.Cancelled -> {
                        _state.value = MatchmakingState()
                    }
                    else -> {}
                }
            }
        }
    }
    
    fun connectWebSocket() {
        viewModelScope.launch {
            val userId = tokenRepository.getUserId() ?: return@launch
            val token = tokenRepository.getToken() ?: return@launch
            val username = tokenRepository.getUserName() ?: "User"
            val deviceId = tokenRepository.getOrCreateDeviceId()
            webSocketManager.connect(userId, token, username, deviceId)
        }
    }
    
    fun findMatch(difficulty: String? = null, category: String? = null) {
        lastDifficulty = difficulty
        lastCategory = category
        viewModelScope.launch {
            val userId = tokenRepository.getUserId() ?: return@launch
            matchmakingRepository.findMatch(userId, "casual", difficulty, category)
            // Save start time so UI can restore timer
            _state.value = _state.value.copy(
                isSearching = true,
                searchStartTime = System.currentTimeMillis()
            )
        }
    }
    
    fun confirmMatch(matchId: String, accept: Boolean) {
        viewModelScope.launch {
            val userId = tokenRepository.getUserId() ?: return@launch
            matchmakingRepository.confirmMatch(userId, matchId, accept)
            
            if (!accept) {
                // If declining, clear state
                _state.value = _state.value.copy(
                    confirmRequest = null,
                    confirmStatus = null
                )
            }
        }
    }
    
    fun cancelMatchmaking() {
        viewModelScope.launch {
            val userId = tokenRepository.getUserId() ?: return@launch
            matchmakingRepository.cancelMatchmaking(userId)
            _state.value = MatchmakingState()
        }
    }
    
    // CRITICAL: Clear matchFound after navigating to prevent reconnecting to old match
    fun clearMatchFound() {
        _state.value = _state.value.copy(matchFound = null)
    }
    
    fun clearConfirmRequest() {
        _state.value = _state.value.copy(confirmRequest = null, confirmStatus = null)
    }
    
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
    
    override fun onCleared() {
        super.onCleared()
        // Don't disconnect WebSocket - it's a singleton shared across activities
        // and needs to stay connected for the game to receive messages
    }
}
