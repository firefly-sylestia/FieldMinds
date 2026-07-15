package fieldmind.research.app.features.field.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import kotlin.math.cos
import kotlin.math.sin

/**
 * Variants of the animated empty scene.
 * Each evokes a different natural setting to match different empty state contexts.
 */
enum class EmptySceneVariant {
    /** Sunny meadow with a butterfly, grass blades, and floating seeds */
    MEADOW,
    /** Twilight garden with a ladybug, leaves, and glowing fireflies */
    GARDEN,
    /** Starry night with twinkling stars, a crescent moon, and fireflies */
    NIGHT,
    /** Seaside with gentle waves, a seagull, and shore grass */
    SEASIDE
}

/**
 * A small Canvas-rendered animated nature scene for empty state cards.
 *
 * Draws a delightful miniature landscape with ambient motion:
 * - Swaying grass / plant stems at the bottom
 * - A gentle floating bug creature (butterfly, ladybug, firefly, seagull)
 * - Subtle ambient particles (sparkles, seeds, stars, fireflies)
 * - Varies by [variant] to match different contexts
 *
 * The scene is purely decorative (no touch handling) and renders at
 * a fixed height (default 120dp) within the card.
 */
@Composable
fun AnimatedEmptyScene(
    variant: EmptySceneVariant = EmptySceneVariant.MEADOW,
    accentColor: Color = Color(0xFF4CAF50),
    modifier: Modifier = Modifier,
    height: Dp = 120.dp
) {
    // ── Infinite animation transitions ──
    val infiniteTransition = rememberInfiniteTransition(label = "emptyScene")

    // Grass sway: slow oscillation (0..1)
    val grassPhase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(3000, easing = LinearEasing),
            RepeatMode.Reverse
        ),
        label = "grassSway"
    )

    // Butterfly/firefly float: smooth figure-8 path ~4s cycle
    val bugProgress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(4000, easing = LinearEasing),
            RepeatMode.Reverse
        ),
        label = "bugFloat"
    )

    // Sparkle/glow pulse
    val sparkleGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(1200, easing = LinearEasing),
            RepeatMode.Reverse
        ),
        label = "sparkleGlow"
    )

    // Seagull wing flap
    val wingPhase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(600, easing = LinearEasing),
            RepeatMode.Reverse
        ),
        label = "wingFlap"
    )

    val isDark = isSystemInDarkTheme() || FieldMindTheme.colors.isDark
    // Significantly boost alpha in dark mode so the scene stays visible against dark surfaces
    val alphaBoost = if (isDark) 2.8f else 1f
    // In dark mode use warmer, brighter accent variants for contrast
    val darkAccent = if (isDark) {
        // Lighten the accent for dark backgrounds — use pastel variants
        Color(
            ((accentColor.red * 0.6f + 1f * 0.4f)).coerceIn(0f, 1f),
            ((accentColor.green * 0.6f + 1f * 0.4f)).coerceIn(0f, 1f),
            ((accentColor.blue * 0.6f + 1f * 0.4f)).coerceIn(0f, 1f),
            accentColor.alpha
        )
    } else accentColor
    // Use theme-aware surface tints for background elements so they adapt to light/dark
    val surfaceTint = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val groundY = h * 0.85f
        val grassColor = accentColor.copy(alpha = (0.45f * alphaBoost).coerceAtMost(1f))
        val accentLight = accentColor.copy(alpha = (0.35f * alphaBoost).coerceAtMost(1f))

        when (variant) {
            EmptySceneVariant.MEADOW -> drawMeadow(
                w, h, cx, groundY, grassPhase, bugProgress, sparkleGlow,
                grassColor, darkAccent, accentLight, surfaceTint, isDark, alphaBoost
            )
            EmptySceneVariant.GARDEN -> drawGarden(
                w, h, cx, groundY, grassPhase, bugProgress, sparkleGlow,
                darkAccent, accentLight, surfaceTint, isDark, alphaBoost
            )
            EmptySceneVariant.NIGHT -> drawNight(
                w, h, cx, bugProgress, sparkleGlow, darkAccent, surfaceTint, isDark, alphaBoost
            )
            EmptySceneVariant.SEASIDE -> drawSeaside(
                w, h, cx, groundY, grassPhase, bugProgress, wingPhase, sparkleGlow,
                darkAccent, accentLight, surfaceTint, isDark, alphaBoost
            )
        }
    }
}

