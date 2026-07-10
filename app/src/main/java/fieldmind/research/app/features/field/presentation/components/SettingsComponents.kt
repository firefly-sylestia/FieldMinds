package fieldmind.research.app.features.field.presentation.components

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fieldmind.research.app.features.field.data.settings.FieldMindSettings
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon
import fieldmind.research.app.ui.theme.CuteGradients
import fieldmind.research.app.ui.theme.cuteShadow
import fieldmind.research.app.shared.presentation.theme.LocalJournalStyle
import fieldmind.research.app.ui.theme.CuteElevations

// ══════════════════════════════════════════════════════════════════════
//  SettingsGroupCard — Card with gradient background
// ══════════════════════════════════════════════════════════════════════

/**
 * A settings card with a dynamic gradient background based on the user's
 * card gradient style and opacity settings. Wraps content in a pill-shaped
 * card with a plush shadow.
 *
 * Phase 3 (Whimsical Redesign): reads [LocalJournalStyle] to apply the
 * active journal's corner radius, border style, and per-style texture
 * overlay (parchment tones for Victorian, paper fibers for Sketchbook,
 * dot-grid for BulletJournal, watercolor washes for Ghibli).
 */
@Composable
fun SettingsGroupCard(content: @Composable ColumnScope.() -> Unit) {
    val context = LocalContext.current
    val gradientSettings = remember {  FieldMindSettings.getInstance(context) }
    val gradientStyleName by gradientSettings.cardGradientStyle.collectAsState()
    val gradientStyle = remember(gradientStyleName) { CuteGradients.fromString(gradientStyleName) }
    val gradientOpacity by gradientSettings.gradientOpacity.collectAsState()
    val userGradient = CuteGradients.brushFor(gradientStyle, opacity = gradientOpacity)
    val journal = LocalJournalStyle.current
    val shape = RoundedCornerShape(journal.cardCornerRadius)
    val effectiveBorder = journalBorderStroke(journal)
    val textureModifier = journalTextureModifier(journal)
    val journalBrush = journalCardBrush(journal, fallbackColor = MaterialTheme.colorScheme.surfaceContainerLow)
    Card(
        modifier = Modifier.fillMaxWidth().cuteShadow().then(textureModifier),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = CuteElevations.nonClickableTier),
        border = effectiveBorder
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = if (journal.useGradientCards) journalBrush else userGradient,
                    shape = shape
                )
        ) { Column(content = content) }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  ToggleItem — Title, body, icon, and animated switch
// ══════════════════════════════════════════════════════════════════════

/**
 * A settings row with a title, description, optional icon, and a Switch.
 */
@Composable
fun ToggleItem(
    title: String,
    body: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: MaterialSymbolIcon? = null
) {
    Row(
        Modifier.fillMaxWidth().clickable {
            onCheckedChange(!checked)
        }.padding(18.dp),
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

        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(
                body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = null
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
            .padding(18.dp),
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
            .padding(18.dp),
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
