package com.mytheclipse.quizbattle

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.mytheclipse.quizbattle.databinding.ActivityProfileBinding
import com.mytheclipse.quizbattle.viewmodel.AuthViewModel
import com.mytheclipse.quizbattle.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

class ProfileActivity : BaseActivity() {
    
    private lateinit var binding: ActivityProfileBinding
    private val profileViewModel: ProfileViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycleScope.launch {
            if (!requireLoginOrRedirect(LoginActivity.REDIRECT_PROFILE)) return@launch
            setupListeners()
            observeProfileData()
            profileViewModel.loadProfile()
        }
    }
    
    private fun setupListeners() {
        binding.backButton?.setOnClickListener {
            finish()
        }
        
        binding.logoutButton?.setOnClickListener {
            authViewModel.logout()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        
        binding.editProfileButton?.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun observeProfileData() {
        lifecycleScope.launch {
            profileViewModel.state.collect { state ->
                binding.progressBar?.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                
                binding.usernameTextView?.text = state.username
                binding.emailTextView?.text = state.email
                binding.pointsTextView?.text = state.points.toString()
                binding.winsTextView?.text = state.wins.toString()
                binding.lossesTextView?.text = state.losses.toString()
                binding.totalGamesTextView?.text = state.totalGames.toString()
                binding.winRateTextView?.text = String.format("%.1f%%", state.winRate)
            }
        }
    }
}
