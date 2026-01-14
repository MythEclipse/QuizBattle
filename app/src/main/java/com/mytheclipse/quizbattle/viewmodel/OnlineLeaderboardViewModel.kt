package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.repository.DataModels.LeaderboardEntry
import com.mytheclipse.quizbattle.data.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OnlineLeaderboardState(
    val leaderboard: List<LeaderboardEntry> = emptyList(),
    val userRank: Int = 0,
    val totalPlayers: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class OnlineLeaderboardViewModel(application: Application) : AndroidViewModel(application) {
    
    private val tokenRepository = TokenRepository(application)
    private val leaderboardRepository = OnlineLeaderboardRepository()
    
    private val webSocketManager = com.mytheclipse.quizbattle.data.remote.websocket.WebSocketManager.getInstance()
    
    // Add WebSocketManager import if needed, but it seems to be used in Repository, not VM directly usually,
    // but here we need to trigger connection.
    // Actually, the Repository uses it. We should probably trigger connection here.
    
    private val _state = MutableStateFlow(OnlineLeaderboardState())
    val state: StateFlow<OnlineLeaderboardState> = _state.asStateFlow()
    
    init {
        connectWebSocket()
        loadGlobalLeaderboard()
        observeLeaderboardEvents()
    }
    
    private fun connectWebSocket() {
        viewModelScope.launch {
            val userId = tokenRepository.getUserId() ?: return@launch
            val token = tokenRepository.getToken() ?: return@launch
            val username = tokenRepository.getUserName() ?: "User"
            val deviceId = tokenRepository.getOrCreateDeviceId()
            
            webSocketManager.connect(userId, token, username, deviceId)
        }
    }
    
    private fun observeLeaderboardEvents() {
        viewModelScope.launch {
            leaderboardRepository.observeLeaderboardEvents().collect { event ->
                when (event) {
                    is LeaderboardEvent.GlobalData -> {
                        _state.value = _state.value.copy(
                            leaderboard = event.entries,
                            userRank = event.userRank,
                            totalPlayers = event.totalPlayers,
                            isLoading = false
                        )
                    }
                    else -> {}
                }
            }
        }
    }
    
    fun loadGlobalLeaderboard() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val userId = tokenRepository.getUserId() ?: return@launch
            leaderboardRepository.syncGlobalLeaderboard(userId)
        }
    }
    
    fun refresh() {
        loadGlobalLeaderboard()
    }
}
