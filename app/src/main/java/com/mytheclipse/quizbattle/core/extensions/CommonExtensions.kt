package com.mytheclipse.quizbattle.core.extensions

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * String extension functions
 */

/**
 * Check if string is a valid email
 */
fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

/**
 * Check if string is a valid password (at least 6 characters)
 */
fun String.isValidPassword(minLength: Int = 6): Boolean {
    return this.length >= minLength
}

/**
 * Capitalize first letter
 */
fun String.capitalizeFirst(): String {
    return if (isNotEmpty()) {
        this[0].uppercaseChar() + substring(1)
    } else this
}

/**
 * Truncate string with ellipsis
 */
fun String.truncate(maxLength: Int, ellipsis: String = "..."): String {
    return if (length > maxLength) {
        take(maxLength - ellipsis.length) + ellipsis
    } else this
}

/**
 * Format as Indonesian phone number
 */
fun String.formatPhoneNumber(): String {
    val digits = filter { it.isDigit() }
    return when {
        digits.startsWith("62") -> "+$digits"
        digits.startsWith("0") -> "+62${digits.drop(1)}"
        else -> "+62$digits"
    }
}

// ===== Number Extensions =====

/**
 * Format number with thousand separator
 */
fun Int.formatWithSeparator(): String {
    return String.format(Locale("id", "ID"), "%,d", this)
}

/**
 * Format number with thousand separator
 */
fun Long.formatWithSeparator(): String {
    return String.format(Locale("id", "ID"), "%,d", this)
}

/**
 * Convert milliseconds to readable duration (e.g., "2m 30s")
 */
fun Long.toReadableDuration(): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(this) % 60
    return when {
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}

/**
 * Convert seconds to MM:SS format
 */
fun Int.toTimeFormat(): String {
    val minutes = this / 60
    val seconds = this % 60
    return String.format("%02d:%02d", minutes, seconds)
}

/**
 * Format as percentage
 */
fun Float.toPercentageString(decimals: Int = 0): String {
    return String.format("%.${decimals}f%%", this * 100)
}

// ===== Date Extensions =====

/**
 * Format timestamp to readable date
 */
fun Long.toFormattedDate(pattern: String = "dd MMM yyyy"): String {
    val sdf = SimpleDateFormat(pattern, Locale("id", "ID"))
    return sdf.format(Date(this))
}

/**
 * Format timestamp to relative time (e.g., "2 jam lalu")
 */
fun Long.toRelativeTime(): String {
    val now = System.currentTimeMillis()
    val diff = now - this
    
    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> "Baru saja"
        diff < TimeUnit.HOURS.toMillis(1) -> {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            "$minutes menit lalu"
        }
        diff < TimeUnit.DAYS.toMillis(1) -> {
            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            "$hours jam lalu"
        }
        diff < TimeUnit.DAYS.toMillis(7) -> {
            val days = TimeUnit.MILLISECONDS.toDays(diff)
            "$days hari lalu"
        }
        else -> toFormattedDate()
    }
}
