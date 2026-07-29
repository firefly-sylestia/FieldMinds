package com.curio.app.features.picker

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioCategory
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.ScreenEntrance
import com.curio.app.ui.theme.CurioGradients
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons

/**
 * Full-screen Category Picker — see CURIO_SPEC.md §4 (v2 — was a bottom
 * sheet in v1, now its own back-stack page for more breathing room).
 *
 * Layout:
 *   - Top bar: ← "What are we exploring?"
 *   - 2-col grid of 6 category tiles, each 120dp tall
 *   - "Manage categories" text button at bottom
 *
 * Each tile scales to 0.96 then springs back to 1.0 on tap, the screen
 * pops after a brief moment, and the app navigates to The Spin pre-loaded.
 */
@Composable
fun CategoryPickerScreen(navController: NavController) {
    val categories = remember { CurioCategories.visible }

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
            Surface(
                onClick = { navController.popBackStack() },
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                CurioIcon(
                    name = CurioIcons.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                    size = 24.dp,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Text(
                text = "What are we exploring?",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Tile grid (3-col for 11 categories — 4 rows, more breathing room) ─
        ScreenEntrance {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(categories) { cat ->
                    CategoryTile(
                        category = cat,
                        onClick = {
                            navController.navigate(
                                CurioRoutes.spinWithCategory(cat.id.routeSlug)
                            )
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Manage categories ──────────────────────────────────────────────
        TextButton(
            onClick = { navController.navigate(CurioRoutes.MANAGE_CATEGORIES) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Manage categories",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CategoryTile(
    category: CurioCategory,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 600f),
        label = "tileScale"
    )

    val isWildcard = category.id == CategoryId.WILDCARD
    val contentColor = if (isWildcard) Color.White else category.accent

    Surface(
        onClick = {
            pressed = true
            onClick()
        },
        shape = RoundedCornerShape(24.dp),
        color = if (isWildcard) Color.Transparent else category.tint,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .scale(scale)
    ) {
        if (isWildcard) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(CurioGradients.WildcardGradientStops)
                    )
            ) {
                TileContent(category = category, contentColor = contentColor)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                TileContent(category = category, contentColor = contentColor)
            }
        }
    }
}

@Composable
private fun TileContent(category: CurioCategory, contentColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        CurioIcon(
            name = category.iconGlyph,
            contentDescription = null,
            tint = contentColor,
            size = 40.dp
        )
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = contentColor
        )
    }
}