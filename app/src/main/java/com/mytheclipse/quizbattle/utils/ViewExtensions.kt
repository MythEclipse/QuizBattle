package com.mytheclipse.quizbattle.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding

/**
 * Enable edge-to-edge (full screen) mode
 * Call this in onCreate after setContentView
 */
fun Activity.enableEdgeToEdge() {
    // Make the app go edge-to-edge
    WindowCompat.setDecorFitsSystemWindows(window, false)
}

/**
 * Apply system bar insets as padding to a view
 * Use this on root ScrollView or top-level layout that needs padding
 */
fun View.applySystemBarInsets(
    applyTop: Boolean = true,
    applyBottom: Boolean = true,
    applyLeft: Boolean = false,
    applyRight: Boolean = false
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(
            top = if (applyTop) insets.top else view.paddingTop,
            bottom = if (applyBottom) insets.bottom else view.paddingBottom,
            left = if (applyLeft) insets.left else view.paddingLeft,
            right = if (applyRight) insets.right else view.paddingRight
        )
        WindowInsetsCompat.CONSUMED
    }
}

/**
 * Hide system bars for immersive full screen
 */
fun Activity.hideSystemBars() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.insetsController?.let {
            it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    } else {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        )
    }
}

/**
 * Show system bars
 */
fun Activity.showSystemBars() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.insetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
    } else {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }
}

/**
 * View visibility extensions
 */
fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.showIf(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}

/**
 * Dimension conversion extensions
 */
fun Int.dpToPx(context: Context): Int {
    return (this * context.resources.displayMetrics.density).toInt()
}

fun Float.dpToPx(context: Context): Float {
    return this * context.resources.displayMetrics.density
}

fun Int.pxToDp(context: Context): Int {
    return (this / context.resources.displayMetrics.density).toInt()
}

/**
 * Loading state helper for views
 */
fun View.setLoadingState(isLoading: Boolean) {
    isEnabled = !isLoading
    alpha = if (isLoading) 0.6f else 1.0f
}

/**
 * Set clickable state with visual feedback
 */
fun View.setClickableState(clickable: Boolean) {
    isClickable = clickable
    isFocusable = clickable
    alpha = if (clickable) 1.0f else 0.5f
}

/**
 * Fade animations
 */
fun View.fadeIn(duration: Long = 300) {
    alpha = 0f
    visibility = View.VISIBLE
    animate()
        .alpha(1f)
        .setDuration(duration)
        .start()
}

fun View.fadeOut(duration: Long = 300, gone: Boolean = true) {
    animate()
        .alpha(0f)
        .setDuration(duration)
        .withEndAction {
            visibility = if (gone) View.GONE else View.INVISIBLE
        }
        .start()
}

/**
 * Scale animations
 */
fun View.scaleUp(duration: Long = 200) {
    scaleX = 0.8f
    scaleY = 0.8f
    alpha = 0f
    visibility = View.VISIBLE
    animate()
        .scaleX(1f)
        .scaleY(1f)
        .alpha(1f)
        .setDuration(duration)
        .start()
}

fun View.pulse(repeatCount: Int = 2) {
    val scale = 1.05f
    animate()
        .scaleX(scale)
        .scaleY(scale)
        .setDuration(100)
        .withEndAction {
            animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(100)
                .start()
        }
        .start()
}

/**
 * Safe click listener to prevent double clicks
 */
inline fun View.setDebouncedClickListener(
    debounceTime: Long = 500L,
    crossinline action: (View) -> Unit
) {
    var lastClickTime = 0L
    setOnClickListener { view ->
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > debounceTime) {
            lastClickTime = currentTime
            action(view)
        }
    }
}

/**
 * Progress bar helpers
 */
fun ProgressBar.animateProgress(toProgress: Int, duration: Long = 300) {
    val animator = android.animation.ObjectAnimator.ofInt(this, "progress", progress, toProgress)
    animator.duration = duration
    animator.interpolator = android.view.animation.DecelerateInterpolator()
    animator.start()
}
