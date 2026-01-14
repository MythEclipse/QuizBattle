package com.mytheclipse.quizbattle

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mytheclipse.quizbattle.databinding.ActivityRegisterBinding
import com.mytheclipse.quizbattle.utils.ResultDialogHelper
import com.mytheclipse.quizbattle.viewmodel.AuthState
import com.mytheclipse.quizbattle.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

/**
 * Registration screen for new users
 * Validates inputs and handles email verification flow
 */
class RegisterActivity : BaseActivity() {
    
    // region Properties
    
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()
    
    // endregion
    
    // region Lifecycle
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarPadding(binding.root)
        
        setupClickListeners()
        observeState()
    }
    
    // endregion
    
    // region Setup
    
    private fun setupClickListeners() {
        with(binding) {
            registerButton.setOnClickListener { withDebounce { attemptRegistration() } }
            loginTextView.setOnClickListener { navigateBack() }
        }
    }
    
    // endregion
    
    // region Registration Logic
    
    private fun attemptRegistration() {
        val username = binding.usernameEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString()
        val confirmPassword = binding.confirmPasswordEditText.text.toString()
        
        clearErrors()
        
        if (!validateInputs(username, email, password, confirmPassword)) return
        
        viewModel.register(username, email, password, confirmPassword)
    }
    
    private fun validateInputs(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true
        
        with(binding) {
            if (username.isEmpty()) {
                usernameInputLayout.error = getString(R.string.error_username_empty)
                isValid = false
            }
            
            if (email.isEmpty()) {
                emailInputLayout.error = getString(R.string.error_email_empty)
                isValid = false
            }
            
            if (password.isEmpty()) {
                passwordInputLayout.error = getString(R.string.error_password_empty)
                isValid = false
            }
            
            if (confirmPassword.isEmpty()) {
                confirmPasswordInputLayout.error = getString(R.string.error_confirm_password_empty)
                isValid = false
            } else if (password != confirmPassword) {
                confirmPasswordInputLayout.error = getString(R.string.error_password_mismatch)
                isValid = false
            }
        }
        
        return isValid
    }
    
    private fun clearErrors() {
        with(binding) {
            usernameInputLayout.error = null
            emailInputLayout.error = null
            passwordInputLayout.error = null
            confirmPasswordInputLayout.error = null
        }
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
            handleSuccess(state)
        }
    }
    
    private fun handleSuccess(state: AuthState) {
        val (title, message) = when {
            state.requiresEmailVerification -> {
                getString(R.string.registration_success) to 
                    (state.message ?: getString(R.string.check_email_verification))
            }
            else -> {
                getString(R.string.registration_success) to 
                    getString(R.string.account_created_login)
            }
        }
        
        ResultDialogHelper.showSuccess(
            context = this,
            title = title,
            message = message,
            onDismiss = { navigateBack() }
        )
    }
    
    private fun handleError(error: String) {
        ResultDialogHelper.showError(
            context = this,
            title = getString(R.string.registration_failed),
            message = error
        )
        viewModel.clearError()
    }
    
    // endregion
    
    // region UI State
    
    private fun setLoadingState(isLoading: Boolean) {
        with(binding) {
            progressBar.isVisible = isLoading
            
            registerButton.isEnabled = !isLoading
            usernameEditText.isEnabled = !isLoading
            emailEditText.isEnabled = !isLoading
            passwordEditText.isEnabled = !isLoading
            confirmPasswordEditText.isEnabled = !isLoading
            loginTextView.isClickable = !isLoading
            
            registerButton.text = getString(
                if (isLoading) R.string.loading else R.string.register
            )
        }
    }
    
    // endregion
}
