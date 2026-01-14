package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.BuildConfig
import com.mytheclipse.quizbattle.data.local.QuizBattleDatabase
import com.mytheclipse.quizbattle.data.local.entity.User
import com.mytheclipse.quizbattle.data.remote.ApiConfig
import com.mytheclipse.quizbattle.data.remote.api.AuthApiService
import com.mytheclipse.quizbattle.data.remote.api.ForgotPasswordRequest
import com.mytheclipse.quizbattle.data.remote.api.GoogleLoginRequest
import com.mytheclipse.quizbattle.data.remote.api.LoginRequest
import com.mytheclipse.quizbattle.data.remote.api.RegisterRequest
import com.mytheclipse.quizbattle.data.remote.model.LoginResponseData
import com.mytheclipse.quizbattle.data.repository.TokenRepository
import com.mytheclipse.quizbattle.data.repository.UserRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Sealed class representing authentication UI states
 * More type-safe than data class with nullable fields
 */
sealed class AuthState {
    open val user: User? get() = null
    open val message: String? get() = null
    
    data object Initial : AuthState()
    data object Loading : AuthState()
    data class Success(override val user: User, override val message: String? = null) : AuthState()
    data class RegistrationSuccess(override val message: String, val requiresVerification: Boolean = true) : AuthState()
    data class ForgotPasswordSent(override val message: String) : AuthState()
    data class Error(override val message: String) : AuthState()
    
    // Helper properties for backward compatibility
    val isLoading: Boolean get() = this is Loading
    val isSuccess: Boolean get() = this is Success || this is RegistrationSuccess
    val error: String? get() = (this as? Error)?.message
    val requiresEmailVerification: Boolean get() = (this as? RegistrationSuccess)?.requiresVerification == true
}

/**
 * One-time events for navigation and UI actions
 */
sealed class AuthEvent {
    data object NavigateToMain : AuthEvent()
    data object NavigateToLogin : AuthEvent()
    data class ShowToast(val message: String) : AuthEvent()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    // ===== Dependencies =====
    private val database = QuizBattleDatabase.getDatabase(application)
    private val userRepository = UserRepository(database.userDao())
    private val tokenRepository = TokenRepository(application)
    private val authApiService: AuthApiService = ApiConfig.createService(AuthApiService::class.java)
    
    // ===== State =====
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    // ===== Events =====
    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()
    
    // ===== Error Handler =====
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logError("Exception", throwable)
        _authState.value = AuthState.Error(throwable.message ?: "Terjadi kesalahan")
    }
    
    init {
        checkLoggedInUser()
    }
    
    // ===== Public Methods =====
    
    fun register(username: String, email: String, password: String, confirmPassword: String) {
        // Validate inputs
        validateRegistration(username, email, password, confirmPassword)?.let { error ->
            _authState.value = AuthState.Error(error)
            return
        }
        
        executeWithLoading {
            logDebug("register", "start email=$email username=$username")
            
            val response = authApiService.register(
                RegisterRequest(name = username, email = email, password = password)
            )
            
            if (response.success) {
                logDebug("register", "success userId=${response.user.id}")
                userRepository.registerUser(username, email, password)
                _authState.value = AuthState.RegistrationSuccess(
                    message = response.message,
                    requiresVerification = true
                )
            } else {
                _authState.value = AuthState.Error("Registrasi gagal")
            }
        }
    }
    
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email dan password harus diisi")
            return
        }
        
        executeWithLoading {
            logDebug("login", "start email=$email")
            
            val response = authApiService.login(LoginRequest(email, password))
            
            if (response.success) {
                logDebug("login", "success userId=${response.user.id}")
                handleLoginSuccess(response, email)
            } else {
                _authState.value = AuthState.Error("Login gagal")
            }
        }
    }

    fun googleLogin(idToken: String) {
        executeWithLoading {
            logDebug("googleLogin", "start")
            
            val response = authApiService.googleLogin(GoogleLoginRequest(idToken = idToken))
            
            if (response.success) {
                logDebug("googleLogin", "success userId=${response.user.id}")
                handleLoginSuccess(response, response.user.email ?: "")
            } else {
                _authState.value = AuthState.Error("Google Login gagal")
            }
        }
    }
    
    fun forgotPassword(email: String) {
        if (email.isBlank()) {
            _authState.value = AuthState.Error("Email tidak boleh kosong")
            return
        }
        
        executeWithLoading {
            logDebug("forgotPassword", "start email=$email")
            
            val response = authApiService.forgotPassword(ForgotPasswordRequest(email = email))
            
            if (response.success) {
                logDebug("forgotPassword", "success")
                _authState.value = AuthState.ForgotPasswordSent(
                    "Link reset password telah dikirim ke email Anda"
                )
            } else {
                _authState.value = AuthState.Error(response.error ?: "Reset password gagal")
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            tokenRepository.clearAll()
            ApiConfig.setAuthToken(null)
            userRepository.logoutUser()
            _authState.value = AuthState.Initial
            _events.emit(AuthEvent.NavigateToLogin)
        }
    }
    
    fun resetState() {
        _authState.value = AuthState.Initial
    }
    
    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Initial
        }
    }
    
    // ===== Private Methods =====
    
    private fun checkLoggedInUser() {
        viewModelScope.launch(exceptionHandler) {
            val token = tokenRepository.getToken() ?: return@launch
            ApiConfig.setAuthToken(token)
            userRepository.getLoggedInUser()?.let { user ->
                _authState.value = AuthState.Success(user)
            }
        }
    }
    
    private fun validateRegistration(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): String? = when {
        username.isBlank() || email.isBlank() || password.isBlank() -> 
            "Semua field harus diisi"
        password != confirmPassword -> 
            "Password tidak cocok"
        !isValidPassword(password) -> 
            "Password minimal 8 karakter dengan huruf besar, kecil, angka, dan simbol"
        else -> null
    }
    
    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8 &&
               password.any { it.isUpperCase() } &&
               password.any { it.isLowerCase() } &&
               password.any { it.isDigit() } &&
               password.any { !it.isLetterOrDigit() }
    }
    
    private fun handleLoginSuccess(response: LoginResponseData, email: String) {
        viewModelScope.launch {
            saveTokens(response)
            saveUserInfo(response, email)
            
            val result = userRepository.createOrLoginFromApi(
                response.user.name ?: "User",
                email
            )
            
            result.onSuccess { user ->
                _authState.value = AuthState.Success(user)
            }.onFailure { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Login gagal")
            }
        }
    }
    
    private suspend fun saveTokens(response: LoginResponseData) {
        tokenRepository.saveToken(response.accessToken)
        tokenRepository.saveRefreshToken(response.refreshToken)
        tokenRepository.saveTokenExpiry(response.expiresIn)
        ApiConfig.setAuthToken(response.accessToken)
    }
    
    private suspend fun saveUserInfo(response: LoginResponseData, email: String) {
        tokenRepository.saveUserId(response.user.id)
        tokenRepository.saveUserName(response.user.name ?: "User")
        tokenRepository.saveUserEmail(response.user.email ?: email)
    }
    
    private inline fun executeWithLoading(crossinline block: suspend () -> Unit) {
        viewModelScope.launch(exceptionHandler) {
            _authState.value = AuthState.Loading
            block()
        }
    }
    
    private fun logDebug(method: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "AuthViewModel.$method - $message")
        }
    }
    
    private fun logError(method: String, throwable: Throwable) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, "AuthViewModel.$method - ${throwable.message}", throwable)
        }
    }
    
    companion object {
        private const val TAG = "AuthViewModel"
    }
}

