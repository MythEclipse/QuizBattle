package com.mytheclipse.quizbattle.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Sound Manager for managing game sound effects
 * Handles loading, playing, and managing sound settings
 */
class SoundManager(private val context: Context) {
    
    private val Context.dataStore by preferencesDataStore(name = "sound_settings")
    private val SOUND_ENABLED_KEY = booleanPreferencesKey("sound_enabled")
    
    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<SoundEffect, Int>()
    private var soundEnabled = true
    
    init {
        initializeSoundPool()
    }
    
    private fun initializeSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()
        
        // Load sound effects here when audio files are available
        // For now, we prepare the structure
        loadSounds()
    }
    
    private fun loadSounds() {
        // TODO: Load actual sound files from res/raw when available
        // Example:
        // soundMap[SoundEffect.BUTTON_CLICK] = soundPool?.load(context, R.raw.button_click, 1) ?: 0
        // soundMap[SoundEffect.CORRECT_ANSWER] = soundPool?.load(context, R.raw.correct_answer, 1) ?: 0
        // soundMap[SoundEffect.WRONG_ANSWER] = soundPool?.load(context, R.raw.wrong_answer, 1) ?: 0
        // soundMap[SoundEffect.VICTORY] = soundPool?.load(context, R.raw.victory, 1) ?: 0
        // soundMap[SoundEffect.DEFEAT] = soundPool?.load(context, R.raw.defeat, 1) ?: 0
    }
    
    /**
     * Play a sound effect
     * @param effect The sound effect to play
     * @param volume Volume level (0.0 to 1.0)
     */
    fun playSound(effect: SoundEffect, volume: Float = 1.0f) {
        if (!soundEnabled) return
        
        soundMap[effect]?.let { soundId ->
            soundPool?.play(soundId, volume, volume, 1, 0, 1.0f)
        }
    }
    
    /**
     * Enable or disable sound effects
     */
    suspend fun setSoundEnabled(enabled: Boolean) {
        soundEnabled = enabled
        context.dataStore.edit { preferences ->
            preferences[SOUND_ENABLED_KEY] = enabled
        }
    }
    
    /**
     * Get sound enabled state as Flow
     */
    fun isSoundEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[SOUND_ENABLED_KEY] ?: true
        }
    }
    
    /**
     * Get current sound enabled state synchronously
     */
    fun isSoundEnabledSync(): Boolean {
        return soundEnabled
    }
    
    /**
     * Release sound resources
     */
    fun release() {
        soundPool?.release()
        soundPool = null
        soundMap.clear()
    }
    
    companion object {
        @Volatile
        private var instance: SoundManager? = null
        
        fun getInstance(context: Context): SoundManager {
            return instance ?: synchronized(this) {
                instance ?: SoundManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

/**
 * Available sound effects in the game
 */
enum class SoundEffect {
    BUTTON_CLICK,      // General button click
    CORRECT_ANSWER,    // Correct answer in quiz
    WRONG_ANSWER,      // Wrong answer in quiz
    VICTORY,           // Battle victory
    DEFEAT,            // Battle defeat
    NOTIFICATION,      // New notification sound
    MESSAGE_SENT,      // Chat message sent
    MESSAGE_RECEIVED,  // Chat message received
    LEVEL_UP,          // Level up/achievement
    MATCH_FOUND        // Online match found
}
