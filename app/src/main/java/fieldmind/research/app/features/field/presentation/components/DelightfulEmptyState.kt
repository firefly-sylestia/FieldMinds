package fieldmind.research.app.features.field.presentation.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon
import fieldmind.research.app.ui.theme.CuteElevations
import fieldmind.research.app.ui.theme.cuteShadow

/**
 * A delightful empty state card with an animated nature scene, warm personality-filled
 * copy, a helpful tip, and a pulsing action button.
 *
 * Use this to replace plain [EmptyState] calls throughout the app. The animated scene
 * and friendly text turn a dead-end blank state into a warm invitation to start exploring.
 *
 * @param context A keyword describing the empty context (e.g. "observations", "notes",
 *   "sources", "flashcards", "search", "data", "map", "projects"). Determines the
 *   animated scene variant, accent color, and friendly copy text.
 * @param customTitle Optional override for the title text. If null, the context-based
 *   friendly title is used.
 * @param customBody Optional override for the body text. If null, the context-based
 *   friendly body is used.
 * @param actionLabel Label for the action button.
 * @param onAction Callback when the action button is tapped.
 * @param onDismiss Optional callback to dismiss/hide the card entirely.
 * @param modifier Modifier for the card.
 */
@Composable
fun DelightfulEmptyState(
    context: String,
    modifier: Modifier = Modifier,
    customTitle: String? = null,
    customBody: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null
) {
    val copy = EmptyStateCopy.forContext(context)
    val title = customTitle ?: copy.title
    val body = customBody ?: copy.body
    val tip = copy.tip
    val sceneVariant = EmptyStateCopy.sceneFor(context)
    val accentColor = EmptyStateCopy.accentFor(context)

    // ── Pulsing glow for action button ──
    val infiniteTransition = rememberInfiniteTransition(label = "emptyStatePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            tween(1500),
            RepeatMode.Reverse
        ),
        label = "actionPulseScale"
    )
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(1200),
            RepeatMode.Reverse
        ),
        label = "actionPulseGlow"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .cuteShadow(elevation = CuteElevations.nonClickableTier, shape = RoundedCornerShape(34.dp)),
        shape = RoundedCornerShape(34.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.03f),
                            Color.Transparent,
                            MaterialTheme.colorScheme.surface
                        )
                    ),
                    shape = RoundedCornerShape(34.dp)
                )
        ) {
            // ── Animated scene ──
            AnimatedEmptyScene(
                variant = sceneVariant,
                accentColor = accentColor,
                height = 120.dp
            )

            // ── Content ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ── Title with warmth ──
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                // ── Friendly body ──
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )

                // ── Helpful tip ──
                if (tip.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                accentColor.copy(alpha = 0.06f),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            MaterialSymbolIcon("lightbulb"),
                            contentDescription = null,
                            tint = accentColor.copy(alpha = 0.7f),
                            size = 16.dp
                        )
                        Text(
                            text = tip,
                            style = MaterialTheme.typography.bodySmall,
                            color = accentColor.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium,
                            maxLines = 2
                        )
                    }
                }

                // ── Pulsing action button ──
                if (actionLabel != null && onAction != null) {
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .scale(pulseScale)
                            .graphicsLayer {
                                this.shadowElevation = pulseGlow * 8f
                            }
                    ) {
                        Button(
                            onClick = onAction,
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Icon(
                                MaterialSymbolIcon("add"),
                                contentDescription = null,
                                size = 18.dp
                            )
                            Spacer(Modifier.size(8.dp))
                            Text(
                                text = actionLabel,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // ── Dismiss hint ──
                if (onDismiss != null) {
                    Spacer(Modifier.height(2.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onDismiss
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Dismiss",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Icon(
                            MaterialSymbolIcon("chevron_right"),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            size = 12.dp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Context-based copy extension for EmptyStateCopy.
 * Maps a context keyword to the appropriate friendly copy set.
 */
private fun EmptyStateCopy.forContext(context: String): EmptyStateCopy.Copy {
    val lower = context.lowercase()
    return when {
        lower in listOf("observations", "observe", "capture", "camera", "log", "field log") -> forObservations()
        lower in listOf("notes", "note", "journal", "canvas") -> forNotes()
        lower in listOf("sources", "source", "library", "reading", "book") -> forSources()
        lower in listOf("flashcards", "learn", "review", "study", "flashcard") -> forFlashcards()
        lower in listOf("search", "archive", "find") -> forSearch()
        lower in listOf("data", "tools", "counter", "measure", "data records") -> forData()
        lower in listOf("map", "location", "field", "map screen") -> forMap()
        lower in listOf("projects", "project") -> forProjects()
        lower.startsWith("voice") -> EmptyStateCopy.Copy(
            title = "Your voice, captured 🎙️",
            body = "Voice notes are a hands-free way to log observations on the go. Record a quick field note while you're outside.",
            tip = "Just tap the mic button and speak — it's that simple!"
        )
        lower in listOf("media", "gallery", "photo", "photos") -> EmptyStateCopy.Copy(
            title = "A gallery of discoveries 📸",
            body = "Photos and media you capture during observations will live here. Start by taking a photo of something interesting!",
            tip = "The in-app camera lets you snap photos and tag species in one flow."
        )
        else -> forObservations()
    }
}
