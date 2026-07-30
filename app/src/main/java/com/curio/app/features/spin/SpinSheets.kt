package com.curio.app.features.spin

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioCategory
import com.curio.app.ui.components.CategoryTile
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.CurioMotion

// ═══════════════════════════════════════════════════════════════════════════
// Category sheet — the SAME tile UI as the "explore categories" picker
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Category selection for The Spin, rendered as the identical colored tile
 * grid used by the Category Picker (via the shared [CategoryTile]).
 *
 * This replaces the old cramped `DropdownMenu` that listed categories as
 * tiny text rows and looked nothing like the rest of the app.
 *
 * The sheet is height-capped at 88% and its header + "Browse all" footer are
 * pinned, so the grid scrolls underneath them rather than pushing the
 * primary action off-screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySheet(
    current: CurioCategory,
    onSelect: (CurioCategory) -> Unit,
    onBrowseAll: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val categories = remember { CurioCategories.visible }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(modifier = Modifier.fillMaxHeight(0.88f)) {
            SheetGrabber()

            // ── Pinned header ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "What are we exploring?",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Pick a lane for the wheel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    onClick = onDismiss,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        CurioIcon(
                            CurioIcons.Close, "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            size = 18.dp
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // ── Scrollable tile grid ───────────────────────────────────
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(items = categories, key = { it.id.name }) { cat ->
                    CategoryTile(
                        category = cat,
                        selected = cat.id == current.id,
                        compact = true,
                        onClick = { onSelect(cat) }
                    )
                }
            }

            // ── Pinned footer ──────────────────────────────────────────
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            TextButton(
                onClick = onBrowseAll,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CurioIcon(
                        CurioIcons.Palette, null,
                        tint = MaterialTheme.colorScheme.primary,
                        size = 18.dp
                    )
                    Text(
                        text = "Open the full category page",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Filter sheet — sticky header + sticky apply footer
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Multi-select filter sheet for The Spin.
 *
 * Redesigned so nothing gets hidden:
 *  - The sheet is capped at 90% height with `skipPartiallyExpanded`, so it
 *    always opens fully rather than at a half-detent that clipped the chips.
 *  - The title/summary header and the Apply button are **pinned** outside
 *    the scroll container. Previously everything lived in one `Column`
 *    inside the sheet, so on a category with many genres the Apply button
 *    was pushed below the fold and became unreachable.
 *  - The scrolling middle is the only thing that moves, and it carries
 *    `navigationBarsPadding()` on the footer so the button clears the
 *    system bar.
 *  - Live match count in the footer so the user sees the effect of a chip
 *    before committing.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterSheet(
    cat: CurioCategory,
    subtypes: List<String>,
    tags: List<String>,
    initialSubtypes: Set<String>,
    initialFilters: Set<String>,
    matchCount: (tags: Set<String>, subtypes: Set<String>) -> Int,
    onDismiss: () -> Unit,
    onApply: (tags: Set<String>, subtypes: Set<String>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var draftFilters by remember(initialFilters) { mutableStateOf(initialFilters) }
    var draftSubtypes by remember(initialSubtypes) { mutableStateOf(initialSubtypes) }

    val activeCount = draftFilters.size + draftSubtypes.size
    val matches = matchCount(draftFilters, draftSubtypes)
    val hasAnyFilter = subtypes.size > 1 || tags.isNotEmpty()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(modifier = Modifier.fillMaxHeight(0.9f)) {
            SheetGrabber()

            // ── Pinned header ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(cat.accent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    CurioIcon(cat.iconGlyph, null, tint = cat.accent, size = 20.dp)
                }
                Spacer(Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Filter ${cat.displayName}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (activeCount == 0) {
                            "Select as many as you like"
                        } else {
                            "$activeCount selected"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (activeCount > 0) {
                    TextButton(onClick = {
                        draftFilters = emptySet()
                        draftSubtypes = emptySet()
                    }) {
                        Text(
                            "Clear",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // ── Scrolling body ─────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                if (subtypes.size > 1) {
                    SectionLabel("Kinds", count = subtypes.size)
                    Spacer(Modifier.height(10.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        subtypes.forEach { st ->
                            MultiSelectChip(
                                label = st,
                                selected = st in draftSubtypes,
                                accent = cat.accent,
                                onClick = {
                                    draftSubtypes = if (st in draftSubtypes) {
                                        draftSubtypes - st
                                    } else {
                                        draftSubtypes + st
                                    }
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(22.dp))
                }

                if (tags.isNotEmpty()) {
                    SectionLabel("Genres & tags", count = tags.size)
                    Spacer(Modifier.height(10.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tags.forEach { tag ->
                            MultiSelectChip(
                                label = tag,
                                selected = tag in draftFilters,
                                accent = cat.accent,
                                onClick = {
                                    draftFilters = if (tag in draftFilters) {
                                        draftFilters - tag
                                    } else {
                                        draftFilters + tag
                                    }
                                }
                            )
                        }
                    }
                }

                if (!hasAnyFilter) {
                    Text(
                        text = "No filters for this category yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))
            }

            // ── Pinned footer ──────────────────────────────────────────
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (matches == 0) {
                        "No topics match — try removing a chip"
                    } else {
                        "$matches ${if (matches == 1) "topic" else "topics"} in the deck"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (matches == 0) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { onApply(draftFilters, draftSubtypes) },
                    enabled = matches > 0,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cat.accent,
                        contentColor = Color.White,
                        disabledContainerColor = cat.accent.copy(alpha = 0.3f),
                        disabledContentColor = Color.White.copy(alpha = 0.6f)
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CurioIcon(CurioIcons.Check, null, tint = Color.White, size = 18.dp)
                        Text(
                            text = if (activeCount > 0) {
                                "Apply $activeCount filter${if (activeCount > 1) "s" else ""}"
                            } else {
                                "Show all topics"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Shared sheet bits
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Custom grab handle. We pass `dragHandle = null` to [ModalBottomSheet] and
 * draw our own so the handle sits inside our fixed-height Column — the
 * default handle is laid out outside it and stole height from the pinned
 * footer.
 */
@Composable
private fun SheetGrabber() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 34.dp, height = 4.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
        )
    }
}

@Composable
private fun SectionLabel(text: String, count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MultiSelectChip(
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.04f else 1f,
        animationSpec = CurioMotion.Springs.Snappy,
        label = "msChipScale"
    )
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) accent else MaterialTheme.colorScheme.surfaceContainerHigh,
        border = if (selected) null
        else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.scale(scale)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (selected) {
                CurioIcon(CurioIcons.Check, null, tint = Color.White, size = 14.dp)
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
                ),
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
