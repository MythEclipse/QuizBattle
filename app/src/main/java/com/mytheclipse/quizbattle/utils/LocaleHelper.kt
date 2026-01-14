package com.mytheclipse.quizbattle.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * Helper class to manage app locale/language settings
 * Supports Indonesian (default) and English
 */
object LocaleHelper {

    private const val PREFS_NAME = "locale_prefs"
    private const val KEY_LANGUAGE = "language"
    
    const val LANGUAGE_INDONESIAN = "id"
    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_SYSTEM = "system"

    /**
     * Get saved language preference
     */
    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, LANGUAGE_INDONESIAN) ?: LANGUAGE_INDONESIAN
    }

    /**
     * Save language preference
     */
    fun setLanguage(context: Context, language: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language).apply()
    }

    /**
     * Apply saved language to context
     * Call this in attachBaseContext of Activity or Application
     */
    fun applyLocale(context: Context): Context {
        val language = getLanguage(context)
        return updateResources(context, language)
    }

    /**
     * Update context resources with new locale
     */
    fun updateResources(context: Context, language: String): Context {
        val locale = when (language) {
            LANGUAGE_SYSTEM -> Locale.getDefault()
            else -> Locale(language)
        }
        
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }

    /**
     * Get display name for a language code
     */
    fun getLanguageDisplayName(context: Context, languageCode: String): String {
        return when (languageCode) {
            LANGUAGE_INDONESIAN -> "Bahasa Indonesia"
            LANGUAGE_ENGLISH -> "English"
            LANGUAGE_SYSTEM -> "System Default"
            else -> languageCode
        }
    }

    /**
     * Get all available languages
     */
    fun getAvailableLanguages(): List<Pair<String, String>> {
        return listOf(
            LANGUAGE_INDONESIAN to "Bahasa Indonesia",
            LANGUAGE_ENGLISH to "English"
        )
    }

    /**
     * Check if app needs restart after language change
     */
    fun isRestartRequired(context: Context, newLanguage: String): Boolean {
        return getLanguage(context) != newLanguage
    }
}
