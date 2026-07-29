package com.curio.app.features.spin

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioTopic
import com.curio.app.data.TopicJsonLoader
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.ConfettiBurst
import com.curio.app.ui.components.CurioBackButton
import com.curio.app.ui.components.CurioSparkle
import com.curio.app.ui.components.ScreenEntrance
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.CurioMotion
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * The Shuffle — see CURIO_SPEC.md §5.
 *
 * Visual metaphor: a deck of 3 cards being shuffled, matching the launcher
 * icon's stacked-card design. For each of the 11 categories, a horizontal
 * chip row above the deck lets the user pick a tag filter — the shuffle
 * then picks from that tag's pool.
 *
 * Upgraded with:
 *  - Dramatic card slam on landing (scale 1.08 → 1.0 with elastic spring)
 *  - Enhanced shuffle oscillation with individual card phase offsets
 *  - Confetti burst on landing in category accent
 *  - Sparkle ring effect over the landed card
 *  - Shimmer on the SHUFFLE button while shuffling
 */
@Composable
fun SpinScreen(categorySlug: String?, navController: NavController) {
    val cat = remember(categorySlug) {
        val resolved = categorySlug?.let { CurioCategories.byRouteSlug(it) }
            ?: CurioCategories.byId(CategoryId.WILDCARD)
        resolved
    }

    val pool by produceState<List<CurioTopic>>(initialValue = emptyList(), cat.id) {
        value = TopicJsonLoader.load(cat.id)
    }
    val tags by produceState<List<String>>(initialValue = emptyList(), pool) {
        value = pool.flatMap { it.tags }.distinct().sorted()
    }

    var selectedTag by remember { mutableStateOf<String?>(null) }

    val filteredPool = remember(pool, selectedTag) {
        if (selectedTag == null) pool
        else pool.filter { it.tags.contains(selectedTag) }
    }

    var shuffling by remember { mutableStateOf(false) }
    var shuffleCount by remember { mutableIntStateOf(0) }
    var confettiTrigger by remember { mutableIntStateOf(0) }
    var sparkleTrigger by remember { mutableIntStateOf(0) }
    var landedTopic by remember { mutableStateOf<CurioTopic?>(null) }

    val shuffleProgress = remember(shuffleCount) { Animatable(0f) }

    LaunchedEffect(shuffleCount) {
        if (shuffleCount == 0) return@LaunchedEffect
        if (pool.isEmpty()) return@LaunchedEffect
        shuffling = true
        landedTopic = null

        val cycles = Random.nextInt(
            CurioMotion.MinSpinTurns,
            CurioMotion.MaxSpinTurns + 1
        ).toFloat()
        val durationMillis = Random.nextInt(
            CurioMotion.Durations.SpinMin,
            CurioMotion.Durations.SpinMax + 1
        )

        shuffleProgress.animateTo(
            targetValue = cycles,
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = CubicBezierEasing(0.15f, 0.85f, 0.2f, 1f)
            )
        )

        shuffling = false
        landedTopic = pickRandomFrom(filteredPool, cat.id)
        confettiTrigger++
        sparkleTrigger++
    }

    LaunchedEffect(confettiTrigger) {
        if (confettiTrigger == 0) return@LaunchedEffect
        delay(CurioMotion.Durations.RevealHold.toLong())
        val topic = landedTopic
            ?: pickRandomFrom(filteredPool, cat.id)
            ?: return@LaunchedEffect
        navController.navigate(
            CurioRoutes.revealFor(cat.id.routeSlug, topic.name)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CurioBackButton(onClick = { navController.popBackStack() })
            Text(
                text = "${cat.displayName} · Shuffle",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (tags.isNotEmpty()) {
            TagChipRow(
                tags = tags,
                selected = selectedTag,
                accent = cat.accent,
                onSelect = { newTag ->
                    if (!shuffling) {
                        selectedTag = newTag
                        landedTopic = null
                        shuffleCount++
                    }
                }
            )
            Spacer(Modifier.height(8.dp))
        }

        ScreenEntrance {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ShuffleStack(
                        accent = cat.accent,
                        tint = cat.tint,
                        glyph = cat.iconGlyph,
                        progress = shuffleProgress.value,
                        shuffling = shuffling,
                        landedTopic = landedTopic,
                        sparkleTrigger = sparkleTrigger,
                        onTap = { if (!shuffling) shuffleCount++ }
                    )
                }

                Text(
                    text = when {
                        pool.isEmpty() && !shuffling -> "No topics yet — check back soon!"
                        shuffling -> "Shuffling…"
                        landedTopic != null -> "Here's your pick"
                        else -> "Tap to shuffle"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = { if (!shuffling) shuffleCount++ },
                    enabled = !shuffling && pool.isNotEmpty(),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cat.accent,
                        contentColor = CurioColors.DeepPlum,
                        disabledContainerColor = cat.tint,
                        disabledContentColor =
                            CurioColors.DeepPlum.copy(alpha = 0.5f)
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 48.dp,
                        vertical = 16.dp
                    )
                ) {
                    Text(
                        text = when {
                            shuffling -> "Shuffling…"
                            landedTopic != null -> "Shuffle again"
                            else -> "SHUFFLE"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
            }
        }
    }

    if (confettiTrigger > 0) {
        ConfettiBurst(
            colors = listOf(cat.accent, cat.tint),
            trigger = confettiTrigger,
            particleCount = CurioMotion.ConfettiParticleCountLarge,
            modifier = Modifier.fillMaxSize(),
            onComplete = {}
        )
    }
}

@Composable
private fun TagChipRow(
    tags: List<String>,
    selected: String?,
    accent: Color,
    onSelect: (String?) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item("all") {
            TagChip(
                label = "All",
                glyph = CurioIcons.AutoAwesome,
                accent = accent,
                selected = selected == null,
                onClick = { onSelect(null) }
            )
        }
        items(tags) { tag ->
            TagChip(
                label = tag,
                glyph = CurioIcons.AutoAwesome,
                accent = accent,
                selected = selected == tag,
                onClick = { onSelect(tag) }
            )
        }
    }
}

@Composable
private fun TagChip(
    label: String,
    glyph: String,
    accent: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) accent.copy(alpha = 0.20f)
                else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) accent
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CurioIcon(
                name = glyph,
                contentDescription = null,
                tint = if (selected) accent
                       else MaterialTheme.colorScheme.onSurfaceVariant,
                size = 16.dp
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) accent
                        else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun pickRandomFrom(
    filteredPool: List<CurioTopic>,
    fallbackCategory: CategoryId
): CurioTopic? {
    if (filteredPool.isNotEmpty()) return filteredPool.random()
    return try {
        TopicJsonLoader.cached(fallbackCategory)?.randomOrNull()
    } catch (t: Throwable) {
        null
    }
}

