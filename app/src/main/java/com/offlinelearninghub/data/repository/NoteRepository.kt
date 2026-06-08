package com.offlinelearninghub.data.repository

import com.offlinelearninghub.data.local.NoteDao
import com.offlinelearninghub.data.local.NoteEntity
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {

    fun getNotesByMediaFile(mediaFileId: Long): Flow<List<NoteEntity>> =
        noteDao.getNotesByMediaFile(mediaFileId)

    suspend fun getNoteById(id: Long): NoteEntity? = noteDao.getNoteById(id)

    suspend fun insertNote(note: NoteEntity): Long = noteDao.insertNote(note)

    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)

    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)

    suspend fun deleteNotesByMediaFile(mediaFileId: Long) =
        noteDao.deleteNotesByMediaFile(mediaFileId)

    suspend fun deleteAll() = noteDao.deleteAll()
}
