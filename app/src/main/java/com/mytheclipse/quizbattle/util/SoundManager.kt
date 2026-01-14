package com.mytheclipse.quizbattle.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.mytheclipse.quizbattle.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.soundDataStore by preferencesDataStore(name = "sound_settings")

class SoundManager(private val context: Context) {

    private val MUSIC_ENABLED_KEY = booleanPreferencesKey("music_enabled")
    private val SFX_ENABLED_KEY = booleanPreferencesKey("sfx_enabled")

    private var musicPlayer: MediaPlayer? = null
    private var soundPool: SoundPool? = null
    
    // Sound IDs
    private var sfxAttackKnight: Int = 0
    private var sfxAttackMonster: Int = 0
    private var sfxHurtKnight: Int = 0
    private var sfxHurtMonster: Int = 0
    private var sfxDieKnight: Int = 0
    private var sfxDieMonster: Int = 0
    private var sfxWin: Int = 0
    private var sfxLose: Int = 0
    
    // Flags - load from preferences
    private var isMusicEnabled = true
    private var isSfxEnabled = true

    init {
        // Load preferences
        runBlocking {
            isMusicEnabled = context.soundDataStore.data.map { 
                it[MUSIC_ENABLED_KEY] ?: true 
            }.first()
            isSfxEnabled = context.soundDataStore.data.map { 
                it[SFX_ENABLED_KEY] ?: true 
            }.first()
        }
        initSoundPool()
    }
    
    private fun initSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
            
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()
            
        loadSounds()
    }
    
    private fun loadSounds() {
        soundPool?.let { pool ->
            sfxAttackKnight = pool.load(context, R.raw.attack_knight, 1)
            sfxAttackMonster = pool.load(context, R.raw.attack_monster, 1)
            sfxHurtKnight = pool.load(context, R.raw.hurt_knight, 1)
            sfxHurtMonster = pool.load(context, R.raw.hurt_monster, 1) // Using hurt_monster for goblin
            sfxDieKnight = pool.load(context, R.raw.die_knight, 1)
            sfxDieMonster = pool.load(context, R.raw.die_monster, 1)
            sfxWin = pool.load(context, R.raw.gem, 1) // Using gem sound for win/correct
            sfxLose = pool.load(context, R.raw.hurt_knight, 1) // Fallback
        }
    }

    fun playBattleMusic() {
        android.util.Log.d("SoundManager", "playBattleMusic called. Enabled: $isMusicEnabled")
        if (!isMusicEnabled) return
        
        try {
            if (musicPlayer == null) {
                android.util.Log.d("SoundManager", "Creating MediaPlayer for R.raw.battle_music")
                musicPlayer = MediaPlayer.create(context, R.raw.battle_music)
                
                if (musicPlayer == null) {
                    android.util.Log.e("SoundManager", "MediaPlayer creation failed (returned null). Resource R.raw.battle_music might be missing or invalid.")
                    return
                }
                
                musicPlayer?.apply {
                    isLooping = true
                    setVolume(1.0f, 1.0f) // Max volume
                    setOnErrorListener { _, what, extra ->
                        android.util.Log.e("SoundManager", "MediaPlayer Error: what=$what, extra=$extra")
                        true // Handled
                    }
                    android.util.Log.d("SoundManager", "MediaPlayer created and configured.")
                }
            }
            
            if (musicPlayer?.isPlaying == false) {
                android.util.Log.d("SoundManager", "Starting music...")
                musicPlayer?.start()
                android.util.Log.d("SoundManager", "Music started.")
            } else {
                 android.util.Log.d("SoundManager", "Music is already playing.")
            }
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Exception in playBattleMusic", e)
        }
    }
    
    fun pauseMusic() {
        android.util.Log.d("SoundManager", "pauseMusic called")
        try {
            if (musicPlayer?.isPlaying == true) {
                musicPlayer?.pause()
            }
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Error pausing music", e)
        }
    }
    
    fun stopMusic() {
        android.util.Log.d("SoundManager", "stopMusic called")
        try {
            musicPlayer?.stop()
            musicPlayer?.release()
            musicPlayer = null
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Error stopping music", e)
        }
    }
    
    fun playAttackKnight() {
        playSfx(sfxAttackKnight)
    }
    
    fun playAttackMonster() {
        playSfx(sfxAttackMonster)
    }
    
    fun playHurtKnight() {
        playSfx(sfxHurtKnight)
    }
    
    fun playHurtMonster() {
        playSfx(sfxHurtMonster)
    }
    
    fun playWin() {
        playSfx(sfxWin)
    }
    
    fun playDieMonster() {
        playSfx(sfxDieMonster)
    }
    
    fun playDieKnight() {
        playSfx(sfxDieKnight)
    }
    
    private fun playSfx(soundId: Int) {
        if (!isSfxEnabled || soundId == 0) return
        soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
    }
    
    fun setMusicEnabled(enabled: Boolean) {
        isMusicEnabled = enabled
        if (!enabled) {
            stopMusic()
        }
        runBlocking {
            context.soundDataStore.edit { prefs ->
                prefs[MUSIC_ENABLED_KEY] = enabled
            }
        }
    }
    
    fun setSfxEnabled(enabled: Boolean) {
        isSfxEnabled = enabled
        runBlocking {
            context.soundDataStore.edit { prefs ->
                prefs[SFX_ENABLED_KEY] = enabled
            }
        }
    }
    
    fun isMusicEnabled(): Boolean = isMusicEnabled
    fun isSfxEnabled(): Boolean = isSfxEnabled
    
    fun release() {
        stopMusic()
        soundPool?.release()
        soundPool = null
    }
}
