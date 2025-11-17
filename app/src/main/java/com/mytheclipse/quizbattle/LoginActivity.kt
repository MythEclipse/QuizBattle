package com.mytheclipse.quizbattle

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mytheclipse.quizbattle.databinding.ActivityLoginBinding
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
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                binding.loginButton.isEnabled = !state.isLoading
                
                state.error?.let { error ->
                    Toast.makeText(this@LoginActivity, error, Toast.LENGTH_LONG).show()
                }
                
                if (state.isSuccess && state.user != null) {
                    // If the LoginActivity was launched with a redirect target, resume it
                    val redirectTarget = intent.getStringExtra(EXTRA_REDIRECT)
                    val matchId = intent.getStringExtra(EXTRA_MATCH_ID)
                    if (redirectTarget != null) {
                        when (redirectTarget) {
                            REDIRECT_ONLINE_MENU -> startActivity(Intent(this@LoginActivity, OnlineMenuActivity::class.java))
                            REDIRECT_FEED -> startActivity(Intent(this@LoginActivity, FeedActivity::class.java))
                            REDIRECT_PROFILE -> startActivity(Intent(this@LoginActivity, ProfileActivity::class.java))
                            REDIRECT_ONLINE_BATTLE -> {
                                val redirectIntent = Intent(this@LoginActivity, OnlineBattleActivity::class.java)
                                if (!matchId.isNullOrBlank()) redirectIntent.putExtra(OnlineBattleActivity.EXTRA_MATCH_ID, matchId)
                                startActivity(redirectIntent)
                            }
                            else -> startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        }
                    } else {
                        val intentMain = Intent(this@LoginActivity, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(intentMain)
                    }

                    finish()
                }
            }
        }
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
