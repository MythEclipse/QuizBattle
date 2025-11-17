package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.local.QuizBattleDatabase
import com.mytheclipse.quizbattle.data.local.entity.GameHistory
import com.mytheclipse.quizbattle.data.repository.GameHistoryRepository
import com.mytheclipse.quizbattle.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GameHistoryState(
    val gameHistoryList: List<GameHistory> = emptyList(),
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
    
    init {
        loadGameHistory()
    }
    
    fun loadGameHistory() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                val currentUser = userRepository.getLoggedInUser()
                
                if (currentUser != null) {
                    gameHistoryRepository.getGameHistoryByUser(currentUser.id).collect { historyList ->
                        _state.value = _state.value.copy(
                            gameHistoryList = historyList,
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
