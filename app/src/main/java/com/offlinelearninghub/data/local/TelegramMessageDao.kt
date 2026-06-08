package com.offlinelearninghub.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TelegramMessageDao {
    @Query("SELECT * FROM telegram_messages ORDER BY receivedAt DESC")
    fun getAllMessages(): Flow<List<TelegramMessageEntity>>

    @Insert
    suspend fun insert(message: TelegramMessageEntity)

    @Query("DELETE FROM telegram_messages")
    suspend fun deleteAll()
}
