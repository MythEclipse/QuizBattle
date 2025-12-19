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

class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
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
        
        binding.forgotPasswordTextView.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }
        
        binding.registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
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
                REDIRECT_ONLINE_BATTLE -> {
                    val redirectIntent = Intent(this, OnlineBattleActivity::class.java)
                    if (!matchId.isNullOrBlank()) redirectIntent.putExtra(OnlineBattleActivity.EXTRA_MATCH_ID, matchId)
                    startActivity(redirectIntent)
                }
                else -> startActivity(Intent(this, MainActivity::class.java))
            }
        } else {
            val intentMain = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
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
    }
}
