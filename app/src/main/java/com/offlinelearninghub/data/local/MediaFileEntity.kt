package com.offlinelearninghub.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "media_files",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("courseId")]
)
data class MediaFileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val courseId: Long? = null,
    val title: String,
    val fileName: String,
    val filePath: String,
    val fileType: String,
    val fileSizeBytes: Long = 0,
    val durationMs: Long = 0,
    val moduleName: String = "",
    val lastPlayedPositionMs: Long = 0,
    val lastPageIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
