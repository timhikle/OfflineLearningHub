package com.offlinelearninghub.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.offlinelearninghub.data.local.AppDatabase
import com.offlinelearninghub.data.local.CourseEntity
import com.offlinelearninghub.data.local.MediaFileEntity
import com.offlinelearninghub.data.repository.CourseRepository
import com.offlinelearninghub.data.repository.MediaFileRepository
import com.offlinelearninghub.util.Constants
import com.offlinelearninghub.util.FileUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

enum class LibraryFilter {
    ALL, VIDEOS, AUDIOS, PDFS
}

data class LibraryUiState(
    val files: List<MediaFileEntity> = emptyList(),
    val courses: List<CourseEntity> = emptyList(),
    val filter: LibraryFilter = LibraryFilter.ALL,
    val searchQuery: String = "",
    val isImporting: Boolean = false,
    val importError: String? = null,
    val importSuccess: String? = null
)

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val mediaRepo = MediaFileRepository(db.mediaFileDao())
    private val courseRepo = CourseRepository(db.courseDao())

    private val _filter = MutableStateFlow(LibraryFilter.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadLibrary()
    }

    private fun loadLibrary() {
        combine(
            _searchQuery.flatMapLatest { query ->
                if (query.isBlank()) {
                    _filter.flatMapLatest { filter ->
                        when (filter) {
                            LibraryFilter.ALL -> mediaRepo.getAllFiles()
                            LibraryFilter.VIDEOS -> mediaRepo.getFilesByType(Constants.FILE_TYPE_VIDEO)
                            LibraryFilter.AUDIOS -> mediaRepo.getFilesByType(Constants.FILE_TYPE_AUDIO)
                            LibraryFilter.PDFS -> mediaRepo.getFilesByType(Constants.FILE_TYPE_PDF)
                        }
                    }
                } else {
                    mediaRepo.searchFiles(query)
                }
            },
            courseRepo.getAllCourses()
        ) { files, courses ->
            _uiState.value = _uiState.value.copy(
                files = files,
                courses = courses
            )
        }.launchIn(viewModelScope)
    }

    fun setFilter(filter: LibraryFilter) {
        _filter.value = filter
        _uiState.value = _uiState.value.copy(filter = filter)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun importFile(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true, importError = null, importSuccess = null)
            try {
                val context = getApplication<Application>()
                val fileName = FileUtils.getFileName(context, uri)
                val fileSize = FileUtils.getFileSize(context, uri)
                val fileType = Constants.getFileTypeFromExtension(fileName)

                val result = FileUtils.copyUriToInternalStorage(context, uri, fileName)
                result.fold(
                    onSuccess = { filePath ->
                        val now = System.currentTimeMillis()
                        val courseName = fileName.substringBeforeLast(".")
                        val courseId = if (fileType == Constants.FILE_TYPE_PDF) {
                            null
                        } else {
                            val course = CourseEntity(
                                title = courseName,
                                totalModules = 1,
                                lastAccessedAt = now
                            )
                            courseRepo.insertCourse(course)
                        }

                        mediaRepo.insertFile(
                            MediaFileEntity(
                                courseId = courseId,
                                title = courseName,
                                fileName = fileName,
                                filePath = filePath,
                                fileType = fileType,
                                fileSizeBytes = fileSize
                            )
                        )

                        _uiState.value = _uiState.value.copy(
                            isImporting = false,
                            importSuccess = "File imported successfully"
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isImporting = false,
                            importError = error.message ?: "Import failed"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    importError = e.message ?: "Import failed"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(importError = null, importSuccess = null)
    }

    fun deleteFile(file: MediaFileEntity) {
        viewModelScope.launch {
            mediaRepo.deleteFile(file)
        }
    }
}
