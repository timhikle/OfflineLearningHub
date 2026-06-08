package com.offlinelearninghub.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val coverImagePath: String? = null,
    val totalModules: Int = 0,
    val completedModules: Int = 0,
    val totalDurationMinutes: Long = 0,
    val lastAccessedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
