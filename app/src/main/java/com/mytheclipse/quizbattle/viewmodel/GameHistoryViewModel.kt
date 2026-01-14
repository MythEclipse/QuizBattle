package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.local.QuizBattleDatabase
import com.mytheclipse.quizbattle.data.model.UiGameHistory
import com.mytheclipse.quizbattle.data.repository.GameHistoryRepository
import com.mytheclipse.quizbattle.data.repository.UserRepository
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
 * UI State for Game History screen
 */
data class GameHistoryState(
    val gameHistoryList: List<UiGameHistory> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    /** Check if history is empty */
    val isEmpty: Boolean get() = gameHistoryList.isEmpty()
    
    /** Get total games count */
    val totalGames: Int get() = gameHistoryList.size
    
    /** Get total wins */
    val totalWins: Int get() = gameHistoryList.count { it.isVictory }
    
    /** Get total losses */
    val totalLosses: Int get() = totalGames - totalWins
    
    /** Calculate win rate percentage */
    val winRate: Float
        get() = if (totalGames > 0) (totalWins.toFloat() / totalGames) * 100 else 0f
}

/**
 * One-time events for Game History screen
 */
sealed class GameHistoryEvent {
    data object HistoryLoaded : GameHistoryEvent()
    data class ShowError(val message: String) : GameHistoryEvent()
}

/**
 * ViewModel for Game History management
 */
class GameHistoryViewModel(application: Application) : AndroidViewModel(application) {
    
    // region Dependencies
    private val database = QuizBattleDatabase.getDatabase(application)
    private val gameHistoryRepository = GameHistoryRepository(database.gameHistoryDao())
    private val userRepository = UserRepository(database.userDao())
    // endregion
    
    // region State
    private val _state = MutableStateFlow(GameHistoryState())
    val state: StateFlow<GameHistoryState> = _state.asStateFlow()
    
    private val _events = MutableSharedFlow<GameHistoryEvent>()
    val events: SharedFlow<GameHistoryEvent> = _events.asSharedFlow()
    
    /** Cache for remote history to avoid re-fetching on every local update */
    private var cachedRemoteHistory: List<UiGameHistory> = emptyList()
    // endregion
    
    // region Exception Handler
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logError("Coroutine error", throwable)
        updateState { copy(isLoading = false, error = throwable.message) }
    }
    // endregion
    
    init {
        loadGameHistory()
    }
    
    // region Public Actions
    
    fun loadGameHistory() {
        launchSafely {
            setLoading(true)
            
            val currentUser = userRepository.getLoggedInUser()
            
            if (currentUser == null) {
                handleError(ERROR_NO_USER)
                return@launchSafely
            }
            
            // Fetch remote history (non-blocking failure)
            fetchRemoteHistorySafely()
            
            // Observe local history
            gameHistoryRepository.getGameHistoryByUser(currentUser.id).collect { localList ->
                val combinedList = mergeAndSortHistory(localList, cachedRemoteHistory)
                updateState { copy(gameHistoryList = combinedList, isLoading = false) }
                emitEvent(GameHistoryEvent.HistoryLoaded)
            }
        }
    }
    
    fun refresh() {
        cachedRemoteHistory = emptyList() // Clear cache to force refresh
        loadGameHistory()
    }
    
    fun clearError() {
        updateState { copy(error = null) }
    }
    
    // endregion
    
    // region Private Methods
    
    private suspend fun fetchRemoteHistorySafely() {
        try {
            cachedRemoteHistory = gameHistoryRepository.getRemoteHistory()
            logDebug("Remote history fetched: ${cachedRemoteHistory.size} items")
        } catch (e: Exception) {
            logError("Failed to fetch remote history", e)
            // Continue with local history only
        }
    }
    
    private fun mergeAndSortHistory(
        localList: List<UiGameHistory>,
        remoteList: List<UiGameHistory>
    ): List<UiGameHistory> {
        return (localList + remoteList)
            .sortedByDescending { it.playedAt }
            .distinctBy { it.id }
    }
    
    // endregion
    
    // region Utility Methods
    
    private inline fun updateState(update: GameHistoryState.() -> GameHistoryState) {
        _state.update { it.update() }
    }
    
    private fun setLoading(isLoading: Boolean) {
        updateState { copy(isLoading = isLoading, error = null) }
    }
    
    private fun handleError(message: String) {
        updateState { copy(isLoading = false, error = message) }
        emitEvent(GameHistoryEvent.ShowError(message))
    }
    
    private fun launchSafely(block: suspend () -> Unit) {
        viewModelScope.launch(exceptionHandler) { block() }
    }
    
    private fun emitEvent(event: GameHistoryEvent) {
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
        private const val TAG = "GameHistoryViewModel"
        private const val ERROR_NO_USER = "No user logged in"
    }
}
