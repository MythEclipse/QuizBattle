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
import com.mytheclipse.quizbattle.data.remote.api.ForgotPasswordRequest
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
    val user: User? = null,
    val requiresEmailVerification: Boolean = false,
    val message: String? = null
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
    
    private fun isValidPassword(password: String): Boolean {
        // Elysia requires: 8+ chars, uppercase, lowercase, digit, special char
        val hasMinLength = password.length >= 8
        val hasUppercase = password.any { it.isUpperCase() }
        val hasLowercase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        return hasMinLength && hasUppercase && hasLowercase && hasDigit && hasSpecial
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
            
            if (!isValidPassword(password)) {
                _authState.value = AuthState(
                    error = "Password minimal 8 karakter dengan huruf besar, kecil, angka, dan simbol"
                )
                return@launch
            }
            
            try {
                if (BuildConfig.DEBUG) Log.d("API", "AuthViewModel.register - start email=$email username=$username")
                // Call API
                val response = authApiService.register(
                    RegisterRequest(name = username, email = email, password = password)
                )
                
                if (response.success) {
                    if (BuildConfig.DEBUG) Log.d("API", "AuthViewModel.register - success userId=${response.user.id}")
                    
                    // Registration successful, but email verification required
                    // Save user to local database for later login
                    val result = userRepository.registerUser(username, email, password)
                    result.onSuccess {
                        _authState.value = AuthState(
                            isSuccess = true,
                            requiresEmailVerification = true,
                            message = response.message
                        )
                    }.onFailure { exception ->
                        // Even if local save fails, registration was successful
                        _authState.value = AuthState(
                            isSuccess = true,
                            requiresEmailVerification = true,
                            message = response.message
                        )
                    }
                } else {
                    if (BuildConfig.DEBUG) Log.e("API", "AuthViewModel.register - failed")
                    _authState.value = AuthState(error = "Registrasi gagal")
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
                
                if (response.success) {
                    if (BuildConfig.DEBUG) Log.d("API", "AuthViewModel.login - success userId=${response.user.id}")
                    
                    // Save tokens
                    tokenRepository.saveToken(response.accessToken)
                    tokenRepository.saveRefreshToken(response.refreshToken)
                    tokenRepository.saveTokenExpiry(response.expiresIn)
                    tokenRepository.saveUserId(response.user.id)
                    tokenRepository.saveUserName(response.user.name ?: "User")
                    tokenRepository.saveUserEmail(response.user.email ?: email)
                    ApiConfig.setAuthToken(response.accessToken)
                    
                    // Create or login user in local database with isLoggedIn = true
                    val result = userRepository.createOrLoginFromApi(
                        response.user.name ?: "User",
                        email
                    )
                    
                    result.onSuccess { user ->
                        _authState.value = AuthState(isSuccess = true, user = user)
                    }.onFailure { exception ->
                        _authState.value = AuthState(error = exception.message ?: "Login gagal")
                    }
                } else {
                    if (BuildConfig.DEBUG) Log.e("API", "AuthViewModel.login - failed")
                    _authState.value = AuthState(error = "Login gagal")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (BuildConfig.DEBUG) Log.e("API", "AuthViewModel.login - exception: ${e.message}", e)
                _authState.value = AuthState(error = e.message ?: "Terjadi kesalahan koneksi")
            }
        }
    }

    fun googleLogin(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            
            try {
                if (BuildConfig.DEBUG) Log.d("API", "AuthViewModel.googleLogin - start")
                // Call API
                val response = authApiService.googleLogin(
                    com.mytheclipse.quizbattle.data.remote.api.GoogleLoginRequest(idToken = idToken)
                )
                
                if (response.success) {
                    if (BuildConfig.DEBUG) Log.d("API", "AuthViewModel.googleLogin - success userId=${response.user.id}")
                    
                    // Save tokens
                    tokenRepository.saveToken(response.accessToken)
                    tokenRepository.saveRefreshToken(response.refreshToken)
                    tokenRepository.saveTokenExpiry(response.expiresIn)
                    tokenRepository.saveUserId(response.user.id)
                    tokenRepository.saveUserName(response.user.name ?: "User")
                    tokenRepository.saveUserEmail(response.user.email ?: "")
                    ApiConfig.setAuthToken(response.accessToken)
                    
                    // Create or login user in local database
                    val result = userRepository.createOrLoginFromApi(
                        response.user.name ?: "User",
                        response.user.email ?: ""
                    )
                    
                    result.onSuccess { user ->
                        _authState.value = AuthState(isSuccess = true, user = user)
                    }.onFailure { exception ->
                        _authState.value = AuthState(error = exception.message ?: "Login gagal")
                    }
                } else {
                    if (BuildConfig.DEBUG) Log.e("API", "AuthViewModel.googleLogin - failed")
                    _authState.value = AuthState(error = "Google Login gagal")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (BuildConfig.DEBUG) Log.e("API", "AuthViewModel.googleLogin - exception: ${e.message}", e)
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
    
    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            
            if (email.isBlank()) {
                _authState.value = AuthState(error = "Email tidak boleh kosong")
                return@launch
            }
            
            try {
                if (BuildConfig.DEBUG) Log.d("API", "AuthViewModel.forgotPassword - start email=$email")
                val response = authApiService.forgotPassword(
                    ForgotPasswordRequest(email = email)
                )
                
                if (response.success) {
                    if (BuildConfig.DEBUG) Log.d("API", "AuthViewModel.forgotPassword - success email=$email")
                    _authState.value = AuthState(
                        isSuccess = true,
                        message = "Link reset password telah dikirim ke email Anda"
                    )
                } else {
                    if (BuildConfig.DEBUG) Log.e("API", "AuthViewModel.forgotPassword - failed: ${response.error}")
                    _authState.value = AuthState(error = response.error ?: "Reset password gagal")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (BuildConfig.DEBUG) Log.e("API", "AuthViewModel.forgotPassword - exception: ${e.message}", e)
                _authState.value = AuthState(error = e.message ?: "Terjadi kesalahan koneksi")
            }
        }
    }
    
    // Deprecated: Use forgotPassword instead
    @Deprecated("Use forgotPassword instead", ReplaceWith("forgotPassword(email)"))
    fun resetPassword(email: String) {
        forgotPassword(email)
    }
}