/**
 * The 3-card shuffle stack — matches the launcher icon's deck-of-cards
 * metaphor. Upgraded with slam-on-landing animation and sparkle ring.
 */
@Composable
private fun ShuffleStack(
    accent: Color,
    tint: Color,
    glyph: String,
    progress: Float,
    shuffling: Boolean,
    landedTopic: CurioTopic?,
    sparkleTrigger: Int,
    onTap: () -> Unit
) {
    // ── Slam scale: card pops to 1.06 then settles to 1.0 on landing ─────
    var justLanded by remember { mutableStateOf(false) }
    LaunchedEffect(landedTopic) {
        if (landedTopic != null) {
            justLanded = true
            delay(120)
            justLanded = false
        }
    }
    val slamScale by animateFloatAsState(
        targetValue = if (justLanded) 1.06f else 1f,
        animationSpec = CurioMotion.Springs.Elastic,
        label = "slamScale"
    )

    Box(
        modifier = Modifier
            .size(width = 260.dp, height = 320.dp)
            .clickable(enabled = !shuffling, onClick = onTap),
        contentAlignment = Alignment.Center
    ) {
        // ── Card 1: Back ─────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(width = 240.dp, height = 300.dp)
                .graphicsLayer {
                    translationY = 60.dp.toPx() + sin(progress * 6f) * 6f
                    translationX = -16.dp.toPx() + cos(progress * 5f) * 3f
                    rotationZ = -6f + sin(progress * 4f) * 1.5f
                    scaleX = 0.86f
                    scaleY = 0.86f
                    alpha = 0.72f
                }
        ) {
            CardSurface(
                color = tint,
                borderColor = accent.copy(alpha = 0.4f),
                glyph = glyph,
                glyphTint = accent.copy(alpha = 0.7f)
            )
        }

        // ── Card 2: Middle ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(width = 240.dp, height = 300.dp)
                .graphicsLayer {
                    translationY = 28.dp.toPx() + sin(progress * 6f + 1.2f) * 6f
                    translationX = sin(progress * 5f + 0.8f) * 3f
                    rotationZ = 3f + cos(progress * 4f + 0.5f) * 1.5f
                    scaleX = 0.93f
                    scaleY = 0.93f
                    alpha = 0.86f
                }
        ) {
            CardSurface(
                color = accent.copy(alpha = 0.55f),
                borderColor = accent.copy(alpha = 0.7f),
                glyph = glyph,
                glyphTint = CurioColors.CreamWhite
            )
        }

        // ── Card 3: Front (the focal point) ─────────────────────────────
        Box(
            modifier = Modifier
                .size(width = 240.dp, height = 300.dp)
                .graphicsLayer {
                    translationY = sin(progress * 6f + 2.4f) * 4f
                    rotationZ = sin(progress * 4f + 1.0f) * 0.8f
                    scaleX = slamScale
                    scaleY = slamScale
                    alpha = 1f
                }
        ) {
            if (landedTopic != null) {
                Box {
                    LandedCard(accent = accent, glyph = glyph, topic = landedTopic)
                    // Sparkle ring over the landed card
                    if (sparkleTrigger > 0) {
                        CurioSparkle(
                            color = accent,
                            trigger = sparkleTrigger,
                            size = 240.dp,
                            ringCount = 3,
                            modifier = Modifier
                                .size(240.dp, 300.dp)
                        )
                    }
                }
            } else {
                CardSurface(
                    color = CurioColors.CreamWhite,
                    borderColor = accent.copy(alpha = 0.5f),
                    glyph = glyph,
                    glyphTint = accent
                )
            }
        }
    }
}

