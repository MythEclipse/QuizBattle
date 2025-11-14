package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RankedState(
    val tier: String = "bronze",
    val division: Int = 1,
    val mmr: Int = 0,
    val rankedPoints: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val totalGames: Int = 0,
    val winRate: Double = 0.0,
    val rank: Int = 0,
    val topPercentage: Double = 0.0,
    val leaderboard: List<RankedLeaderboardEntry> = emptyList(),
    val userRank: Int = 0,
    val totalPlayers: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class RankedViewModel(application: Application) : AndroidViewModel(application) {
    
    private val tokenRepository = TokenRepository(application)
    private val rankedRepository = RankedRepository()
    
    private val _state = MutableStateFlow(RankedState())
    val state: StateFlow<RankedState> = _state.asStateFlow()
    
    init {
        loadRankedStats()
        observeRankedEvents()
    }
    
    private fun observeRankedEvents() {
        viewModelScope.launch {
            rankedRepository.observeRankedEvents().collect { event ->
                when (event) {
                    is RankedEvent.RankedStatsData -> {
                        val totalGames = event.wins + event.losses
                        _state.value = _state.value.copy(
                            tier = event.tier,
                            division = event.division,
                            mmr = event.mmr,
                            rankedPoints = event.rankedPoints,
                            wins = event.wins,
                            losses = event.losses,
                            totalGames = totalGames,
                            winRate = event.winRate,
                            rank = event.rank,
                            topPercentage = event.topPercentage,
                            isLoading = false
                        )
                    }
                    is RankedEvent.RankedLeaderboardData -> {
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
    
    fun loadRankedStats() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val userId = tokenRepository.getUserId() ?: return@launch
            rankedRepository.requestRankedStats(userId)
        }
    }
    
    fun loadRankedLeaderboard(tier: String? = null) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val userId = tokenRepository.getUserId() ?: return@launch
            rankedRepository.requestRankedLeaderboard(userId, tier)
        }
    }
}
