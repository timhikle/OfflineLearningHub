package com.offlinelearninghub.util

object Constants {
    const val FILE_TYPE_VIDEO = "video"
    const val FILE_TYPE_AUDIO = "audio"
    const val FILE_TYPE_PDF = "pdf"

    const val POMODORO_DEFAULT_FOCUS = 25
    const val POMODORO_DEFAULT_BREAK = 5

    const val STREAK_CALCULATION_DAYS = 30

    fun getFileTypeFromExtension(fileName: String): String {
        return when {
            fileName.endsWith(".mp4", true) || fileName.endsWith(".mkv", true) ||
                fileName.endsWith(".webm", true) || fileName.endsWith(".avi", true) ||
                fileName.endsWith(".mov", true) -> FILE_TYPE_VIDEO

            fileName.endsWith(".mp3", true) || fileName.endsWith(".wav", true) ||
                fileName.endsWith(".aac", true) || fileName.endsWith(".ogg", true) ||
                fileName.endsWith(".m4a", true) || fileName.endsWith(".flac", true) -> FILE_TYPE_AUDIO

            fileName.endsWith(".pdf", true) -> FILE_TYPE_PDF
            else -> FILE_TYPE_PDF
        }
    }

    fun getMimeTypeFromFileType(fileType: String): String {
        return when (fileType) {
            FILE_TYPE_VIDEO -> "video/*"
            FILE_TYPE_AUDIO -> "audio/*"
            FILE_TYPE_PDF -> "application/pdf"
            else -> "*/*"
        }
    }
}
