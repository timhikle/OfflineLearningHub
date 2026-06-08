package com.offlinelearninghub.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions ORDER BY sessionDate DESC")
    fun getAllSessions(): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE sessionDate >= :startOfDay AND sessionDate < :endOfDay")
    fun getTodaySessions(startOfDay: Long, endOfDay: Long): Flow<List<StudySessionEntity>>

    @Query("SELECT COALESCE(SUM(durationMinutes), 0) FROM study_sessions WHERE sessionDate >= :startOfDay AND sessionDate < :endOfDay")
    fun getTodayStudyMinutes(startOfDay: Long, endOfDay: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM study_sessions WHERE isCompleted = 1")
    fun getCompletedModulesCount(): Flow<Int>

    @Query("""
        SELECT COUNT(DISTINCT CAST(sessionDate / 86400000 AS INTEGER)) 
        FROM study_sessions 
        WHERE sessionDate >= :sinceTimestamp
    """)
    fun getActiveDaysCount(sinceTimestamp: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySessionEntity): Long

    @Query("DELETE FROM study_sessions")
    suspend fun deleteAll()

    @Query("SELECT DISTINCT CAST(sessionDate / 86400000 AS INTEGER) AS day FROM study_sessions ORDER BY day DESC")
    fun getDistinctStudyDays(): Flow<List<Long>>
}
