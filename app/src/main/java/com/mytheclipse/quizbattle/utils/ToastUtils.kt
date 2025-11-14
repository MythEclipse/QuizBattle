package com.mytheclipse.quizbattle.utils

import android.content.Context
import android.widget.Toast
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Toast utility functions for showing quick messages
 */
object ToastUtils {
    fun showShort(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    
    fun showLong(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    
    fun showSuccess(context: Context, message: String) {
        Toast.makeText(context, "✓ $message", Toast.LENGTH_SHORT).show()
    }
    
    fun showError(context: Context, message: String) {
        Toast.makeText(context, "✗ $message", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Snackbar utility functions for showing action messages
 */
object SnackbarUtils {
    fun showSnackbar(
        scope: CoroutineScope,
        snackbarHostState: SnackbarHostState,
        message: String,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
        onActionPerformed: (() -> Unit)? = null
    ) {
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                duration = duration
            )
            if (result == SnackbarResult.ActionPerformed) {
                onActionPerformed?.invoke()
            }
        }
    }
    
    fun showSuccess(
        scope: CoroutineScope,
        snackbarHostState: SnackbarHostState,
        message: String
    ) {
        showSnackbar(scope, snackbarHostState, "✓ $message")
    }
    
    fun showError(
        scope: CoroutineScope,
        snackbarHostState: SnackbarHostState,
        message: String,
        actionLabel: String? = "Retry",
        onRetry: (() -> Unit)? = null
    ) {
        showSnackbar(
            scope = scope,
            snackbarHostState = snackbarHostState,
            message = "✗ $message",
            actionLabel = actionLabel,
            duration = SnackbarDuration.Long,
            onActionPerformed = onRetry
        )
    }
    
    fun showInfo(
        scope: CoroutineScope,
        snackbarHostState: SnackbarHostState,
        message: String,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null
    ) {
        showSnackbar(
            scope = scope,
            snackbarHostState = snackbarHostState,
            message = message,
            actionLabel = actionLabel,
            duration = SnackbarDuration.Short,
            onActionPerformed = onAction
        )
    }
}