// ── Scene drawing functions ──

private fun DrawScope.drawMeadow(
    w: Float, h: Float, cx: Float, groundY: Float,
    grassPhase: Float, bugProgress: Float, glow: Float,
    grassColor: Color, accentColor: Color, accentLight: Color,
    surfaceTint: Color, isDark: Boolean, alphaBoost: Float
) {
    // ── Ground line ──
    drawLine(grassColor, Offset(0f, groundY), Offset(w, groundY), strokeWidth = 2f)

    // ── Swaying grass blades ──
    val bladeCount = 12
    for (i in 0 until bladeCount) {
        val x = (w * 0.08f + i * w * 0.075f).coerceAtMost(w * 0.92f)
        val sway = sin(grassPhase * 6.28f + i * 0.8f) * 6f
        val bladeHeight = 14f + (i % 3) * 8f
        drawLine(
            grassColor.copy(alpha = 0.5f + (i % 3) * 0.15f),
            Offset(x, groundY),
            Offset(x + sway, groundY - bladeHeight),
            strokeWidth = 2.5f - (i % 3) * 0.5f
        )
        // Small leaf
        if (i % 2 == 0) {
            drawCircle(
                accentLight.copy(alpha = 0.3f),
                radius = 3f,
                center = Offset(x + sway + 4f, groundY - bladeHeight * 0.6f)
            )
        }
    }

    // ── Small flowers at base ──
    val flowerColors = listOf(
        Color(0xFFFFF176), Color(0xFFCE93D8),
        Color(0xFFFFAB91), Color(0xFFA5D6A7)
    )
    for (i in 0..3) {
        val fx = w * 0.15f + i * w * 0.22f
        drawCircle(
            flowerColors[i % flowerColors.size].copy(alpha = (0.55f * alphaBoost).coerceAtMost(1f)),
            radius = 4f,
            center = Offset(fx, groundY - 2f)
        )
        drawCircle(
            flowerColors[i % flowerColors.size].copy(alpha = (0.35f * alphaBoost).coerceAtMost(1f)),
            radius = 2f,
            center = Offset(fx - 3f, groundY - 3f)
        )
    }

    // ── Butterfly ──
    val bugX = cx + sin(bugProgress * 6.28f) * w * 0.3f
    val bugY = groundY - 35f + cos(bugProgress * 6.28f * 1.5f) * 10f
    drawButterfly(bugX, bugY, bugProgress, accentColor, alphaBoost)

    // ── Floating seeds (ambient particles) ──
    val seedBaseX = (bugProgress * 2f % 1f) * w
    val seedBaseY = groundY - 20f - bugProgress * 30f + 15f
    for (i in 0..2) {
        val sx = seedBaseX + sin(bugProgress * 3f + i * 2f) * 20f + i * 40f
        val sy = seedBaseY + cos(bugProgress * 2f + i * 1.5f) * 10f - i * 8f
        drawCircle(
            accentLight.copy(alpha = (0.45f * glow * alphaBoost).coerceAtMost(1f)),
            radius = 2.5f,
            center = Offset(sx % w, sy.coerceIn(10f, groundY - 10f))
        )
    }
}

