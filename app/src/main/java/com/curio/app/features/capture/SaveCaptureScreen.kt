package com.curio.app.features.capture

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
import com.curio.app.data.MockTopics
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.ConfettiBurst
import com.curio.app.ui.components.LiveWaveform
import com.curio.app.ui.components.ScreenEntrance
import com.curio.app.ui.components.formatRecordingTime
import com.curio.app.ui.components.rememberPulseScale
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import kotlinx.coroutines.delay

/**
 * Save / Capture — see CURIO_SPEC.md §8.
 *
 * The shared outer shell is the same for all 6 formats; only the middle
 * format body changes. For the design phase we ship the Sound Bite
 * format (Music) — ReelNotes / Marginalia / GalleryWall / FieldNotes /
 * OpenNotebook bodies land in Phase 4 once the data layer is wired.
 *
 * Sound Bite flow (CURIO_SPEC.md §8.1):
 *   - IDLE: tap big mic → RECORDING
 *   - RECORDING: pulsing ring + live waveform + mm:ss timer +
 *                Pause / Stop / Discard controls
 *   - PAUSED: ring pauses pulse, controls swap Pause → Resume
 *   - STOPPED: shows "Recording saved (m:ss)" + "Record over" reset
 *   - Any state with seconds > 0 enables the bottom "Save entry" CTA
 *
 * Save sequence: 400ms simulated save → confetti in category accent →
 * ~700ms pause → navigate to EntryDetail.
 */
@Composable
fun SaveCaptureScreen(
    categorySlug: String,
    topicName: String,
    navController: NavController
) {
    val cat = remember(categorySlug) {
        CurioCategories.byRouteSlug(categorySlug)
            ?: CurioCategories.byId(CategoryId.WILDCARD)
    }

    val topic = remember(topicName) {
        MockTopics.samplePool.find { it.name == topicName }
            ?: MockTopics.samplePool.first()
    }

    // ── Recording state ────────────────────────────────────────────────────
    var recordingState by remember { mutableStateOf(RecordingState.IDLE) }
    var recordingSeconds by remember { mutableStateOf(0) }
    var title by remember { mutableStateOf("") }

    // ── Save-in-progress + confetti flow ───────────────────────────────────
    var saveInProgress by remember { mutableStateOf(false) }
    var confettiTrigger by remember { mutableStateOf(0) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Top bar: ← "Save your take" ─────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                onClick = {
                    if (recordingState != RecordingState.IDLE) {
                        // TODO Phase 4: confirm discard dialog per §8 spec
                        recordingState = RecordingState.IDLE
                        recordingSeconds = 0
                    } else {
                        navController.popBackStack()
                    }
                },
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                CurioIcon(
                    name = CurioIcons.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                    size = 24.dp,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Text(
                text = "Save your take",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // ── Topic reminder strip (tap → re-view Topic Reveal) ───────────────
        Surface(
            color = cat.tint,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CurioIcon(
                    name = cat.iconGlyph,
                    contentDescription = null,
                    tint = cat.accent,
                    size = 20.dp
                )
                Text(
                    text = "${topic.name} \u00B7 ${cat.displayName}",
                    style = MaterialTheme.typography.titleSmall,
                    color = cat.accent
                )
            }
        }

        // ── Format body ─────────────────────────────────────────────────────
        ScreenEntrance {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (recordingState) {
                    RecordingState.IDLE -> IdleControls(
                        accent = cat.accent,
                        onRecord = {
                            recordingState = RecordingState.RECORDING
                            recordingSeconds = 0
                        }
                    )
                    RecordingState.RECORDING,
                    RecordingState.PAUSED -> LiveControls(
                        accent = cat.accent,
                        tint = cat.tint,
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
                        accent = cat.accent,
                        tint = cat.tint,
                        seconds = recordingSeconds,
                        onReRecord = {
                            recordingState = RecordingState.IDLE
                            recordingSeconds = 0
                        }
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Optional one-line title (CURIO_SPEC.md §8.1)
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

        // ── Sticky bottom Save CTA ──────────────────────────────────────────
        Surface(
            color = MaterialTheme.colorScheme.background,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = { saveInProgress = true },
                    enabled = canSave && !saveInProgress,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cat.accent,
                        contentColor = CurioColors.DeepPlum,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (saveInProgress) {
                        CircularProgressIndicator(
                            color = CurioColors.DeepPlum,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Saving\u2026",
                            style = MaterialTheme.typography.titleMedium
                        )
                    } else {
                        Text(
                            text = "Save entry",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }

    // Simulated save: 400ms shimmer → confetti → 700ms pause → navigate
    LaunchedEffect(saveInProgress) {
        if (saveInProgress) {
            delay(400)
            confettiTrigger++
            saveInProgress = false
        }
    }
    LaunchedEffect(confettiTrigger) {
        if (confettiTrigger > 0) {
            delay(700)
            navController.navigate(
                CurioRoutes.entryDetail("entry-new-${System.currentTimeMillis()}")
            )
        }
    }

    if (confettiTrigger > 0) {
        ConfettiBurst(
            color = cat.accent,
            trigger = confettiTrigger,
            modifier = Modifier.fillMaxSize(),
            onComplete = {}
        )
    }
}

private enum class RecordingState { IDLE, RECORDING, PAUSED, STOPPED }

@Composable
private fun IdleControls(
    accent: Color,
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

        // Live waveform — always rendered (visible=true was a no-op wrapper)
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
        TextButton(onClick = onReRecord) {
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