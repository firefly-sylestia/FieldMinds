package fieldmind.research.app.features.field.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import fieldmind.research.app.features.field.presentation.components.FieldMindIcons
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import fieldmind.research.app.shared.presentation.components.icons.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A visual color scheme picker that shows each scheme's palette swatches
 * (primary, secondary, tertiary, background, surface) as colored dots
 * alongside the scheme name and description.
 *
 * Currently shows: Default, Midnight Flora, Noir Amethyst, Warm Terrain.
 * Auto-selects the matching gradient style when a scheme is chosen.
 */
@Composable
fun ColorSchemeSwatchPicker(
    selected: String,
    onSelected: (String) -> Unit
) {
    data class SchemeSwatch(
        val name: String,
        val primary: Color,
        val secondary: Color,
        val tertiary: Color,
        val background: Color,
        val surface: Color,
        val desc: String
    )

    val swatches = listOf(
        SchemeSwatch(
            name = "Default",
            primary = Color(0xFF1F6B4C),
            secondary = Color(0xFF4F6353),
            tertiary = Color(0xFF8A5A00),
            background = Color(0xFFFAF9F7),
            surface = Color(0xFFFAF9F7),
            desc = "FieldMind brand — forest green + ochre"
        ),
        SchemeSwatch(
            name = "Midnight Flora",
            primary = Color(0xFF1A6B4C),
            secondary = Color(0xFF5B6770),
            tertiary = Color(0xFFB8860B),
            background = Color(0xFFF8F6F2),
            surface = Color(0xFFF8F6F2),
            desc = "Deep emerald + warm amber"
        ),
        SchemeSwatch(
            name = "Noir Amethyst",
            primary = Color(0xFF5B3E96),
            secondary = Color(0xFF6B5E7A),
            tertiary = Color(0xFFD4726A),
            background = Color(0xFFFCFAFF),
            surface = Color(0xFFFCFAFF),
            desc = "Deep violet + amethyst glow"
        ),
        SchemeSwatch(
            name = "Warm Terrain",
            primary = Color(0xFF8B6B4A),
            secondary = Color(0xFF6B7D6B),
            tertiary = Color(0xFFC07050),
            background = Color(0xFFFAF6F0),
            surface = Color(0xFFFAF6F0),
            desc = "Earthy brown + sage"
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        swatches.forEach { swatch ->
            val isSelected = selected == swatch.name
            Surface(
                onClick = { onSelected(swatch.name) },
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = if (isSelected) {
                    BorderStroke(2.dp, swatch.primary)
                } else {
                    null
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Palette swatch dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Primary, secondary, tertiary — solid 20dp circles
                        listOf(swatch.primary, swatch.secondary, swatch.tertiary).forEach { color ->
                            Box(
                                Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                        }
                        // Background — 16dp with hairline border
                        Box(
                            Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(swatch.background)
                                .border(0.5.dp, Color.Gray.copy(alpha = 0.25f), CircleShape)
                        )
                        // Surface — 16dp with hairline border
                        Box(
                            Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(swatch.surface)
                                .border(0.5.dp, Color.Gray.copy(alpha = 0.25f), CircleShape)
                        )
                    }

                    // Name + description
                    Column(Modifier.weight(1f)) {
                        Text(
                            swatch.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            swatch.desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Selection indicator
                    if (isSelected) {
                        Box(
                            Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(swatch.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                FieldMindIcons.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                size = 18.dp
                            )
                        }
                    } else {
                        Icon(
                            FieldMindIcons.Forward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            size = 20.dp
                        )
                    }
                }
            }
        }
    }
}
