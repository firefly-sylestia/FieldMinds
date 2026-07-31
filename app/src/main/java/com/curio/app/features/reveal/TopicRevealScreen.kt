package com.curio.app.features.reveal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioTopic
import com.curio.app.data.TopicCatalog
import com.curio.app.data.TopicJsonLoader
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.ConfettiBurst
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioGradients
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.CurioMotion

/**
 * Topic Reveal — see CURIO_SPEC.md §6 (v2 polish).
 *
 * Upgraded from the previous §6 design:
 *  - Gradient-ticket hero header card (260dp) matching the Spin screen:
 *    accent → DeepPlum vertical gradient (rainbow for wildcard), white
 *    watermark glyph, white pill badges ("verb + duration" top-left,
 *    subtype bottom-right).
 *  - Hero shows the action you need to take immediately — the verb +
 *    duration badge sits on the ticket, not buried under the body copy.
 *  - Bigger, eye-catching topic name (uses the geom typography).
 *  - Tags row immediately under the title — gives instant context for
 *    genres / eras (e.g. "1970s · British · Art Rock").
 *  - Existing teaser card + explore-action prompt card are preserved.
 *  - Refined spacing — top padding tight (statusBarsPadding + 8dp.
 *
 * Layout, top → bottom:
 *   24-44 dp   statusBarsPadding()
 *   40 dp      Top bar (close ✕ → Pop to Home)
 *    8 dp      gap
 *   ~260 dp    Hero card (gradient ticket: watermark glyph + badges)
 *   24 dp      gap
 *   ~84 dp     Topic name (geom displaySmall, multi-line)
 *    8 dp      gap
 *   ~42 dp     Tags chip row
 *   20 dp      gap
 *   ~auto     "One quirky fact to get you curious" card
 *   16 dp      gap
 *   ~auto     "{verb} {target}" action prompt card + "~N min"
 *   24 dp      gap
 *   ~56 dp     Start exploring CTA button
 *    8 dp      gap
 *   ~auto     "Spin again instead" text button
 *   24 dp      bottom inset + navigation bars
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

    // v5.8 — saveable so a rotation mid-celebration doesn't drop the confetti burst.
    var confettiTrigger by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(300)
        confettiTrigger++
    }

    val resolved = topic
    val navInsets = WindowInsets.navigationBars.asPaddingValues()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ── 1. Top bar (close ✕ only) ──────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 0.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
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
                    size = 22.dp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
                // ── 2. Hero card — category watermark + verb/duration badge ──
                HeroCard(
                    cat = cat,
                    resolved = resolved,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // ── 3. Topic name ───────────────────────────────────────────
                Text(
                    text = resolved?.name ?: cat.displayName,
                    style = MaterialTheme.typography.displaySmall.copy(
                        lineHeight = 40.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                )

                // ── 4. Tags chip row (genre / era context) ─────────────────
                if (!resolved?.tags.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        resolved.tags.take(4).forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = cat.accent.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = cat.accent,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Spacer so subsequent sections don't crowd up.
                    Spacer(Modifier.height(10.dp))
                }

                // ── 5. Teaser card ──────────────────────────────────────────
                TeaserCard(
                    cat = cat,
                    teaser = resolved?.teaser,
                    modifier = Modifier.padding(top = 20.dp)
                )

                // ── 6. Action prompt card ──────────────────────────────────
                if (resolved != null) {
                    ActionPromptCard(
                        cat = cat,
                        action = resolved.exploreAction,
                        subtype = resolved.subtype,
                        modifier = Modifier.padding(top = 14.dp)
                    )
                }

                // ── 7. Primary CTA ─────────────────────────────────────────
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
                    contentPadding = PaddingValues(horizontal = 28.dp, vertical = 18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
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

                // ── 8. Secondary action text button ────────────────────────
                TextButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CurioIcon(CurioIcons.Refresh, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 16.dp)
                        Text(
                            text = "Spin again instead",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
            }

        Spacer(Modifier.height(navInsets.calculateBottomPadding()))
    }

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

// ═══════════════════════════════════════════════════════════════════════════
// Hero card — large category watermark with verb + duration badge
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun HeroCard(
    cat: com.curio.app.data.CurioCategory,
    resolved: CurioTopic?,
    modifier: Modifier = Modifier
) {
    val action = resolved?.exploreAction

    // Same gradient "ticket" language as the Spin screen's hero card —
    // every stop is deepened toward DeepPlum so white text stays readable
    // (category accents are pastels). Wildcard keeps its rainbow identity,
    // just saturated for contrast.
    val ticketBrush = if (cat.id == CategoryId.WILDCARD) {
        Brush.verticalGradient(
            CurioGradients.WildcardGradientStops.map { lerp(it, CurioColors.DeepPlum, 0.35f) }
        )
    } else {
        Brush.verticalGradient(
            listOf(lerp(cat.accent, CurioColors.DeepPlum, 0.45f), CurioColors.DeepPlum)
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp),
        shape = RoundedCornerShape(32.dp),
        color = cat.accent,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
        shadowElevation = 6.dp,
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ticketBrush)
        ) {
            // ── Watermark glyph (category icon) ─────────────────────────
            CurioIcon(
                name = cat.iconGlyph,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.16f),
                size = 190.dp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(end = 0.dp)
            )
            // ── Action badge (verb + duration) — white pill on ticket ───
            if (action != null && resolved != null) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.22f),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Text(
                            text = "${action.verb} for ~${action.durationMinutes} min",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
            // ── Subtype pill — white pill on ticket ────────────────────
            if (resolved?.subtype?.isNotBlank() == true) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.22f),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Text(
                        text = resolved.subtype,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Teaser card ("One quirky fact to get you curious")
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun TeaserCard(
    cat: com.curio.app.data.CurioCategory,
    teaser: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CurioIcon(
                    name = CurioIcons.AutoAwesome,
                    contentDescription = null,
                    tint = cat.accent,
                    size = 16.dp
                )
                Text(
                    text = "One quirky fact to get you curious",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = cat.accent
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = teaser ?: "Loading topic…",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                softWrap = true,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Action prompt card ("{verb} {target}" + instruction)
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun ActionPromptCard(
    cat: com.curio.app.data.CurioCategory,
    action: com.curio.app.data.ExploreAction,
    subtype: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = cat.accent.copy(alpha = 0.08f),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.6f)
                ) {
                    CurioIcon(
                        name = verbIcon(action.verb),
                        contentDescription = null,
                        tint = cat.accent,
                        size = 18.dp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${action.verb} ${action.targetName}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = cat.accent,
                        maxLines = 2,
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtype.isNotBlank()) {
                        Text(
                            text = subtype,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = action.instruction,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                softWrap = true,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Map exploreAction verb to a Material Symbols glyph (no emoji). */
private fun verbIcon(verb: String): String = when (verb.lowercase().trim()) {
    "listen" -> "headphones"
    "watch" -> "play_arrow"
    "read" -> "menu_book"
    "look at", "look", "view" -> "image"
    "explore" -> "explore"
    "read about", "think about" -> "auto_awesome"
    "research" -> "search"
    "cook" -> "restaurant"
    "build" -> "construction"
    "write" -> "edit"
    "play" -> "play_arrow"
    else -> "auto_awesome"
}
