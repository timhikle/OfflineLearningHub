package com.offlinelearninghub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.offlinelearninghub.data.local.AppDatabase
import com.offlinelearninghub.data.local.CourseEntity
import com.offlinelearninghub.data.local.StudySessionEntity
import com.offlinelearninghub.data.repository.CourseRepository
import com.offlinelearninghub.data.repository.MediaFileRepository
import com.offlinelearninghub.data.repository.StudySessionRepository
import com.offlinelearninghub.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val lastCourse: CourseEntity? = null,
    val todayStudyMinutes: Int = 0,
    val completedModules: Int = 0,
    val activeStreakDays: Int = 0,
    val totalCourses: Int = 0,
    val isLoading: Boolean = true
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val courseRepo = CourseRepository(db.courseDao())
    private val studyRepo = StudySessionRepository(db.studySessionDao())
    private val mediaRepo = MediaFileRepository(db.mediaFileDao())

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            val startOfDay = getStartOfDayMillis()
            val endOfDay = startOfDay + 86400000L
            val streakSince = System.currentTimeMillis() -
                (Constants.STREAK_CALCULATION_DAYS.toLong() * 86400000L)

            combine(
                courseRepo.getLastAccessedCourse(),
                studyRepo.getTodayStudyMinutes(startOfDay, endOfDay),
                studyRepo.getCompletedModulesCount(),
                studyRepo.getActiveDaysCount(streakSince),
                courseRepo.getCourseCount()
            ) { lastCourse, todayMins, completedMods, activeDays, courseCount ->
                DashboardUiState(
                    lastCourse = lastCourse,
                    todayStudyMinutes = todayMins,
                    completedModules = completedMods,
                    activeStreakDays = activeDays,
                    totalCourses = courseCount,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun recordStudySession(durationMinutes: Int, mediaFileId: Long? = null) {
        viewModelScope.launch {
            studyRepo.insertSession(
                StudySessionEntity(
                    mediaFileId = mediaFileId,
                    durationMinutes = durationMinutes,
                    isCompleted = true
                )
            )
        }
    }

    private fun getStartOfDayMillis(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
