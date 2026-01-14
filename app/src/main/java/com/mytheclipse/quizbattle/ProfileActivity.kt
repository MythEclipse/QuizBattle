package com.mytheclipse.quizbattle

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.mytheclipse.quizbattle.databinding.ActivityProfileBinding
import com.mytheclipse.quizbattle.viewmodel.AuthViewModel
import com.mytheclipse.quizbattle.viewmodel.ProfileState
import com.mytheclipse.quizbattle.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

/**
 * User profile screen showing stats and profile information
 * Allows navigation to edit profile and logout
 */
class ProfileActivity : BaseActivity() {
    
    // region Properties
    
    private lateinit var binding: ActivityProfileBinding
    private val profileViewModel: ProfileViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    
    // endregion
    
    // region Lifecycle
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarPadding(binding.root)
        
        setupClickListeners()
        observeState()
        profileViewModel.loadProfile()
    }
    
    // endregion
    
    // region Setup
    
    private fun setupClickListeners() {
        binding.backButton?.setOnClickListener { navigateBack() }
        binding.logoutButton?.setOnClickListener { withDebounce { handleLogout() } }
        binding.editProfileButton?.setOnClickListener { withDebounce { navigateTo<EditProfileActivity>() } }
    }
    
    // endregion
    
    // region Actions
    
    private fun handleLogout() {
        authViewModel.logout()
        navigateTo<LoginActivity>(clearTask = true)
        finish()
    }
    
    // endregion
    
    // region State Observation
    
    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileViewModel.state.collect { state ->
                    handleState(state)
                }
            }
        }
    }
    
    private fun handleState(state: ProfileState) {
        updateLoadingState(state.isLoading)
        updateProfileInfo(state)
        updateStats(state)
        state.image?.let { updateAvatar(it) }
    }
    
    private fun updateLoadingState(isLoading: Boolean) {
        binding.progressBar?.isVisible = isLoading
    }
    
    private fun updateProfileInfo(state: ProfileState) {
        binding.usernameTextView?.text = state.username
        binding.emailTextView?.text = state.email
    }
    
    private fun updateStats(state: ProfileState) {
        with(binding) {
            pointsTextView?.text = state.points.toString()
            winsTextView?.text = state.wins.toString()
            lossesTextView?.text = state.losses.toString()
            totalGamesTextView?.text = state.totalGames.toString()
            winRateTextView?.text = String.format(WIN_RATE_FORMAT, state.winRate)
        }
    }
    
    private fun updateAvatar(imageUrl: String) {
        binding.avatarImageView?.load(imageUrl) {
            placeholder(R.mipmap.ic_launcher)
            error(R.mipmap.ic_launcher)
        }
    }
    
    // endregion
    
    companion object {
        private const val WIN_RATE_FORMAT = "%.1f%%"
    }
}
