package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.local.QuizBattleDatabase
import com.mytheclipse.quizbattle.data.local.entity.User
import com.mytheclipse.quizbattle.data.remote.ApiConfig
import com.mytheclipse.quizbattle.data.remote.api.AuthApiService
import com.mytheclipse.quizbattle.data.remote.api.LoginRequest
import com.mytheclipse.quizbattle.data.remote.api.RegisterRequest
import com.mytheclipse.quizbattle.data.repository.TokenRepository
import com.mytheclipse.quizbattle.data.repository.UserRepository
import android.util.Log
import com.mytheclipse.quizbattle.BuildConfig
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
    private val tokenRepository = TokenRepository(application)
    private val authApiService = ApiConfig.createService(AuthApiService::class.java)
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    init {
        checkLoggedInUser()
    }
    
    private fun checkLoggedInUser() {
        viewModelScope.launch {
            try {
                val token = tokenRepository.getToken()
                if (token != null) {
                    ApiConfig.setAuthToken(token)
                    val user = userRepository.getLoggedInUser()
                    if (user != null) {
                        _authState.value = AuthState(user = user)
                    }
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
            
            try {
                if (BuildConfig.DEBUG) Log.d("API", "AuthViewModel.register - start email=$email username=$username")
                // Call API
                val response = authApiService.register(
                    RegisterRequest(name = username, email = email, password = password)
                )
                
                if (response.success && response.data != null) {
                    if (BuildConfig.DEBUG) Log.d("API", "AuthViewModel.register - success userId=${response.data.user.id}")
                    val authData = response.data
                    
                    // Save token
                    tokenRepository.saveToken(authData.token)
                    tokenRepository.saveUserId(authData.user.id)
                    tokenRepository.saveUserName(authData.user.name ?: username)
                    tokenRepository.saveUserEmail(authData.user.email ?: email)
                    ApiConfig.setAuthToken(authData.token)
                    
                    // Save user to local database
                    val localUser = User(
                        id = 0,
                        username = authData.user.name ?: username,
                        email = authData.user.email ?: email,
                        password = password,
                        points = 0,
                        wins = 0,
                        losses = 0,
                        totalGames = 0,
                        isLoggedIn = true
                    )
                    
                    val result = userRepository.registerUser(username, email, password)
                    result.onSuccess { user ->
                        _authState.value = AuthState(isSuccess = true, user = user)
                    }.onFailure { exception ->
                        _authState.value = AuthState(error = exception.message ?: "Registrasi gagal")
                    }
                } else {
                    if (BuildConfig.DEBUG) Log.e("API", "AuthViewModel.register - failed: ${response.error}")
                    _authState.value = AuthState(error = response.error ?: "Registrasi gagal")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (BuildConfig.DEBUG) Log.e("API", "AuthViewModel.register - exception: ${e.message}", e)
                _authState.value = AuthState(error = e.message ?: "Terjadi kesalahan koneksi")
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
            
            try {
                if (BuildConfig.DEBUG) Log.d("API", "AuthViewModel.login - start email=$email")
                // Call API
                val response = authApiService.login(
                    LoginRequest(email = email, password = password)
                )
                
                if (response.success && response.data != null) {
                    if (BuildConfig.DEBUG) Log.d("API", "AuthViewModel.login - success userId=${response.data.user.id}")
                    val authData = response.data
                    
                    // Save token
                    tokenRepository.saveToken(authData.token)
                    tokenRepository.saveUserId(authData.user.id)
                    tokenRepository.saveUserName(authData.user.name ?: "User")
                    tokenRepository.saveUserEmail(authData.user.email ?: email)
                    ApiConfig.setAuthToken(authData.token)
                    
                    // Login or create user in local database
                    val result = userRepository.loginUser(email, password)
                    
                    if (result.isFailure) {
                        // User doesn't exist locally, create it
                        val registerResult = userRepository.registerUser(
                            authData.user.name ?: "User",
                            email,
                            password
                        )
                        registerResult.onSuccess { user ->
                            _authState.value = AuthState(isSuccess = true, user = user)
                        }.onFailure { exception ->
                            _authState.value = AuthState(error = exception.message ?: "Login gagal")
                        }
                    } else {
                        result.onSuccess { user ->
                            _authState.value = AuthState(isSuccess = true, user = user)
                        }.onFailure { exception ->
                            _authState.value = AuthState(error = exception.message ?: "Login gagal")
                        }
                    }
                } else {
                    if (BuildConfig.DEBUG) Log.e("API", "AuthViewModel.login - failed: ${response.error}")
                    _authState.value = AuthState(error = response.error ?: "Login gagal")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (BuildConfig.DEBUG) Log.e("API", "AuthViewModel.login - exception: ${e.message}", e)
                _authState.value = AuthState(error = e.message ?: "Terjadi kesalahan koneksi")
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            tokenRepository.clearAll()
            ApiConfig.setAuthToken(null)
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
    
    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            
            if (email.isBlank()) {
                _authState.value = AuthState(error = "Email tidak boleh kosong")
                return@launch
            }
            
            try {
                if (BuildConfig.DEBUG) Log.d("API", "AuthViewModel.resetPassword - start email=$email")
                val response = authApiService.resetPassword(
                    com.mytheclipse.quizbattle.data.remote.api.ResetPasswordRequest(email = email)
                )
                
                if (response.success) {
                    if (BuildConfig.DEBUG) Log.d("API", "AuthViewModel.resetPassword - success email=$email")
                    _authState.value = AuthState(isSuccess = true)
                } else {
                    if (BuildConfig.DEBUG) Log.e("API", "AuthViewModel.resetPassword - failed: ${response.error}")
                    _authState.value = AuthState(error = response.error ?: "Reset password gagal")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (BuildConfig.DEBUG) Log.e("API", "AuthViewModel.resetPassword - exception: ${e.message}", e)
                _authState.value = AuthState(error = e.message ?: "Terjadi kesalahan koneksi")
            }
        }
    }
}
