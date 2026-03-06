package com.subnavar.app.util

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import java.util.Locale

object LocaleManager {

    private const val PREFS_NAME = "subnav_prefs"
    private const val KEY_LANGUAGE = "app_language"

    enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
        ENGLISH("en", "English", "English"),
        HEBREW("iw", "Hebrew", "עברית")
    }

    fun getLanguage(context: Context): AppLanguage {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val code = prefs.getString(KEY_LANGUAGE, "en") ?: "en"
        return AppLanguage.entries.find { it.code == code } ?: AppLanguage.ENGLISH
    }

    fun setLanguage(context: Context, language: AppLanguage) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
    }

    fun applyLanguage(context: Context): Context {
        val language = getLanguage(context)
        val locale = Locale(language.code)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }

    fun isRtl(context: Context): Boolean {
        return getLanguage(context) == AppLanguage.HEBREW
    }
}
