package com.mytheclipse.quizbattle

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mytheclipse.quizbattle.databinding.ActivityResetPasswordBinding
import com.mytheclipse.quizbattle.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class ResetPasswordActivity : BaseActivity() {
    
    private lateinit var binding: ActivityResetPasswordBinding
    private val authViewModel: AuthViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarPadding(binding.root)
        
        setupListeners()
        observeAuthState()
    }
    
    private fun setupListeners() {
        binding.sendResetLinkButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            
            if (email.isEmpty()) {
                binding.emailInputLayout.error = "Email tidak boleh kosong"
            } else {
                binding.emailInputLayout.error = null
                authViewModel.forgotPassword(email)
            }
        }
        
        binding.backToLoginTextView.setOnClickListener {
            finish()
        }
    }
    
    private fun observeAuthState() {
        lifecycleScope.launch {
            authViewModel.authState.collect { state ->
                // Set loading state - disable all interactive elements
                setLoadingState(state.isLoading)
                
                // Handle errors - show toast and clear error to prevent duplicate toasts
                state.error?.let { error ->
                    Toast.makeText(this@ResetPasswordActivity, error, Toast.LENGTH_LONG).show()
                    authViewModel.clearError()
                }
                
                if (state.isSuccess) {
                    val message = state.message ?: "Link reset password telah dikirim ke email Anda"
                    Toast.makeText(this@ResetPasswordActivity, message, Toast.LENGTH_LONG).show()
                    authViewModel.resetState()
                    finish()
                }
            }
        }
    }
    
    private fun setLoadingState(isLoading: Boolean) {
        // Show/hide progress bar
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        
        // Disable/enable all interactive elements to prevent spam
        binding.sendResetLinkButton.isEnabled = !isLoading
        binding.emailEditText.isEnabled = !isLoading
        binding.backToLoginTextView.isClickable = !isLoading
        
        // Change button text to indicate loading
        binding.sendResetLinkButton.text = if (isLoading) "Loading..." else "Kirim Link Reset"
    }
}

