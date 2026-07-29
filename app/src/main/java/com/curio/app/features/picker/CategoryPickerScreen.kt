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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioCategory
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.MorphEntrance
import com.curio.app.ui.components.StaggeredEntrance
import com.curio.app.ui.components.StaggeredItem
import com.curio.app.ui.components.rememberBreathingScale
import com.curio.app.ui.theme.CurioGradients
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

    // Wildcard gradient animation
    val wildcardShift = rememberBreathingScale(active = true, amplitude = 0.02f)

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
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(categories) { index, cat ->
                    StaggeredItem(index = index, staggerDelayMs = CurioMotion.Stagger.Fast) {
                        CategoryTile(
                            category = cat,
                            wildcardShift = wildcardShift,
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
    wildcardShift: Float,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = CurioMotion.Springs.Press,
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
                    .graphicsLayer {
                        scaleX = 1f + wildcardShift * 0.3f
                        scaleY = 1f + wildcardShift * 0.3f
                    }
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