private fun DrawScope.drawGarden(
    w: Float, h: Float, cx: Float, groundY: Float,
    grassPhase: Float, bugProgress: Float, glow: Float,
    accentColor: Color, accentLight: Color,
    surfaceTint: Color, isDark: Boolean, alphaBoost: Float
) {
    // ── Ground with deeper soil color ──
    val earthColor = if (isDark) surfaceTint.copy(alpha = 0.25f) else Color(0xFF795548).copy(alpha = 0.2f)
    drawLine(earthColor, Offset(0f, groundY), Offset(w, groundY), strokeWidth = 3f)

    // ── Broad leaves ──
    val leafColor = accentColor.copy(alpha = (0.45f * alphaBoost).coerceAtMost(1f))
    for (i in 0..5) {
        val lx = w * 0.08f + i * w * 0.16f
        val sway = sin(grassPhase * 5f + i * 1.1f) * 4f
        val leafH = 18f + (i % 3) * 6f
        // Leaf stem
        drawLine(leafColor, Offset(lx, groundY), Offset(lx + sway, groundY - leafH), strokeWidth = 2f)
        // Leaf blob
        drawCircle(
            leafColor.copy(alpha = (0.35f * alphaBoost).coerceAtMost(1f)),
            radius = 5f,
            center = Offset(lx + sway + 3f, groundY - leafH * 0.5f)
        )
    }

    // ── Ladybug ──
    val bugX = cx + sin(bugProgress * 5f) * w * 0.25f
    val bugY = groundY - 30f + cos(bugProgress * 4f) * 8f
    drawLadybug(bugX, bugY, bugProgress, Color(0xFFE53935), alphaBoost)

    // ── Glowing fireflies ──
    val fireflyColors = listOf(Color(0xFFFFF176), Color(0xFF80D8FF), Color(0xFFA5D6A7))
    for (i in 0..4) {
        val fx = (bugProgress * 1.7f + i * 0.2f) % 1f * w
        val fy = groundY * 0.3f + sin(bugProgress * 3f + i * 1.2f) * groundY * 0.2f
        val alpha = (glow * 0.6f + 0.2f)
        drawCircle(
            fireflyColors[i % fireflyColors.size].copy(alpha = (alpha * 0.45f * alphaBoost).coerceAtMost(1f)),
            radius = 6f,
            center = Offset(fx, fy)
        )
        drawCircle(
            fireflyColors[i % fireflyColors.size].copy(alpha = (alpha * 0.95f * alphaBoost).coerceAtMost(1f)),
            radius = 2.5f,
            center = Offset(fx, fy)
        )
    }
}

private fun DrawScope.drawNight(
    w: Float, h: Float, cx: Float,
    bugProgress: Float, glow: Float,
    accentColor: Color, surfaceTint: Color, isDark: Boolean, alphaBoost: Float
) {
    val baseAlpha = (0.55f * alphaBoost).coerceAtMost(1f)
    val starColor = if (isDark) Color(0xFFFFF9C4) else Color(0xFFFFB300)

    // ── Crescent moon — much brighter in dark mode
    val moonX = w * 0.8f
    val moonY = h * 0.18f
    val moonColor = if (isDark) Color(0xFFFFFDE7) else starColor
    drawCircle(
        moonColor.copy(alpha = baseAlpha * 1.8f * glow),
        radius = 12f,
        center = Offset(moonX, moonY)
    )
    drawCircle(
        starColor.copy(alpha = baseAlpha * 1.2f),
        radius = 8f,
        center = Offset(moonX + 4f, moonY - 2f)
    )

    // ── Stars — brighter with larger glow halos in dark mode
    val starPositions = listOf(
        Pair(0.1f, 0.15f), Pair(0.25f, 0.08f), Pair(0.4f, 0.2f),
        Pair(0.55f, 0.1f), Pair(0.7f, 0.25f), Pair(0.15f, 0.35f),
        Pair(0.35f, 0.3f), Pair(0.6f, 0.35f)
    )
    starPositions.forEachIndexed { i, (sx, sy) ->
        val starGlow = (glow * 0.6f + 0.4f + (i % 2) * 0.3f).coerceIn(0f, 1f)
        drawCircle(
            starColor.copy(alpha = starGlow * baseAlpha * 2.0f),
            radius = 2.5f,
            center = Offset(w * sx, h * sy)
        )
        // Cross glow for brighter stars — larger halo in dark mode
        if (i % 3 == 0) {
            drawCircle(
                starColor.copy(alpha = starGlow * baseAlpha * 0.8f),
                radius = if (isDark) 7f else 5f,
                center = Offset(w * sx, h * sy)
            )
        }
    }

    // ── Fireflies — brighter in dark mode
    for (i in 0..3) {
        val fx = (bugProgress * 2.3f + i * 0.25f) % 1f * w
        val fy = h * 0.5f + sin(bugProgress * 2f + i * 1.5f) * h * 0.15f
        drawCircle(
            starColor.copy(alpha = (glow * 0.55f * alphaBoost).coerceAtMost(1f)),
            radius = 5f,
            center = Offset(fx, fy)
        )
        drawCircle(
            starColor.copy(alpha = (glow * 0.95f * alphaBoost).coerceAtMost(1f)),
            radius = 2.5f,
            center = Offset(fx, fy)
        )
    }

    // ── Ground silhouette ──
    val groundColor = if (isDark) surfaceTint.copy(alpha = 0.25f) else Color(0xFF2E7D32).copy(alpha = baseAlpha * 0.35f)
    for (i in 0..8) {
        val gx = i * w * 0.12f
        val gh = 6f + sin(i * 1.3f) * 4f
        drawLine(groundColor, Offset(gx, h), Offset(gx, h - gh), strokeWidth = 2f)
    }
}

