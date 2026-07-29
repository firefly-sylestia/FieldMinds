package com.curio.app.features.reveal

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
import com.curio.app.data.MockTopics
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.ScreenEntrance
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioGradients
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons

/**
 * Topic Reveal — see CURIO_SPEC.md §6. The payoff screen.
 *
 * Layout (top to bottom):
 *   - ✕ close (top-right) → discards topic, exits all the way to Home
 *   - Slowly-rotating sparkle motif (decorative accent in category color)
 *   - Topic image placeholder (~280dp tall, category gradient)
 *   - Topic name (geom, displaySmall)
 *   - Category chip + subtype chip
 *   - "One quirky fact to get you curious..." teaser (1-2 sentences)
 *   - Action prompt card (the "what to do" reminder)
 *   - "Start exploring →" primary filled button → Save/Capture (§8)
 *   - "Spin again instead" text button → back to Spin
 *
 * Per CURIO_SPEC.md v2: "Start exploring" routes directly to Save/Capture
 * (Exploration Hub was removed from the flow; scratchpad state preserved
 * for a possible v3, see §13.6).
 */
@Composable
fun TopicRevealScreen(
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Top bar — close (✕) only ────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                onClick = {
                    // Discard topic, exit all the way back to Home. Uses the
                    // standard "clear back stack + navigate to target" pattern
                    // so the user lands on a fresh HOME (no stale SPIN /
                    // PICKER / REVEAL screens behind it). Without the
                    // popUpTo(graph.id), `navigate(HOME)` would just push
                    // HOME on top of the current stack and the back button
                    // would take the user back into Reveal.
                    navController.navigate(CurioRoutes.HOME) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                CurioIcon(
                    name = CurioIcons.Close,
                    contentDescription = "Discard and back to Home",
                    tint = MaterialTheme.colorScheme.onSurface,
                    size = 24.dp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        ScreenEntrance {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Sparkle motif (slowly rotating + pulsing) ─────────────────
                SparkleMotif(color = cat.accent)
                Spacer(Modifier.height(16.dp))

                // ── Topic image placeholder ───────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(
                            brush = if (cat.id == CategoryId.WILDCARD)
                                Brush.horizontalGradient(CurioGradients.WildcardGradientStops)
                            else Brush.verticalGradient(listOf(cat.accent, cat.tint)),
                            shape = RoundedCornerShape(28.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CurioIcon(
                        name = cat.iconGlyph,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        size = 120.dp
                    )
                }
                Spacer(Modifier.height(24.dp))

                // ── Topic name ────────────────────────────────────────────────
                Text(
                    text = topic.name,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))

                // ── Category chip + subtype chip ──────────────────────────────
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = cat.tint
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 6.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CurioIcon(
                                name = cat.iconGlyph,
                                contentDescription = null,
                                tint = cat.accent,
                                size = 14.dp
                            )
                            Text(
                                text = cat.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                color = cat.accent
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = topic.subtype,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 6.dp
                            )
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))

                // ── Teaser ────────────────────────────────────────────────────
                Text(
                    text = "One quirky fact to get you curious...",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = cat.accent
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = topic.teaser,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(32.dp))

                // ── Action prompt card ────────────────────────────────────────
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CurioIcon(
                                name = CurioIcons.AutoAwesome,
                                contentDescription = null,
                                tint = cat.accent,
                                size = 18.dp
                            )
                            Text(
                                text = "${topic.actionPrompt.verb} " +
                                       "${topic.actionPrompt.targetName}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = topic.actionPrompt.instruction,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                // ── Primary CTA: Start exploring → ────────────────────────────
                Button(
                    onClick = {
                        navController.navigate(
                            CurioRoutes.captureFor(cat.id.routeSlug, topic.name)
                        )
                    },
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cat.accent,
                        contentColor = CurioColors.DeepPlum
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 32.dp,
                        vertical = 16.dp
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Start exploring  \u2192",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Spin again instead",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Decorative accent — slow rotation + gentle pulse. Replaces the emoji
 * sparkle motif that v1 had; uses the Material Symbols `auto_awesome`
 * glyph in the category accent.
 */
@Composable
private fun SparkleMotif(color: Color) {
    val transition = rememberInfiniteTransition(label = "sparkle")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing)
        ),
        label = "sparkleRot"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparklePulse"
    )

    Box(
        modifier = Modifier
            .size(64.dp)
            .scale(pulse)
            .rotate(rotation),
        contentAlignment = Alignment.Center
    ) {
        CurioIcon(
            name = CurioIcons.AutoAwesome,
            contentDescription = null,
            tint = color,
            size = 56.dp
        )
    }
}