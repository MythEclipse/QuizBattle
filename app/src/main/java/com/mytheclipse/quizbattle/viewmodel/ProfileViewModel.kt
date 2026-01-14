package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.local.QuizBattleDatabase
import com.mytheclipse.quizbattle.data.local.entity.User
import com.mytheclipse.quizbattle.data.repository.TokenRepository
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
import java.io.File

/**
 * UI State for Profile Screen
 */
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
) {
    val hasError: Boolean get() = error != null
    val isEmpty: Boolean get() = username.isEmpty() && !isLoading
}

/**
 * One-time events for Profile Screen
 */
sealed class ProfileEvent {
    data object LogoutSuccess : ProfileEvent()
    data object ProfileUpdated : ProfileEvent()
    data class ImageUploaded(val url: String) : ProfileEvent()
    data class ShowError(val message: String) : ProfileEvent()
}

/**
 * ViewModel for Profile Screen
 */
class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    
    // ===== Dependencies =====
    private val userRepository = UserRepository(
        QuizBattleDatabase.getDatabase(application).userDao()
    )
    private val tokenRepository = TokenRepository(application)
    
    // ===== State =====
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()
    
    // ===== Events =====
    private val _events = MutableSharedFlow<ProfileEvent>()
    val events: SharedFlow<ProfileEvent> = _events.asSharedFlow()
    
    // ===== Error Handler =====
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _state.update { it.copy(isLoading = false, error = throwable.message) }
    }
    
    init {
        loadProfile()
    }
    
    // ===== Public Methods =====
    
    fun loadProfile() {
        viewModelScope.launch(exceptionHandler) {
            setLoading(true)
            
            val user = userRepository.getLoggedInUser()
            
            if (user != null) {
                updateStateFromUser(user)
            } else {
                setError("Pengguna tidak ditemukan. Silakan login kembali.")
            }
        }
    }

    fun uploadProfileImage(file: File) {
        viewModelScope.launch(exceptionHandler) {
            setLoading(true)
            
            userRepository.uploadAvatar(file)
                .onSuccess { url ->
                    _state.update { it.copy(isLoading = false, image = url) }
                    emitEvent(ProfileEvent.ImageUploaded(url))
                    loadProfile()
                }
                .onFailure { e ->
                    setError(e.message ?: "Gagal mengunggah gambar")
                }
        }
    }
    
    fun updateProfile(username: String, email: String) {
        viewModelScope.launch(exceptionHandler) {
            setLoading(true)
            
            val currentUser = userRepository.getLoggedInUser()
            
            if (currentUser != null) {
                val updatedUser = currentUser.copy(username = username, email = email)
                userRepository.updateUser(updatedUser)
                
                tokenRepository.saveUserName(username)
                tokenRepository.saveUserEmail(email)
                
                _state.update { it.copy(
                    username = username,
                    email = email,
                    isLoading = false
                )}
                
                emitEvent(ProfileEvent.ProfileUpdated)
            } else {
                setError("Pengguna tidak ditemukan")
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            try {
                tokenRepository.clearAll()
                userRepository.logoutUser()
                emitEvent(ProfileEvent.LogoutSuccess)
            } catch (e: Exception) {
                emitEvent(ProfileEvent.ShowError(e.message ?: "Gagal logout"))
            }
        }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    // ===== Private Methods =====
    
    private fun updateStateFromUser(user: User) {
        val winRate = calculateWinRate(user.wins, user.totalGames)
        
        _state.update {
            ProfileState(
                username = user.username,
                email = user.email,
                wins = user.wins,
                losses = user.losses,
                totalGames = user.totalGames,
                points = user.points,
                winRate = winRate,
                isLoading = false,
                image = user.image
            )
        }
    }
    
    private fun calculateWinRate(wins: Int, totalGames: Int): Double {
        if (totalGames <= 0) return 0.0
        val rate = (wins.toDouble() / totalGames) * WIN_RATE_MULTIPLIER
        return String.format("%.1f", rate).toDouble()
    }
    
    private fun setLoading(loading: Boolean) {
        _state.update { it.copy(isLoading = loading, error = null) }
    }
    
    private fun setError(message: String) {
        _state.update { it.copy(isLoading = false, error = message) }
    }
    
    private fun emitEvent(event: ProfileEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
    
    companion object {
        private const val WIN_RATE_MULTIPLIER = 100
    }
}
