package com.curio.app.features.capture.formats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.curio.app.data.CaptureData
import com.curio.app.ui.components.LiveWaveform
import com.curio.app.ui.components.formatRecordingTime
import com.curio.app.ui.components.rememberPulseScale
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import kotlinx.coroutines.delay

/**
 * Sound Bite format body — CURIO_SPEC §8.1 (Music / Artists).
 *
 * 4-state machine: IDLE / RECORDING / PAUSED / STOPPED.
 * - IDLE: big mic button to start, "Tap to record your take" helper
 * - RECORDING / PAUSED: pulsing ring + live waveform + mm:ss timer +
 *   Pause/Stop/Discard controls
 * - STOPPED: shows "Recording saved (m:ss)" + Record over reset
 *
 * Owns its own recording state internally. Notifies parent via
 * [onCanSaveChange] when state is STOPPED AND seconds > 0.
 *
 * Also owns the optional one-line title field per spec §8.1.
 */
@Composable
fun SoundBiteFormat(
    accent: Color,
    tint: Color,
    onCanSaveChange: (Boolean) -> Unit,
    onDataChanged: (CaptureData?) -> Unit = {}
) {
    var recordingState by remember { mutableStateOf(RecordingState.IDLE) }
    var recordingSeconds by remember { mutableIntStateOf(0) }
    var title by remember { mutableStateOf("") }

    // Tick the recording timer every second while RECORDING.
    LaunchedEffect(recordingState) {
        if (recordingState == RecordingState.RECORDING) {
            while (recordingState == RecordingState.RECORDING) {
                delay(1000)
                recordingSeconds++
            }
        }
    }

    val canSave = recordingSeconds > 0 &&
                  recordingState == RecordingState.STOPPED
    LaunchedEffect(canSave) {
        onCanSaveChange(canSave)
        onDataChanged(
            if (canSave) CaptureData.SoundBite(recordingSeconds, title)
            else null
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Format body (state-dependent) ──────────────────────────────────
        when (recordingState) {
            RecordingState.IDLE -> IdleControls(accent = accent, onRecord = {
                recordingState = RecordingState.RECORDING
                recordingSeconds = 0
            })
            RecordingState.RECORDING,
            RecordingState.PAUSED -> LiveControls(
                accent = accent,
                tint = tint,
                state = recordingState,
                seconds = recordingSeconds,
                onPauseResume = {
                    recordingState =
                        if (recordingState == RecordingState.RECORDING)
                            RecordingState.PAUSED
                        else RecordingState.RECORDING
                },
                onStop = { recordingState = RecordingState.STOPPED },
                onDiscard = {
                    recordingState = RecordingState.IDLE
                    recordingSeconds = 0
                }
            )
            RecordingState.STOPPED -> StoppedControls(
                accent = accent,
                tint = tint,
                seconds = recordingSeconds,
                onReRecord = {
                    recordingState = RecordingState.IDLE
                    recordingSeconds = 0
                }
            )
        }

        // ── Optional title field (CURIO_SPEC §8.1) ─────────────────────────
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Add a quick title (optional)") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** Internal state machine — package-private since this file is the only consumer. */
internal enum class RecordingState { IDLE, RECORDING, PAUSED, STOPPED }

@Composable
private fun IdleControls(accent: Color, onRecord: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Surface(
            onClick = onRecord,
            shape = CircleShape,
            color = accent,
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                CurioIcon(
                    name = CurioIcons.Mic,
                    contentDescription = "Start recording",
                    tint = CurioColors.DeepPlum,
                    size = 48.dp
                )
            }
        }
        Text(
            text = "Tap to record your take",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LiveControls(
    accent: Color,
    tint: Color,
    state: RecordingState,
    seconds: Int,
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
    onDiscard: () -> Unit
) {
    val pulseScale = rememberPulseScale(active = state == RecordingState.RECORDING)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Pulsing outer ring + solid mic center
        Box(
            modifier = Modifier.size(140.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = tint,
                modifier = Modifier
                    .size(120.dp)
                    .scale(pulseScale)
            ) {}
            Surface(
                shape = CircleShape,
                color = accent,
                modifier = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CurioIcon(
                        name = if (state == RecordingState.PAUSED)
                            CurioIcons.MicNone
                        else CurioIcons.Mic,
                        contentDescription = null,
                        tint = CurioColors.DeepPlum,
                        size = 48.dp
                    )
                }
            }
        }

        // Live waveform
        LiveWaveform(
            color = accent,
            active = state == RecordingState.RECORDING,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        )

        // Running mm:ss timer
        Text(
            text = formatRecordingTime(seconds),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Pause / Stop / Discard controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ControlButton(
                icon = if (state == RecordingState.PAUSED)
                    CurioIcons.PlayArrow
                else CurioIcons.Pause,
                label = if (state == RecordingState.PAUSED) "Resume" else "Pause",
                tint = accent,
                onClick = onPauseResume
            )
            ControlButton(
                icon = CurioIcons.Stop,
                label = "Stop",
                tint = accent,
                onClick = onStop
            )
            ControlButton(
                icon = CurioIcons.Replay,
                label = "Discard",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = onDiscard
            )
        }
    }
}

@Composable
private fun StoppedControls(
    accent: Color,
    tint: Color,
    seconds: Int,
    onReRecord: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = tint,
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                CurioIcon(
                    name = CurioIcons.Mic,
                    contentDescription = null,
                    tint = accent,
                    size = 48.dp
                )
            }
        }
        Text(
            text = "Recording saved (${formatRecordingTime(seconds)})",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        TextButton(
            onClick = onReRecord,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Record over",
                color = accent
            )
        }
    }
}

@Composable
private fun ControlButton(
    icon: String,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = tint.copy(alpha = 0.15f)
        ) {
            Box(
                modifier = Modifier.padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                CurioIcon(
                    name = icon,
                    contentDescription = label,
                    tint = tint,
                    size = 24.dp
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}