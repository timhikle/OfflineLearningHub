package com.offlinelearninghub.data.repository

import com.offlinelearninghub.data.local.CourseDao
import com.offlinelearninghub.data.local.CourseEntity
import kotlinx.coroutines.flow.Flow

class CourseRepository(private val courseDao: CourseDao) {

    fun getAllCourses(): Flow<List<CourseEntity>> = courseDao.getAllCourses()

    fun getLastAccessedCourse(): Flow<CourseEntity?> = courseDao.getLastAccessedCourse()

    suspend fun getLastAccessedCourseSync(): CourseEntity? = courseDao.getLastAccessedCourseSync()

    suspend fun getCourseById(id: Long): CourseEntity? = courseDao.getCourseById(id)

    suspend fun insertCourse(course: CourseEntity): Long = courseDao.insertCourse(course)

    suspend fun updateCourse(course: CourseEntity) = courseDao.updateCourse(course)

    suspend fun deleteCourse(course: CourseEntity) = courseDao.deleteCourse(course)

    suspend fun deleteAll() = courseDao.deleteAll()

    fun getCourseCount(): Flow<Int> = courseDao.getCourseCount()
}