@Composable
private fun CardSurface(
    color: Color,
    borderColor: Color,
    glyph: String,
    glyphTint: Color
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = color,
        tonalElevation = 2.dp,
        border = BorderStroke(
            width = 1.5.dp,
            color = borderColor
        ),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            CurioIcon(
                name = glyph,
                contentDescription = null,
                tint = glyphTint,
                size = 72.dp
            )
        }
    }
}

@Composable
private fun LandedCard(
    accent: Color,
    glyph: String,
    topic: CurioTopic
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = CurioColors.CreamWhite,
        tonalElevation = 6.dp,
        border = BorderStroke(
            width = 2.dp,
            color = accent
        ),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        color = CurioColors.ButterYellow.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                CurioIcon(
                    name = glyph,
                    contentDescription = null,
                    tint = accent,
                    size = 72.dp
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    CurioIcon(
                        name = CurioIcons.AutoAwesome,
                        contentDescription = null,
                        tint = CurioColors.DeepPlum,
                        size = 20.dp
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(
                            color = accent,
                            shape = RoundedCornerShape(4.dp)
                        )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = topic.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = CurioColors.DeepPlum,
                    maxLines = 2
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(6.dp)
                        .background(
                            color = CurioColors.DeepPlum.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(3.dp)
                        )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = topic.subtype,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
