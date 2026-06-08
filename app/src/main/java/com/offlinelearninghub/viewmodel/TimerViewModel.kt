package com.offlinelearninghub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.offlinelearninghub.data.preferences.UserPreferences
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class TimerState {
    IDLE, FOCUS, BREAK, PAUSED
}

data class TimerUiState(
    val timerState: TimerState = TimerState.IDLE,
    val remainingSeconds: Int = 0,
    val totalSeconds: Int = 0,
    val focusMinutes: Int = 25,
    val breakMinutes: Int = 5,
    val isRunning: Boolean = false,
    val sessionComplete: Boolean = false,
    val isBreakComplete: Boolean = false,
    val previousState: TimerState = TimerState.IDLE
)

class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)
    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            val focusMin = userPreferences.pomodoroFocusMinutes.first()
            val breakMin = userPreferences.pomodoroBreakMinutes.first()
            _uiState.value = _uiState.value.copy(
                focusMinutes = focusMin,
                breakMinutes = breakMin
            )
        }
    }

    fun startFocus() {
        val totalSec = _uiState.value.focusMinutes * 60
        _uiState.value = _uiState.value.copy(
            timerState = TimerState.FOCUS,
            previousState = TimerState.FOCUS,
            remainingSeconds = totalSec,
            totalSeconds = totalSec,
            isRunning = true,
            sessionComplete = false,
            isBreakComplete = false
        )
        startTimer()
    }

    fun startBreak() {
        val totalSec = _uiState.value.breakMinutes * 60
        _uiState.value = _uiState.value.copy(
            timerState = TimerState.BREAK,
            previousState = TimerState.BREAK,
            remainingSeconds = totalSec,
            totalSeconds = totalSec,
            isRunning = true,
            sessionComplete = false,
            isBreakComplete = false
        )
        startTimer()
    }

    fun pause() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            timerState = TimerState.PAUSED,
            isRunning = false
        )
    }

    fun resume() {
        _uiState.value = _uiState.value.copy(
            timerState = _uiState.value.previousState,
            isRunning = true
        )
        startTimer()
    }

    fun reset() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            timerState = TimerState.IDLE,
            remainingSeconds = 0,
            totalSeconds = 0,
            isRunning = false,
            sessionComplete = false,
            isBreakComplete = false,
            previousState = TimerState.IDLE
        )
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0) {
                delay(1000L)
                _uiState.value = _uiState.value.copy(
                    remainingSeconds = _uiState.value.remainingSeconds - 1
                )
            }
            onTimerComplete()
        }
    }

    private fun onTimerComplete() {
        when (_uiState.value.previousState) {
            TimerState.FOCUS -> {
                _uiState.value = _uiState.value.copy(
                    isRunning = false,
                    sessionComplete = true,
                    timerState = TimerState.IDLE,
                    previousState = TimerState.IDLE
                )
            }
            TimerState.BREAK -> {
                _uiState.value = _uiState.value.copy(
                    isRunning = false,
                    isBreakComplete = true,
                    timerState = TimerState.IDLE,
                    previousState = TimerState.IDLE
                )
            }
            else -> {}
        }
    }

    fun dismissSessionComplete() {
        _uiState.value = _uiState.value.copy(
            sessionComplete = false,
            isBreakComplete = false
        )
    }

    fun setFocusMinutes(minutes: Int) {
        _uiState.value = _uiState.value.copy(focusMinutes = minutes)
        viewModelScope.launch {
            userPreferences.setPomodoroFocusMinutes(minutes)
        }
    }

    fun setBreakMinutes(minutes: Int) {
        _uiState.value = _uiState.value.copy(breakMinutes = minutes)
        viewModelScope.launch {
            userPreferences.setPomodoroBreakMinutes(minutes)
        }
    }

    fun getFormattedTime(): String {
        val secs = _uiState.value.remainingSeconds
        val mins = secs / 60
        val sec = secs % 60
        return String.format("%02d:%02d", mins, sec)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
