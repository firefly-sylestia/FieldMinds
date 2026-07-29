package com.curio.app.features.spin

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
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
import com.curio.app.data.MusicGenre
import com.curio.app.data.TopicCatalog
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.ConfettiBurst
import com.curio.app.ui.components.CurioBackButton
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
 * icon's stacked-card design. For the Music category, a horizontal chip
 * row above the deck lets the user pick a [MusicGenre] filter — the
 * shuffle then picks from that genre's pool (per user directive: "for
 * music add genre based with multiple choice system for the spin and it
 * should use those category or genre to show a random one").
 *
 *   Back card      : scaled 0.86, offset Y +60dp, rotated -6°, alpha 0.72
 *   Middle card    : scaled 0.93, offset Y +28dp, rotated +3°, alpha 0.86
 *   Front card     : scaled 1.00, offset Y 0,     rotation 0°,  alpha 1.0
 *                    (reveals chosen topic + category tag on landing)
 */
@Composable
fun SpinScreen(categorySlug: String?, navController: NavController) {
    val cat = remember(categorySlug) {
        val resolved = categorySlug?.let { CurioCategories.byRouteSlug(it) }
            ?: CurioCategories.byId(CategoryId.WILDCARD)
        resolved
    }

    // ── Genre filter (Music only — other categories ignore it).
    //    null = "All Genres" (no filter); any other value narrows the pool.
    var selectedGenre by remember { mutableStateOf<MusicGenre?>(null) }

    // ── Shuffle state ─────────────────────────────────────────────────────
    var shuffling by remember { mutableStateOf(false) }
    var shuffleCount by remember { mutableIntStateOf(0) }
    var confettiTrigger by remember { mutableIntStateOf(0) }
    var landedTopic by remember { mutableStateOf<CurioTopic?>(null) }

    val shuffleProgress = remember(shuffleCount) { Animatable(0f) }

    LaunchedEffect(shuffleCount) {
        if (shuffleCount == 0) return@LaunchedEffect
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
        landedTopic = pickRandomTopic(cat.id, selectedGenre)
        confettiTrigger++
    }

    LaunchedEffect(confettiTrigger) {
        if (confettiTrigger == 0) return@LaunchedEffect
        delay(CurioMotion.Durations.RevealHold.toLong())
        val topic = landedTopic ?: pickRandomTopic(cat.id, selectedGenre)
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
        // ── Top bar ────────────────────────────────────────────────────────
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

        // ── Genre chips (Music only) ──────────────────────────────────────
        if (cat.id == CategoryId.MUSIC) {
            GenreChipRow(
                selected = selectedGenre,
                accent = cat.accent,
                onSelect = { newGenre ->
                    // Don't change mid-shuffle; otherwise, snap the deck
                    // to a clean state for the new genre.
                    if (!shuffling) {
                        selectedGenre = newGenre
                        landedTopic = null
                        shuffleCount++  // re-trigger a fresh shuffle
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
                // ── Card stack (shuffle visual) ────────────────────────────
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
                        onTap = { if (!shuffling) shuffleCount++ }
                    )
                }

                // ── Helper text ────────────────────────────────────────────
                Text(
                    text = when {
                        shuffling -> "Shuffling…"
                        landedTopic != null -> "Here's your pick"
                        else -> "Tap to shuffle"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // ── SHUFFLE button ─────────────────────────────────────────
                Button(
                    onClick = { if (!shuffling) shuffleCount++ },
                    enabled = !shuffling,
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
            color = cat.accent,
            trigger = confettiTrigger,
            modifier = Modifier.fillMaxSize(),
            onComplete = { /* navigation handled by confettiTrigger LaunchedEffect */ }
        )
    }
}

/**
 * Horizontally-scrollable genre chip row — visible only for Music.
 *
 * The first chip is "All Genres" (clears the filter, represented by
 * `null` in [selectedGenre]). The rest are the 9 [MusicGenre.all]
 * genres in declared order. Selecting a chip filters the shuffle pool
 * to that genre. Tapping a chip also re-triggers a fresh shuffle so the
 * user immediately sees the effect of their filter choice (rather than
 * having to tap Shuffle again).
 */
@Composable
private fun GenreChipRow(
    selected: MusicGenre?,
    accent: Color,
    onSelect: (MusicGenre?) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "All" comes first (the "no filter" state, represented by null).
        item("all") {
            GenreChip(
                label = "All Genres",
                glyph = CurioIcons.AutoAwesome,
                accent = accent,
                selected = selected == null,
                onClick = { onSelect(null) }
            )
        }
        items(MusicGenre.all) { genre ->
            GenreChip(
                label = genre.displayName,
                glyph = genre.glyph,
                accent = accent,
                selected = selected == genre,
                onClick = { onSelect(genre) }
            )
        }
    }
}

@Composable
private fun GenreChip(
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

/**
 * Pick a random topic from the active pool.
 *
 * For Music, applies the current [MusicGenre] filter (null = no filter,
 * i.e. "All Genres"). For other categories, returns the canonical pool.
 * Falls back to the Wildcard pool (then the full Music pool) if the
 * chosen category pool is somehow empty — defensive, not expected in
 * practice.
 */
private fun pickRandomTopic(categoryId: CategoryId, genre: MusicGenre?): CurioTopic {
    val pool: List<CurioTopic> = when (categoryId) {
        CategoryId.MUSIC -> genre?.let { TopicCatalog.musicPoolByGenre[it] }
            ?: TopicCatalog.musicPoolAll
        CategoryId.WILDCARD -> TopicCatalog.wildcardPool
        else -> TopicCatalog.poolFor(categoryId)
    }
    val source = pool.ifEmpty { TopicCatalog.wildcardPool }
        .ifEmpty { TopicCatalog.musicPoolAll }
    return source.random()
}

/**
 * The 3-card shuffle stack — matches the launcher icon's deck-of-cards
 * metaphor (see `app/src/main/res/drawable/ic_launcher_foreground.xml`
 * and `curio-icon.svg`).
 *
 * Each card is rendered with `graphicsLayer` so translation / rotation /
 * scale all animate on the GPU. During a shuffle (progress > 0), each
 * card oscillates with its own phase offset so the deck feels like
 * cards cycling past each other rather than swaying in unison.
 */
@Composable
private fun ShuffleStack(
    accent: Color,
    tint: Color,
    glyph: String,
    progress: Float,
    shuffling: Boolean,
    landedTopic: CurioTopic?,
    onTap: () -> Unit
) {
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
                    scaleX = if (landedTopic != null) 1.04f else 1f
                    scaleY = if (landedTopic != null) 1.04f else 1f
                    alpha = 1f
                }
        ) {
            if (landedTopic != null) {
                LandedCard(accent = accent, glyph = glyph, topic = landedTopic)
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

/**
 * The "back" of a card — solid color block with the category glyph
 * centered. Used for back + middle card slots, and as the idle state
 * of the front card (while shuffling).
 */
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

/**
 * The "front" of a card when the shuffle has landed — mirrors the
 * launcher icon's front card design: cream-white surface with a coral
 * title bar (the topic name) + deep-plum subtitle bar (the category
 * subtype, e.g. "Album" / "Artist" / "Book") + a sparkle + the
 * category glyph on the image area.
 */
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
            // ── Image area (butter yellow block + glyph + sparkle) ─────
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
                // Sparkle — top-right corner, matches launcher icon motif
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
                // ── Topic name (coral title bar) ─────────────────────────
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
                // ── Subtitle bar (deep plum line) ────────────────────────
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