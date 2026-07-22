package fieldmind.research.app.features.field.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import kotlin.math.cos
import kotlin.math.sin

/**
 * Variants of the animated empty scene. Each uses clean floating geometry
 * instead of literal nature illustrations — works beautifully in light & dark mode.
 */
enum class EmptySceneVariant {
    /** Warm floating circles and diamonds drifting organically */
    MEADOW,
    /** Layered rounded shapes with soft pulse */
    GARDEN,
    /** Twinkling dots with a glowing moon */
    NIGHT,
    /** Gentle horizontal wave lines with drifting elements */
    SEASIDE
}

/**
 * A minimal Canvas-rendered animated scene for empty state cards.
 *
 * Uses clean floating geometry (circles, rounded diamonds, wave lines)
 * with ambient organic drift animations. No complex path drawings —
 * just simple, elegant shapes that render beautifully in both themes.
 *
 * The scene is purely decorative and renders at a fixed height (default 120dp).
 */
@Composable
fun AnimatedEmptyScene(
    variant: EmptySceneVariant = EmptySceneVariant.MEADOW,
    accentColor: Color = Color(0xFF4CAF50),
    modifier: Modifier = Modifier,
    height: Dp = 120.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "emptyScene")

    // ── Single unified drift phase (4-7 second cycle) ──
    val drift by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing), RepeatMode.Reverse),
        label = "drift"
    )
    // ── Secondary phase offset for variety ──
    val drift2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3800, easing = LinearEasing), RepeatMode.Reverse),
        label = "drift2"
    )
    // ── Glow pulse ──
    val glow by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse),
        label = "glow"
    )

    val isDark = FieldMindTheme.colors.isDark
    // Lighten accent for dark mode readability, use full accent in light mode
    val sceneAccent = if (isDark) {
        Color(
            (accentColor.red * 0.55f + 0.45f).coerceIn(0f, 1f),
            (accentColor.green * 0.55f + 0.45f).coerceIn(0f, 1f),
            (accentColor.blue * 0.55f + 0.45f).coerceIn(0f, 1f),
            accentColor.alpha
        )
    } else accentColor

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val w = size.width
        val h = size.height

        when (variant) {
            EmptySceneVariant.MEADOW -> drawMeadowGeometric(w, h, drift, drift2, glow, sceneAccent, isDark)
            EmptySceneVariant.GARDEN -> drawGardenGeometric(w, h, drift, glow, sceneAccent)
            EmptySceneVariant.NIGHT -> drawNightGeometric(w, h, drift, drift2, glow, sceneAccent, isDark)
            EmptySceneVariant.SEASIDE -> drawSeasideGeometric(w, h, drift, glow, sceneAccent, isDark)
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  MEADOW — Warm floating circles & diamonds
// ══════════════════════════════════════════════════════════════════════

private fun DrawScope.drawMeadowGeometric(
    w: Float, h: Float, drift: Float, drift2: Float, glow: Float,
    accent: Color, isDark: Boolean
) {
    val baseAlpha = if (isDark) 0.55f else 0.40f
    val cx = w / 2f
    val cy = h / 2f

    // ── Soft ground glow ──
    drawCircle(
        accent.copy(alpha = 0.08f * glow),
        radius = w * 0.40f,
        center = Offset(cx, h * 0.82f)
    )

    // ── Floating circles (5 elements at different positions) ──
    val elements = listOf(
        Triple(0.18f, 0.50f, 0.75f),  // xFrac, yFrac, sizeFrac
        Triple(0.38f, 0.32f, 0.55f),
        Triple(0.62f, 0.44f, 0.65f),
        Triple(0.78f, 0.28f, 0.50f),
        Triple(0.50f, 0.60f, 0.70f)
    )
    elements.forEachIndexed { i, (xFrac, yFrac, sizeFrac) ->
        val ox = sin(drift * 6.28f + i * 1.3f) * w * 0.06f
        val oy = cos(drift2 * 6.28f + i * 0.9f) * h * 0.04f
        val alpha = (baseAlpha * (0.6f + 0.4f * glow)).coerceIn(0f, 1f)
        drawCircle(
            accent.copy(alpha = alpha),
            radius = 5f + sizeFrac * 6f,
            center = Offset(w * xFrac + ox, h * yFrac + oy)
        )
    }

    // ── Small diamonds ──
    for (i in 0..2) {
        val dx = w * (0.25f + i * 0.25f) + sin(drift2 * 5f + i * 2f) * w * 0.04f
        val dy = h * (0.55f + cos(drift * 4f + i) * 0.15f)
        val diamondPath = Path().apply {
            moveTo(dx, dy - 5f)
            lineTo(dx + 4f, dy)
            lineTo(dx, dy + 5f)
            lineTo(dx - 4f, dy)
            close()
        }
        drawPath(diamondPath, accent.copy(alpha = baseAlpha * 0.7f * glow))
    }
}

// ══════════════════════════════════════════════════════════════════════
//  GARDEN — Layered rounded shapes with soft pulse
// ══════════════════════════════════════════════════════════════════════

private fun DrawScope.drawGardenGeometric(
    w: Float, h: Float, drift: Float, glow: Float,
    accent: Color
) {
    val cx = w / 2f
    val cy = h / 2f

    // ── Large soft background circle ──
    drawCircle(
        accent.copy(alpha = 0.06f),
        radius = w * 0.38f,
        center = Offset(cx, cy)
    )

    // ── Medium pulsing circle ──
    val pulseRadius = (28f + glow * 8f)
    drawCircle(
        accent.copy(alpha = 0.14f),
        radius = pulseRadius,
        center = Offset(cx, cy)
    )

    // ── Small orbiting circles ──
    for (i in 0..4) {
        val angle = drift * 6.28f + i * 1.256f
        val orbitR = 35f + i * 5f
        val ox = cx + cos(angle) * orbitR
        val oy = cy + sin(angle) * orbitR * 0.6f
        drawCircle(
            accent.copy(alpha = 0.22f + 0.12f * glow),
            radius = 3f + i * 1f,
            center = Offset(ox, oy)
        )
    }

    // ── Petal-like accent dots ──
    for (i in 0..6) {
        val angle = drift * 5f + i * 0.897f
        val px = cx + cos(angle) * (50f + i % 2 * 15f)
        val py = cy + sin(angle) * (50f + i % 2 * 15f) * 0.5f
        drawCircle(
            accent.copy(alpha = 0.18f * glow),
            radius = 2.5f + i % 3 * 1.5f,
            center = Offset(px, py)
        )
    }
}

// ══════════════════════════════════════════════════════════════════════
//  NIGHT — Twinkling dots with glowing moon
// ══════════════════════════════════════════════════════════════════════

private fun DrawScope.drawNightGeometric(
    w: Float, h: Float, drift: Float, drift2: Float, glow: Float,
    accent: Color, isDark: Boolean
) {
    val baseAlpha = if (isDark) 0.60f else 0.45f
    val starColor = accent

    // ── Glowing moon (top-right quadrant) ──
    val moonX = w * 0.78f
    val moonY = h * 0.22f
    drawCircle(
        starColor.copy(alpha = baseAlpha * 0.25f * glow),
        radius = 18f,
        center = Offset(moonX, moonY)
    )
    drawCircle(
        starColor.copy(alpha = baseAlpha * 0.40f),
        radius = 10f,
        center = Offset(moonX + 2f, moonY - 1f)
    )

    // ── Twinkling stars (12 dots at fixed positions) ──
    val stars = listOf(
        0.10f to 0.12f, 0.22f to 0.08f, 0.40f to 0.18f,
        0.55f to 0.10f, 0.68f to 0.22f, 0.15f to 0.32f,
        0.35f to 0.28f, 0.58f to 0.32f, 0.85f to 0.14f,
        0.48f to 0.48f, 0.72f to 0.45f, 0.92f to 0.38f
    )
    stars.forEachIndexed { i, (sx, sy) ->
        val twinkle = (glow * 0.5f + 0.5f + (i % 3) * 0.25f).coerceIn(0f, 1f)
        val phaseDrift = sin(drift * 4f + i * 0.8f) * 0.3f
        drawCircle(
            starColor.copy(alpha = twinkle * baseAlpha * (0.7f + phaseDrift)),
            radius = 1.5f + (i % 2) * 1f,
            center = Offset(w * sx, h * sy)
        )
    }

    // ── Ground glow ──
    drawCircle(
        starColor.copy(alpha = 0.06f * glow),
        radius = w * 0.45f,
        center = Offset(w * 0.5f, h * 0.88f)
    )
}

// ══════════════════════════════════════════════════════════════════════
//  SEASIDE — Gentle horizontal wave lines with drifting elements
// ══════════════════════════════════════════════════════════════════════

private fun DrawScope.drawSeasideGeometric(
    w: Float, h: Float, drift: Float, glow: Float,
    accent: Color, isDark: Boolean
) {
    val baseAlpha = if (isDark) 0.50f else 0.38f
    val groundY = h * 0.82f

    // ── Horizontal wave lines ──
    for (wave in 0..2) {
        val wy = groundY + 6f + wave * 10f
        val path = Path().apply {
            moveTo(0f, wy)
            for (x in 0..20) {
                val px = x * w / 20f
                val py = wy + sin(drift * 6f + x * 0.4f + wave) * 4f
                lineTo(px, py)
            }
        }
        drawPath(
            path,
            accent.copy(alpha = (baseAlpha * (0.5f / (wave + 1))).coerceAtMost(1f)),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
        )
    }

    // ── Floating circles (like clouds/seafoam) ──
    for (i in 0..4) {
        val fx = (drift * w * 1.5f + i * w * 0.22f) % w
        val fy = h * (0.15f + i * 0.10f) + sin(drift * 3f + i) * h * 0.04f
        drawCircle(
            accent.copy(alpha = baseAlpha * 0.55f * glow),
            radius = 4f + i * 1.5f,
            center = Offset(fx, fy)
        )
    }

    // ── Small accent diamonds near ground ──
    for (i in 0..5) {
        val dx = w * (0.08f + i * 0.16f) + sin(drift * 5f + i) * w * 0.03f
        val dy = groundY - 6f - i % 3 * 4f
        val diamondPath = Path().apply {
            moveTo(dx, dy - 3f)
            lineTo(dx + 2.5f, dy)
            lineTo(dx, dy + 3f)
            lineTo(dx - 2.5f, dy)
            close()
        }
        drawPath(diamondPath, accent.copy(alpha = baseAlpha * 0.5f))
    }
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
