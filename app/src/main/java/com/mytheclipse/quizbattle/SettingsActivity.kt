package com.mytheclipse.quizbattle

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mytheclipse.quizbattle.databinding.ActivitySettingsBinding
import com.mytheclipse.quizbattle.utils.LocaleHelper
import com.mytheclipse.quizbattle.viewmodel.AuthViewModel
import com.mytheclipse.quizbattle.viewmodel.SettingsState
import com.mytheclipse.quizbattle.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

/**
 * Application settings screen
 * Handles audio, notifications, language, and logout
 */
class SettingsActivity : BaseActivity() {
    
    // region Properties
    
    private lateinit var binding: ActivitySettingsBinding
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    
    // endregion
    
    // region Lifecycle
    
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarPadding(binding.root)
        
        setupClickListeners()
        setupSwitchListeners()
        observeState()
        updateLanguageDisplay()
    }
    
    // endregion
    
    // region Setup
    
    private fun setupClickListeners() {
        with(binding) {
            backButton.setOnClickListener { navigateBack() }
            languageButton.setOnClickListener { showLanguageSelectionDialog() }
            logoutButton.setOnClickListener { showLogoutConfirmation() }
            clearCacheButton.setOnClickListener { handleClearCache() }
            aboutButton.setOnClickListener { showAboutDialog() }
        }
    }
    
    private fun setupSwitchListeners() {
        with(binding) {
            soundEffectsSwitch.setOnCheckedChangeListener { _, isChecked ->
                settingsViewModel.setSoundEffectsEnabled(isChecked)
            }
            
            musicSwitch.setOnCheckedChangeListener { _, isChecked ->
                settingsViewModel.setMusicEnabled(isChecked)
            }
            
            vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
                settingsViewModel.setVibrationEnabled(isChecked)
            }
            
            notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
                settingsViewModel.setNotificationsEnabled(isChecked)
            }
        }
    }
    
    // endregion
    
    // region State Observation
    
    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.state.collect { state ->
                    updateSwitches(state)
                    updateVersionDisplay(state.appVersion)
                }
            }
        }
    }
    
    private fun updateSwitches(state: SettingsState) {
        with(binding) {
            soundEffectsSwitch.isChecked = state.soundEffectsEnabled
            musicSwitch.isChecked = state.musicEnabled
            vibrationSwitch.isChecked = state.vibrationEnabled
            notificationsSwitch.isChecked = state.notificationsEnabled
        }
    }
    
    private fun updateVersionDisplay(version: String) {
        binding.appVersionTextView.text = "${getString(R.string.version)} $version"
    }
    
    private fun updateLanguageDisplay() {
        val currentLanguage = LocaleHelper.getLanguage(this)
        binding.currentLanguageText.text = LocaleHelper.getLanguageDisplayName(this, currentLanguage)
    }
    
    // endregion
    
    // region Actions
    
    private fun handleClearCache() {
        settingsViewModel.clearCache()
        showToast(getString(R.string.done))
    }
    
    // endregion
    
    // region Dialogs
    
    private fun showLanguageSelectionDialog() {
        val languages = LocaleHelper.getAvailableLanguages()
        val languageNames = languages.map { it.second }.toTypedArray()
        val currentLanguage = LocaleHelper.getLanguage(this)
        val currentIndex = languages.indexOfFirst { it.first == currentLanguage }.coerceAtLeast(0)
        
        var selectedIndex = currentIndex
        
        AlertDialog.Builder(this)
            .setTitle(R.string.select_language)
            .setSingleChoiceItems(languageNames, currentIndex) { _, which ->
                selectedIndex = which
            }
            .setPositiveButton(R.string.ok) { dialog, _ ->
                val selectedLanguage = languages[selectedIndex].first
                if (LocaleHelper.isRestartRequired(this, selectedLanguage)) {
                    LocaleHelper.setLanguage(this, selectedLanguage)
                    showRestartDialog()
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun showRestartDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.language)
            .setMessage(R.string.app_restart_required)
            .setPositiveButton(R.string.restart_now) { _, _ -> restartApp() }
            .setNegativeButton(R.string.restart_later) { _, _ -> updateLanguageDisplay() }
            .setCancelable(false)
            .show()
    }
    
    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_confirm)
            .setPositiveButton(R.string.yes) { _, _ -> handleLogout() }
            .setNegativeButton(R.string.no, null)
            .show()
    }
    
    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("${getString(R.string.about)} QuizBattle")
            .setMessage(ABOUT_MESSAGE)
            .setPositiveButton(R.string.ok, null)
            .show()
    }
    
    // endregion
    
    // region Navigation
    
    private fun handleLogout() {
        authViewModel.logout()
        navigateTo<LoginActivity>(clearTask = true)
        finish()
    }
    
    private fun restartApp() {
        navigateTo<SplashActivity>(clearTask = true)
        Runtime.getRuntime().exit(0)
    }
    
    // endregion
    
    companion object {
        private const val ABOUT_MESSAGE = """QuizBattle v1.0

Developed by Asep Haryana Saputra
NIM: 20230810043

Mata Kuliah: Bahasa Pemrograman 3
Universitas Kuningan"""
    }
}
