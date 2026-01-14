package com.mytheclipse.quizbattle

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.mytheclipse.quizbattle.databinding.ActivitySettingsBinding
import com.mytheclipse.quizbattle.viewmodel.AuthViewModel
import com.mytheclipse.quizbattle.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

class SettingsActivity : BaseActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarPadding(binding.root)
        
        setupListeners()
        observeSettings()
    }
    
    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }
        
        binding.soundEffectsSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.setSoundEffectsEnabled(isChecked)
        }
        
        binding.musicSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.setMusicEnabled(isChecked)
        }
        
        binding.vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.setVibrationEnabled(isChecked)
        }
        
        binding.notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.setNotificationsEnabled(isChecked)
        }
        
        binding.logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }
        
        binding.clearCacheButton.setOnClickListener {
            settingsViewModel.clearCache()
            Toast.makeText(this, "Cache cleared", Toast.LENGTH_SHORT).show()
        }
        
        binding.aboutButton.setOnClickListener {
            showAboutDialog()
        }
    }
    
    private fun observeSettings() {
        lifecycleScope.launch {
            settingsViewModel.state.collect { state ->
                binding.soundEffectsSwitch.isChecked = state.soundEffectsEnabled
                binding.musicSwitch.isChecked = state.musicEnabled
                binding.vibrationSwitch.isChecked = state.vibrationEnabled
                binding.notificationsSwitch.isChecked = state.notificationsEnabled
                binding.appVersionTextView.text = "Version ${state.appVersion}"
            }
        }
    }
    
    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_confirm)
            .setPositiveButton(R.string.yes) { _, _ ->
                authViewModel.logout()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }
    
    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("About QuizBattle")
            .setMessage("QuizBattle v1.0\n\nDeveloped by Asep Haryana Saputra\nNIM: 20230810043\n\nMata Kuliah: Bahasa Pemrograman 3\nUniversitas Kuningan")
            .setPositiveButton(R.string.ok, null)
            .show()
    }
}
