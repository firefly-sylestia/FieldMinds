package com.curio.app.features.spin

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioTopic
import com.curio.app.data.SpinSessionStore
import com.curio.app.data.TopicCatalog
import com.curio.app.data.TopicJsonLoader
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.CategoryTile
import com.curio.app.ui.components.CompactInsets
import com.curio.app.ui.components.ConfettiBurst
import com.curio.app.ui.components.CurioBackButton
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.CurioMotion
import kotlinx.coroutines.delay

/**
 * The redesigned Spin screen with:
 * - Swipeable card deck (browse up/down, tap to open, swipe left to reject)
 * - Beautiful spin button + animation
 * - Bottom sticky bar: category chip + filter button
 * - Auto-navigate to TopicReveal after landing
 * - Persists landed topic across app restarts
 */
@Composable
fun SpinScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionStore = remember { SpinSessionStore(context) }

    // Restore session on first load
    val restoredSession = remember {
        sessionStore.restoreSession()
    }

    var selectedCategory by remember { mutableStateOf(restoredSession?.categoryId ?: CategoryId.WILDCARD) }
    var filterChips by remember { mutableStateOf(restoredSession?.filterChips ?: emptyList()) }
    var showCategorySheet by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var landedTopic by remember { mutableStateOf(restoredSession?.landedTopic) }
    var showConfetti by remember { mutableStateOf(false) }

    // Load category pool for filter UI
    val categoryTopics by produceState<List<CurioTopic>>(initialValue = emptyList(), selectedCategory) {
        value = TopicCatalog.poolFor(selectedCategory)
    }
    val tagsList = remember(categoryTopics) { categoryTopics.flatMap { it.tags }.distinct().sorted() }
    val subtypesList = remember(categoryTopics) { categoryTopics.map { it.subtype }.distinct().sorted() }

    // Auto-navigate to reveal after celebrate pause
    LaunchedEffect(landedTopic) {
        if (landedTopic != null && showConfetti) {
            delay(900) // celebration pause
            navController.navigate(
                CurioRoutes.TopicReveal(
                    topicId = landedTopic!!.id,
                    categoryId = selectedCategory.stringValue
                )
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(CurioColors.EerieBlack)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(CompactInsets.statusBarOnly()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: back button + title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CurioBackButton(onNavigateBack = { navController.popBackStack() })
                Text(
                    text = "Spin the Wheel",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = CurioColors.CreamWhite
                )
                Box(modifier = Modifier.width(40.dp)) // spacer for symmetry
            }

            // Card deck + spin button (flexbox column)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.Center)
            ) {
                // Swipeable card deck
                SpinDeck(
                    selectedCategory = selectedCategory,
                    filterChips = filterChips,
                    onCardLand = { topic ->
                        landedTopic = topic
                        showConfetti = true
                        sessionStore.saveSession(
                            categoryId = selectedCategory,
                            filterChips = filterChips,
                            landedTopic = topic
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                )

                // Spin button (beautiful, large)
                SpinButtonAnimated(
                    enabled = landedTopic == null,
                    onClick = {
                        landedTopic = null
                        showConfetti = false
                    },
                    modifier = Modifier.size(92.dp)
                )
            }

            // Bottom sticky bar: category + filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        CurioColors.SpacedInk,
                        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category chip
                Surface(
                    onClick = { showCategorySheet = true },
                    shape = RoundedCornerShape(16.dp),
                    color = selectedCategory.accent.copy(alpha = 0.15f),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CurioIcon(
                            name = selectedCategory.iconGlyph,
                            contentDescription = null,
                            tint = selectedCategory.accent,
                            size = 18.dp
                        )
                        Text(
                            text = selectedCategory.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            color = selectedCategory.accent
                        )
                    }
                }

                // Filter button
                Surface(
                    onClick = { showFilterSheet = true },
                    shape = RoundedCornerShape(16.dp),
                    color = if (filterChips.isNotEmpty()) {
                        CurioColors.Citrine.copy(alpha = 0.15f)
                    } else {
                        CurioColors.Silver.copy(alpha = 0.08f)
                    },
                    modifier = Modifier
                        .height(42.dp)
                        .width(42.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CurioIcon(
                            name = if (filterChips.isNotEmpty()) CurioIcons.FilterFilled else CurioIcons.Filter,
                            contentDescription = "Filter",
                            tint = if (filterChips.isNotEmpty()) CurioColors.Citrine else CurioColors.Silver,
                            size = 20.dp
                        )
                    }
                }
            }
        }

        // Confetti overlay
        if (showConfetti) {
            Box(modifier = Modifier.fillMaxSize()) {
                ConfettiBurst(
                    isActive = showConfetti,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // Sheets
    if (showCategorySheet) {
        CategoryPickerSheet(
            selectedCategory = selectedCategory,
            onCategorySelected = { cat ->
                selectedCategory = cat
                showCategorySheet = false
            },
            onDismiss = { showCategorySheet = false }
        )
    }

    if (showFilterSheet) {
        FilterSheet(
            cat = selectedCategory,
            subtypes = subtypesList,
            tags = tagsList,
            initialSubtypes = filterChips.filter { it in subtypesList }.toSet(),
            initialFilters = filterChips.filter { it !in subtypesList }.toSet(),
            matchCount = { tags, subtypes ->
                categoryTopics.count { topic ->
                    (subtypes.isEmpty() || topic.subtype in subtypes) &&
                    (tags.isEmpty() || topic.tags.any { it in tags })
                }
            },
            onApply = { tags, subtypes ->
                filterChips = (tags + subtypes).toList()
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false }
        )
    }
}

/**
 * Beautiful spin button with pulsing glow animation.
 */
@Composable
private fun SpinButtonAnimated(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Glow pulse (background)
        if (enabled) {
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = CurioColors.DuskPink.copy(alpha = 0.2f),
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(50.dp))
            ) {}
        }

        // Button
        Surface(
            onClick = onClick,
            enabled = enabled,
            shape = RoundedCornerShape(50.dp),
            color = CurioColors.DuskPink,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(50.dp))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SPIN",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Category picker sheet — uses the existing CategorySheet from SpinSheets
 * which renders the category grid in a bottom sheet.
 */
@Composable
private fun CategoryPickerSheet(
    selectedCategory: CurioCategory,
    onCategorySelected: (CurioCategory) -> Unit,
    onDismiss: () -> Unit
) {
    CategorySheet(
        current = selectedCategory,
        onSelect = onCategorySelected,
        onBrowseAll = onDismiss,
        onDismiss = onDismiss
    )
}
