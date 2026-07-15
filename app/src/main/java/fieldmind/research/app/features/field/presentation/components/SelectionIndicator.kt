package fieldmind.research.app.features.field.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import fieldmind.research.app.ui.theme.CuteCardDefaults

/**
 * A cute, high-visibility checkbox with a soft pressed scale and theme-aware colors.
 * Uses a rounded chip shape for smooth edges — no hard rectangular borders.
 */
@Composable
fun FieldMindCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = if (checked) 1.05f else 1f,
        animationSpec = FieldMindMotion.expressiveSpring,
        label = "checkboxScale"
    )
    val checkedColor by animateColorAsState(
        targetValue = if (checked) accentColor else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(durationMillis = FieldMindMotion.durationSubtle),
        label = "checkboxColor"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .size(28.dp)
            .clip(CuteCardDefaults.ChipShape)
            .background(
                if (checked) accentColor.copy(alpha = if (FieldMindTheme.colors.isDark) 0.24f else 0.12f)
                else MaterialTheme.colorScheme.surfaceContainerHigh
            )
            .then(
                if (!checked)
                    Modifier.border(
                        width = 1.5.dp,
                        color = checkedColor,
                        shape = CuteCardDefaults.ChipShape
                    )
                else Modifier
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = { onCheckedChange(!checked) }
            ),
        contentAlignment = Alignment.Center
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            colors = CheckboxDefaults.colors(
                checkedColor = accentColor,
                uncheckedColor = Color.Transparent,
                checkmarkColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}

/**
 * A cute, high-visibility radio button with a soft pressed scale and theme-aware colors.
 */
@Composable
fun FieldMindRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1f,
        animationSpec = FieldMindMotion.expressiveSpring,
        label = "radioScale"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) accentColor else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(durationMillis = FieldMindMotion.durationSubtle),
        label = "radioBorder"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .size(24.dp)
            .clip(CircleShape)
            .border(2.dp, borderColor, CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
        }
    }
}

/**
 * A cute, high-visibility switch with theme-aware thumb/track colors and a soft scale.
 */
@Composable
fun FieldMindSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.surface,
            checkedTrackColor = accentColor,
            checkedIconColor = accentColor,
            uncheckedThumbColor = MaterialTheme.colorScheme.surface,
            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            uncheckedIconColor = MaterialTheme.colorScheme.outlineVariant,
            disabledCheckedThumbColor = MaterialTheme.colorScheme.surface,
            disabledCheckedTrackColor = accentColor.copy(alpha = 0.3f),
            disabledUncheckedThumbColor = MaterialTheme.colorScheme.surface,
            disabledUncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
        )
    )
}

/**
 * A complete selection row: label + optional description + selection control.
 * Uses soft rounded card shape with subtle selection highlight — no hard rectangular edges.
 */
@Composable
fun SelectionRow(
    label: String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    mode: SelectionMode = SelectionMode.CHECKBOX,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier
            .clip(CuteCardDefaults.OptionShape)
            .clickable { onSelectedChange(!selected) }
            .background(
                if (selected) accentColor.copy(alpha = if (FieldMindTheme.colors.isDark) 0.14f else 0.08f)
                else MaterialTheme.colorScheme.surfaceContainerLow
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (mode) {
            SelectionMode.CHECKBOX -> FieldMindCheckbox(
                checked = selected,
                onCheckedChange = onSelectedChange,
                accentColor = accentColor
            )
            SelectionMode.RADIO -> FieldMindRadioButton(
                selected = selected,
                onClick = { onSelectedChange(!selected) },
                accentColor = accentColor
            )
            SelectionMode.SWITCH -> FieldMindSwitch(
                checked = selected,
                onCheckedChange = onSelectedChange,
                accentColor = accentColor
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) accentColor else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

enum class SelectionMode { CHECKBOX, RADIO, SWITCH }
