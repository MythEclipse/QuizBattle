package com.mytheclipse.quizbattle.utils

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

/**
 * Remember SoundManager instance in Composable
 * Provides easy access to sound manager in Compose UI
 */
@Composable
fun rememberSoundManager(): SoundManager {
    val context = LocalContext.current
    return remember { SoundManager.getInstance(context) }
}

/**
 * Play sound effect with optional check for enabled state
 * Simplified API for common use cases
 */
@Composable
fun PlaySound(effect: SoundEffect, volume: Float = 1.0f) {
    val soundManager = rememberSoundManager()
    LaunchedEffect(Unit) {
        soundManager.playSound(effect, volume)
    }
}

/**
 * Extension function to play sound from non-Composable context
 */
fun Context.playSound(effect: SoundEffect, volume: Float = 1.0f) {
    SoundManager.getInstance(this).playSound(effect, volume)
}

/**
 * Helper to play button click sound
 */
fun SoundManager.playButtonClick() {
    playSound(SoundEffect.BUTTON_CLICK, 0.5f)
}

/**
 * Helper to play correct answer sound
 */
fun SoundManager.playCorrectAnswer() {
    playSound(SoundEffect.CORRECT_ANSWER, 0.8f)
}

/**
 * Helper to play wrong answer sound
 */
fun SoundManager.playWrongAnswer() {
    playSound(SoundEffect.WRONG_ANSWER, 0.7f)
}

/**
 * Helper to play victory sound
 */
fun SoundManager.playVictory() {
    playSound(SoundEffect.VICTORY, 1.0f)
}

/**
 * Helper to play defeat sound
 */
fun SoundManager.playDefeat() {
    playSound(SoundEffect.DEFEAT, 1.0f)
}

/**
 * Helper to play notification sound
 */
fun SoundManager.playNotification() {
    playSound(SoundEffect.NOTIFICATION, 0.6f)
}

/**
 * Helper to play message sent sound
 */
fun SoundManager.playMessageSent() {
    playSound(SoundEffect.MESSAGE_SENT, 0.4f)
}

/**
 * Helper to play message received sound
 */
fun SoundManager.playMessageReceived() {
    playSound(SoundEffect.MESSAGE_RECEIVED, 0.5f)
}

/**
 * Helper to play level up sound
 */
fun SoundManager.playLevelUp() {
    playSound(SoundEffect.LEVEL_UP, 0.9f)
}

/**
 * Helper to play match found sound
 */
fun SoundManager.playMatchFound() {
    playSound(SoundEffect.MATCH_FOUND, 0.8f)
}
