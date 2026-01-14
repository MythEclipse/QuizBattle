package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsState(
    val soundEffectsEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val appVersion: String = "1.0.0"
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val settingsRepository = SettingsRepository(application)
    
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            val appVersion = try {
                val packageInfo = getApplication<Application>().packageManager
                    .getPackageInfo(getApplication<Application>().packageName, 0)
                packageInfo.versionName ?: "1.0.0"
            } catch (e: PackageManager.NameNotFoundException) {
                "1.0.0"
            }
            
            settingsRepository.getSettings().collect { settings ->
                _state.value = SettingsState(
                    soundEffectsEnabled = settings.soundEffectsEnabled,
                    musicEnabled = settings.musicEnabled,
                    vibrationEnabled = settings.vibrationEnabled,
                    notificationsEnabled = settings.notificationsEnabled,
                    appVersion = appVersion
                )
            }
        }
    }
    
    fun setSoundEffectsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSoundEffectsEnabled(enabled)
        }
    }
    
    fun setMusicEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setMusicEnabled(enabled)
        }
    }
    
    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setVibrationEnabled(enabled)
        }
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
        }
    }
    
    fun clearCache() {
        viewModelScope.launch {
            val cacheDir = getApplication<Application>().cacheDir
            cacheDir.deleteRecursively()
        }
    }
}
