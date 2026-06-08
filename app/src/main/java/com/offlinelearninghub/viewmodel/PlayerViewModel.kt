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
import com.offlinelearninghub.util.FileUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlayerUiState(
    val mediaFile: MediaFileEntity? = null,
    val notes: List<NoteEntity> = emptyList(),
    val bookmarks: List<BookmarkEntity> = emptyList(),
    val playbackSpeed: Float = 1.0f,
    val currentPosition: Long = 0,
    val isPlaying: Boolean = false,
    val showNoteDialog: Boolean = false,
    val noteTimestamp: Long = 0
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val mediaRepo = MediaFileRepository(db.mediaFileDao())
    private val noteRepo = NoteRepository(db.noteDao())
    private val bookmarkRepo = BookmarkRepository(db.bookmarkDao())

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var mediaFileId: Long = -1

    fun loadMediaFile(fileId: Long) {
        mediaFileId = fileId
        viewModelScope.launch {
            val file = mediaRepo.getFileById(fileId)
            _uiState.value = _uiState.value.copy(
                mediaFile = file,
                currentPosition = file?.lastPlayedPositionMs ?: 0
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

    fun setPlaybackSpeed(speed: Float) {
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    }

    fun updatePosition(positionMs: Long) {
        _uiState.value = _uiState.value.copy(currentPosition = positionMs)
    }

    fun setPlaying(isPlaying: Boolean) {
        _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
    }

    fun savePosition(positionMs: Long) {
        viewModelScope.launch {
            _uiState.value.mediaFile?.let { file ->
                mediaRepo.updateFile(file.copy(lastPlayedPositionMs = positionMs))
            }
        }
    }

    fun showNoteDialog(timestampMs: Long) {
        _uiState.value = _uiState.value.copy(
            showNoteDialog = true,
            noteTimestamp = timestampMs
        )
    }

    fun hideNoteDialog() {
        _uiState.value = _uiState.value.copy(showNoteDialog = false)
    }

    fun saveNote(content: String, timestampMs: Long) {
        viewModelScope.launch {
            noteRepo.insertNote(
                NoteEntity(
                    mediaFileId = mediaFileId,
                    content = content,
                    timestampMs = timestampMs
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

    fun addBookmark(title: String, timestampMs: Long) {
        viewModelScope.launch {
            bookmarkRepo.insertBookmark(
                BookmarkEntity(
                    mediaFileId = mediaFileId,
                    title = title,
                    timestampMs = timestampMs
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
