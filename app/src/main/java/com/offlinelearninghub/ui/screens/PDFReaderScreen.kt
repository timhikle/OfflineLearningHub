package com.offlinelearninghub.ui.screens

import android.widget.RelativeLayout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.offlinelearninghub.R
import com.offlinelearninghub.data.local.BookmarkEntity
import com.offlinelearninghub.data.local.NoteEntity
import com.offlinelearninghub.viewmodel.PDFReaderViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PDFReaderScreen(
    viewModel: PDFReaderViewModel,
    fileId: Long,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showBookmarkDialog by remember { mutableStateOf(false) }
    var bookmarkTitle by remember { mutableStateOf("") }
    var showNotesSection by remember { mutableStateOf(false) }
    var currentPage by remember { mutableIntStateOf(0) }
    var totalPages by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        viewModel.loadPdf(fileId)
        onDispose {
            viewModel.saveLastPage(currentPage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.mediaFile?.title ?: "",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (totalPages > 0) {
                            Text(
                                text = "${stringResource(R.string.page)} $currentPage ${stringResource(R.string.of)} $totalPages",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.saveLastPage(currentPage)
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                factory = { ctx ->
                    PDFView(ctx, null).apply {
                        val filePath = state.mediaFile?.filePath
                        if (filePath != null) {
                            fromFile(File(filePath))
                                .defaultPage(state.mediaFile?.lastPageIndex ?: 0)
                                .enableSwipe(true)
                                .swipeHorizontal(false)
                                .enableDoubletap(true)
                                .scrollHandle(DefaultScrollHandle(ctx))
                                .onPageChange { page, pageCount ->
                                    currentPage = page + 1
                                    totalPages = pageCount
                                    viewModel.setCurrentPage(page)
                                    viewModel.setTotalPages(pageCount)
                                }
                                .load()
                        }
                    }
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !showNotesSection,
                    onClick = { showNotesSection = false },
                    label = { Text(stringResource(R.string.bookmarked_pages)) }
                )
                FilterChip(
                    selected = showNotesSection,
                    onClick = { showNotesSection = true },
                    label = { Text(stringResource(R.string.notes)) }
                )
                Spacer(modifier = Modifier.weight(1f))
                FilterChip(
                    selected = false,
                    onClick = {
                        viewModel.showNoteDialog(currentPage)
                    },
                    label = { Text(stringResource(R.string.add_note)) },
                    leadingIcon = {
                        Icon(Icons.Default.NoteAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )
                FilterChip(
                    selected = false,
                    onClick = {
                        bookmarkTitle = "Page $currentPage"
                        showBookmarkDialog = true
                    },
                    label = { Text(stringResource(R.string.bookmark_page)) },
                    leadingIcon = {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )
            }

            if (showNotesSection) {
                PdfNotesSection(
                    notes = state.notes,
                    onDelete = { viewModel.deleteNote(it) }
                )
            } else {
                PdfBookmarksSection(
                    bookmarks = state.bookmarks,
                    onDelete = { viewModel.deleteBookmark(it) }
                )
            }
        }
    }

    if (showBookmarkDialog) {
        AlertDialog(
            onDismissRequest = { showBookmarkDialog = false },
            title = { Text(stringResource(R.string.bookmark_page)) },
            text = {
                OutlinedTextField(
                    value = bookmarkTitle,
                    onValueChange = { bookmarkTitle = it }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addBookmark(
                        bookmarkTitle.ifBlank { "Page $currentPage" },
                        currentPage - 1
                    )
                    showBookmarkDialog = false
                }) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showBookmarkDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (state.showNoteDialog) {
        var noteContent by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { viewModel.hideNoteDialog() },
            title = { Text("${stringResource(R.string.add_note)} - ${stringResource(R.string.page)} ${state.notePage}") },
            text = {
                OutlinedTextField(
                    value = noteContent,
                    onValueChange = { noteContent = it },
                    placeholder = { Text(stringResource(R.string.note_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.saveNote(noteContent, state.notePage - 1)
                    noteContent = ""
                }) {
                    Text(stringResource(R.string.save_note))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideNoteDialog() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun PdfNotesSection(
    notes: List<NoteEntity>,
    onDelete: (NoteEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (notes.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.no_notes_yet),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(notes, key = { it.id }) { note ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.NoteAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            if (note.pageIndex >= 0) {
                                Text(
                                    text = "${stringResource(R.string.page)} ${note.pageIndex + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = note.content,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        IconButton(onClick = { onDelete(note) }, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun PdfBookmarksSection(
    bookmarks: List<BookmarkEntity>,
    onDelete: (BookmarkEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (bookmarks.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.no_bookmarks),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(bookmarks, key = { it.id }) { bookmark ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = bookmark.title.ifBlank { "${stringResource(R.string.page)} ${bookmark.pageIndex + 1}" },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${stringResource(R.string.page)} ${bookmark.pageIndex + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { onDelete(bookmark) }, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
