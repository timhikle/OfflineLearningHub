package com.offlinelearninghub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.offlinelearninghub.data.local.AppDatabase
import com.offlinelearninghub.data.preferences.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SettingsUiState(
    val currentLanguage: String = "en",
    val themeMode: Int = 0,
    val storageBytes: Long = 0
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    val userPreferences = UserPreferences(application)
    private val db = AppDatabase.getInstance(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val lang = userPreferences.language.first()
            val theme = userPreferences.themeMode.first()
            val storageBytes = db.mediaFileDao().getTotalStorageBytes().first() ?: 0
            _uiState.value = SettingsUiState(
                currentLanguage = lang,
                themeMode = theme,
                storageBytes = storageBytes
            )
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            userPreferences.setLanguage(language)
            _uiState.value = _uiState.value.copy(currentLanguage = language)
        }
    }

    fun setThemeMode(mode: Int) {
        viewModelScope.launch {
            userPreferences.setThemeMode(mode)
            _uiState.value = _uiState.value.copy(themeMode = mode)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            db.mediaFileDao().deleteAll()
            db.courseDao().deleteAll()
            db.noteDao().deleteAll()
            db.bookmarkDao().deleteAll()
            db.studySessionDao().deleteAll()
            userPreferences.clearAll()
            loadSettings()
        }
    }

    fun refreshStorage() {
        viewModelScope.launch {
            val storageBytes = db.mediaFileDao().getTotalStorageBytes().first() ?: 0
            _uiState.value = _uiState.value.copy(storageBytes = storageBytes)
        }
    }
}