private fun DrawScope.drawSeaside(
    w: Float, h: Float, cx: Float, groundY: Float,
    grassPhase: Float, bugProgress: Float, wingPhase: Float, glow: Float,
    accentColor: Color, accentLight: Color,
    surfaceTint: Color, isDark: Boolean, alphaBoost: Float
) {
    // ── Gentle waves ──
    val waveColor = if (isDark) accentColor.copy(alpha = (0.35f * alphaBoost).coerceAtMost(1f)) else Color(0xFF42A5F5).copy(alpha = 0.25f)
    for (wave in 0..2) {
        val wy = groundY + 8f + wave * 10f
        val path = Path().apply {
            moveTo(0f, wy)
            for (x in 0..20) {
                val px = x * w / 20f
                val py = wy + sin(grassPhase * 4f + x * 0.3f + wave) * 4f
                lineTo(px, py)
            }
            lineTo(w, wy + 15f)
            lineTo(0f, wy + 15f)
            close()
        }
        drawPath(path, waveColor.copy(alpha = (0.2f / (wave + 1) * alphaBoost).coerceAtMost(1f)))
    }

    // ── Shore grass ──
    val shoreColor = accentColor.copy(alpha = (0.5f * alphaBoost).coerceAtMost(1f))
    for (i in 0..8) {
        val gx = w * 0.05f + i * w * 0.1f
        val sway = sin(grassPhase * 5f + i * 0.9f) * 5f
        drawLine(shoreColor, Offset(gx, groundY), Offset(gx + sway, groundY - 16f), strokeWidth = 2f)
    }

    // ── Seagull ──
    val gullX = cx + sin(bugProgress * 3f) * w * 0.35f
    val gullY = groundY * 0.35f + cos(bugProgress * 2f) * 10f
    val gullColor = if (isDark) surfaceTint else Color.White
    // Body
    drawCircle(gullColor.copy(alpha = (0.75f * alphaBoost).coerceAtMost(1f)), radius = 4f, center = Offset(gullX, gullY))
    // Wings (flapping)
    val wingAngle = sin(wingPhase * 6.28f) * 0.4f
    drawLine(
        gullColor.copy(alpha = (0.6f * alphaBoost).coerceAtMost(1f)),
        Offset(gullX, gullY),
        Offset(gullX - 8f, gullY - 6f + wingAngle * 8f),
        strokeWidth = 2f
    )
    drawLine(
        gullColor.copy(alpha = (0.6f * alphaBoost).coerceAtMost(1f)),
        Offset(gullX, gullY),
        Offset(gullX + 8f, gullY - 6f + wingAngle * 8f),
        strokeWidth = 2f
    )
    // Beak
    drawLine(Color(0xFFFFA726).copy(alpha = (0.6f * alphaBoost).coerceAtMost(1f)), Offset(gullX, gullY), Offset(gullX + 5f, gullY + 1f), strokeWidth = 1.5f)

    // ── Clouds ──
    val cloudProgress = (bugProgress * 0.7f) % 1f
    drawCloud(Offset(cloudProgress * w * 1.2f - 30f, h * 0.08f), glow, alphaBoost, isDark)
    drawCloud(Offset((cloudProgress + 0.4f) * w * 1.2f - 30f, h * 0.18f), glow * 0.7f, alphaBoost, isDark)
}

