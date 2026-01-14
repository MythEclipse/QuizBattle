package com.mytheclipse.quizbattle.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class AppSettings(
    val soundEffectsEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true
)

class SettingsRepository(private val context: Context) {
    
    companion object {
        private val SOUND_EFFECTS_KEY = booleanPreferencesKey("sound_effects_enabled")
        private val MUSIC_KEY = booleanPreferencesKey("music_enabled")
        private val VIBRATION_KEY = booleanPreferencesKey("vibration_enabled")
        private val NOTIFICATIONS_KEY = booleanPreferencesKey("notifications_enabled")
    }
    
    fun getSettings(): Flow<AppSettings> {
        return context.settingsDataStore.data.map { preferences ->
            AppSettings(
                soundEffectsEnabled = preferences[SOUND_EFFECTS_KEY] ?: true,
                musicEnabled = preferences[MUSIC_KEY] ?: true,
                vibrationEnabled = preferences[VIBRATION_KEY] ?: true,
                notificationsEnabled = preferences[NOTIFICATIONS_KEY] ?: true
            )
        }
    }
    
    suspend fun setSoundEffectsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[SOUND_EFFECTS_KEY] = enabled
        }
    }
    
    suspend fun setMusicEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[MUSIC_KEY] = enabled
        }
    }
    
    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[VIBRATION_KEY] = enabled
        }
    }
    
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[NOTIFICATIONS_KEY] = enabled
        }
    }
}
