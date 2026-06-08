package com.offlinelearninghub.data.repository

import com.offlinelearninghub.data.local.StudySessionDao
import com.offlinelearninghub.data.local.StudySessionEntity
import kotlinx.coroutines.flow.Flow

class StudySessionRepository(private val studySessionDao: StudySessionDao) {

    fun getAllSessions(): Flow<List<StudySessionEntity>> = studySessionDao.getAllSessions()

    fun getTodaySessions(startOfDay: Long, endOfDay: Long): Flow<List<StudySessionEntity>> =
        studySessionDao.getTodaySessions(startOfDay, endOfDay)

    fun getTodayStudyMinutes(startOfDay: Long, endOfDay: Long): Flow<Int> =
        studySessionDao.getTodayStudyMinutes(startOfDay, endOfDay)

    fun getCompletedModulesCount(): Flow<Int> = studySessionDao.getCompletedModulesCount()

    fun getActiveDaysCount(sinceTimestamp: Long): Flow<Int> =
        studySessionDao.getActiveDaysCount(sinceTimestamp)

    suspend fun insertSession(session: StudySessionEntity): Long =
        studySessionDao.insertSession(session)

    suspend fun deleteAll() = studySessionDao.deleteAll()
}
