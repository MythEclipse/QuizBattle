package com.mytheclipse.quizbattle

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mytheclipse.quizbattle.databinding.ActivityResetPasswordBinding
import com.mytheclipse.quizbattle.viewmodel.AuthState
import com.mytheclipse.quizbattle.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

/**
 * Password reset screen
 * Allows users to request a password reset link via email
 */
class ResetPasswordActivity : BaseActivity() {
    
    // region Properties
    
    private lateinit var binding: ActivityResetPasswordBinding
    private val viewModel: AuthViewModel by viewModels()
    
    // endregion
    
    // region Lifecycle
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarPadding(binding.root)
        
        setupClickListeners()
        observeState()
    }
    
    // endregion
    
    // region Setup
    
    private fun setupClickListeners() {
        with(binding) {
            sendResetLinkButton.setOnClickListener { withDebounce { attemptSendResetLink() } }
            backToLoginTextView.setOnClickListener { navigateBack() }
        }
    }
    
    // endregion
    
    // region Reset Password Logic
    
    private fun attemptSendResetLink() {
        val email = binding.emailEditText.text.toString().trim()
        
        clearErrors()
        
        if (!validateEmail(email)) return
        
        viewModel.forgotPassword(email)
    }
    
    private fun validateEmail(email: String): Boolean {
        if (email.isEmpty()) {
            binding.emailInputLayout.error = getString(R.string.error_email_empty)
            return false
        }
        return true
    }
    
    private fun clearErrors() {
        binding.emailInputLayout.error = null
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
        setLoadingState(state.isLoading)
        
        state.error?.let { error ->
            handleError(error)
        }
        
        if (state.isSuccess) {
            handleSuccess(state.message)
        }
    }
    
    private fun handleSuccess(message: String?) {
        val successMessage = message ?: getString(R.string.reset_link_sent)
        showToast(successMessage)
        viewModel.resetState()
        navigateBack()
    }
    
    private fun handleError(error: String) {
        showToast(error)
        viewModel.clearError()
    }
    
    // endregion
    
    // region UI State
    
    private fun setLoadingState(isLoading: Boolean) {
        with(binding) {
            progressBar.isVisible = isLoading
            
            sendResetLinkButton.isEnabled = !isLoading
            emailEditText.isEnabled = !isLoading
            backToLoginTextView.isClickable = !isLoading
            
            sendResetLinkButton.text = getString(
                if (isLoading) R.string.loading else R.string.send_reset_link
            )
        }
    }
    
    // endregion
}

