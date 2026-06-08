package com.offlinelearninghub.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.offlinelearninghub.R
import com.offlinelearninghub.data.local.BookmarkEntity
import com.offlinelearninghub.data.local.MediaFileEntity
import com.offlinelearninghub.data.local.NoteEntity
import com.offlinelearninghub.util.FileUtils
import com.offlinelearninghub.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    fileId: Long,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showBookmarkDialog by remember { mutableStateOf(false) }
    var bookmarkTitle by remember { mutableStateOf("") }
    var showNotes by remember { mutableStateOf(false) }
    var isPlayerReady by remember { mutableStateOf(false) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    viewModel.setPlaying(isPlaying)
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        isPlayerReady = true
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                }
            })
        }
    }

    LaunchedEffect(fileId) {
        viewModel.loadMediaFile(fileId)
    }

    LaunchedEffect(state.mediaFile) {
        state.mediaFile?.let { file ->
            val mediaItem = MediaItem.fromUri(Uri.parse(file.filePath))
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.seekTo(file.lastPlayedPositionMs)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.run {
                viewModel.savePosition(currentPosition)
                release()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.mediaFile?.title ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.savePosition(exoPlayer.currentPosition)
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (state.mediaFile != null) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = {
                            PlayerView(context).apply {
                                player = exoPlayer
                                useController = true
                                setShowNextButton(false)
                                setShowPreviousButton(false)
                                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                                setKeepContentOnPlayerReset(true)
                            }
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.loading),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            PlaybackControls(
                playbackSpeed = state.playbackSpeed,
                isPlaying = state.isPlaying,
                onSpeedChange = { speed ->
                    exoPlayer.setPlaybackSpeed(speed)
                    viewModel.setPlaybackSpeed(speed)
                },
                onSkipBackward = {
                    exoPlayer.seekTo((exoPlayer.currentPosition - 10000).coerceAtLeast(0))
                },
                onSkipForward = {
                    exoPlayer.seekTo(
                        (exoPlayer.currentPosition + 10000).coerceAtMost(exoPlayer.duration.coerceAtLeast(0))
                    )
                },
                onBookmark = { showBookmarkDialog = true },
                onTogglePlay = {
                    if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !showNotes,
                    onClick = { showNotes = false },
                    label = { Text(stringResource(R.string.bookmark)) }
                )
                FilterChip(
                    selected = showNotes,
                    onClick = { showNotes = true },
                    label = { Text(stringResource(R.string.notes)) }
                )
            }

            if (showNotes) {
                NotesSection(
                    notes = state.notes,
                    onDeleteNote = { viewModel.deleteNote(it) },
                    onAddNote = {
                        viewModel.showNoteDialog(exoPlayer.currentPosition)
                    },
                    formatTimestamp = { FileUtils.formatDuration(it) }
                )
            } else {
                BookmarksSection(
                    bookmarks = state.bookmarks,
                    onDeleteBookmark = { viewModel.deleteBookmark(it) },
                    onBookmarkClick = {
                        exoPlayer.seekTo(it.timestampMs)
                    },
                    formatTimestamp = { FileUtils.formatDuration(it) }
                )
            }
        }
    }

    if (showBookmarkDialog) {
        AlertDialog(
            onDismissRequest = { showBookmarkDialog = false },
            title = { Text(stringResource(R.string.bookmark)) },
            text = {
                OutlinedTextField(
                    value = bookmarkTitle,
                    onValueChange = { bookmarkTitle = it },
                    placeholder = {
                        Text("${FileUtils.formatDuration(exoPlayer.currentPosition)}")
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addBookmark(
                        bookmarkTitle.ifBlank { "Bookmark" },
                        exoPlayer.currentPosition
                    )
                    bookmarkTitle = ""
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
            title = {
                Text(
                    "${stringResource(R.string.add_note)} @ ${
                        FileUtils.formatDuration(state.noteTimestamp)
                    }"
                )
            },
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
                    viewModel.saveNote(noteContent, state.noteTimestamp)
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
private fun PlaybackControls(
    playbackSpeed: Float,
    isPlaying: Boolean,
    onSpeedChange: (Float) -> Unit,
    onSkipBackward: () -> Unit,
    onSkipForward: () -> Unit,
    onBookmark: () -> Unit,
    onTogglePlay: () -> Unit
) {
    var showSpeedMenu by remember { mutableStateOf(false) }
    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onSkipBackward) {
                    Icon(
                        Icons.Default.FastRewind,
                        contentDescription = stringResource(R.string.skip_backward),
                        modifier = Modifier.size(32.dp)
                    )
                }
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                IconButton(onClick = onSkipForward) {
                    Icon(
                        Icons.Default.FastForward,
                        contentDescription = stringResource(R.string.skip_forward),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box {
                    FilterChip(
                        selected = false,
                        onClick = { showSpeedMenu = !showSpeedMenu },
                        label = { Text("${playbackSpeed}x") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Speed,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
                FilterChip(
                    selected = false,
                    onClick = onBookmark,
                    label = { Text(stringResource(R.string.bookmark)) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            if (showSpeedMenu) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    speeds.forEach { speed ->
                        FilterChip(
                            selected = playbackSpeed == speed,
                            onClick = {
                                onSpeedChange(speed)
                                showSpeedMenu = false
                            },
                            label = { Text("${speed}x") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotesSection(
    notes: List<NoteEntity>,
    onDeleteNote: (NoteEntity) -> Unit,
    onAddNote: () -> Unit,
    formatTimestamp: (Long) -> String
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.notes),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onAddNote) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_note)
                    )
                }
            }
        }
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
                NoteCard(note, onDeleteNote, formatTimestamp)
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun NoteCard(
    note: NoteEntity,
    onDelete: (NoteEntity) -> Unit,
    formatTimestamp: (Long) -> String
) {
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
                if (note.timestampMs > 0) {
                    Text(
                        text = formatTimestamp(note.timestampMs),
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

@Composable
private fun BookmarksSection(
    bookmarks: List<BookmarkEntity>,
    onDeleteBookmark: (BookmarkEntity) -> Unit,
    onBookmarkClick: (BookmarkEntity) -> Unit,
    formatTimestamp: (Long) -> String
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.bookmarked_pages),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
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
                    onClick = { onBookmarkClick(bookmark) },
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
                                text = bookmark.title.ifBlank { formatTimestamp(bookmark.timestampMs) },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = formatTimestamp(bookmark.timestampMs),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = { onDeleteBookmark(bookmark) },
                            modifier = Modifier.size(32.dp)
                        ) {
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
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
