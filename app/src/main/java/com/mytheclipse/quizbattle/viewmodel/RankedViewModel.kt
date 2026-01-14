package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
 * UI State for Ranked screen
 */
data class RankedState(
    val tier: String = DEFAULT_TIER,
    val division: Int = DEFAULT_DIVISION,
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
) {
    /** Get formatted tier and division string */
    val tierDisplay: String
        get() = "${tier.replaceFirstChar { it.uppercase() }} $division"
    
    /** Check if user is in high tier (platinum or above) */
    val isHighTier: Boolean
        get() = tier.lowercase() in listOf("platinum", "diamond", "master", "grandmaster", "challenger")
    
    /** Get win rate as percentage string */
    val winRateDisplay: String
        get() = String.format("%.1f%%", winRate)
    
    /** Check if leaderboard has data */
    val hasLeaderboard: Boolean get() = leaderboard.isNotEmpty()
    
    /** Check if user is in top 10 */
    val isInTopTen: Boolean get() = userRank in 1..10
    
    companion object {
        const val DEFAULT_TIER = "bronze"
        const val DEFAULT_DIVISION = 1
    }
}

/**
 * One-time events for Ranked screen
 */
sealed class RankedEvent {
    data object StatsLoaded : RankedEvent()
    data object LeaderboardLoaded : RankedEvent()
    data class ShowError(val message: String) : RankedEvent()
}

/**
 * ViewModel for Ranked functionality
 */
class RankedViewModel(application: Application) : AndroidViewModel(application) {
    
    // region Dependencies
    private val tokenRepository = TokenRepository(application)
    private val rankedRepository = RankedRepository()
    // endregion
    
    // region State
    private val _state = MutableStateFlow(RankedState())
    val state: StateFlow<RankedState> = _state.asStateFlow()
    
    private val _events = MutableSharedFlow<RankedEvent>()
    val events: SharedFlow<RankedEvent> = _events.asSharedFlow()
    // endregion
    
    // region Exception Handler
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logError("Coroutine error", throwable)
        updateState { copy(isLoading = false, error = throwable.message) }
    }
    // endregion
    
    init {
        loadRankedStats()
        observeRankedEvents()
    }
    
    // region Public Actions
    
    fun loadRankedStats() {
        launchSafely {
            setLoading(true)
            val userId = getUserIdOrReturn() ?: return@launchSafely
            rankedRepository.requestRankedStats(userId)
        }
    }
    
    fun loadRankedLeaderboard(tier: String? = null) {
        launchSafely {
            setLoading(true)
            val userId = getUserIdOrReturn() ?: return@launchSafely
            rankedRepository.requestRankedLeaderboard(userId, tier)
        }
    }
    
    fun refresh() {
        loadRankedStats()
        loadRankedLeaderboard(_state.value.tier)
    }
    
    fun clearError() {
        updateState { copy(error = null) }
    }
    
    // endregion
    
    // region Private Methods
    
    private fun observeRankedEvents() {
        launchSafely {
            rankedRepository.observeRankedEvents().collect { event ->
                handleRankedRepositoryEvent(event)
            }
        }
    }
    
    private fun handleRankedRepositoryEvent(event: com.mytheclipse.quizbattle.data.repository.RankedEvent) {
        when (event) {
            is com.mytheclipse.quizbattle.data.repository.RankedEvent.RankedStatsData -> 
                handleStatsData(event)
            is com.mytheclipse.quizbattle.data.repository.RankedEvent.RankedLeaderboardData -> 
                handleLeaderboardData(event)
            else -> logDebug("Unhandled ranked event: $event")
        }
    }
    
    private fun handleStatsData(event: com.mytheclipse.quizbattle.data.repository.RankedEvent.RankedStatsData) {
        val totalGames = event.wins + event.losses
        
        updateState {
            copy(
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
        emitEvent(RankedEvent.StatsLoaded)
    }
    
    private fun handleLeaderboardData(event: com.mytheclipse.quizbattle.data.repository.RankedEvent.RankedLeaderboardData) {
        updateState {
            copy(
                leaderboard = event.entries,
                userRank = event.userRank,
                totalPlayers = event.totalPlayers,
                isLoading = false
            )
        }
        emitEvent(RankedEvent.LeaderboardLoaded)
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
    
    private inline fun updateState(update: RankedState.() -> RankedState) {
        _state.update { it.update() }
    }
    
    private fun setLoading(isLoading: Boolean) {
        updateState { copy(isLoading = isLoading, error = null) }
    }
    
    private fun launchSafely(block: suspend () -> Unit) {
        viewModelScope.launch(exceptionHandler) { block() }
    }
    
    private fun emitEvent(event: RankedEvent) {
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
        private const val TAG = "RankedViewModel"
    }
}
