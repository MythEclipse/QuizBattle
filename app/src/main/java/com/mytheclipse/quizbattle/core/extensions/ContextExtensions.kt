package com.mytheclipse.quizbattle.core.extensions

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Context extension functions for common operations
 */

/**
 * Show a short toast
 */
fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

/**
 * Show a short toast from string resource
 */
fun Context.toast(@StringRes resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
}

/**
 * Show a long toast
 */
fun Context.longToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

/**
 * Check if network is available
 */
fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

/**
 * Start activity with type-safe intent
 */
inline fun <reified T> Context.startActivity(configIntent: Intent.() -> Unit = {}) {
    startActivity(Intent(this, T::class.java).apply(configIntent))
}

/**
 * Show a Material alert dialog
 */
fun Context.showMaterialDialog(
    title: String,
    message: String,
    positiveText: String = "OK",
    negativeText: String? = null,
    onPositive: (() -> Unit)? = null,
    onNegative: (() -> Unit)? = null,
    cancellable: Boolean = true
): AlertDialog {
    return MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(message)
        .setCancelable(cancellable)
        .setPositiveButton(positiveText) { dialog, _ ->
            onPositive?.invoke()
            dialog.dismiss()
        }
        .apply {
            if (negativeText != null) {
                setNegativeButton(negativeText) { dialog, _ ->
                    onNegative?.invoke()
                    dialog.dismiss()
                }
            }
        }
        .show()
}

/**
 * Show a confirmation dialog
 */
fun Context.showConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Ya",
    cancelText: String = "Batal",
    onConfirm: () -> Unit
): AlertDialog {
    return showMaterialDialog(
        title = title,
        message = message,
        positiveText = confirmText,
        negativeText = cancelText,
        onPositive = onConfirm
    )
}
