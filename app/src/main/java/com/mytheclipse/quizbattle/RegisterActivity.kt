package com.mytheclipse.quizbattle

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mytheclipse.quizbattle.databinding.ActivityRegisterBinding
import com.mytheclipse.quizbattle.utils.ResultDialogHelper
import com.mytheclipse.quizbattle.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class RegisterActivity : BaseActivity() {
    
    private lateinit var binding: ActivityRegisterBinding
    private val authViewModel: AuthViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarPadding(binding.root)
        
        setupListeners()
        observeAuthState()
    }
    
    private fun setupListeners() {
        binding.registerButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()
            
            if (validateInputs(username, email, password, confirmPassword)) {
                authViewModel.register(username, email, password, confirmPassword)
            }
        }
        
        binding.loginTextView.setOnClickListener {
            finish()
        }
    }
    
    private fun validateInputs(username: String, email: String, password: String, confirmPassword: String): Boolean {
        if (username.isEmpty()) {
            binding.usernameInputLayout.error = "Username tidak boleh kosong"
            return false
        }
        if (email.isEmpty()) {
            binding.emailInputLayout.error = "Email tidak boleh kosong"
            return false
        }
        if (password.isEmpty()) {
            binding.passwordInputLayout.error = "Password tidak boleh kosong"
            return false
        }
        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordInputLayout.error = "Konfirmasi password tidak boleh kosong"
            return false
        }
        if (password != confirmPassword) {
            binding.confirmPasswordInputLayout.error = "Password tidak cocok"
            return false
        }
        
        binding.usernameInputLayout.error = null
        binding.emailInputLayout.error = null
        binding.passwordInputLayout.error = null
        binding.confirmPasswordInputLayout.error = null
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
                        context = this@RegisterActivity,
                        title = "Registrasi Gagal",
                        message = error
                    )
                    authViewModel.clearError()
                }
                
                if (state.isSuccess && state.requiresEmailVerification) {
                    val message = state.message ?: "Registrasi berhasil! Silakan cek email Anda untuk verifikasi."
                    ResultDialogHelper.showSuccess(
                        context = this@RegisterActivity,
                        title = "Registrasi Berhasil!",
                        message = message,
                        onDismiss = { finish() }
                    )
                } else if (state.isSuccess) {
                    ResultDialogHelper.showSuccess(
                        context = this@RegisterActivity,
                        title = "Registrasi Berhasil!",
                        message = "Akun berhasil dibuat. Silakan login.",
                        onDismiss = { finish() }
                    )
                }
            }
        }
    }
    
    private fun setLoadingState(isLoading: Boolean) {
        // Show/hide progress bar
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        
        // Disable/enable all interactive elements to prevent spam
        binding.registerButton.isEnabled = !isLoading
        binding.usernameEditText.isEnabled = !isLoading
        binding.emailEditText.isEnabled = !isLoading
        binding.passwordEditText.isEnabled = !isLoading
        binding.confirmPasswordEditText.isEnabled = !isLoading
        binding.loginTextView.isClickable = !isLoading
        
        // Change button text to indicate loading
        binding.registerButton.text = if (isLoading) "Loading..." else "Daftar"
    }
}
