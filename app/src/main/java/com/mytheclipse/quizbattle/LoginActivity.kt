package com.mytheclipse.quizbattle

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.mytheclipse.quizbattle.databinding.ActivityLoginBinding
import com.mytheclipse.quizbattle.utils.AppLogger
import com.mytheclipse.quizbattle.utils.ResultDialogHelper
import com.mytheclipse.quizbattle.viewmodel.AuthState
import com.mytheclipse.quizbattle.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

/**
 * Login screen with email/password and Google sign-in options.
 * 
 * Handles user authentication and redirects to appropriate screen
 * after successful login. Supports deep-linking via redirect extras.
 */
class LoginActivity : BaseActivity() {

    // region Properties
    
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()
    
    // endregion

    // region Lifecycle
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarPadding(binding.root)
        
        setupClickListeners()
        observeState()
    }
    
    // endregion

    // region Setup
    
    private fun setupClickListeners() {
        with(binding) {
            loginButton.setOnClickListener { 
                withDebounce { attemptLogin() }
            }
            
            googleLoginButton.setOnClickListener { 
                withDebounce { signInWithGoogle() }
            }
            
            forgotPasswordTextView.setOnClickListener {
                navigateTo<ResetPasswordActivity>()
            }
            
            registerTextView.setOnClickListener {
                navigateTo<RegisterActivity>()
            }
        }
    }
    
    // endregion

    // region Login Logic
    
    private fun attemptLogin() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString()
        
        clearErrors()
        
        if (!validateInputs(email, password)) {
            AppLogger.Auth.loginFailed(email, "Validation failed")
            return
        }
        
        AppLogger.Auth.loginAttempt(email)
        viewModel.login(email, password)
    }
    
    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true
        
        if (email.isEmpty()) {
            binding.emailInputLayout.error = getString(R.string.error_email_empty)
            isValid = false
        }
        
        if (password.isEmpty()) {
            binding.passwordInputLayout.error = getString(R.string.error_password_empty)
            isValid = false
        }
        
        return isValid
    }
    
    private fun clearErrors() {
        binding.emailInputLayout.error = null
        binding.passwordInputLayout.error = null
    }
    
    // endregion

    // region Google Sign In
    
    private fun signInWithGoogle() {
        lifecycleScope.launch {
            try {
                AppLogger.log(AppLogger.LogLevel.INFO, "Auth", "Google login attempt")
                showLoading()
                val idToken = getGoogleIdToken()
                idToken?.let { 
                    AppLogger.log(AppLogger.LogLevel.INFO, "Auth", "Google token received")
                    viewModel.googleLogin(it) 
                }
            } catch (e: GetCredentialCancellationException) {
                AppLogger.log(AppLogger.LogLevel.INFO, "Auth", "Google login cancelled")
                hideLoading()
                // User cancelled, do nothing
            } catch (e: Exception) {
                AppLogger.log(AppLogger.LogLevel.ERROR, "Auth", "Google login failed: ${e.message}")
                hideLoading()
                showError(e.message ?: getString(R.string.error_occurred))
            }
        }
    }
    
    private suspend fun getGoogleIdToken(): String? {
        val credentialManager = CredentialManager.create(this)
        
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .build()
        
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        
        val result = credentialManager.getCredential(request = request, context = this)
        
        return extractGoogleIdToken(result.credential)
    }
    
    private fun extractGoogleIdToken(credential: Credential): String? {
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
            return googleCredential.idToken
        }
        
        showError(getString(R.string.unrecognized_credential))
        return null
    }
    
    // endregion

    // region State Observation
    
    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collect { state ->
                    handleAuthState(state)
                }
            }
        }
    }
    
    private fun handleAuthState(state: AuthState) {
        when (state) {
            is AuthState.Initial -> hideLoading()
            is AuthState.Loading -> showLoading()
            is AuthState.Success -> handleLoginSuccess(state)
            is AuthState.Error -> handleError(state.message)
            else -> hideLoading()
        }
    }
    
    private fun handleLoginSuccess(state: AuthState.Success) {
        hideLoading()
        AppLogger.Auth.loginSuccess(state.user.id.toString(), state.user.username)
        ResultDialogHelper.showSuccess(
            context = this,
            title = getString(R.string.login_success),
            message = getString(R.string.welcome_user, state.user.username),
            onDismiss = { navigateAfterLogin() }
        )
    }
    
    private fun handleError(message: String) {
        hideLoading()
        AppLogger.Auth.loginFailed("unknown", message)
        showError(message)
        viewModel.clearError()
    }
    
    // endregion

    // region UI State
    
    private fun showLoading() {
        with(binding) {
            progressBar.isVisible = true
            loginButton.isEnabled = false
            googleLoginButton.isEnabled = false
            emailEditText.isEnabled = false
            passwordEditText.isEnabled = false
            loginButton.text = getString(R.string.loading)
        }
    }
    
    private fun hideLoading() {
        with(binding) {
            progressBar.isVisible = false
            loginButton.isEnabled = true
            googleLoginButton.isEnabled = true
            emailEditText.isEnabled = true
            passwordEditText.isEnabled = true
            loginButton.text = getString(R.string.login)
        }
    }
    
    private fun showError(message: String) {
        ResultDialogHelper.showError(this, getString(R.string.login_failed), message)
    }
    
    // endregion

    // region Navigation
    
    private fun navigateAfterLogin() {
        when (intent.getStringExtra(EXTRA_REDIRECT)) {
            REDIRECT_ONLINE_MENU -> navigateTo<OnlineMenuActivity>()
            REDIRECT_FEED -> navigateTo<GameHistoryActivity>()
            REDIRECT_PROFILE -> navigateTo<ProfileActivity>()
            REDIRECT_LEADERBOARD -> navigateTo<LeaderboardActivity>()
            REDIRECT_ONLINE_BATTLE -> navigateToOnlineBattle()
            else -> navigateToMainScreen()
        }
        finish()
    }
    
    private fun navigateToOnlineBattle() {
        val matchId = intent.getStringExtra(EXTRA_MATCH_ID)
        navigateTo<OnlineBattleActivity> {
            matchId?.let { putExtra(OnlineBattleActivity.EXTRA_MATCH_ID, it) }
        }
    }
    
    private fun navigateToMainScreen() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
    }
    
    // endregion

    companion object {
        const val EXTRA_REDIRECT = "extra_redirect"
        const val EXTRA_MATCH_ID = "extra_match_id"

        const val REDIRECT_ONLINE_MENU = "online_menu"
        const val REDIRECT_FEED = "feed"
        const val REDIRECT_PROFILE = "profile"
        const val REDIRECT_ONLINE_BATTLE = "online_battle"
        const val REDIRECT_LEADERBOARD = "leaderboard"
    }
}
