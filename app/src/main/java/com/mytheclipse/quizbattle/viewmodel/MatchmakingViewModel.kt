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

class MatchmakingViewModel(application: Application) : AndroidViewModel(application) {
    
    private val tokenRepository = TokenRepository(application)
    private val matchmakingRepository = MatchmakingRepository()
    private val webSocketManager = WebSocketManager.getInstance()
    
    private val _state = MutableStateFlow(MatchmakingState())
    val state: StateFlow<MatchmakingState> = _state.asStateFlow()
    
    init {
        observeMatchmakingEvents()
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
        viewModelScope.launch {
            val userId = tokenRepository.getUserId() ?: return@launch
            matchmakingRepository.findMatch(userId, "casual", difficulty, category)
            _state.value = _state.value.copy(isSearching = true)
        }
    }
    
    fun cancelMatchmaking() {
        viewModelScope.launch {
            val userId = tokenRepository.getUserId() ?: return@launch
            matchmakingRepository.cancelMatchmaking(userId)
            _state.value = MatchmakingState()
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // Don't disconnect WebSocket - it's a singleton shared across activities
        // and needs to stay connected for the game to receive messages
    }
}
