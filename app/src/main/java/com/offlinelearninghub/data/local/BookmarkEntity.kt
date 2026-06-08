package com.offlinelearninghub.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = MediaFileEntity::class,
            parentColumns = ["id"],
            childColumns = ["mediaFileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("mediaFileId")]
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mediaFileId: Long,
    val title: String = "",
    val timestampMs: Long = 0,
    val pageIndex: Int = -1,
    val createdAt: Long = System.currentTimeMillis()
)
