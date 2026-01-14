package com.mytheclipse.quizbattle.core.extensions

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Flow extension functions for common operations
 */

/**
 * Collect flow with lifecycle awareness in Activity/Fragment
 * Only collects when lifecycle is at least STARTED
 */
fun <T> Flow<T>.collectWithLifecycle(
    lifecycleOwner: LifecycleOwner,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    collector: suspend (T) -> Unit
) {
    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(state) {
            collectLatest(collector)
        }
    }
}

/**
 * Collect flow in Fragment's viewLifecycleOwner scope
 */
fun <T> Flow<T>.collectInFragment(
    fragment: Fragment,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    collector: suspend (T) -> Unit
) {
    fragment.viewLifecycleOwner.lifecycleScope.launch {
        fragment.repeatOnLifecycle(state) {
            collectLatest(collector)
        }
    }
}

/**
 * Get first value with timeout
 */
suspend fun <T> Flow<T>.firstWithTimeout(timeoutMs: Long): T? {
    return withTimeoutOrNull(timeoutMs) {
        first()
    }
}

/**
 * Map and collect StateFlow
 */
fun <T, R> StateFlow<T>.mapState(transform: (T) -> R): StateFlow<R> {
    val mutableStateFlow = MutableStateFlow(transform(value))
    // Note: This creates a new StateFlow, use with caution in ViewModels
    return mutableStateFlow
}

/**
 * Update MutableStateFlow atomically
 */
inline fun <T> MutableStateFlow<T>.updateState(transform: (T) -> T) {
    update(transform)
}

/**
 * Emit value to MutableStateFlow (alias for value assignment)
 */
fun <T> MutableStateFlow<T>.emit(value: T) {
    this.value = value
}
