package com.offlinelearninghub.ui.screens

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.offlinelearninghub.R
import com.offlinelearninghub.viewmodel.TimerState
import com.offlinelearninghub.viewmodel.TimerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(viewModel: TimerViewModel) {
    val state by viewModel.uiState.collectAsState()
    val progress = if (state.totalSeconds > 0) {
        state.remainingSeconds.toFloat() / state.totalSeconds.toFloat()
    } else 1f

    if (state.sessionComplete) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSessionComplete() },
            title = { Text(stringResource(R.string.session_complete)) },
            text = { Text(stringResource(R.string.session_complete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissSessionComplete()
                    viewModel.startBreak()
                }) {
                    Text(stringResource(R.string.break_time))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissSessionComplete() }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }

    if (state.isBreakComplete) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSessionComplete() },
            title = { Text(stringResource(R.string.break_complete)) },
            text = { Text(stringResource(R.string.break_complete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissSessionComplete()
                    viewModel.startFocus()
                }) {
                    Text(stringResource(R.string.focus_time))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissSessionComplete() }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.pomodoro_timer),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(260.dp)
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(260.dp),
                strokeWidth = 12.dp,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                color = when (state.timerState) {
                    TimerState.BREAK -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                }
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = viewModel.getFormattedTime(),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 52.sp
                    ),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (state.timerState) {
                        TimerState.FOCUS -> stringResource(R.string.focus_session)
                        TimerState.BREAK -> stringResource(R.string.break_session)
                        TimerState.PAUSED -> stringResource(R.string.pause)
                        TimerState.IDLE -> stringResource(R.string.pomodoro_timer)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                state.timerState == TimerState.IDLE || state.timerState == TimerState.PAUSED -> {
                    Button(
                        onClick = { viewModel.startFocus() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.start))
                    }
                }
                else -> {
                    FilledTonalButton(
                        onClick = { viewModel.pause() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.pause))
                    }
                }
            }
            OutlinedButton(
                onClick = { viewModel.reset() },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.reset))
            }
        }

        if (state.timerState == TimerState.PAUSED) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.resume() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.resume))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${stringResource(R.string.focus_time)}: ${state.focusMinutes} ${stringResource(R.string.minutes)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = state.focusMinutes.toFloat(),
                    onValueChange = { viewModel.setFocusMinutes(it.toInt()) },
                    valueRange = 1f..120f,
                    steps = 118
                )

                Text(
                    text = "${stringResource(R.string.break_time)}: ${state.breakMinutes} ${stringResource(R.string.minutes)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = state.breakMinutes.toFloat(),
                    onValueChange = { viewModel.setBreakMinutes(it.toInt()) },
                    valueRange = 1f..30f,
                    steps = 28
                )
            }
        }
    }
}
