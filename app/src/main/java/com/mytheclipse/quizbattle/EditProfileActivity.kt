package com.mytheclipse.quizbattle

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mytheclipse.quizbattle.databinding.ActivityEditProfileBinding
import com.mytheclipse.quizbattle.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import coil.load
import java.io.File
import java.io.FileOutputStream

class EditProfileActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityEditProfileBinding
    private val profileViewModel: ProfileViewModel by viewModels()
    
    private val pickMedia = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val file = getFileFromUri(uri)
            if (file != null) {
                profileViewModel.uploadProfileImage(file)
            } else {
                Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupListeners()
        observeProfileData()
        profileViewModel.loadProfile()
    }
    
    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }
        
        binding.avatarImageView.setOnClickListener {
            pickMedia.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        
        binding.saveButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            
            if (validateInputs(username, email)) {
                profileViewModel.updateProfile(username, email)
            }
        }
    }
    
    private fun observeProfileData() {
        lifecycleScope.launch {
            profileViewModel.state.collect { state ->
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                binding.saveButton.isEnabled = !state.isLoading
                
                binding.avatarImageView.load(state.image) {
                    placeholder(R.drawable.ic_launcher_foreground)
                    error(R.drawable.ic_launcher_foreground)
                }
                
                // Pre-fill current data
                if (binding.usernameEditText.text.isNullOrEmpty() && state.username.isNotEmpty()) {
                    binding.usernameEditText.setText(state.username)
                }
                if (binding.emailEditText.text.isNullOrEmpty() && state.email.isNotEmpty()) {
                    binding.emailEditText.setText(state.email)
                }
                
                // Handle errors
                if (state.error != null) {
                    binding.errorTextView.text = state.error
                    binding.errorTextView.visibility = View.VISIBLE
                } else {
                    binding.errorTextView.visibility = View.GONE
                }
                
                // Handle success (when not loading and no error)
                if (!state.isLoading && state.error == null && 
                    state.username.isNotEmpty() && 
                    binding.usernameEditText.text.toString() == state.username) {
                    Toast.makeText(this@EditProfileActivity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
    
    private fun validateInputs(username: String, email: String): Boolean {
        if (username.isEmpty()) {
            binding.usernameInputLayout.error = "Username is required"
            return false
        }
        binding.usernameInputLayout.error = null
        
        if (email.isEmpty()) {
            binding.emailInputLayout.error = "Email is required"
            return false
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.error = "Invalid email format"
            return false
        }
        binding.emailInputLayout.error = null
        
        return true
    }

    private fun getFileFromUri(uri: android.net.Uri): java.io.File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val file = java.io.File(cacheDir, "avatar_upload.jpg") // Use a temp file
            val outputStream = java.io.FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
