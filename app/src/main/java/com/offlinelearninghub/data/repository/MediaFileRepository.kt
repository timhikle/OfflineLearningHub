package com.offlinelearninghub.data.repository

import com.offlinelearninghub.data.local.MediaFileDao
import com.offlinelearninghub.data.local.MediaFileEntity
import kotlinx.coroutines.flow.Flow

class MediaFileRepository(private val mediaFileDao: MediaFileDao) {

    fun getAllFiles(): Flow<List<MediaFileEntity>> = mediaFileDao.getAllFiles()

    fun getFilesByCourse(courseId: Long): Flow<List<MediaFileEntity>> =
        mediaFileDao.getFilesByCourse(courseId)

    fun getFilesByType(fileType: String): Flow<List<MediaFileEntity>> =
        mediaFileDao.getFilesByType(fileType)

    suspend fun getFileById(id: Long): MediaFileEntity? = mediaFileDao.getFileById(id)

    fun getFileByIdFlow(id: Long): Flow<MediaFileEntity?> = mediaFileDao.getFileByIdFlow(id)

    fun searchFiles(query: String): Flow<List<MediaFileEntity>> =
        mediaFileDao.searchFiles(query)

    suspend fun insertFile(file: MediaFileEntity): Long = mediaFileDao.insertFile(file)

    suspend fun updateFile(file: MediaFileEntity) = mediaFileDao.updateFile(file)

    suspend fun deleteFile(file: MediaFileEntity) = mediaFileDao.deleteFile(file)

    suspend fun deleteAll() = mediaFileDao.deleteAll()

    fun getFileCount(): Flow<Int> = mediaFileDao.getFileCount()

    fun getTotalStorageBytes(): Flow<Long?> = mediaFileDao.getTotalStorageBytes()
}
