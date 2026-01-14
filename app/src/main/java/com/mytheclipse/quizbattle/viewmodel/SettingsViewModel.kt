package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for Settings screen
 */
data class SettingsState(
    val soundEffectsEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val appVersion: String = DEFAULT_VERSION,
    val error: String? = null
) {
    /** Check if any audio is enabled */
    val hasAudioEnabled: Boolean
        get() = soundEffectsEnabled || musicEnabled
    
    /** Get app version display string */
    val versionDisplay: String
        get() = "v$appVersion"
    
    companion object {
        const val DEFAULT_VERSION = "1.0.0"
    }
}

/**
 * One-time events for Settings screen
 */
sealed class SettingsEvent {
    data object CacheCleared : SettingsEvent()
    data class SettingChanged(val setting: String, val value: Boolean) : SettingsEvent()
    data class ShowError(val message: String) : SettingsEvent()
}

/**
 * ViewModel for Settings functionality
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    // region Dependencies
    private val settingsRepository = SettingsRepository(application)
    // endregion
    
    // region State
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()
    
    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()
    // endregion
    
    // region Exception Handler
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logError("Coroutine error", throwable)
        updateState { copy(error = throwable.message) }
    }
    // endregion
    
    init {
        loadSettings()
    }
    
    // region Public Actions
    
    fun setSoundEffectsEnabled(enabled: Boolean) {
        launchSafely {
            settingsRepository.setSoundEffectsEnabled(enabled)
            emitSettingChanged(SETTING_SOUND_EFFECTS, enabled)
        }
    }
    
    fun setMusicEnabled(enabled: Boolean) {
        launchSafely {
            settingsRepository.setMusicEnabled(enabled)
            emitSettingChanged(SETTING_MUSIC, enabled)
        }
    }
    
    fun setVibrationEnabled(enabled: Boolean) {
        launchSafely {
            settingsRepository.setVibrationEnabled(enabled)
            emitSettingChanged(SETTING_VIBRATION, enabled)
        }
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        launchSafely {
            settingsRepository.setNotificationsEnabled(enabled)
            emitSettingChanged(SETTING_NOTIFICATIONS, enabled)
        }
    }
    
    fun clearCache() {
        launchSafely {
            val cacheDir = getApplication<Application>().cacheDir
            val deleted = cacheDir.deleteRecursively()
            
            if (deleted) {
                logDebug("Cache cleared successfully")
                emitEvent(SettingsEvent.CacheCleared)
            } else {
                logError("Failed to clear cache")
            }
        }
    }
    
    fun clearError() {
        updateState { copy(error = null) }
    }
    
    // endregion
    
    // region Private Methods
    
    private fun loadSettings() {
        launchSafely {
            val appVersion = getAppVersion()
            
            settingsRepository.getSettings().collect { settings ->
                updateState {
                    copy(
                        soundEffectsEnabled = settings.soundEffectsEnabled,
                        musicEnabled = settings.musicEnabled,
                        vibrationEnabled = settings.vibrationEnabled,
                        notificationsEnabled = settings.notificationsEnabled,
                        appVersion = appVersion
                    )
                }
            }
        }
    }
    
    private fun getAppVersion(): String {
        return try {
            val app = getApplication<Application>()
            val packageInfo = app.packageManager.getPackageInfo(app.packageName, 0)
            packageInfo.versionName ?: SettingsState.DEFAULT_VERSION
        } catch (e: PackageManager.NameNotFoundException) {
            logError("Failed to get app version", e)
            SettingsState.DEFAULT_VERSION
        }
    }
    
    private fun emitSettingChanged(setting: String, value: Boolean) {
        emitEvent(SettingsEvent.SettingChanged(setting, value))
    }
    
    // endregion
    
    // region Utility Methods
    
    private inline fun updateState(update: SettingsState.() -> SettingsState) {
        _state.update { it.update() }
    }
    
    private fun launchSafely(block: suspend () -> Unit) {
        viewModelScope.launch(exceptionHandler) { block() }
    }
    
    private fun emitEvent(event: SettingsEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
    
    private fun logDebug(message: String) {
        Log.d(TAG, message)
    }
    
    private fun logError(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }
    
    // endregion
    
    companion object {
        private const val TAG = "SettingsViewModel"
        
        // Setting keys
        private const val SETTING_SOUND_EFFECTS = "sound_effects"
        private const val SETTING_MUSIC = "music"
        private const val SETTING_VIBRATION = "vibration"
        private const val SETTING_NOTIFICATIONS = "notifications"
    }
}
