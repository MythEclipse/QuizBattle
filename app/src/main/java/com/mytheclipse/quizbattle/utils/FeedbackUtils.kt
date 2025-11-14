package com.mytheclipse.quizbattle.utils

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Haptic Feedback Helper
 */
class HapticFeedback(private val context: Context) {
    
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
    
    /**
     * Light tap feedback
     */
    fun lightTap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(10)
        }
    }
    
    /**
     * Medium tap feedback
     */
    fun mediumTap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(30)
        }
    }
    
    /**
     * Strong tap feedback
     */
    fun strongTap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(50)
        }
    }
    
    /**
     * Success feedback (double tap)
     */
    fun success() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 20, 50, 20),
                    intArrayOf(0, 100, 0, 150),
                    -1
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 20, 50, 20), -1)
        }
    }
    
    /**
     * Error feedback (long vibration)
     */
    fun error() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(200)
        }
    }
}

/**
 * Remember haptic feedback in Composable
 */
@Composable
fun rememberHapticFeedback(): HapticFeedback {
    val context = LocalContext.current
    return remember { HapticFeedback(context) }
}

/**
 * Sound Effects Manager
 */
class SoundEffects(private val context: Context) {
    
    private var mediaPlayer: MediaPlayer? = null
    private var isSoundEnabled = true
    
    fun setSoundEnabled(enabled: Boolean) {
        isSoundEnabled = enabled
    }
    
    /**
     * Play sound from raw resource
     */
    fun playSound(resId: Int) {
        if (!isSoundEnabled) return
        
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, resId)
            mediaPlayer?.setOnCompletionListener {
                it.release()
            }
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Play button click sound
     */
    fun playButtonClick() {
        // TODO: Add sound resource
        // playSound(R.raw.button_click)
    }
    
    /**
     * Play correct answer sound
     */
    fun playCorrectAnswer() {
        // TODO: Add sound resource
        // playSound(R.raw.correct_answer)
    }
    
    /**
     * Play wrong answer sound
     */
    fun playWrongAnswer() {
        // TODO: Add sound resource
        // playSound(R.raw.wrong_answer)
    }
    
    /**
     * Play victory sound
     */
    fun playVictory() {
        // TODO: Add sound resource
        // playSound(R.raw.victory)
    }
    
    /**
     * Play defeat sound
     */
    fun playDefeat() {
        // TODO: Add sound resource
        // playSound(R.raw.defeat)
    }
    
    /**
     * Play match found sound
     */
    fun playMatchFound() {
        // TODO: Add sound resource
        // playSound(R.raw.match_found)
    }
    
    /**
     * Release resources
     */
    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

/**
 * Remember sound effects in Composable
 */
@Composable
fun rememberSoundEffects(): SoundEffects {
    val context = LocalContext.current
    return remember { SoundEffects(context) }
}
