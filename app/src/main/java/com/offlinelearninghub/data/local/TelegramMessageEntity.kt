package com.offlinelearninghub.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "telegram_messages")
data class TelegramMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val messageId: Long,
    val chatId: Long,
    val title: String,
    val summary: String,
    val category: String,
    val originalText: String,
    val receivedAt: Long = System.currentTimeMillis()
)
