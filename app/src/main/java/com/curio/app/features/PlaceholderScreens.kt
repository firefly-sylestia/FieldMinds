package com.curio.app.features

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons

/**
 * Universal scaffold for placeholder-phase screens. Each placeholder has the
 * same shape:
 *
 *   ← back arrow       (glyph: arrow_back)
 *
 *   ┌──────────────────┐
 *   │  [ category or   │
 *   │    feature       │   large Material Symbols glyph in category accent
 *   │    glyph ]       │
 *   │                  │
 *   │  Screen Title    │
 *   │  (geom, headline)│
 *   │                  │
 *   │  Subtitle:       │
 *   │  "Coming soon..."│
 *   └──────────────────┘
 *
 * When the logic for each screen lands in a later phase, this generic
 * scaffold gets replaced with the feature-specific implementation. Keeping
 * a single placeholder file means the placeholder phase stays readable
 * (one file = all stubs) without littering the tree with 11 nearly-empty
 * composables.
 *
 * Each screen consumes its own optional argument (e.g. categorySlug) via
 * the [String] params even if it doesn't use them yet — this lets the
 * navigation signatures stay stable when real implementations land.
 */
@Composable
private fun PlaceholderScaffold(
    title: String,
    subtitle: String,
    navController: NavController,
    glyph: String = CurioIcons.AutoAwesome,
    glyphTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    showBack: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(16.dp)
    ) {
        if (showBack) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = { navController.popBackStack() },
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CurioIcon(
                            name = CurioIcons.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            size = 24.dp
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CurioIcon(
                    name = glyph,
                    contentDescription = null,
                    tint = glyphTint,
                    size = 96.dp
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Design phase · logic comes later",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Bottom-nav tab screens (currently all stubs)

// The Spin tab — also the destination for `spin/{categorySlug}` from Home.
// Full dial+segments rendering lands in Phase 3.
@Composable
fun SpinScreen(categorySlug: String?, navController: NavController) {
    val cat = categorySlug?.let { com.curio.app.data.CurioCategories.byRouteSlug(it) }
    PlaceholderScaffold(
        title = cat?.let { "${it.displayName} · Spin" } ?: "The Spin",
        subtitle = "The signature roulette screen. Dial + segments + spring-eased 2.5–4s spin lands here in Phase 3.",
        navController = navController,
        glyph = CurioIcons.AutoAwesome,
        glyphTint = cat?.accent ?: MaterialTheme.colorScheme.primary,
        showBack = categorySlug != null   // hide back when the spin tab is the tab target itself
    )
}

// The Cabinet tab — also the destination of Home's recent saves.
@Composable
fun CabinetScreen(navController: NavController) {
    PlaceholderScaffold(
        title = "The Cabinet",
        subtitle = "Your trophy shelf of saved captures. Masonry grid + filter chips + search land in Phase 3.",
        navController = navController,
        glyph = CurioIcons.Inventory2,
        glyphTint = MaterialTheme.colorScheme.tertiary,
        showBack = false
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// Push destinations (from Home/Hub/Cabinet/Detail)

// Full-screen Category Picker — see CURIO_SPEC.md §4 (v2).
@Composable
fun CategoryPickerScreen(navController: NavController) {
    PlaceholderScaffold(
        title = "What are we exploring?",
        subtitle = "6-tile full-screen picker with category accent tints (the Wildcard tile uses the rainbow gradient).",
        navController = navController,
        glyph = CurioIcons.Menu,
        glyphTint = MaterialTheme.colorScheme.primary
    )
}

// Topic Reveal — see CURIO_SPEC.md §6.
@Composable
fun TopicRevealScreen(categorySlug: String, topicName: String, navController: NavController) {
    val cat = com.curio.app.data.CurioCategories.byRouteSlug(categorySlug)
    PlaceholderScaffold(
        title = topicName,
        subtitle = cat?.let { "${it.displayName} · Heading toward Save/Capture in Phase 3." }
                    ?: "Heading toward Save/Capture in Phase 3.",
        navController = navController,
        glyph = CurioIcons.AutoAwesome,
        glyphTint = cat?.accent ?: MaterialTheme.colorScheme.primary
    )
}

// Save / Capture — see CURIO_SPEC.md §8 (6 format bodies, all stubbed).
@Composable
fun SaveCaptureScreen(categorySlug: String, topicName: String, navController: NavController) {
    val cat = com.curio.app.data.CurioCategories.byRouteSlug(categorySlug)
    PlaceholderScaffold(
        title = "Save your take",
        subtitle = "${cat?.displayName ?: "Topic"} · $topicName · Format-specific capture area lands in Phase 3.",
        navController = navController,
        glyph = cat?.iconGlyph ?: CurioIcons.Edit,
        glyphTint = cat?.accent ?: MaterialTheme.colorScheme.primary
    )
}

// Entry Detail — see CURIO_SPEC.md §10.
@Composable
fun EntryDetailScreen(entryId: String, navController: NavController) {
    PlaceholderScaffold(
        title = "Entry",
        subtitle = "Framed presentation of a saved capture · Edit / Share / Delete overflow menu lands in Phase 3.",
        navController = navController,
        glyph = CurioIcons.MenuBook,
        glyphTint = MaterialTheme.colorScheme.primary
    )
}

// Settings — see CURIO_SPEC.md §11 + §13.4 + §13.5 + §13.8.
@Composable
fun SettingsScreen(navController: NavController) {
    PlaceholderScaffold(
        title = "Settings",
        subtitle = "Profile · Manage categories · Appearance · Daily spin reminder · Replay intro · Version",
        navController = navController,
        glyph = CurioIcons.Settings,
        glyphTint = MaterialTheme.colorScheme.onSurface
    )
}

// Onboarding — see CURIO_SPEC.md §2.
@Composable
fun OnboardingScreen(navController: NavController) {
    PlaceholderScaffold(
        title = "Welcome to Curio",
        subtitle = "3-slide first-launch onboarding · Spin → Explore → Save · Page dots + Skip/Next lands in Phase 3.",
        navController = navController,
        glyph = CurioIcons.AutoAwesome,
        glyphTint = MaterialTheme.colorScheme.primary
    )
}

// Manage Categories — see CURIO_SPEC.md §13.4.
@Composable
fun ManageCategoriesScreen(navController: NavController) {
    PlaceholderScaffold(
        title = "Manage categories",
        subtitle = "Drag to reorder · toggle visibility · past entries in hidden categories are preserved.",
        navController = navController,
        glyph = CurioIcons.DragHandle,
        glyphTint = MaterialTheme.colorScheme.primary
    )
}

// Topic History — see CURIO_SPEC.md §13.5.
@Composable
fun TopicHistoryScreen(navController: NavController) {
    PlaceholderScaffold(
        title = "Topic history",
        subtitle = "Every topic you've ever spun, grouped by day · tap a topic to reopen Topic Reveal.",
        navController = navController,
        glyph = CurioIcons.History,
        glyphTint = MaterialTheme.colorScheme.primary
    )
}

// Lightbox — see CURIO_SPEC.md §13.2.
@Composable
fun LightboxScreen(imageUrl: String, navController: NavController) {
    PlaceholderScaffold(
        title = "Image viewer",
        subtitle = "Pinch-zoom · swipe-down to dismiss · full-screen topic image viewer lands in Phase 3.",
        navController = navController,
        glyph = CurioIcons.Image,
        glyphTint = MaterialTheme.colorScheme.onSurface
    )
}
