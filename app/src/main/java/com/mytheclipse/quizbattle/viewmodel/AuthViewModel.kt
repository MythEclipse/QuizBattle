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

data class AuthState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val user: User? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = QuizBattleDatabase.getDatabase(application)
    private val userRepository = UserRepository(database.userDao())
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    init {
        checkLoggedInUser()
    }
    
    private fun checkLoggedInUser() {
        viewModelScope.launch {
            try {
                val user = userRepository.getLoggedInUser()
                if (user != null) {
                    _authState.value = AuthState(user = user)
                }
            } catch (e: Exception) {
                // User not logged in
            }
        }
    }
    
    fun register(username: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            
            // Validation
            if (username.isBlank() || email.isBlank() || password.isBlank()) {
                _authState.value = AuthState(error = "Semua field harus diisi")
                return@launch
            }
            
            if (password != confirmPassword) {
                _authState.value = AuthState(error = "Password tidak cocok")
                return@launch
            }
            
            if (password.length < 6) {
                _authState.value = AuthState(error = "Password minimal 6 karakter")
                return@launch
            }
            
            // Register user
            val result = userRepository.registerUser(username, email, password)
            result.onSuccess { user ->
                _authState.value = AuthState(isSuccess = true, user = user)
            }.onFailure { exception ->
                _authState.value = AuthState(error = exception.message ?: "Registrasi gagal")
            }
        }
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            
            // Validation
            if (email.isBlank() || password.isBlank()) {
                _authState.value = AuthState(error = "Email dan password harus diisi")
                return@launch
            }
            
            // Login user
            val result = userRepository.loginUser(email, password)
            result.onSuccess { user ->
                _authState.value = AuthState(isSuccess = true, user = user)
            }.onFailure { exception ->
                _authState.value = AuthState(error = exception.message ?: "Login gagal")
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            userRepository.logoutUser()
            _authState.value = AuthState()
        }
    }
    
    fun resetState() {
        _authState.value = AuthState()
    }
    
    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}
