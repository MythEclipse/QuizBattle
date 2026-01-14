package com.mytheclipse.quizbattle

import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.mytheclipse.quizbattle.databinding.ActivityEditProfileBinding
import com.mytheclipse.quizbattle.viewmodel.ProfileState
import com.mytheclipse.quizbattle.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

/**
 * Edit profile screen for updating username, email, and avatar
 */
class EditProfileActivity : BaseActivity() {
    
    // region Properties
    
    private lateinit var binding: ActivityEditProfileBinding
    private val viewModel: ProfileViewModel by viewModels()
    
    private var initialDataLoaded = false
    
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { handleSelectedImage(it) }
    }
    
    // endregion
    
    // region Lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarPadding(binding.root)
        
        setupClickListeners()
        observeState()
        viewModel.loadProfile()
    }
    
    // endregion
    
    // region Setup
    
    private fun setupClickListeners() {
        with(binding) {
            backButton.setOnClickListener { navigateBack() }
            avatarImageView.setOnClickListener { openImagePicker() }
            saveButton.setOnClickListener { withDebounce { saveProfile() } }
        }
    }
    
    // endregion
    
    // region Image Handling
    
    private fun openImagePicker() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
    
    private fun handleSelectedImage(uri: Uri) {
        val file = getFileFromUri(uri)
        if (file != null) {
            viewModel.uploadProfileImage(file)
        } else {
            showToast(getString(R.string.error_processing_image))
        }
    }
    
    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val file = File(cacheDir, TEMP_AVATAR_FILENAME)
            FileOutputStream(file).use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()
            file
        } catch (e: Exception) {
            logError("Failed to get file from URI", e)
            null
        }
    }
    
    // endregion
    
    // region Profile Update
    
    private fun saveProfile() {
        val username = binding.usernameEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        
        clearErrors()
        
        if (!validateInputs(username, email)) return
        
        viewModel.updateProfile(username, email)
    }
    
    private fun validateInputs(username: String, email: String): Boolean {
        var isValid = true
        
        if (username.isEmpty()) {
            binding.usernameInputLayout.error = getString(R.string.error_username_empty)
            isValid = false
        }
        
        if (email.isEmpty()) {
            binding.emailInputLayout.error = getString(R.string.error_email_empty)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.error = getString(R.string.invalid_email)
            isValid = false
        }
        
        return isValid
    }
    
    private fun clearErrors() {
        binding.usernameInputLayout.error = null
        binding.emailInputLayout.error = null
    }
    
    // endregion
    
    // region State Observation
    
    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    handleState(state)
                }
            }
        }
    }
    
    private fun handleState(state: ProfileState) {
        updateLoadingState(state.isLoading)
        updateAvatar(state.image)
        prefillFormIfNeeded(state)
        handleError(state.error)
        checkForSuccess(state)
    }
    
    private fun updateLoadingState(isLoading: Boolean) {
        with(binding) {
            progressBar.isVisible = isLoading
            saveButton.isEnabled = !isLoading
        }
    }
    
    private fun updateAvatar(imageUrl: String) {
        binding.avatarImageView.load(imageUrl) {
            placeholder(R.mipmap.ic_launcher)
            error(R.mipmap.ic_launcher)
        }
    }
    
    private fun prefillFormIfNeeded(state: ProfileState) {
        if (!initialDataLoaded && state.username.isNotEmpty()) {
            binding.usernameEditText.setText(state.username)
            binding.emailEditText.setText(state.email)
            initialDataLoaded = true
        }
    }
    
    private fun handleError(error: String?) {
        with(binding.errorTextView) {
            if (error != null) {
                text = error
                isVisible = true
            } else {
                isVisible = false
            }
        }
    }
    
    private fun checkForSuccess(state: ProfileState) {
        val currentUsername = binding.usernameEditText.text.toString()
        val isSuccess = !state.isLoading && 
                        state.error == null && 
                        state.username.isNotEmpty() && 
                        currentUsername == state.username &&
                        initialDataLoaded
        
        if (isSuccess && state.username == currentUsername) {
            showToast(getString(R.string.profile_updated))
            navigateBack()
        }
    }
    
    // endregion
    
    companion object {
        private const val TEMP_AVATAR_FILENAME = "avatar_upload.jpg"
    }
}
