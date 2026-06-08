package com.offlinelearninghub.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY lastAccessedAt DESC")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getCourseById(id: Long): CourseEntity?

    @Query("SELECT * FROM courses ORDER BY lastAccessedAt DESC LIMIT 1")
    fun getLastAccessedCourse(): Flow<CourseEntity?>

    @Query("SELECT * FROM courses ORDER BY lastAccessedAt DESC LIMIT 1")
    suspend fun getLastAccessedCourseSync(): CourseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity): Long

    @Update
    suspend fun updateCourse(course: CourseEntity)

    @Delete
    suspend fun deleteCourse(course: CourseEntity)

    @Query("DELETE FROM courses")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM courses")
    fun getCourseCount(): Flow<Int>
}
