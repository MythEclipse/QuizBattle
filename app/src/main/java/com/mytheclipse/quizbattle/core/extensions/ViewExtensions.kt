package com.mytheclipse.quizbattle.core.extensions

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.AnimRes
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible

/**
 * View extension functions for common operations
 */

/**
 * Set visibility to VISIBLE
 */
fun View.show() {
    isVisible = true
}

/**
 * Set visibility to GONE
 */
fun View.hide() {
    isGone = true
}

/**
 * Set visibility to INVISIBLE
 */
fun View.invisible() {
    isInvisible = true
}

/**
 * Toggle visibility between VISIBLE and GONE
 */
fun View.toggleVisibility() {
    isVisible = !isVisible
}

/**
 * Set visibility based on condition
 */
fun View.showIf(condition: Boolean) {
    isVisible = condition
}

/**
 * Set visibility to GONE based on condition
 */
fun View.hideIf(condition: Boolean) {
    isVisible = !condition
}

/**
 * Safe click listener with debounce
 * Prevents double-clicks within 500ms window
 */
fun View.setOnSafeClickListener(
    debounceTime: Long = 500L,
    action: (View) -> Unit
) {
    var lastClickTime = 0L
    setOnClickListener { view ->
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= debounceTime) {
            lastClickTime = currentTime
            action(view)
        }
    }
}

/**
 * Set click listener with lambda
 */
inline fun View.onClick(crossinline action: () -> Unit) {
    setOnClickListener { action() }
}

/**
 * Load and start animation from resource
 */
fun View.startAnimation(@AnimRes animRes: Int) {
    val animation = AnimationUtils.loadAnimation(context, animRes)
    startAnimation(animation)
}

/**
 * Animate fade in
 */
fun View.fadeIn(duration: Long = 300L) {
    alpha = 0f
    show()
    animate()
        .alpha(1f)
        .setDuration(duration)
        .start()
}

/**
 * Animate fade out and optionally hide
 */
fun View.fadeOut(duration: Long = 300L, hideAfter: Boolean = true) {
    animate()
        .alpha(0f)
        .setDuration(duration)
        .withEndAction {
            if (hideAfter) hide()
        }
        .start()
}

/**
 * Scale up animation
 */
fun View.scaleUp(duration: Long = 200L, scale: Float = 1.1f) {
    animate()
        .scaleX(scale)
        .scaleY(scale)
        .setDuration(duration)
        .start()
}

/**
 * Scale down animation
 */
fun View.scaleDown(duration: Long = 200L) {
    animate()
        .scaleX(1f)
        .scaleY(1f)
        .setDuration(duration)
        .start()
}

/**
 * Pulse animation effect
 */
fun View.pulse(scale: Float = 1.1f, duration: Long = 200L) {
    animate()
        .scaleX(scale)
        .scaleY(scale)
        .setDuration(duration / 2)
        .withEndAction {
            animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(duration / 2)
                .start()
        }
        .start()
}

/**
 * Enable view with alpha change
 */
fun View.enable() {
    isEnabled = true
    alpha = 1f
}

/**
 * Disable view with alpha change
 */
fun View.disable(alpha: Float = 0.5f) {
    isEnabled = false
    this.alpha = alpha
}

/**
 * Set enabled state based on condition
 */
fun View.enableIf(condition: Boolean) {
    isEnabled = condition
    alpha = if (condition) 1f else 0.5f
}
