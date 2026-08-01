package com.curio.app.features.capture.formats

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.curio.app.ui.components.AudioTrimmer
import com.curio.app.ui.components.LiveWaveform
import com.curio.app.ui.components.TrimWaveform
import com.curio.app.ui.components.WaveformExtractor
import com.curio.app.ui.components.formatRecordingTime
import com.curio.app.ui.components.rememberPulseScale
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Sound Bite format body — CURIO_SPEC §8.1 (Music / Artists).
 *
 * 4-state machine: IDLE / RECORDING / PAUSED / STOPPED.
 * - IDLE: big mic button to start, requests RECORD_AUDIO permission if needed
 * - RECORDING / PAUSED: pulsing ring + live waveform + mm:ss timer +
 *   Pause/Stop/Discard controls — driven by real [AudioRecorder] (MediaRecorder)
 * - STOPPED: shows trim waveform with draggable handles, plus title field
 *   and Record-over button. User can trim the audio before saving.
 *
 * Uses real [AudioRecorder] (MediaRecorder) for actual voice capture,
 * and [AudioTrimmer] to trim the recorded file after recording stops.
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
    val scope = rememberCoroutineScope()
    val recorder = remember(context) { AudioRecorder(context) }
    var recordingState by remember { mutableStateOf(AudioRecorder.State.IDLE) }
    var recordingSeconds by remember { mutableIntStateOf(0) }
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var savedFilePath by remember { mutableStateOf<String?>(null) }
    var permissionDenied by remember { mutableStateOf(false) }

    // ── Trim state ───────────────────────────────────────────────────────
    var showTrimmer by remember { mutableStateOf(false) }
    var startTrim by remember { mutableFloatStateOf(0f) }
    var endTrim by remember { mutableFloatStateOf(1f) }
    var trimInProgress by remember { mutableStateOf(false) }

    // Runtime permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            try {
                recorder.start()
                recordingState = recorder.state
                recordingSeconds = 0
                permissionDenied = false
            } catch (_: Exception) {
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

    // ── Extract waveform when entering STOPPED with a valid file ─────────
    val waveformSamples by produceState<FloatArray>(
        initialValue = FloatArray(120),
        key1 = savedFilePath
    ) {
        if (savedFilePath != null) {
            value = withContext(Dispatchers.Default) {
                WaveformExtractor.extract(savedFilePath!!, barCount = 120)
            } ?: FloatArray(120) { kotlin.random.Random.nextFloat() * 0.6f + 0.2f }
        }
    }

    // ── Show trimmer when first entering STOPPED ─────────────────────────
    LaunchedEffect(recordingState, savedFilePath) {
        if (recordingState == AudioRecorder.State.STOPPED && savedFilePath != null) {
            showTrimmer = true
            startTrim = 0f
            endTrim = 1f
        }
    }

    // ── Clean up recorder on dispose ─────────────────────────────────────
    DisposableEffect(Unit) {
        onDispose { recorder.release() }
    }

    // ── Report can-save + capture data ───────────────────────────────────
    val canSave = recordingState == AudioRecorder.State.STOPPED &&
                  recordingSeconds > 0 &&
                  savedFilePath != null &&
                  !trimInProgress
    LaunchedEffect(canSave, savedFilePath, title, note) {
        onCanSaveChange(canSave)
        onDataChanged(
            if (canSave) CaptureData.SoundBite(
                durationSeconds = recordingSeconds,
                title = title,
                note = note,
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
            AudioRecorder.State.STOPPED -> {
                // ── Trim mode (before trimming or after) ─────────────────
                if (showTrimmer && savedFilePath != null) {
                    TrimSection(
                        accent = accent,
                        tint = tint,
                        seconds = recordingSeconds,
                        waveformSamples = waveformSamples,
                        startTrim = startTrim,
                        endTrim = endTrim,
                        trimInProgress = trimInProgress,
                        onStartTrimChange = { startTrim = it },
                        onEndTrimChange = { endTrim = it },
                        onApplyTrim = {
                            trimInProgress = true
                            scope.launch {
                                try {
                                    val trimmedPath = withContext(Dispatchers.Default) {
                                        AudioTrimmer.trim(
                                            outputDir = context.cacheDir,
                                            inputPath = savedFilePath!!,
                                            startMs = (startTrim * recordingSeconds * 1000).toLong(),
                                            endMs = (endTrim * recordingSeconds * 1000).toLong()
                                        )
                                    }
                                    if (trimmedPath != null) {
                                        // Update the file path and duration
                                        savedFilePath = trimmedPath
                                        recordingSeconds = ((endTrim - startTrim) * recordingSeconds).toInt()
                                    }
                                } catch (_: Exception) { /* keep original file */ }
                                trimInProgress = false
                                showTrimmer = false
                            }
                        },
                        onKeepFull = {
                            showTrimmer = false
                        }
                    )
                } else {
                    StoppedControls(
                        accent = accent,
                        tint = tint,
                        seconds = recordingSeconds,
                        onReRecord = {
                            recorder.release()
                            recordingState = recorder.state
                            recordingSeconds = 0
                            savedFilePath = null
                            showTrimmer = false
                            startTrim = 0f
                            endTrim = 1f
                        }
                    )
                }
            }
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

        // ── Optional title field — show always, disabled during trim ─────
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Add a quick title (optional)") },
            singleLine = true,
            enabled = recordingState != AudioRecorder.State.RECORDING,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Add notes or context (optional)") },
            placeholder = { Text("What did this recording capture?") },
            enabled = recordingState != AudioRecorder.State.RECORDING,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Trim section — shown when recording is stopped and trim UI is active
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun TrimSection(
    accent: Color,
    tint: Color,
    seconds: Int,
    waveformSamples: FloatArray,
    startTrim: Float,
    endTrim: Float,
    trimInProgress: Boolean,
    onStartTrimChange: (Float) -> Unit,
    onEndTrimChange: (Float) -> Unit,
    onApplyTrim: () -> Unit,
    onKeepFull: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Saved indicator
        Text(
            text = "✓ Recording saved (${formatRecordingTime(seconds)})",
            style = MaterialTheme.typography.titleSmall,
            color = accent
        )

        // Waveform with trim handles
        TrimWaveform(
            samples = waveformSamples,
            startTrim = startTrim,
            endTrim = endTrim,
            accent = accent,
            tint = tint,
            totalSeconds = seconds,
            onTrimChange = { start, end ->
                onStartTrimChange(start)
                onEndTrimChange(end)
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Keep Full — dismiss trim UI
            Surface(
                onClick = onKeepFull,
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Keep full",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 12.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            // Apply Trim — enabled when range is narrower than full
            val hasTrim = startTrim > 0.01f || endTrim < 0.99f
            Button(
                onClick = onApplyTrim,
                enabled = hasTrim && !trimInProgress,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = Color.White,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                if (trimInProgress) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = if (trimInProgress) "Trimming…" else "Apply trim",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
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
                    tint = Color.White,
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
                        tint = Color.White,
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
            Text(text = "Record over", color = accent)
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
