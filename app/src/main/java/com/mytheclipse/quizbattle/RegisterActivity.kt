package com.mytheclipse.quizbattle

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mytheclipse.quizbattle.databinding.ActivityRegisterBinding
import com.mytheclipse.quizbattle.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRegisterBinding
    private val authViewModel: AuthViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
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
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                binding.registerButton.isEnabled = !state.isLoading
                
                state.error?.let { error ->
                    Toast.makeText(this@RegisterActivity, error, Toast.LENGTH_LONG).show()
                }
                
                if (state.isSuccess && state.requiresEmailVerification) {
                    val message = state.message ?: "Registrasi berhasil! Silakan cek email Anda untuk verifikasi."
                    Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_LONG).show()
                    finish()
                } else if (state.isSuccess) {
                    Toast.makeText(this@RegisterActivity, "Registrasi berhasil! Silakan login.", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }
}
