package com.offlinelearninghub.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mediaFileId: Long? = null,
    val durationMinutes: Int = 0,
    val isCompleted: Boolean = false,
    val sessionDate: Long = System.currentTimeMillis()
)
