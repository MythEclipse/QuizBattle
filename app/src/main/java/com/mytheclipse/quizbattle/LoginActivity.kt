package com.mytheclipse.quizbattle

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mytheclipse.quizbattle.databinding.ActivityLoginBinding
import com.mytheclipse.quizbattle.utils.ResultDialogHelper
import com.mytheclipse.quizbattle.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarPadding(binding.root)
        
        setupListeners()
        observeAuthState()
    }
    
    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()
            
            if (validateInputs(email, password)) {
                authViewModel.login(email, password)
            }
        }
        
        binding.googleLoginButton.setOnClickListener {
            signInWithGoogle()
        }
        
        binding.forgotPasswordTextView.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }
        
        binding.registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
    
    private fun signInWithGoogle() {
        lifecycleScope.launch {
            try {
                // Initialize Credential Manager
                val credentialManager = androidx.credentials.CredentialManager.create(this@LoginActivity)
                
                // Build Google ID Option
                // NOTE: Replace "YOUR_WEB_CLIENT_ID" with actual client ID from Google Cloud Console
                val googleIdOption = com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID) 
                    .setAutoSelectEnabled(false)
                    .build()
                
                // Build Request
                val request = androidx.credentials.GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                
                // Launch
                val result = credentialManager.getCredential(
                    request = request,
                    context = this@LoginActivity
                )
                
                // Handle Result
                val credential = result.credential
                if (credential is androidx.credentials.CustomCredential && 
                    credential.type == com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    
                    val googleIdTokenCredential = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    
                    // Send token to backend
                    authViewModel.googleLogin(idToken)
                } else {
                    ResultDialogHelper.showError(this@LoginActivity, "Login Gagal", "Tipe kredensial tidak dikenali")
                }
                
            } catch (e: androidx.credentials.exceptions.GetCredentialException) {
                // Handle cancellation or errors
                if (e !is androidx.credentials.exceptions.GetCredentialCancellationException) {
                     e.printStackTrace()
                     ResultDialogHelper.showError(this@LoginActivity, "Login Gagal", e.message ?: "Terjadi kesalahan")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ResultDialogHelper.showError(this@LoginActivity, "Login Gagal", e.message ?: "Terjadi kesalahan")
            }
        }
    }
    
    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.emailInputLayout.error = "Email tidak boleh kosong"
            return false
        }
        if (password.isEmpty()) {
            binding.passwordInputLayout.error = "Password tidak boleh kosong"
            return false
        }
        binding.emailInputLayout.error = null
        binding.passwordInputLayout.error = null
        return true
    }
    
    private fun observeAuthState() {
        lifecycleScope.launch {
            authViewModel.authState.collect { state ->
                // Set loading state - disable all interactive elements
                setLoadingState(state.isLoading)
                
                // Handle errors - show dialog and clear error to prevent duplicates
                state.error?.let { error ->
                    ResultDialogHelper.showError(
                        context = this@LoginActivity,
                        title = "Login Gagal",
                        message = error
                    )
                    authViewModel.clearError()
                }
                
                if (state.isSuccess && state.user != null) {
                    // Show success dialog briefly then navigate
                    ResultDialogHelper.showSuccess(
                        context = this@LoginActivity,
                        title = "Login Berhasil!",
                        message = "Selamat datang, ${state.user.username}!",
                        onDismiss = {
                            navigateAfterLogin()
                        }
                    )
                }
            }
        }
    }
    
    private fun navigateAfterLogin() {
        // If the LoginActivity was launched with a redirect target, resume it
        val redirectTarget = intent.getStringExtra(EXTRA_REDIRECT)
        val matchId = intent.getStringExtra(EXTRA_MATCH_ID)
        
        if (redirectTarget != null) {
            when (redirectTarget) {
                REDIRECT_ONLINE_MENU -> startActivity(Intent(this, OnlineMenuActivity::class.java))
                REDIRECT_FEED -> startActivity(Intent(this, FeedActivity::class.java))
                REDIRECT_PROFILE -> startActivity(Intent(this, ProfileActivity::class.java))
                REDIRECT_LEADERBOARD -> startActivity(Intent(this, LeaderboardActivity::class.java))
                REDIRECT_ONLINE_BATTLE -> {
                    val redirectIntent = Intent(this, OnlineBattleActivity::class.java)
                    if (!matchId.isNullOrBlank()) redirectIntent.putExtra(OnlineBattleActivity.EXTRA_MATCH_ID, matchId)
                    startActivity(redirectIntent)
                }
                else -> startActivity(Intent(this, MainActivity::class.java))
            }
        } else {
            val intentMain = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intentMain)
        }
        
        finish()
    }
    
    private fun setLoadingState(isLoading: Boolean) {
        // Show/hide progress bar
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        
        // Disable/enable all interactive elements to prevent spam
        binding.loginButton.isEnabled = !isLoading
        binding.googleLoginButton.isEnabled = !isLoading
        binding.emailEditText.isEnabled = !isLoading
        binding.passwordEditText.isEnabled = !isLoading
        binding.forgotPasswordTextView.isClickable = !isLoading
        binding.registerTextView.isClickable = !isLoading
        
        // Change button text to indicate loading
        binding.loginButton.text = if (isLoading) "Loading..." else getString(R.string.login)
    }

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
