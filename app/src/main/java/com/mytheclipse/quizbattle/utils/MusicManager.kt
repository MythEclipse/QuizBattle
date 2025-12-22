package com.mytheclipse.quizbattle.utils

import android.content.Context
import android.media.MediaPlayer
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.mytheclipse.quizbattle.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

/**
 * Music Manager for managing background music
 * Uses MediaPlayer for looping background tracks
 */
class MusicManager(private val context: Context) {
    
    private val Context.dataStore by preferencesDataStore(name = "music_settings")
    private val MUSIC_ENABLED_KEY = booleanPreferencesKey("music_enabled")
    
    private var mediaPlayer: MediaPlayer? = null
    private var currentTrack: MusicTrack? = null
    private var musicEnabled = true
    private var volume = 0.5f
    
    init {
        // Load saved preference
        runBlocking {
            musicEnabled = context.dataStore.data.map { 
                it[MUSIC_ENABLED_KEY] ?: true 
            }.first()
        }
    }
    
    /**
     * Play background music track
     * @param track The music track to play
     * @param loop Whether to loop the track (default: true)
     */
    fun playMusic(track: MusicTrack, loop: Boolean = true) {
        if (!musicEnabled) return
        
        // If same track is already playing, don't restart
        if (currentTrack == track && mediaPlayer?.isPlaying == true) {
            return
        }
        
        // Stop current music
        stopMusic()
        
        try {
            val resId = when (track) {
                MusicTrack.BATTLE -> R.raw.battle_music
                MusicTrack.MENU -> R.raw.battle_music
                MusicTrack.VICTORY -> R.raw.battle_music
                MusicTrack.LOBBY -> R.raw.battle_music
            }
            
            mediaPlayer = MediaPlayer.create(context, resId)?.apply {
                isLooping = loop
                setVolume(volume, volume)
                start()
            }
            currentTrack = track
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Stop current music
     */
    fun stopMusic() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            currentTrack = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Pause current music
     */
    fun pauseMusic() {
        try {
            mediaPlayer?.pause()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Resume paused music
     */
    fun resumeMusic() {
        if (!musicEnabled) return
        try {
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Set music volume
     * @param volume Volume level (0.0 to 1.0)
     */
    fun setVolume(newVolume: Float) {
        volume = newVolume.coerceIn(0f, 1f)
        try {
            mediaPlayer?.setVolume(volume, volume)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Enable or disable music
     */
    suspend fun setMusicEnabled(enabled: Boolean) {
        musicEnabled = enabled
        context.dataStore.edit { preferences ->
            preferences[MUSIC_ENABLED_KEY] = enabled
        }
        
        if (!enabled) {
            stopMusic()
        }
    }
    
    /**
     * Get music enabled state as Flow
     */
    fun isMusicEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[MUSIC_ENABLED_KEY] ?: true
        }
    }
    
    /**
     * Get current music enabled state synchronously
     */
    fun isMusicEnabledSync(): Boolean {
        return musicEnabled
    }
    
    /**
     * Check if music is currently playing
     */
    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }
    
    /**
     * Release all resources
     */
    fun release() {
        stopMusic()
    }
    
    companion object {
        @Volatile
        private var instance: MusicManager? = null
        
        fun getInstance(context: Context): MusicManager {
            return instance ?: synchronized(this) {
                instance ?: MusicManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

/**
 * Available music tracks
 */
enum class MusicTrack {
    BATTLE,     // In-game battle music
    MENU,       // Main menu background
    VICTORY,    // Victory celebration
    LOBBY       // Multiplayer lobby
}