// ── Helper drawings ──

private fun DrawScope.drawButterfly(x: Float, y: Float, phase: Float, color: Color, alphaBoost: Float) {
    val wingFlap = sin(phase * 8f) * 3f
    val alpha = (0.8f * alphaBoost).coerceAtMost(1f)
    // Left wing
    val pathL = Path().apply {
        moveTo(x, y)
        quadraticTo(x - 8f - wingFlap, y - 6f, x - 3f, y - 2f)
        close()
    }
    drawPath(pathL, color.copy(alpha = alpha), style = Fill)
    drawPath(pathL, color.copy(alpha = alpha * 0.7f), style = Stroke(width = 1f))

    // Right wing
    val pathR = Path().apply {
        moveTo(x, y)
        quadraticTo(x + 8f + wingFlap, y - 6f, x + 3f, y - 2f)
        close()
    }
    drawPath(pathR, color.copy(alpha = alpha), style = Fill)
    drawPath(pathR, color.copy(alpha = alpha * 0.7f), style = Stroke(width = 1f))

    // Body
    drawCircle(color.copy(alpha = alpha), radius = 1.5f, center = Offset(x, y))
}

private fun DrawScope.drawLadybug(x: Float, y: Float, phase: Float, color: Color, alphaBoost: Float) {
    val bounce = sin(phase * 6f) * 2f
    // Body
    drawCircle(color.copy(alpha = (0.85f * alphaBoost).coerceAtMost(1f)), radius = 5f, center = Offset(x, y + bounce))
    // Head
    drawCircle(Color(0xFF212121).copy(alpha = (0.7f * alphaBoost).coerceAtMost(1f)), radius = 2.5f, center = Offset(x - 4f, y + bounce - 1f))
    // Spot
    drawCircle(Color(0xFF212121).copy(alpha = (0.55f * alphaBoost).coerceAtMost(1f)), radius = 1.5f, center = Offset(x + 2f, y + bounce - 2f))
    drawCircle(Color(0xFF212121).copy(alpha = (0.55f * alphaBoost).coerceAtMost(1f)), radius = 1.5f, center = Offset(x - 1f, y + bounce + 3f))
    // Antennae
    drawLine(Color(0xFF212121).copy(alpha = (0.55f * alphaBoost).coerceAtMost(1f)), Offset(x - 5.5f, y + bounce - 3f), Offset(x - 8f, y + bounce - 6f), strokeWidth = 1f)
    drawLine(Color(0xFF212121).copy(alpha = (0.55f * alphaBoost).coerceAtMost(1f)), Offset(x - 4.5f, y + bounce - 3f), Offset(x - 3f, y + bounce - 7f), strokeWidth = 1f)
}

private fun DrawScope.drawCloud(offset: Offset, glow: Float, alphaBoost: Float, isDark: Boolean) {
    val cloudColor = if (isDark) Color(0xFFE0E0E0) else Color.White
    val baseAlpha = 0.2f * glow * alphaBoost
    drawCircle(cloudColor.copy(alpha = baseAlpha), radius = 8f, center = offset)
    drawCircle(cloudColor.copy(alpha = baseAlpha), radius = 10f, center = Offset(offset.x + 8f, offset.y - 2f))
    drawCircle(cloudColor.copy(alpha = baseAlpha), radius = 7f, center = Offset(offset.x + 16f, offset.y + 1f))
    drawCircle(cloudColor.copy(alpha = baseAlpha), radius = 6f, center = Offset(offset.x + 22f, offset.y + 2f))
}

/**
 * Friendly, personality-filled copy text for different empty state contexts.
 * Returns a [Copy] with a warm title, friendly body, and helpful tip.
 */
object EmptyStateCopy {
    data class Copy(val title: String, val body: String, val tip: String = "")

