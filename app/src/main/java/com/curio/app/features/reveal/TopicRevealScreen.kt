package com.curio.app.features.reveal

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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.curio.app.ui.components.ConfettiBurst
import com.curio.app.ui.components.MorphEntrance
import com.curio.app.ui.components.StaggeredEntrance
import com.curio.app.ui.components.StaggeredItem
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.CurioMotion

/**
 * Topic Reveal — see CURIO_SPEC.md §6. The payoff screen.
 *
 * Redesigned to match the Spin card aesthetic:
 *  - Flat accent colors (no gradients)
 *  - Card-based layout with 28dp corners
 *  - Smart-cast topic reference (no forced-non-nulls)
 *  - Static sparkle icon (cleaner than rotating/pulsing)
 *  - Confetti burst on first appearance
 *
 * Layout (top to bottom):
 *   - ✕ close (top-right) → discards topic, exits all the way to Home
 *   - Sparkle icon (static, accent-colored)
 *   - Topic image card (~200dp, flat accent + category glyph)
 *   - Topic name (geom, displaySmall)
 *   - Category chip + subtype chip
 *   - Teaser card ("One quirky fact…")
 *   - Action prompt card (verb + target + instruction)
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

    var confettiTrigger by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(300)
        confettiTrigger++
    }

    // Smart-cast safe reference
    val resolved = topic

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ── 1. Top bar — close (✕) only ──────────────────────────────────────
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
                // ── 2. Sparkle icon ──────────────────────────────────────────
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CurioIcon(
                        name = CurioIcons.AutoAwesome,
                        contentDescription = null,
                        tint = cat.accent,
                        size = 48.dp
                    )
                }
                Spacer(Modifier.height(8.dp))

                // ── 3. Staggered content ─────────────────────────────────────
                StaggeredEntrance {
                    // ── Topic image card ──────────────────────────────────────
                    StaggeredItem(index = 0) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            shape = RoundedCornerShape(28.dp),
                            color = cat.accent.copy(alpha = 0.12f)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                CurioIcon(
                                    name = cat.iconGlyph,
                                    contentDescription = null,
                                    tint = cat.accent.copy(alpha = 0.35f),
                                    size = 100.dp
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))

                    // ── Topic name ───────────────────────────────────────────
                    StaggeredItem(index = 1) {
                        Text(
                            text = resolved?.name ?: cat.displayName,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = CurioColors.DeepPlum,
                            textAlign = TextAlign.Center,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(Modifier.height(8.dp))

                    // ── Category + subtype chips ─────────────────────────────
                    StaggeredItem(index = 2) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = cat.accent.copy(alpha = 0.15f)
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
                            if (resolved?.subtype?.isNotBlank() == true) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = resolved.subtype,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))

                    // ── Teaser card ──────────────────────────────────────────
                    StaggeredItem(index = 3) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        Modifier
                                            .width(3.dp)
                                            .height(18.dp)
                                            .background(cat.accent, RoundedCornerShape(2.dp))
                                    )
                                    Text(
                                        text = "One quirky fact to get you curious",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = cat.accent
                                    )
                                }
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = resolved?.teaser ?: "Loading topic…",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 5,
                                    softWrap = true,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    // ── Action prompt card ────────────────────────────────────
                    StaggeredItem(index = 4) {
                        if (resolved != null) {
                            val action = resolved.exploreAction
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = cat.accent.copy(alpha = 0.06f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CurioIcon(name = CurioIcons.AutoAwesome, contentDescription = null, tint = cat.accent, size = 18.dp)
                                        Text(
                                            text = "${action.verb} ${action.targetName}",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = cat.accent,
                                            maxLines = 2,
                                            softWrap = true,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = action.instruction,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 3,
                                        softWrap = true,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))

                    // ── Primary CTA ───────────────────────────────────────────
                    StaggeredItem(index = 5) {
                        Button(
                            onClick = {
                                val name = resolved?.name ?: return@Button
                                navController.navigate(CurioRoutes.captureFor(cat.id.routeSlug, name))
                            },
                            enabled = resolved != null,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = cat.accent,
                                contentColor = Color.White,
                                disabledContainerColor = cat.tint,
                                disabledContentColor = CurioColors.DeepPlum.copy(alpha = 0.4f)
                            ),
                            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 18.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CurioIcon(CurioIcons.AutoAwesome, null, tint = Color.White, size = 20.dp)
                                Text(
                                    text = "Start exploring",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                                )
                            }
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

    // ── Confetti on entrance ────────────────────────────────────────────────
    if (confettiTrigger > 0) {
        ConfettiBurst(
            colors = listOf(cat.accent, cat.tint, CurioColors.ButterYellow),
            trigger = confettiTrigger,
            particleCount = CurioMotion.ConfettiParticleCountLarge,
            modifier = Modifier.fillMaxSize(),
            onComplete = {}
        )
    }
}
