package com.mytheclipse.quizbattle.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.media.ToneGenerator
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.mytheclipse.quizbattle.R
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
    private var toneGenerator: ToneGenerator? = null
    
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

        // Prepare tone generator for fallback when raw assets are not present
        toneGenerator = try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (e: Exception) {
            null
        }
        
        // Load sound effects here when audio files are available in res/raw.
        // When raw assets are absent, ToneGenerator fallback below ensures audible feedback.
        loadSounds()
    }
    
    private fun loadSounds() {
        // Load sound effects from res/raw
        try {
            soundMap[SoundEffect.BUTTON_CLICK] = soundPool?.load(context, R.raw.coin, 1) ?: 0
            soundMap[SoundEffect.CORRECT_ANSWER] = soundPool?.load(context, R.raw.attack_knight, 1) ?: 0
            soundMap[SoundEffect.WRONG_ANSWER] = soundPool?.load(context, R.raw.hurt_knight, 1) ?: 0
            soundMap[SoundEffect.VICTORY] = soundPool?.load(context, R.raw.roar_monster, 1) ?: 0
            soundMap[SoundEffect.DEFEAT] = soundPool?.load(context, R.raw.die_knight, 1) ?: 0
            soundMap[SoundEffect.NOTIFICATION] = soundPool?.load(context, R.raw.gem, 1) ?: 0
            soundMap[SoundEffect.MESSAGE_SENT] = soundPool?.load(context, R.raw.jump_knight, 1) ?: 0
            soundMap[SoundEffect.MESSAGE_RECEIVED] = soundPool?.load(context, R.raw.coin, 1) ?: 0
            soundMap[SoundEffect.LEVEL_UP] = soundPool?.load(context, R.raw.gem, 1) ?: 0
            soundMap[SoundEffect.MATCH_FOUND] = soundPool?.load(context, R.raw.roar2_monster, 1) ?: 0
            soundMap[SoundEffect.OPPONENT_ATTACK] = soundPool?.load(context, R.raw.attack_monster, 1) ?: 0
            soundMap[SoundEffect.OPPONENT_HURT] = soundPool?.load(context, R.raw.hurt_monster, 1) ?: 0
            soundMap[SoundEffect.TIMER_WARNING] = soundPool?.load(context, R.raw.roar3_monster, 1) ?: 0
            soundMap[SoundEffect.COUNTDOWN] = soundPool?.load(context, R.raw.bit_monster, 1) ?: 0
        } catch (e: Exception) {
            // Fallback to ToneGenerator if files not found
            e.printStackTrace()
        }
    }
    
    /**
     * Play a sound effect
     * @param effect The sound effect to play
     * @param volume Volume level (0.0 to 1.0)
     */
    fun playSound(effect: SoundEffect, volume: Float = 1.0f) {
        if (!soundEnabled) return

        val soundId = soundMap[effect]
        if (soundId != null && soundId != 0) {
            soundPool?.play(soundId, volume, volume, 1, 0, 1.0f)
        } else {
            // Fallback to tone generator if raw assets are not available
            playFallbackTone(effect)
        }
    }

    private fun playFallbackTone(effect: SoundEffect) {
        val tg = toneGenerator ?: return
        val (tone, duration) = when (effect) {
            SoundEffect.BUTTON_CLICK -> ToneGenerator.TONE_PROP_BEEP2 to 50
            SoundEffect.CORRECT_ANSWER -> ToneGenerator.TONE_PROP_ACK to 120
            SoundEffect.WRONG_ANSWER -> ToneGenerator.TONE_PROP_NACK to 150
            SoundEffect.VICTORY -> ToneGenerator.TONE_PROP_PROMPT to 250
            SoundEffect.DEFEAT -> ToneGenerator.TONE_PROP_BEEP to 200
            SoundEffect.NOTIFICATION -> ToneGenerator.TONE_PROP_BEEP to 120
            SoundEffect.MESSAGE_SENT -> ToneGenerator.TONE_PROP_BEEP to 60
            SoundEffect.MESSAGE_RECEIVED -> ToneGenerator.TONE_PROP_BEEP2 to 80
            SoundEffect.LEVEL_UP -> ToneGenerator.TONE_PROP_ACK to 180
            SoundEffect.MATCH_FOUND -> ToneGenerator.TONE_PROP_PROMPT to 220
            SoundEffect.OPPONENT_ATTACK -> ToneGenerator.TONE_PROP_NACK to 100
            SoundEffect.OPPONENT_HURT -> ToneGenerator.TONE_PROP_ACK to 100
            SoundEffect.TIMER_WARNING -> ToneGenerator.TONE_PROP_BEEP to 100
            SoundEffect.COUNTDOWN -> ToneGenerator.TONE_PROP_BEEP2 to 80
        }
        try {
            tg.startTone(tone, duration)
        } catch (_: Exception) {
            // Ignore tone errors
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
        toneGenerator?.release()
        toneGenerator = null
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
    BUTTON_CLICK,       // General button click
    CORRECT_ANSWER,     // Correct answer in quiz
    WRONG_ANSWER,       // Wrong answer in quiz
    VICTORY,            // Battle victory
    DEFEAT,             // Battle defeat
    NOTIFICATION,       // New notification sound
    MESSAGE_SENT,       // Chat message sent
    MESSAGE_RECEIVED,   // Chat message received
    LEVEL_UP,           // Level up/achievement
    MATCH_FOUND,        // Online match found
    OPPONENT_ATTACK,    // Opponent attacks
    OPPONENT_HURT,      // Opponent takes damage
    TIMER_WARNING,      // Timer running low
    COUNTDOWN           // Countdown tick
}