    fun forObservations(): Copy = Copy(
        title = "Your field notebook is waiting 📓",
        body = "No observations yet — but every discovery starts with a single step outside. Tap the + button and begin your first entry!",
        tip = "Try logging a bird, plant, or weather pattern you notice today."
    )

    fun forNotes(): Copy = Copy(
        title = "A blank canvas of ideas 🎨",
        body = "Notes are where thoughts take shape. Capture a quick idea, a field sketch, or a question that popped into your head.",
        tip = "Even a one-liner is a great start. You can always come back to expand!"
    )

    fun forSources(): Copy = Copy(
        title = "Build your knowledge library 📚",
        body = "Import articles, book references, or web links to build a personal research library. Each source adds depth to your findings.",
        tip = "Try importing a Wikipedia link or a research paper you've been reading."
    )

    fun forFlashcards(): Copy = Copy(
        title = "Your memory palace awaits 🧠",
        body = "Flashcards turn what you've learned into lasting knowledge. Create your first card from an observation, a species, or a concept.",
        tip = "Start with a common species you want to remember — make a card for it!"
    )

    fun forSearch(query: String = ""): Copy = Copy(
        title = "Search your field notes 🔍",
        body = if (query.isNotBlank()) "No results for \"$query\" — try a different word or browse your observations instead."
            else "Type what you're looking for — observations, species, notes, or anything in your research.",
        tip = "Try searching for a species name, location, or a keyword from your notes."
    )

    fun forData(): Copy = Copy(
        title = "Data starts with observation 📊",
        body = "Use the data tools to log measurements, counts, weather conditions, and more. Numbers tell a story!",
        tip = "Try the Counter tool for quick tallies or the Weather Log for conditions."
    )

    fun forMap(): Copy = Copy(
        title = "Explore your world 🌍",
        body = "Observations with GPS location appear here on the map. Start capturing with location enabled to build your personal field map.",
        tip = "Enable location permission to auto-tag where each observation was made."
    )

    fun forProjects(): Copy = Copy(
        title = "Organize your research 📁",
        body = "Projects group your observations, questions, and sources into focused studies. Create your first project to bring everything together.",
        tip = "Start with a small project — like 'Birds in my backyard' or 'Spring wildflowers'."
    )

    /** Pick a context-appropriate scene variant for the given context string. */
    fun sceneFor(context: String): EmptySceneVariant = when {
        context in listOf("observations", "observe", "capture", "camera", "log") -> EmptySceneVariant.MEADOW
        context in listOf("notes", "note", "journal", "canvas") -> EmptySceneVariant.GARDEN
        context in listOf("sources", "source", "library", "reading", "book") -> EmptySceneVariant.NIGHT
        context in listOf("flashcards", "learn", "review", "study") -> EmptySceneVariant.GARDEN
        context in listOf("search", "archive", "find") -> EmptySceneVariant.NIGHT
        context in listOf("data", "tools", "counter", "measure") -> EmptySceneVariant.SEASIDE
        context in listOf("map", "location", "field") -> EmptySceneVariant.SEASIDE
        context in listOf("projects", "project") -> EmptySceneVariant.GARDEN
        else -> EmptySceneVariant.MEADOW
    }

    /** Accent color matching the scene/context. */
    fun accentFor(context: String): Color = when {
        context in listOf("observations", "observe", "capture", "log") -> Color(0xFF4CAF50)
        context in listOf("notes", "note", "journal", "canvas") -> Color(0xFFAB47BC)
        context in listOf("sources", "source", "library", "reading", "book") -> Color(0xFF5C6BC0)
        context in listOf("flashcards", "learn", "review", "study") -> Color(0xFFEC407A)
        context in listOf("search", "archive", "find") -> Color(0xFF26A69A)
        context in listOf("data", "tools", "counter", "measure") -> Color(0xFFFF7043)
        context in listOf("map", "location", "field") -> Color(0xFF42A5F5)
        context in listOf("projects", "project") -> Color(0xFF66BB6A)
        else -> Color(0xFF78909C)
    }
}
