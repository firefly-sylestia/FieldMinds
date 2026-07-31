package com.curio.app.features.picker

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.unit.sp
import com.curio.app.ui.theme.CurioColors
import androidx.navigation.NavController
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioCategory
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.MorphEntrance
import com.curio.app.ui.theme.CurioGradients
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.CurioMotion

/**
 * Full-screen Category Picker — gradient tiles with a large watermark
 * icon tucked into the bottom-right corner and bold coloured title text.
 */
@Composable
fun CategoryPickerScreen(navController: NavController) {
    val categories = remember { CurioCategories.visible }
    val gridState = rememberLazyGridState()

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
                .padding(vertical = 0.dp),
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ExpressivePill(CurioIcons.AutoAwesome, "Focused decks")
            ExpressivePill(CurioIcons.Casino, "Surprise mix")
        }

        Text(
            text = "Pick a mood for your next Shuffle. Every card is a complete deck with its own rhythm, color, and prompts.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        MorphEntrance {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(top = 4.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(categories) { cat ->
                    CategoryTile(
                        category = cat,
                        onClick = {
                            navController.navigate(
                                CurioRoutes.spinWithCategory(cat.id.routeSlug)
                            ) { launchSingleTop = true }
                        }
                    )
                }
            }
        }

        FilledTonalButton(
            onClick = { navController.navigate(CurioRoutes.MANAGE_CATEGORIES) { launchSingleTop = true } },
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            CurioIcon(CurioIcons.Settings, null, size = 18.dp)
            Text(
                text = "Manage categories",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 8.dp)
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
        animationSpec = CurioMotion.Springs.Press,
        label = "tileScale"
    )

    val isWildcard = category.id == CategoryId.WILDCARD
    val tileGradient = remember(isWildcard, category.accent) {
        if (isWildcard) CurioGradients.wildcardCardGradient()
        else CurioGradients.cardGradient(category.accent)
    }

    Surface(
        onClick = { onClick() },
        shape = RoundedCornerShape(30.dp),
        color = Color.Transparent,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(164.dp)
            .scale(scale)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(tileGradient), RoundedCornerShape(30.dp))
                .padding(14.dp)
        ) {
            CurioIcon(
                name = category.iconGlyph,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.16f),
                size = 118.dp,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.22f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CurioIcon(category.iconGlyph, null, tint = Color.White, size = 22.dp)
                }
            }
            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    text = category.displayName,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 25.sp,
                        lineHeight = 28.sp
                    ),
                    color = Color.White
                )
                Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.20f)) {
                    Text(
                        text = if (isWildcard) "Surprise Shuffle" else "Start deck",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                        color = Color.White.copy(alpha = 0.92f),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpressivePill(icon: String, label: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = CurioColors.CoralBlush.copy(alpha = 0.13f),
        border = BorderStroke(1.dp, CurioColors.CoralBlush.copy(alpha = 0.22f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CurioIcon(icon, null, tint = CurioColors.CoralBlush, size = 17.dp)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
