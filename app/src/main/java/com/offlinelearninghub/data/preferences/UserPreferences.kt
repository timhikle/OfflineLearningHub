package com.offlinelearninghub.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    companion object {
        private val KEY_LANGUAGE = stringPreferencesKey("language")
        private val KEY_THEME_MODE = intPreferencesKey("theme_mode")
        private val KEY_POMODORO_FOCUS_MINUTES = intPreferencesKey("pomodoro_focus_minutes")
        private val KEY_POMODORO_BREAK_MINUTES = intPreferencesKey("pomodoro_break_minutes")

        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_ARABIC = "ar"
        const val THEME_SYSTEM = 0
        const val THEME_LIGHT = 1
        const val THEME_DARK = 2
    }

    val language: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_LANGUAGE] ?: LANGUAGE_ENGLISH
    }

    val themeMode: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_THEME_MODE] ?: THEME_SYSTEM
    }

    val pomodoroFocusMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_POMODORO_FOCUS_MINUTES] ?: 25
    }

    val pomodoroBreakMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_POMODORO_BREAK_MINUTES] ?: 5
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = language
        }
    }

    suspend fun setThemeMode(mode: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode
        }
    }

    suspend fun setPomodoroFocusMinutes(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_POMODORO_FOCUS_MINUTES] = minutes
        }
    }

    suspend fun setPomodoroBreakMinutes(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_POMODORO_BREAK_MINUTES] = minutes
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
