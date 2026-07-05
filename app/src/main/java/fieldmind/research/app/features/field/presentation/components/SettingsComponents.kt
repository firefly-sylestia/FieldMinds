package fieldmind.research.app.features.field.presentation.components

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fieldmind.research.app.features.field.data.settings.FieldMindSettings
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon
import fieldmind.research.app.ui.theme.CuteGradients
import fieldmind.research.app.ui.theme.cuteShadow
import kotlinx.coroutines.launch

// ══════════════════════════════════════════════════════════════════════
//  SettingsGroupCard — Card with gradient background
// ══════════════════════════════════════════════════════════════════════

/**
 * A settings card with a dynamic gradient background based on the user's
 * card gradient style and opacity settings. Wraps content in a pill-shaped
 * card with a plush shadow.
 */
@Composable
fun SettingsGroupCard(content: @Composable ColumnScope.() -> Unit) {
    val context = LocalContext.current
    val gradientSettings = remember {  FieldMindSettings.getInstance(context) }
    val gradientStyleName by gradientSettings.cardGradientStyle.collectAsState()
    val gradientStyle = remember(gradientStyleName) { CuteGradients.fromString(gradientStyleName) }
    val gradientOpacity by gradientSettings.gradientOpacity.collectAsState()
    val gradient = CuteGradients.brushFor(gradientStyle)
    Card(
        modifier = Modifier.fillMaxWidth().cuteShadow(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = gradient, shape = RoundedCornerShape(32.dp))
        ) { Column(content = content) }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  ToggleItem — Title, body, icon, and animated switch
// ══════════════════════════════════════════════════════════════════════

/**
 * A settings row with a title, description, optional icon, and a Switch.
 * Features an animated checkmark that springs in with bounce when enabled
 * and a subtle pulse animation on the switch when toggled.
 */
@Composable
fun ToggleItem(
    title: String,
    body: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: MaterialSymbolIcon? = null
) {
    val scope = rememberCoroutineScope()
    val switchPulse = remember { Animatable(1f) }
    val checkmarkScale = remember { Animatable(0f) }
    val checkmarkRotate = remember { Animatable(-90f) }

    // Animate checkmark with spring bounce when toggled
    LaunchedEffect(checked) {
        if (checked) {
            // Spring-bounce the checkmark in
            checkmarkScale.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 300f))
            checkmarkRotate.animateTo(0f, spring(dampingRatio = 0.5f, stiffness = 250f))
        } else {
            checkmarkScale.animateTo(0f, tween(150))
            checkmarkRotate.animateTo(-90f, tween(150))
        }
    }

    // Pulse on switch toggles
    LaunchedEffect(checked) {
        if (checked) {
            switchPulse.animateTo(1.3f, spring(dampingRatio = 0.55f, stiffness = 500f))
            switchPulse.animateTo(1f, spring(dampingRatio = 0.85f, stiffness = 350f))
        }
    }

    Row(
        Modifier.fillMaxWidth().clickable {
            scope.launch {
                if (!checked) {
                    // Pre-bounce the checkmark before state changes
                    checkmarkScale.snapTo(0f)
                    checkmarkScale.animateTo(1.2f, spring(dampingRatio = 0.45f, stiffness = 300f))
                    checkmarkScale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 200f))
                    checkmarkRotate.snapTo(-90f)
                    checkmarkRotate.animateTo(0f, spring(dampingRatio = 0.5f, stiffness = 250f))
                }
                switchPulse.snapTo(1.3f)
                switchPulse.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 350f))
            }
            onCheckedChange(!checked)
        }.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Optional icon
        if (icon != null) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, size = 22.dp)
            }
        }

        // Title and body text with animated checkmark
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Animated checkmark icon
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer {
                            scaleX = checkmarkScale.value.coerceAtLeast(0f)
                            scaleY = checkmarkScale.value.coerceAtLeast(0f)
                            rotationZ = checkmarkRotate.value
                            alpha = if (checked) 1f else 0f
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (checked) {
                        Icon(
                            icon = FieldMindIcons.Check,
                            contentDescription = "Enabled",
                            tint = MaterialTheme.colorScheme.primary,
                            size = 18.dp
                        )
                    }
                }
                Text(title, fontWeight = FontWeight.SemiBold)
            }
            Text(
                body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Switch with subtle pulse animation
        Switch(
            checked = checked,
            onCheckedChange = null,
            modifier = Modifier.graphicsLayer {
                scaleX = switchPulse.value
                scaleY = switchPulse.value
            }
        )
    }
}

// ══════════════════════════════════════════════════════════════════════
//  ChoiceItemForm — Row that opens an OptionPickerDialog on tap
// ══════════════════════════════════════════════════════════════════════

/**
 * Settings row that shows the current selected value and opens an [OptionPickerDialog] on tap.
 * Used inside [SettingsGroupCard] for choice-based settings (e.g., map type, temperature unit).
 */
@Composable
fun ChoiceItemForm(
    label: String,
    options: List<String>,
    selected: String,
    icon: MaterialSymbolIcon,
    onSelected: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val haptics = rememberFieldMindHaptics()

    Row(
        Modifier
            .fillMaxWidth()
            .clickable {
                haptics.light()
                showDialog = true
            }
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, size = 22.dp)
        }
        Column(Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.SemiBold)
            Text(selected, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(icon = FieldMindIcons.Forward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 18.dp)
    }

    if (showDialog) {
        OptionPickerDialog(
            title = label,
            options = options,
            selected = selected,
            onSelect = { value ->
                onSelected(value)
                haptics.confirm()
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

// ══════════════════════════════════════════════════════════════════════
//  StepperItem — Row with +/- buttons for numeric settings
// ══════════════════════════════════════════════════════════════════════

/**
 * Settings row with stepper (+/-) buttons for adjusting a numeric value.
 * Used inside [SettingsGroupCard] for incremental settings (e.g., daily goal).
 */
@Composable
fun StepperItem(
    label: String,
    body: String,
    value: Int,
    icon: MaterialSymbolIcon,
    onValueChange: (Int) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, size = 22.dp)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, fontWeight = FontWeight.SemiBold)
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        // Minus button
        IconButton(
            onClick = { if (value > 1) onValueChange(value - 1) },
            modifier = Modifier.size(40.dp),
            enabled = value > 1
        ) {
            Icon(icon = MaterialSymbolIcon("remove"), contentDescription = "Decrease", tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 20.dp)
        }
        // Value display
        Box(
            Modifier
                .size(width = 48.dp, height = 40.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Text(
                value.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        // Plus button
        IconButton(
            onClick = { if (value < 999) onValueChange(value + 1) },
            modifier = Modifier.size(40.dp),
            enabled = value < 999
        ) {
            Icon(icon = FieldMindIcons.Add, contentDescription = "Increase", tint = MaterialTheme.colorScheme.primary, size = 20.dp)
        }
    }
}
