package com.mytheclipse.quizbattle.core.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Debouncer utility for handling rapid input events
 * Useful for search inputs, button clicks, etc.
 */
class Debouncer(
    private val scope: CoroutineScope,
    private val delayMs: Long = 300L
) {
    private var debounceJob: Job? = null
    
    /**
     * Execute action after delay, cancelling any pending execution
     */
    fun debounce(action: suspend () -> Unit) {
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(delayMs)
            action()
        }
    }
    
    /**
     * Cancel any pending execution
     */
    fun cancel() {
        debounceJob?.cancel()
    }
}

/**
 * Throttler utility for rate-limiting actions
 * Unlike debounce, this executes immediately and blocks subsequent calls
 */
class Throttler(
    private val intervalMs: Long = 500L
) {
    private var lastExecutionTime = 0L
    
    /**
     * Execute action if enough time has passed since last execution
     * Returns true if action was executed
     */
    fun throttle(action: () -> Unit): Boolean {
        val currentTime = System.currentTimeMillis()
        return if (currentTime - lastExecutionTime >= intervalMs) {
            lastExecutionTime = currentTime
            action()
            true
        } else {
            false
        }
    }
    
    /**
     * Reset the throttle timer
     */
    fun reset() {
        lastExecutionTime = 0L
    }
}

/**
 * Click throttler for preventing double-clicks
 */
class ClickThrottler(intervalMs: Long = 500L) {
    private val throttler = Throttler(intervalMs)
    
    fun onClick(action: () -> Unit) {
        throttler.throttle(action)
    }
}

/**
 * Extension function for scoped debouncing
 */
fun CoroutineScope.debounce(
    delayMs: Long = 300L,
    action: suspend () -> Unit
): Job {
    return launch {
        delay(delayMs)
        action()
    }
}
