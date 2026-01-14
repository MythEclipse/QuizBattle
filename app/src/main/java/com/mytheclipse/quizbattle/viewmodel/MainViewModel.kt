package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.local.QuizBattleDatabase
import com.mytheclipse.quizbattle.data.local.entity.User
import com.mytheclipse.quizbattle.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MainScreenState(
    val currentUser: User? = null,
    val topUsers: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = QuizBattleDatabase.getDatabase(application)
    private val userRepository = UserRepository(database.userDao())
    private val tokenRepository = com.mytheclipse.quizbattle.data.repository.TokenRepository(application)
    
    private val _state = MutableStateFlow(MainScreenState())
    val state: StateFlow<MainScreenState> = _state.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                // Initialize Auth Token from repository
                val token = tokenRepository.getToken()
                if (token != null) {
                    com.mytheclipse.quizbattle.data.remote.ApiConfig.setAuthToken(token)
                }

                // Get current logged in user
                val currentUser = userRepository.getLoggedInUser()
                
                // Get top users for leaderboard
                userRepository.getTopUsers(10).collect { topUsers ->
                    _state.value = MainScreenState(
                        currentUser = currentUser,
                        topUsers = topUsers,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Gagal memuat data"
                )
            }
        }
    }
    
    fun refreshData() {
        loadData()
    }
}
