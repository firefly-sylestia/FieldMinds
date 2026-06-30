package fieldmind.research.app.features.field.presentation.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fieldmind.research.app.features.field.presentation.components.FieldMindIcons
import fieldmind.research.app.features.field.data.settings.FieldMindSettings
import fieldmind.research.app.features.field.presentation.components.AnimationConfig
import fieldmind.research.app.features.field.presentation.components.FieldMindMotion
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon
import kotlinx.coroutines.delay

// ══════════════════════════════════════════════════════════════════════
//  Developer Debug Tools — Gesture Thresholds
// ══════════════════════════════════════════════════════════════════════

@Composable
fun GestureThresholdsCard() {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(FieldMindTheme.colors.hypothesis.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                    Icon(MaterialSymbolIcon("gesture"), null, tint = FieldMindTheme.colors.hypothesis, size = 18.dp)
                }
                Text("Gesture thresholds", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(4.dp))
            ThresholdRow("SwipeBack", "${(FieldMindMotion.swipeThreshold * 100).toInt()}%")
            ThresholdRow("Tab swipe commit", "10%")
            ThresholdRow("Press scale-down", "0.95×")
            Spacer(Modifier.height(4.dp))
            Text(
                "Gesture overlay uses awaitEachGesture with PointerEventPass.Main. " +
                        "Taps on interactive elements pass through unconsumed.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Developer Debug Tools — Animation State
// ══════════════════════════════════════════════════════════════════════

@Composable
fun AnimationStateCard() {
    val reduceMotion = FieldMindMotion.isReduceMotion()

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(FieldMindTheme.colors.observation.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                    Icon(MaterialSymbolIcon("motion_photos_on"), null, tint = FieldMindTheme.colors.observation, size = 18.dp)
                }
                Text("Animation state", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            }

            // Reduce-motion indicator
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(
                    if (reduceMotion) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                ).padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (reduceMotion) "Reduce motion: ON (animator scale = 0)" else "Reduce motion: OFF",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (reduceMotion) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Box(
                    Modifier.size(12.dp).clip(CircleShape).background(
                        if (reduceMotion) MaterialTheme.colorScheme.error else FieldMindTheme.colors.positive
                    )
                )
            }

            // Animation spec overview
            Text("Animation specs", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            SpecRow("Expressive spring", damping = "${FieldMindMotion.expressiveSpring.dampingRatio}", stiffness = "${FieldMindMotion.expressiveSpring.stiffness}")
            SpecRow("Expressive snap", damping = "${FieldMindMotion.expressiveSnap.dampingRatio}", stiffness = "${FieldMindMotion.expressiveSnap.stiffness}")
            SpecRow("Expressive soft", damping = "${FieldMindMotion.expressiveSoft.dampingRatio}", stiffness = "${FieldMindMotion.expressiveSoft.stiffness}")
            SpecRow("Press spring", damping = "${FieldMindMotion.pressSpring.dampingRatio}", stiffness = "${FieldMindMotion.pressSpring.stiffness}")
            SpecRow("Stagger", damping = "${FieldMindMotion.staggerInitialDelayMs}ms initial", stiffness = "${FieldMindMotion.staggerItemDelayMs}ms item")

            Spacer(Modifier.height(2.dp))
            Text(
                "Note: Animation spec property display varies by Compose version. Values shown are the spec objects themselves.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Developer Debug Tools — Animation Tuning
// ══════════════════════════════════════════════════════════════════════

@Composable
fun AnimationTuningCard(
    settings: FieldMindSettings
) {
    val entranceDamping by settings.animEntranceDamping.collectAsState()
    val swipeBackDamping by settings.animSwipeBackDamping.collectAsState()
    val swipeThreshold by settings.animSwipeThreshold.collectAsState()
    val swipeScale by settings.animSwipeScaleFactor.collectAsState()
    val tabDamping by settings.animTabEntranceDamping.collectAsState()

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(FieldMindTheme.colors.flashcard.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                    Icon(MaterialSymbolIcon("tune"), null, tint = FieldMindTheme.colors.flashcard, size = 18.dp)
                }
                Text("Animation tuning", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(4.dp))

            // ── Entrance damping slider ──
            Text("Entrance damping: %.2f".format(entranceDamping), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            Slider(
                value = entranceDamping,
                onValueChange = { settings.setAnimEntranceDamping(it.coerceIn(0.3f, 1.0f)) },
                valueRange = 0.3f..1.0f,
                steps = 13,
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Bouncy", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Smooth", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // ── Swipe-back damping slider ──
            Text("Swipe-back damping: %.2f".format(swipeBackDamping), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            Slider(
                value = swipeBackDamping,
                onValueChange = { settings.setAnimSwipeBackDamping(it.coerceIn(0.3f, 1.0f)) },
                valueRange = 0.3f..1.0f,
                steps = 13,
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Bouncy", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Smooth", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // ── Tab entrance damping slider ──
            Text("Tab entrance damping: %.2f".format(tabDamping), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            Slider(
                value = tabDamping,
                onValueChange = { settings.setAnimTabEntranceDamping(it.coerceIn(0.3f, 1.0f)) },
                valueRange = 0.3f..1.0f,
                steps = 13,
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Bouncy", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Smooth", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // ── Swipe threshold slider ──
            Text("Swipe threshold: %.0f%%".format(swipeThreshold * 100), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            Slider(
                value = swipeThreshold,
                onValueChange = { settings.setAnimSwipeThreshold(it.coerceIn(0.05f, 0.5f)) },
                valueRange = 0.05f..0.5f,
                steps = 8,
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Easy", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Hard", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // ── Swipe scale factor slider ──
            Text("Swipe scale: %.0f%%".format(swipeScale * 100), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            Slider(
                value = swipeScale,
                onValueChange = { settings.setAnimSwipeScaleFactor(it.coerceIn(0.80f, 0.99f)) },
                valueRange = 0.80f..0.99f,
                steps = 18,
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Subtle", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Dramatic", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // ── Reset button ──
            Spacer(Modifier.height(4.dp))
            Surface(
                onClick = {
                    val def = AnimationConfig.DEFAULT
                    settings.setAnimEntranceDamping(def.entranceDampingRatio)
                    settings.setAnimSwipeBackDamping(def.swipeBackDampingRatio)
                    settings.setAnimSwipeThreshold(def.swipeThreshold)
                    settings.setAnimSwipeScaleFactor(def.swipeScaleFactor)
                    settings.setAnimTabEntranceDamping(def.tabEntranceDampingRatio)
                },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(FieldMindIcons.Refresh, null, tint = MaterialTheme.colorScheme.onErrorContainer, size = 16.dp)
                    Text("Reset to defaults", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Developer Debug Tools — Tap Test Area
// ══════════════════════════════════════════════════════════════════════

@Composable
fun TapTestCard() {
    var tapCount by remember { mutableIntStateOf(0) }
    var lastTapPosition by remember { mutableStateOf("—") }
    var colorFlash by remember { mutableStateOf(false) }

    val bgColor by animateColorAsState(
        targetValue = if (colorFlash) FieldMindTheme.colors.positive.copy(alpha = 0.12f)
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        label = "tapFlash"
    )

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(FieldMindTheme.colors.info.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                    Icon(MaterialSymbolIcon("touch_app"), null, tint = FieldMindTheme.colors.info, size = 18.dp)
                }
                Text("Tap test area", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            }

            // Tap stats row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatChip("Taps", tapCount.toString())
                StatChip("Last position", lastTapPosition)
                StatChip("Gesture", "detectTapGestures")
            }

            // Interactive tap area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgColor)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            tapCount++
                            lastTapPosition = "x=${offset.x.toInt()} y=${offset.y.toInt()}"
                            colorFlash = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (tapCount == 0) {
                    Text(
                        "Tap anywhere in this box",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "$tapCount tap${if (tapCount != 1) "s" else ""} detected",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium,
                            color = FieldMindTheme.colors.positive
                        )
                        Text(
                            lastTapPosition,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Reset button
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { tapCount = 0; lastTapPosition = "—"; colorFlash = false }) {
                    Text("Reset", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }

    // Flash reset: short delay then fade back
    LaunchedEffect(colorFlash) {
        if (colorFlash) {
            delay(200)
            colorFlash = false
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Private helpers
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun ThresholdRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
            Text(
                value,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SpecRow(label: String, damping: String, stiffness: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.35f))
        Text(damping, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.3f))
        Text(stiffness, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.35f), textAlign = TextAlign.End)
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
    }
}
