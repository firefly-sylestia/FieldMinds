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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioTopic
import com.curio.app.data.TopicCatalog
import com.curio.app.data.TopicJsonLoader
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.CurioSparkle
import com.curio.app.ui.components.MorphEntrance
import com.curio.app.ui.components.ScreenEntrance
import com.curio.app.ui.components.StaggeredEntrance
import com.curio.app.ui.components.StaggeredItem
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioGradients
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.CurioMotion

/**
 * Topic Reveal — see CURIO_SPEC.md §6. The payoff screen.
 *
 * Upgraded with:
 *  - Morph entrance: content scales up from 0.85 with elastic spring
 *  - Staggered reveal: sparkle → image → name → chips → teaser → CTA
 *  - Enhanced sparkle motif: rotating + pulsing auto_awesome glyph
 *  - Sparkle ring burst when the screen first appears
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

    val topic by produceState<CurioTopic?>(initialValue = null, topicName, cat.id) {
        val cached = TopicCatalog.findByName(topicName)
        if (cached != null) {
            value = cached
            return@produceState
        }
        val pool = TopicJsonLoader.load(cat.id)
        value = pool.firstOrNull { it.name == topicName } ?: pool.firstOrNull()
    }

    // Sparkle ring trigger on first appearance (fires after morph entrance)
    var sparkleTrigger by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(400)
        sparkleTrigger++
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
                    navController.navigate(CurioRoutes.HOME) {
                        popUpTo(navController.graph.id) { inclusive = true }
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

        MorphEntrance(delayMs = 100) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Compact sparkle motif ───────────────────────────────────
                EnhancedSparkleMotif(color = cat.accent, trigger = sparkleTrigger)

                Spacer(Modifier.height(12.dp))

                // ── Staggered content ──────────────────────────────────────────
                StaggeredEntrance {
                    // ── Topic image placeholder ────────────────────────────────
                    StaggeredItem(index = 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
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
                    }
                    Spacer(Modifier.height(24.dp))

                    // ── Topic name ────────────────────────────────────────────
                    StaggeredItem(index = 1) {
                        Text(
                            text = topic?.name ?: cat.displayName,
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(Modifier.height(8.dp))

                    // ── Category chip + subtype chip ──────────────────────────
                    StaggeredItem(index = 2) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = cat.tint
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    CurioIcon(name = cat.iconGlyph, contentDescription = null, tint = cat.accent, size = 14.dp)
                                    Text(text = cat.displayName, style = MaterialTheme.typography.labelMedium, color = cat.accent)
                                }
                            }
                            if (topic?.subtype?.isNotBlank() == true) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = topic!!.subtype,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))

                    // ── Teaser ────────────────────────────────────────────────
                    StaggeredItem(index = 3) {
                        Text(
                            text = "One quirky fact to get you curious...",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = cat.accent,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = topic?.teaser ?: "Loading topic…",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(Modifier.height(32.dp))

                    // ── Action prompt card ────────────────────────────────────
                    StaggeredItem(index = 4) {
                        if (topic != null) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CurioIcon(name = CurioIcons.AutoAwesome, contentDescription = null, tint = cat.accent, size = 18.dp)
                                        Text(
                                            text = "${topic!!.exploreAction.verb} ${topic!!.exploreAction.targetName}",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = topic!!.exploreAction.instruction,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(32.dp))

                    // ── Primary CTA ───────────────────────────────────────────
                    StaggeredItem(index = 5) {
                        Button(
                            onClick = {
                                val topicNameResolved = topic?.name ?: return@Button
                                navController.navigate(CurioRoutes.captureFor(cat.id.routeSlug, topicNameResolved))
                            },
                            enabled = topic != null,
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = cat.accent,
                                contentColor = CurioColors.DeepPlum
                            ),
                            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Start exploring  \u2192",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    StaggeredItem(index = 6) {
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
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

/**
 * Enhanced decorative accent — slow rotation + pulsing + sparkle ring
 * on initial appearance.
 */
@Composable
private fun EnhancedSparkleMotif(color: Color, trigger: Int) {
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
        modifier = Modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        // Sparkle ring background
        CurioSparkle(
            color = color.copy(alpha = 0.3f),
            trigger = trigger,
            size = 48.dp,
            ringCount = 2
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .scale(pulse)
                .rotate(rotation),
            contentAlignment = Alignment.Center
        ) {
            CurioIcon(
                name = CurioIcons.AutoAwesome,
                contentDescription = null,
                tint = color,
                size = 36.dp
            )
        }
    }
}
