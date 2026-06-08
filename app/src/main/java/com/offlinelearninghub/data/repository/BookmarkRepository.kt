package com.offlinelearninghub.data.repository

import com.offlinelearninghub.data.local.BookmarkDao
import com.offlinelearninghub.data.local.BookmarkEntity
import kotlinx.coroutines.flow.Flow

class BookmarkRepository(private val bookmarkDao: BookmarkDao) {

    fun getBookmarksByMediaFile(mediaFileId: Long): Flow<List<BookmarkEntity>> =
        bookmarkDao.getBookmarksByMediaFile(mediaFileId)

    suspend fun getBookmarkById(id: Long): BookmarkEntity? = bookmarkDao.getBookmarkById(id)

    suspend fun insertBookmark(bookmark: BookmarkEntity): Long =
        bookmarkDao.insertBookmark(bookmark)

    suspend fun updateBookmark(bookmark: BookmarkEntity) = bookmarkDao.updateBookmark(bookmark)

    suspend fun deleteBookmark(bookmark: BookmarkEntity) = bookmarkDao.deleteBookmark(bookmark)

    suspend fun deleteBookmarksByMediaFile(mediaFileId: Long) =
        bookmarkDao.deleteBookmarksByMediaFile(mediaFileId)

    suspend fun deleteAll() = bookmarkDao.deleteAll()
}
