package com.offlinelearninghub.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocalizationUtils {
    fun applyLanguage(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }

    fun isRtl(languageCode: String): Boolean {
        return languageCode == "ar"
    }
}
