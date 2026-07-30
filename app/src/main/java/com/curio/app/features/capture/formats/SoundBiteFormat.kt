package com.curio.app.features.capture.formats

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.curio.app.data.CaptureData
import com.curio.app.features.capture.AudioRecorder
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
 * - IDLE: big mic button to start, requests RECORD_AUDIO permission if needed
 * - RECORDING / PAUSED: pulsing ring + live waveform + mm:ss timer +
 *   Pause/Stop/Discard controls — driven by real [AudioRecorder] (MediaRecorder)
 * - STOPPED: shows "Recording saved (m:ss)" + Record over reset
 *
 * Uses real [AudioRecorder] (MediaRecorder) for actual voice capture.
 * The audio file path is stored in [CaptureData.SoundBite.audioFilePath].
 *
 * Runtime permission (RECORD_AUDIO) is requested on first tap of the mic button.
 */
@Composable
fun SoundBiteFormat(
    accent: Color,
    tint: Color,
    onCanSaveChange: (Boolean) -> Unit,
    onDataChanged: (CaptureData?) -> Unit = {}
) {
    val context = LocalContext.current
    val recorder = remember(context) { AudioRecorder(context) }
    var recordingState by remember { mutableStateOf(AudioRecorder.State.IDLE) }
    var recordingSeconds by remember { mutableIntStateOf(0) }
    var title by remember { mutableStateOf("") }
    var savedFilePath by remember { mutableStateOf<String?>(null) }
    var permissionDenied by remember { mutableStateOf(false) }

    // ── Runtime permission launcher ──────────────────────────────────────
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            try {
                recorder.start()
                recordingState = recorder.state
                recordingSeconds = 0
                permissionDenied = false
            } catch (e: Exception) {
                // Start failed — silently return to IDLE
                recordingState = AudioRecorder.State.IDLE
            }
        } else {
            permissionDenied = true
        }
    }

    // ── Tick the recording timer every second while RECORDING ────────────
    LaunchedEffect(recordingState) {
        if (recordingState == AudioRecorder.State.RECORDING) {
            while (recordingState == AudioRecorder.State.RECORDING) {
                delay(1000)
                recordingSeconds = recorder.elapsedSeconds
            }
        }
    }

    // ── Clean up recorder on dispose ─────────────────────────────────────
    DisposableEffect(Unit) {
        onDispose { recorder.release() }
    }

    // ── Report can-save + capture data ───────────────────────────────────
    val canSave = recordingState == AudioRecorder.State.STOPPED &&
                  recordingSeconds > 0 &&
                  savedFilePath != null
    LaunchedEffect(canSave, savedFilePath, title) {
        onCanSaveChange(canSave)
        onDataChanged(
            if (canSave) CaptureData.SoundBite(
                durationSeconds = recordingSeconds,
                title = title,
                audioFilePath = savedFilePath
            )
            else null
        )
    }

    // ── Check initial permission state ───────────────────────────────────
    val hasPermission = remember {
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
        PackageManager.PERMISSION_GRANTED
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Format body (state-dependent) ────────────────────────────────
        when (recordingState) {
            AudioRecorder.State.IDLE -> IdleControls(
                accent = accent,
                hasPermission = hasPermission,
                permissionDenied = permissionDenied,
                onRecord = {
                    if (hasPermission) {
                        try {
                            recorder.start()
                            recordingState = recorder.state
                            recordingSeconds = 0
                        } catch (_: Exception) {
                            recordingState = AudioRecorder.State.IDLE
                        }
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            )
            AudioRecorder.State.RECORDING,
            AudioRecorder.State.PAUSED -> LiveControls(
                accent = accent,
                tint = tint,
                isPaused = recordingState == AudioRecorder.State.PAUSED,
                seconds = recordingSeconds,
                onPauseResume = {
                    if (recordingState == AudioRecorder.State.RECORDING) {
                        recorder.pause()
                    } else {
                        recorder.resume()
                    }
                    recordingState = recorder.state
                },
                onStop = {
                    try {
                        savedFilePath = recorder.stop()
                    } catch (_: Exception) {
                        savedFilePath = null
                    }
                    recordingState = recorder.state
                    recordingSeconds = recorder.elapsedSeconds
                },
                onDiscard = {
                    recorder.discard()
                    recordingState = recorder.state
                    recordingSeconds = 0
                    savedFilePath = null
                }
            )
            AudioRecorder.State.STOPPED -> StoppedControls(
                accent = accent,
                tint = tint,
                seconds = recordingSeconds,
                onReRecord = {
                    recorder.release()
                    recordingState = recorder.state
                    recordingSeconds = 0
                    savedFilePath = null
                }
            )
        }

        // ── Permission denied hint ───────────────────────────────────────
        if (permissionDenied) {
            Text(
                text = "Microphone access is needed to record. Grant permission in Settings.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // ── Optional title field (CURIO_SPEC §8.1) ───────────────────────
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

// ═══════════════════════════════════════════════════════════════════════════
// Private sub-composables
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun IdleControls(
    accent: Color,
    hasPermission: Boolean,
    permissionDenied: Boolean,
    onRecord: () -> Unit
) {
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
            text = if (!hasPermission && !permissionDenied) "Tap to grant mic access & record"
                   else "Tap to record your take",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LiveControls(
    accent: Color,
    tint: Color,
    isPaused: Boolean,
    seconds: Int,
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
    onDiscard: () -> Unit
) {
    val pulseScale = rememberPulseScale(active = !isPaused)

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
                        name = if (isPaused) CurioIcons.MicNone else CurioIcons.Mic,
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
            active = !isPaused,
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
                icon = if (isPaused) CurioIcons.PlayArrow else CurioIcons.Pause,
                label = if (isPaused) "Resume" else "Pause",
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
