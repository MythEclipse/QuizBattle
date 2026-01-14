package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.local.QuizBattleDatabase
import com.mytheclipse.quizbattle.data.local.entity.User
import com.mytheclipse.quizbattle.data.remote.ApiConfig
import com.mytheclipse.quizbattle.data.repository.TokenRepository
import com.mytheclipse.quizbattle.data.repository.UserRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for Main Screen
 */
data class MainScreenState(
    val currentUser: User? = null,
    val topUsers: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val hasUser: Boolean get() = currentUser != null
    val isEmpty: Boolean get() = topUsers.isEmpty() && !isLoading
}

/**
 * ViewModel for Main Screen
 * Handles user data and leaderboard
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    // ===== Dependencies =====
    private val database = QuizBattleDatabase.getDatabase(application)
    private val userRepository = UserRepository(database.userDao())
    private val tokenRepository = TokenRepository(application)
    
    // ===== State =====
    private val _state = MutableStateFlow(MainScreenState())
    val state: StateFlow<MainScreenState> = _state.asStateFlow()
    
    // ===== Error Handler =====
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _state.update { it.copy(isLoading = false, error = throwable.message) }
    }
    
    init {
        loadData()
    }
    
    // ===== Public Methods =====
    
    fun refreshData() {
        loadData()
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    // ===== Private Methods =====
    
    private fun loadData() {
        viewModelScope.launch(exceptionHandler) {
            _state.update { it.copy(isLoading = true) }
            
            initializeAuthToken()
            val currentUser = userRepository.getLoggedInUser()
            
            userRepository.getTopUsers(LEADERBOARD_LIMIT).collect { topUsers ->
                _state.update { 
                    MainScreenState(
                        currentUser = currentUser,
                        topUsers = topUsers,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    private suspend fun initializeAuthToken() {
        tokenRepository.getToken()?.let { token ->
            ApiConfig.setAuthToken(token)
        }
    }
    
    companion object {
        private const val LEADERBOARD_LIMIT = 10
    }
}
