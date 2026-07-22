package fieldmind.research.app.features.field.presentation.screens
import fieldmind.research.app.ui.theme.CuteCardDefaults

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.graphics.shapes.Morph
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import fieldmind.research.app.features.field.presentation.components.FieldMindIcons
import fieldmind.research.app.features.field.data.settings.FieldMindSettings
import fieldmind.research.app.features.field.presentation.components.AnimationConfig
import fieldmind.research.app.features.field.presentation.components.FieldMindMotion
import fieldmind.research.app.features.field.presentation.components.MorphPolygonShape
import fieldmind.research.app.features.field.presentation.components.MorphPolygons
import fieldmind.research.app.features.field.presentation.components.StandardScreenHeader
import fieldmind.research.app.features.field.presentation.components.BackButton
import fieldmind.research.app.features.field.presentation.components.LocalAnimationConfig
import fieldmind.research.app.features.field.presentation.components.SectionHeader
import fieldmind.research.app.features.field.presentation.components.SettingsGroupCard
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon
import androidx.activity.compose.BackHandler
import fieldmind.research.app.ui.theme.CuteElevations

// ══════════════════════════════════════════════════════════════════════
//  Developer Debug Tools — Gesture Thresholds
// ══════════════════════════════════════════════════════════════════════

@Composable
fun GestureThresholdsCard() {
    Card(
        shape = CuteCardDefaults.Shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(32.dp).clip(CuteCardDefaults.ChipShape).background(FieldMindTheme.colors.hypothesis.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
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
        shape = CuteCardDefaults.Shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(32.dp).clip(CuteCardDefaults.ChipShape).background(FieldMindTheme.colors.observation.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                    Icon(MaterialSymbolIcon("motion_photos_on"), null, tint = FieldMindTheme.colors.observation, size = 18.dp)
                }
                Text("Animation state", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            }

            // Reduce-motion indicator
            Row(
                Modifier.fillMaxWidth().clip(CuteCardDefaults.ButtonShape).background(
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
    val entranceStiffness by settings.animEntranceStiffness.collectAsState()
    val swipeBackDamping by settings.animSwipeBackDamping.collectAsState()
    val swipeBackStiffness by settings.animSwipeBackStiffness.collectAsState()
    val swipeThreshold by settings.animSwipeThreshold.collectAsState()
    val swipeScale by settings.animSwipeScaleFactor.collectAsState()
    val tabDamping by settings.animTabEntranceDamping.collectAsState()
    val tabStiffness by settings.animTabEntranceStiffness.collectAsState()
    val morphEnabled by settings.animMorphEnabled.collectAsState()
    val morphDamping by settings.animMorphDamping.collectAsState()
    val morphStiffness by settings.animMorphStiffness.collectAsState()
    val predictiveBackEnabled by settings.predictiveBackEnabled.collectAsState()
    val predictiveBackScaleMin by settings.predictiveBackScaleMin.collectAsState()
    val sideSwipeEnabled by settings.sideSwipeEnabled.collectAsState()
    val sideSwipeThreshold by settings.sideSwipeThreshold.collectAsState()
    val sideSwipeDamping by settings.sideSwipeDamping.collectAsState()
    val sideSwipeStiffness by settings.sideSwipeStiffness.collectAsState()
    val sideSwipeMaxReveal by settings.sideSwipeMaxRevealDp.collectAsState()

    Card(
        shape = CuteCardDefaults.Shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(32.dp).clip(CuteCardDefaults.ChipShape).background(FieldMindTheme.colors.flashcard.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                    Icon(MaterialSymbolIcon("tune"), null, tint = FieldMindTheme.colors.flashcard, size = 18.dp)
                }
                Text("Animation tuning", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(4.dp))

            // ── Predictive back ──
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Predictive back", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    Text("Android 13+ system back gesture with peek", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = predictiveBackEnabled,
                    onCheckedChange = { settings.setPredictiveBackEnabled(it) }
                )
            }
            Text("Predictive back scale: %.0f%%".format(predictiveBackScaleMin * 100), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            Slider(
                value = predictiveBackScaleMin,
                onValueChange = { settings.setPredictiveBackScaleMin(it.coerceIn(0.70f, 0.95f)) },
                valueRange = 0.70f..0.95f,
                steps = 24,
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Small", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Large", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(4.dp))

            // ── Entrance damping + stiffness ──
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
            Text("Entrance stiffness: %.0f".format(entranceStiffness), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            Slider(
                value = entranceStiffness,
                onValueChange = { settings.setAnimEntranceStiffness(it.coerceIn(50f, 5000f)) },
                valueRange = 50f..5000f,
                steps = 99,
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Soft", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Stiff", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // ── Swipe-back damping + stiffness ──
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
            Text("Swipe-back stiffness: %.0f".format(swipeBackStiffness), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            Slider(
                value = swipeBackStiffness,
                onValueChange = { settings.setAnimSwipeBackStiffness(it.coerceIn(50f, 5000f)) },
                valueRange = 50f..5000f,
                steps = 99,
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Soft", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Stiff", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // ── Tab entrance damping + stiffness ──
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
            Text("Tab entrance stiffness: %.0f".format(tabStiffness), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            Slider(
                value = tabStiffness,
                onValueChange = { settings.setAnimTabEntranceStiffness(it.coerceIn(50f, 5000f)) },
                valueRange = 50f..5000f,
                steps = 99,
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Soft", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Stiff", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

            // ── Morph damping + stiffness ──
            Text("Morph damping: %.2f".format(morphDamping), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            Slider(
                value = morphDamping,
                onValueChange = { settings.setAnimMorphDamping(it.coerceIn(0.3f, 1.0f)) },
                valueRange = 0.3f..1.0f,
                steps = 13,
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Bouncy", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Smooth", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("Morph stiffness: %.0f".format(morphStiffness), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            Slider(
                value = morphStiffness,
                onValueChange = { settings.setAnimMorphStiffness(it.coerceIn(50f, 5000f)) },
                valueRange = 50f..5000f,
                steps = 99,
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Soft", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Stiff", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // ── Side swipe tuning ──
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Side swipe", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    Text("Telegram-style item swipe actions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = sideSwipeEnabled,
                    onCheckedChange = { settings.setSideSwipeEnabled(it) }
                )
            }
            Text("Side swipe threshold: %.0f%%".format(sideSwipeThreshold * 100), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            Slider(
                value = sideSwipeThreshold,
                onValueChange = { settings.setSideSwipeThreshold(it.coerceIn(0.10f, 0.50f)) },
                valueRange = 0.10f..0.50f,
                steps = 7,
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Easy", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Firm", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("Side swipe reveal: %.0f dp".format(sideSwipeMaxReveal), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            Slider(
                value = sideSwipeMaxReveal,
                onValueChange = { settings.setSideSwipeMaxRevealDp(it.coerceIn(40f, 150f)) },
                valueRange = 40f..150f,
                steps = 10,
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Narrow", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Wide", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // ── Morph enabled toggle ──
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Morph animations", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                Switch(
                    checked = morphEnabled,
                    onCheckedChange = { settings.setAnimMorphEnabled(it) }
                )
            }

            // ── Live Preview ──
            AnimationPreviewDemo()

            // ── Morph Preview ──
            MorphPreviewDemo()

            // ── Reset button ──
            Spacer(Modifier.height(4.dp))
            Surface(
                onClick = {
                    val def = AnimationConfig.DEFAULT
                    settings.setAnimEntranceDamping(def.dampingRatio)
                    settings.setAnimEntranceStiffness(def.stiffness)
                    settings.setAnimSwipeBackDamping(def.swipeBackDampingRatio)
                    settings.setAnimSwipeBackStiffness(def.swipeBackStiffness)
                    settings.setAnimSwipeThreshold(def.swipeThreshold)
                    settings.setAnimSwipeScaleFactor(def.swipeScaleFactor)
                    settings.setAnimTabEntranceDamping(def.dampingRatio)
                    settings.setAnimTabEntranceStiffness(def.stiffness)
                    settings.setAnimMorphEnabled(def.morphEnabled)
                    settings.setAnimMorphDamping(def.morphDampingRatio)
                    settings.setAnimMorphStiffness(def.morphStiffness)
                    settings.setPredictiveBackEnabled(def.predictiveBackEnabled)
                    settings.setPredictiveBackScaleMin(def.predictiveBackScaleMin)
                    settings.setSideSwipeEnabled(def.sideSwipeEnabled)
                    settings.setSideSwipeThreshold(def.sideSwipeThreshold)
                    settings.setSideSwipeDamping(def.sideSwipeDampingRatio)
                    settings.setSideSwipeStiffness(def.sideSwipeStiffness)
                    settings.setSideSwipeMaxRevealDp(def.sideSwipeMaxRevealDp)
                },
                shape = MaterialTheme.shapes.medium,
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
        shape = CuteCardDefaults.Shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(32.dp).clip(CuteCardDefaults.ChipShape).background(FieldMindTheme.colors.info.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
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
                    .clip(CuteCardDefaults.ShapeCompact)
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
        Surface(shape = CuteCardDefaults.ChipShape, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
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

// ══════════════════════════════════════════════════════════════════════
//  Live Animation Preview — interactively demonstrates current spring tuning
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun AnimationPreviewDemo() {
    val animConfig = LocalAnimationConfig.current
    val scope = rememberCoroutineScope()
    val colors = FieldMindTheme.colors

    // ── Animatable properties for three demonstration modes ──
    val scaleAnim = remember { Animatable(0f) }
    val offsetXAnim = remember { Animatable(0f) }
    val offsetYAnim = remember { Animatable(0f) }

    // ── Track which animation last played (for label feedback) ──
    var lastAction by remember { mutableStateOf("Tap a button to preview") }

    Spacer(Modifier.height(8.dp))

    // ── Header ──
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            Modifier.size(28.dp).clip(CuteCardDefaults.ChipShape)
                .background(colors.flashcard.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(MaterialSymbolIcon("play_circle"), null, tint = colors.flashcard, size = 16.dp)
        }
        Text(
            "Live preview",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.weight(1f))
        Text(
            lastAction,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }

    Spacer(Modifier.height(8.dp))

    // ── Preview area ──
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(CuteCardDefaults.ShapeCompact)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                        MaterialTheme.colorScheme.surfaceContainerLow
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Demo element — a gradient pill with icon that animates
        Box(
            modifier = Modifier
                .size(64.dp)
                .graphicsLayer {
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                    translationX = offsetXAnim.value
                    translationY = offsetYAnim.value
                }
                .clip(CuteCardDefaults.FieldShape)
                .background(
                    Brush.sweepGradient(
                        colors = listOf(
                            colors.positive,
                            colors.observation,
                            colors.data,
                            colors.positive
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                MaterialSymbolIcon("play_arrow"),
                null,
                tint = Color.White,
                size = 32.dp
            )
        }
    }

    Spacer(Modifier.height(8.dp))

    // ── Action buttons ──
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Entrance: scale 0→1 with spring
        Button(
            onClick = {
                lastAction = "Entrance"
                scope.launch {
                    scaleAnim.snapTo(0f)
                    offsetXAnim.snapTo(0f)
                    offsetYAnim.snapTo(0f)
                    scaleAnim.animateTo(1f, animConfig.spring())
                }
            },
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Text("Entrance", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        }

        // Swipe-back: translate right and spring back
        OutlinedButton(
            onClick = {
                lastAction = "Snap back"
                scope.launch {
                    offsetXAnim.snapTo(0f)
                    offsetXAnim.animateTo(160f, animConfig.swipeBackSpring())
                    offsetXAnim.animateTo(0f, animConfig.swipeBackSpring())
                }
            },
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Text("Snap back", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        }

        // Dismiss: fly out with spring, then reset
        OutlinedButton(
            onClick = {
                lastAction = "Dismiss"
                scope.launch {
                    offsetYAnim.snapTo(0f)
                    scaleAnim.snapTo(1f)
                    offsetYAnim.animateTo(-300f, animConfig.spring())
                    // Reset after fly-out completes
                    scaleAnim.snapTo(0f)
                    offsetYAnim.snapTo(0f)
                    scaleAnim.animateTo(1f, animConfig.spring())
                }
            },
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Text("Dismiss", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        }
    }

    Spacer(Modifier.height(4.dp))
}

// ══════════════════════════════════════════════════════════════════════
//  Animation Tuning Settings — Full-screen page inside Developer Options
// ══════════════════════════════════════════════════════════════════════

@Composable
fun AnimationTuningSettingsPage(
    settings: FieldMindSettings,
    onBack: () -> Unit
) {
    val entranceDamping by settings.animEntranceDamping.collectAsState()
    val entranceStiffness by settings.animEntranceStiffness.collectAsState()
    val swipeBackDamping by settings.animSwipeBackDamping.collectAsState()
    val swipeBackStiffness by settings.animSwipeBackStiffness.collectAsState()
    val swipeThreshold by settings.animSwipeThreshold.collectAsState()
    val swipeScale by settings.animSwipeScaleFactor.collectAsState()
    val tabDamping by settings.animTabEntranceDamping.collectAsState()
    val tabStiffness by settings.animTabEntranceStiffness.collectAsState()
    val morphEnabled by settings.animMorphEnabled.collectAsState()
    val morphDamping by settings.animMorphDamping.collectAsState()
    val morphStiffness by settings.animMorphStiffness.collectAsState()
    val predictiveBackEnabled by settings.predictiveBackEnabled.collectAsState()
    val predictiveBackScaleMin by settings.predictiveBackScaleMin.collectAsState()
    val sideSwipeEnabled by settings.sideSwipeEnabled.collectAsState()
    val sideSwipeThreshold by settings.sideSwipeThreshold.collectAsState()
    val sideSwipeDamping by settings.sideSwipeDamping.collectAsState()
    val sideSwipeStiffness by settings.sideSwipeStiffness.collectAsState()
    val sideSwipeMaxReveal by settings.sideSwipeMaxRevealDp.collectAsState()

    // Hardware/gesture back button support
    BackHandler(enabled = true) { onBack() }

    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp, 12.dp, 20.dp, 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StandardScreenHeader(
                    title = "Animation Tuning",
                    subtitle = "Adjust spring physics, damping, and stiffness for every animation in the app",
                    icon = MaterialSymbolIcon("tune"),
                    trailing = { BackButton(onClick = onBack) }
                )
            }

            // ── Live Preview Section ──
            item {
                Card(
                    shape = CuteCardDefaults.Shape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.size(32.dp).clip(CuteCardDefaults.ChipShape).background(FieldMindTheme.colors.flashcard.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                                Icon(MaterialSymbolIcon("play_circle"), null, tint = FieldMindTheme.colors.flashcard, size = 18.dp)
                            }
                            Text("Live Preview", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.weight(1f))
                        }
                        AnimationPreviewDemo()
                    }
                }
            }

            // ── Entrance Spring Tuning ──
            item {
                SectionHeader("Entrance animation", "Controls how screens and elements appear")
            }
            item {
                SettingsGroupCard {
                    DampingSlider(
                        label = "Entrance damping",
                        value = entranceDamping,
                        onValueChange = { settings.setAnimEntranceDamping(it.coerceIn(0.3f, 1.0f)) },
                        lowLabel = "Bouncy",
                        highLabel = "Smooth"
                    )
                    HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    StiffnessSlider(
                        label = "Entrance stiffness",
                        value = entranceStiffness,
                        onValueChange = { settings.setAnimEntranceStiffness(it.coerceIn(50f, 5000f)) },
                        lowLabel = "Soft",
                        highLabel = "Stiff"
                    )
                }
            }

            // ── Swipe-back Spring Tuning ──
            item {
                SectionHeader("Swipe-back gesture", "Controls the back-swipe animation feel")
            }
            item {
                SettingsGroupCard {
                    DampingSlider(
                        label = "Swipe-back damping",
                        value = swipeBackDamping,
                        onValueChange = { settings.setAnimSwipeBackDamping(it.coerceIn(0.3f, 1.0f)) },
                        lowLabel = "Bouncy",
                        highLabel = "Smooth"
                    )
                    HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    StiffnessSlider(
                        label = "Swipe-back stiffness",
                        value = swipeBackStiffness,
                        onValueChange = { settings.setAnimSwipeBackStiffness(it.coerceIn(50f, 5000f)) },
                        lowLabel = "Soft",
                        highLabel = "Stiff"
                    )
                }
            }

            // ── Tab Switch Spring Tuning ──
            item {
                SectionHeader("Tab switch", "Controls the tab-switch pop animation")
            }
            item {
                SettingsGroupCard {
                    DampingSlider(
                        label = "Tab entrance damping",
                        value = tabDamping,
                        onValueChange = { settings.setAnimTabEntranceDamping(it.coerceIn(0.3f, 1.0f)) },
                        lowLabel = "Bouncy",
                        highLabel = "Smooth"
                    )
                    HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    StiffnessSlider(
                        label = "Tab entrance stiffness",
                        value = tabStiffness,
                        onValueChange = { settings.setAnimTabEntranceStiffness(it.coerceIn(50f, 5000f)) },
                        lowLabel = "Soft",
                        highLabel = "Stiff"
                    )
                }
            }

            // ── Side Swipe ──
            item {
                SectionHeader("Side swipe actions", "Telegram-style item swipe to reveal actions")
            }
            item {
                SettingsGroupCard {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Side swipe", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Swipe items left/right from anywhere to reveal actions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = sideSwipeEnabled,
                            onCheckedChange = { settings.setSideSwipeEnabled(it) }
                        )
                    }
                    HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    DampingSlider(
                        label = "Side swipe damping",
                        value = sideSwipeDamping,
                        onValueChange = { settings.setSideSwipeDamping(it.coerceIn(0.3f, 1.0f)) },
                        lowLabel = "Bouncy",
                        highLabel = "Smooth"
                    )
                    HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    StiffnessSlider(
                        label = "Side swipe stiffness",
                        value = sideSwipeStiffness,
                        onValueChange = { settings.setSideSwipeStiffness(it.coerceIn(50f, 5000f)) },
                        lowLabel = "Soft",
                        highLabel = "Stiff"
                    )
                    HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    StiffnessSlider(
                        label = "Side swipe threshold",
                        value = sideSwipeThreshold * 100,
                        onValueChange = { settings.setSideSwipeThreshold((it / 100f).coerceIn(0.10f, 0.50f)) },
                        valueRange = 10f..50f,
                        lowLabel = "Easy",
                        highLabel = "Firm",
                        formatValue = { "%.0f%%".format(it) }
                    )
                    HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    StiffnessSlider(
                        label = "Side swipe reveal",
                        value = sideSwipeMaxReveal,
                        onValueChange = { settings.setSideSwipeMaxRevealDp(it.coerceIn(40f, 150f)) },
                        valueRange = 40f..150f,
                        lowLabel = "Narrow",
                        highLabel = "Wide",
                        formatValue = { "%.0f dp".format(it) }
                    )
                }
            }

            // ── Predictive Back ──
            item {
                SectionHeader("Predictive back", "Android 13+ system back gesture with peek")
            }
            item {
                SettingsGroupCard {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Predictive back", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("iOS/Telegram-style peek when swiping back from edge", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = predictiveBackEnabled,
                            onCheckedChange = { settings.setPredictiveBackEnabled(it) }
                        )
                    }
                    HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    StiffnessSlider(
                        label = "Scale on back",
                        value = predictiveBackScaleMin * 100,
                        onValueChange = { settings.setPredictiveBackScaleMin((it / 100f).coerceIn(0.70f, 0.95f)) },
                        valueRange = 70f..95f,
                        lowLabel = "Small",
                        highLabel = "Large",
                        formatValue = { "%.0f%%".format(it) }
                    )
                }
            }

            // ── Shape Morphing ──
            item {
                SectionHeader("Shape morphing", "Polygon morph animation: circle ↔ rounded rect")
            }
            item {
                SettingsGroupCard {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Morph animations", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Smooth shape transitions between geometric forms", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = morphEnabled,
                            onCheckedChange = { settings.setAnimMorphEnabled(it) }
                        )
                    }
                    HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    DampingSlider(
                        label = "Morph damping",
                        value = morphDamping,
                        onValueChange = { settings.setAnimMorphDamping(it.coerceIn(0.3f, 1.0f)) },
                        lowLabel = "Bouncy",
                        highLabel = "Smooth"
                    )
                    HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    StiffnessSlider(
                        label = "Morph stiffness",
                        value = morphStiffness,
                        onValueChange = { settings.setAnimMorphStiffness(it.coerceIn(50f, 5000f)) },
                        lowLabel = "Soft",
                        highLabel = "Stiff"
                    )
                }
            }

            // ── Morph Live Preview ──
            item {
                MorphPreviewDemo()
            }

            // ── Swipe Threshold & Scale ──
            item {
                SectionHeader("Swipe behavior", "Commit threshold and scale effect")
            }
            item {
                SettingsGroupCard {
                    StiffnessSlider(
                        label = "Swipe threshold",
                        value = swipeThreshold * 100,
                        onValueChange = { settings.setAnimSwipeThreshold((it / 100f).coerceIn(0.05f, 0.5f)) },
                        valueRange = 5f..50f,
                        lowLabel = "Easy",
                        highLabel = "Hard",
                        formatValue = { "%.0f%%".format(it) }
                    )
                    HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    StiffnessSlider(
                        label = "Swipe scale",
                        value = swipeScale * 100,
                        onValueChange = { settings.setAnimSwipeScaleFactor((it / 100f).coerceIn(0.80f, 0.99f)) },
                        valueRange = 80f..99f,
                        lowLabel = "Subtle",
                        highLabel = "Dramatic",
                        formatValue = { "%.0f%%".format(it) }
                    )
                }
            }

            // ── Reset button ──
            item {
                Surface(
                    onClick = {
                        val def = AnimationConfig.DEFAULT
                    settings.setAnimEntranceDamping(def.dampingRatio)
                    settings.setAnimEntranceStiffness(def.stiffness)
                        settings.setAnimSwipeBackDamping(def.swipeBackDampingRatio)
                        settings.setAnimSwipeBackStiffness(def.swipeBackStiffness)
                        settings.setAnimSwipeThreshold(def.swipeThreshold)
                        settings.setAnimSwipeScaleFactor(def.swipeScaleFactor)
                        settings.setAnimTabEntranceDamping(def.dampingRatio)
                        settings.setAnimTabEntranceStiffness(def.stiffness)
                        settings.setAnimMorphEnabled(def.morphEnabled)
                        settings.setAnimMorphDamping(def.morphDampingRatio)
                        settings.setAnimMorphStiffness(def.morphStiffness)
                        settings.setPredictiveBackEnabled(def.predictiveBackEnabled)
                        settings.setPredictiveBackScaleMin(def.predictiveBackScaleMin)
                        settings.setSideSwipeEnabled(def.sideSwipeEnabled)
                        settings.setSideSwipeThreshold(def.sideSwipeThreshold)
                        settings.setSideSwipeDamping(def.sideSwipeDampingRatio)
                        settings.setSideSwipeStiffness(def.sideSwipeStiffness)
                        settings.setSideSwipeMaxRevealDp(def.sideSwipeMaxRevealDp)
                    },
                    shape = MaterialTheme.shapes.medium,
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

            // ── Info card ──
            item {
                Card(shape = CuteCardDefaults.Shape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Tip", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        Text("Lower stiffness = slower, more elegant motion. Higher damping = less bounce. Changes take effect immediately.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Shared slider helpers for Animation Tuning
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun DampingSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    lowLabel: String,
    highLabel: String
) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("$label: %.2f".format(value), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0.3f..1.0f,
            steps = 13,
            modifier = Modifier.fillMaxWidth()
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(lowLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(highLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StiffnessSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 50f..5000f,
    lowLabel: String,
    highLabel: String,
    formatValue: ((Float) -> String)? = null
) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            if (formatValue != null) formatValue(value) else "$label: %.0f".format(value),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = 99,
            modifier = Modifier.fillMaxWidth()
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(lowLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(highLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Morph Preview Demo — interactive polygon morphing live preview
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun MorphPreviewDemo() {
    val animConfig = LocalAnimationConfig.current
    val scope = rememberCoroutineScope()
    val colors = FieldMindTheme.colors

    var isMorphed by remember { mutableStateOf(false) }
    var shapePair by remember { mutableIntStateOf(0) }

    // Pre-defined shape pairs for the demo
    val shapePairs = remember {
        listOf(
            Pair(MorphPolygons.roundedRect(), MorphPolygons.circle()),
            Pair(MorphPolygons.triangle(), MorphPolygons.circle()),
            Pair(MorphPolygons.diamond(), MorphPolygons.roundedRect()),
            Pair(MorphPolygons.star(), MorphPolygons.hexagon()),
            Pair(MorphPolygons.pillRect(), MorphPolygons.circle()),
        )
    }

    val (startPoly, endPoly) = shapePairs[shapePair % shapePairs.size]

    val morph = remember(startPoly, endPoly) {
        Morph(startPoly, endPoly)
    }

    val progress by animateFloatAsState(
        targetValue = if (isMorphed) 1f else 0f,
        animationSpec = if (animConfig.morphEnabled) {
            spring(dampingRatio = animConfig.morphDampingRatio, stiffness = animConfig.morphStiffness)
        } else {
            spring(stiffness = Spring.StiffnessMedium)
        },
        label = "morphPreview"
    )

    // Re-trigger after a delay for auto-demo
    LaunchedEffect(shapePair) {
        isMorphed = false
        delay(200)
        isMorphed = true
    }

    val shapePairNames = listOf(
        "Rect → Circle",
        "Triangle → Circle",
        "Diamond → Rect",
        "Star → Hexagon",
        "Pill → Circle"
    )

    Card(
        shape = CuteCardDefaults.Shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    Modifier.size(28.dp).clip(CuteCardDefaults.ChipShape)
                        .background(colors.hypothesis.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(MaterialSymbolIcon("shapes"), null, tint = colors.hypothesis, size = 16.dp)
                }
                Text(
                    "Morph preview",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.weight(1f))
                Text(
                    shapePairNames[shapePair % shapePairNames.size],
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Preview area — morphing gradient pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(CuteCardDefaults.ShapeCompact)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceContainerHigh,
                                MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Morphing shape container
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(MorphPolygonShape(morph, progress))
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(
                                    colors.positive,
                                    colors.flashcard,
                                    colors.observation,
                                    colors.data,
                                    colors.positive
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isMorphed) MaterialSymbolIcon("circle") else MaterialSymbolIcon("square"),
                        null,
                        tint = Color.White.copy(alpha = 0.9f),
                        size = 28.dp
                    )
                }
            }

            // Toggle + cycle buttons
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { isMorphed = !isMorphed },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(
                        if (isMorphed) MaterialSymbolIcon("undo") else MaterialSymbolIcon("transform"),
                        null,
                        size = 14.dp
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (isMorphed) "Revert" else "Morph",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = { shapePair = (shapePair + 1) % shapePairs.size },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(MaterialSymbolIcon("swap_horiz"), null, size = 14.dp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Next pair",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
