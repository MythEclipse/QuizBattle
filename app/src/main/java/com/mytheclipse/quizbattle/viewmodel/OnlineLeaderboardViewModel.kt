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
    val showFriendsOnly: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class OnlineLeaderboardViewModel(application: Application) : AndroidViewModel(application) {
    
    private val tokenRepository = TokenRepository(application)
    private val leaderboardRepository = OnlineLeaderboardRepository()
    
    private val _state = MutableStateFlow(OnlineLeaderboardState())
    val state: StateFlow<OnlineLeaderboardState> = _state.asStateFlow()
    
    init {
        loadGlobalLeaderboard()
        observeLeaderboardEvents()
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
                    is LeaderboardEvent.FriendsData -> {
                        _state.value = _state.value.copy(
                            leaderboard = event.entries,
                            userRank = event.userRank,
                            totalPlayers = event.totalFriends,
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
            _state.value = _state.value.copy(isLoading = true, showFriendsOnly = false)
            val userId = tokenRepository.getUserId() ?: return@launch
            leaderboardRepository.syncGlobalLeaderboard(userId)
        }
    }
    
    fun loadFriendsLeaderboard() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, showFriendsOnly = true)
            val userId = tokenRepository.getUserId() ?: return@launch
            leaderboardRepository.syncFriendsLeaderboard(userId)
        }
    }
    
    fun toggleFriendsOnly() {
        if (_state.value.showFriendsOnly) {
            loadGlobalLeaderboard()
        } else {
            loadFriendsLeaderboard()
        }
    }
}
