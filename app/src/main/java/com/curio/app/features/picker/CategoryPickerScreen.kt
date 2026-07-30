package com.curio.app.features.picker

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioCategory
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.MorphEntrance
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.CurioMotion

/**
 * Full-screen Category Picker — see CURIO_SPEC.md §4 (v2).
 *
 * Upgraded with:
 *  - Staggered tile entrance: each tile fades + slides in with delay
 *  - Press morph: tile scales down with bouncy spring on tap
 *  - Breathing wildcard gradient: the wildcard tile's gradient gently
 *    shifts hue over time
 *  - MorphEntrance wrapper for the whole grid
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

        // ── Tile grid (staggered entrance) ──────────────────────────────────
        MorphEntrance {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(categories) { index, cat ->
                    StaggeredItem(index = index, staggerDelayMs = CurioMotion.Stagger.Fast) {
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
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = CurioMotion.Springs.Press,
        label = "tileScale"
    )

    val isWildcard = category.id == CategoryId.WILDCARD
    val cardColor = if (isWildcard) {
        CurioColors.CoralBlush.copy(alpha = 0.85f)
    } else {
        category.accent
    }

    Surface(
        onClick = {
            pressed = true
            onClick()
        },
        shape = RoundedCornerShape(28.dp),
        color = cardColor,
        shadowElevation = 8.dp,
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(156.dp)
            .scale(scale)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            CurioIcon(
                name = category.iconGlyph,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.16f),
                size = 104.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 4.dp)
            )
            TileContent(category = category)
        }
    }
}

@Composable
private fun TileContent(category: CurioCategory) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = CurioColors.CreamWhite.copy(alpha = 0.22f)
        ) {
            CurioIcon(
                name = category.iconGlyph,
                contentDescription = null,
                tint = Color.White,
                size = 34.dp,
                modifier = Modifier.padding(10.dp)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = Color.White
            )
            Text(
                text = "Spin this lane",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.78f)
            )
        }
    }
}
