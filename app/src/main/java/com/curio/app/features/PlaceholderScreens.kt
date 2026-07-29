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
 * Universal scaffold for the remaining placeholder-phase screens
 * (Settings / Onboarding / ManageCategories / TopicHistory / Lightbox).
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
 * The 6 main user-facing screens (Spin / CategoryPicker / TopicReveal /
 * SaveCapture / Cabinet / EntryDetail) have been promoted to their own
 * feature packages with full designs. The screens below are deliberately
 * kept as placeholders until Phase 4 — they have their own scope
 * (manage-categories reorder logic, onboarding 3-slide flow, settings
 * toggles, topic history persistence, lightbox pinch-zoom) and aren't
 * in the main happy path.
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
            .statusBarsPadding()
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
                    text = "Design phase \u00B7 logic comes later",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                        .copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Lower-priority placeholders (not on the main happy path) — full
// implementations land in Phase 4 alongside the Room data layer.

// Settings — see CURIO_SPEC.md §11.
@Composable
fun SettingsScreen(navController: NavController) {
    PlaceholderScaffold(
        title = "Settings",
        subtitle = "Profile \u00B7 Manage categories \u00B7 Appearance \u00B7 Daily spin reminder \u00B7 Replay intro \u00B7 Version",
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
        subtitle = "3-slide first-launch onboarding \u00B7 Spin \u2192 Explore \u2192 Save \u00B7 Page dots + Skip/Next lands in Phase 4.",
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
        subtitle = "Drag to reorder \u00B7 toggle visibility \u00B7 past entries in hidden categories are preserved.",
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
        subtitle = "Every topic you've ever spun, grouped by day \u00B7 tap a topic to reopen Topic Reveal.",
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
        subtitle = "Pinch-zoom \u00B7 swipe-down to dismiss \u00B7 full-screen topic image viewer lands in Phase 4.",
        navController = navController,
        glyph = CurioIcons.Image,
        glyphTint = MaterialTheme.colorScheme.onSurface
    )
}