package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.local.QuizBattleDatabase

import com.mytheclipse.quizbattle.data.repository.GameHistoryRepository
import com.mytheclipse.quizbattle.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.mytheclipse.quizbattle.data.model.UiGameHistory

data class GameHistoryState(
    val gameHistoryList: List<UiGameHistory> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class GameHistoryViewModel(application: Application) : AndroidViewModel(application) {
    
    private val gameHistoryRepository: GameHistoryRepository = GameHistoryRepository(
        QuizBattleDatabase.getDatabase(application).gameHistoryDao()
    )
    
    private val userRepository: UserRepository = UserRepository(
        QuizBattleDatabase.getDatabase(application).userDao()
    )
    
    private val _state = MutableStateFlow(GameHistoryState())
    val state: StateFlow<GameHistoryState> = _state.asStateFlow()
    
    // Cache for remote history to avoid re-fetching on every local update
    private var cachedRemoteHistory: List<UiGameHistory> = emptyList()
    
    init {
        loadGameHistory()
    }
    
    fun loadGameHistory() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                val currentUser = userRepository.getLoggedInUser()
                
                if (currentUser != null) {
                    // Fetch remote history first (or in parallel)
                    try {
                        cachedRemoteHistory = gameHistoryRepository.getRemoteHistory()
                    } catch (e: Exception) {
                        // Ignore remote error, just log
                        e.printStackTrace()
                    }

                    gameHistoryRepository.getGameHistoryByUser(currentUser.id).collect { localList ->
                        val combinedList = (localList + cachedRemoteHistory)
                            .sortedByDescending { it.playedAt }
                            .distinctBy { it.id } // Avoid duplicates if any ID collision (though local uses Long string, remote uses UUID)

                        _state.value = _state.value.copy(
                            gameHistoryList = combinedList,
                            isLoading = false
                        )
                    }
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "No user logged in"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load game history"
                )
            }
        }
    }
    
    fun refresh() {
        loadGameHistory()
    }
}
