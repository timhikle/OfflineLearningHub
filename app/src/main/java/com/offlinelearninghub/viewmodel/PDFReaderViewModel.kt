package com.offlinelearninghub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.offlinelearninghub.data.local.AppDatabase
import com.offlinelearninghub.data.local.BookmarkEntity
import com.offlinelearninghub.data.local.MediaFileEntity
import com.offlinelearninghub.data.local.NoteEntity
import com.offlinelearninghub.data.repository.BookmarkRepository
import com.offlinelearninghub.data.repository.MediaFileRepository
import com.offlinelearninghub.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PDFReaderUiState(
    val mediaFile: MediaFileEntity? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val notes: List<NoteEntity> = emptyList(),
    val bookmarks: List<BookmarkEntity> = emptyList(),
    val showNoteDialog: Boolean = false,
    val notePage: Int = 0
)

class PDFReaderViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val mediaRepo = MediaFileRepository(db.mediaFileDao())
    private val noteRepo = NoteRepository(db.noteDao())
    private val bookmarkRepo = BookmarkRepository(db.bookmarkDao())

    private val _uiState = MutableStateFlow(PDFReaderUiState())
    val uiState: StateFlow<PDFReaderUiState> = _uiState.asStateFlow()

    private var mediaFileId: Long = -1

    fun loadPdf(fileId: Long) {
        mediaFileId = fileId
        viewModelScope.launch {
            val file = mediaRepo.getFileById(fileId)
            _uiState.value = _uiState.value.copy(
                mediaFile = file,
                currentPage = file?.lastPageIndex ?: 0
            )
        }
        viewModelScope.launch {
            noteRepo.getNotesByMediaFile(fileId).collect { notes ->
                _uiState.value = _uiState.value.copy(notes = notes)
            }
        }
        viewModelScope.launch {
            bookmarkRepo.getBookmarksByMediaFile(fileId).collect { bookmarks ->
                _uiState.value = _uiState.value.copy(bookmarks = bookmarks)
            }
        }
    }

    fun setCurrentPage(page: Int) {
        _uiState.value = _uiState.value.copy(currentPage = page)
    }

    fun setTotalPages(total: Int) {
        _uiState.value = _uiState.value.copy(totalPages = total)
    }

    fun saveLastPage(page: Int) {
        viewModelScope.launch {
            _uiState.value.mediaFile?.let { file ->
                mediaRepo.updateFile(file.copy(lastPageIndex = page))
            }
        }
    }

    fun showNoteDialog(page: Int) {
        _uiState.value = _uiState.value.copy(
            showNoteDialog = true,
            notePage = page
        )
    }

    fun hideNoteDialog() {
        _uiState.value = _uiState.value.copy(showNoteDialog = false)
    }

    fun saveNote(content: String, page: Int) {
        viewModelScope.launch {
            noteRepo.insertNote(
                NoteEntity(
                    mediaFileId = mediaFileId,
                    content = content,
                    pageIndex = page
                )
            )
            hideNoteDialog()
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            noteRepo.deleteNote(note)
        }
    }

    fun addBookmark(title: String, page: Int) {
        viewModelScope.launch {
            bookmarkRepo.insertBookmark(
                BookmarkEntity(
                    mediaFileId = mediaFileId,
                    title = title,
                    pageIndex = page
                )
            )
        }
    }

    fun deleteBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            bookmarkRepo.deleteBookmark(bookmark)
        }
    }
}
