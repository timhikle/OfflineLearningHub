package com.offlinelearninghub.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaFileDao {
    @Query("SELECT * FROM media_files ORDER BY createdAt DESC")
    fun getAllFiles(): Flow<List<MediaFileEntity>>

    @Query("SELECT * FROM media_files WHERE courseId = :courseId ORDER BY createdAt ASC")
    fun getFilesByCourse(courseId: Long): Flow<List<MediaFileEntity>>

    @Query("SELECT * FROM media_files WHERE fileType = :fileType ORDER BY createdAt DESC")
    fun getFilesByType(fileType: String): Flow<List<MediaFileEntity>>

    @Query("SELECT * FROM media_files WHERE id = :id")
    suspend fun getFileById(id: Long): MediaFileEntity?

    @Query("SELECT * FROM media_files WHERE id = :id")
    fun getFileByIdFlow(id: Long): Flow<MediaFileEntity?>

    @Query("SELECT * FROM media_files WHERE title LIKE '%' || :query || '%' OR fileName LIKE '%' || :query || '%'")
    fun searchFiles(query: String): Flow<List<MediaFileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: MediaFileEntity): Long

    @Update
    suspend fun updateFile(file: MediaFileEntity)

    @Delete
    suspend fun deleteFile(file: MediaFileEntity)

    @Query("DELETE FROM media_files")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM media_files")
    fun getFileCount(): Flow<Int>

    @Query("SELECT SUM(fileSizeBytes) FROM media_files")
    fun getTotalStorageBytes(): Flow<Long?>
}
