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

class ResetPasswordActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityResetPasswordBinding
    private val authViewModel: AuthViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
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
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                binding.sendResetLinkButton.isEnabled = !state.isLoading
                
                state.error?.let { error ->
                    Toast.makeText(this@ResetPasswordActivity, error, Toast.LENGTH_LONG).show()
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
}

