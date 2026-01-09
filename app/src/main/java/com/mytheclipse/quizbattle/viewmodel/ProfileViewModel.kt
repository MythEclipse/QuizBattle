package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.local.QuizBattleDatabase
import com.mytheclipse.quizbattle.data.repository.TokenRepository
import com.mytheclipse.quizbattle.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileState(
    val username: String = "",
    val email: String = "",
    val wins: Int = 0,
    val losses: Int = 0,
    val totalGames: Int = 0,
    val points: Int = 0,
    val winRate: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val image: String? = null
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    
    private val userRepository: UserRepository = UserRepository(
        QuizBattleDatabase.getDatabase(application).userDao()
    )
    private val tokenRepository: TokenRepository = TokenRepository(application)
    
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()
    
    init {
        loadProfile()
    }
    
    fun loadProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                val currentUser = userRepository.getLoggedInUser()
                
                if (currentUser != null) {
                    val winRate = if (currentUser.totalGames > 0) {
                        (currentUser.wins.toDouble() / currentUser.totalGames.toDouble()) * 100
                    } else {
                        0.0
                    }
                    
                    _state.value = _state.value.copy(
                        username = currentUser.username,
                        email = currentUser.email,
                        wins = currentUser.wins,
                        losses = currentUser.losses,
                        totalGames = currentUser.totalGames,
                        points = currentUser.points,
                        winRate = String.format("%.1f", winRate).toDouble(),
                        isLoading = false,
                        image = currentUser.image
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "No user found. Please login again."
                    )
                }

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load profile"
                )
            }
        }
    }

    fun uploadProfileImage(file: java.io.File) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            val result = userRepository.uploadAvatar(file)
            
            result.onSuccess { url ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    image = url
                )
                // Also reload profile to ensure everything is consistent
                loadProfile()
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to upload image"
                )
            }
        }
    }
    
    fun updateProfile(username: String, email: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                val currentUser = userRepository.getLoggedInUser()
                
                if (currentUser != null) {
                    val updatedUser = currentUser.copy(
                        username = username,
                        email = email
                    )
                    
                    userRepository.updateUser(updatedUser)
                    
                    tokenRepository.saveUserName(username)
                    tokenRepository.saveUserEmail(email)
                    
                    _state.value = _state.value.copy(
                        username = username,
                        email = email,
                        isLoading = false
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "User not found"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to update profile"
                )
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            try {
                tokenRepository.clearAll()
                userRepository.logoutUser()
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
}
