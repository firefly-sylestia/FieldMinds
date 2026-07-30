package com.curio.app.features.managecategories

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.curio.app.data.CategoryId
import com.curio.app.data.CurioCategories
import com.curio.app.data.CurioCategory
import com.curio.app.ui.components.CurioBackButton
import com.curio.app.ui.components.ScreenEntrance
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons

/**
 * Manage Categories — see CURIO_SPEC.md §13.4 (v2 brief).
 *
 * Reorderable list of all 6 categories with drag handle (visual for the
 * placeholder phase; up/down stepper buttons are wired to [moveCategory])
 * + visibility toggle (live, in-memory for the placeholder phase; lands
 * into DataStore alongside the settings persistence work).
 *
 * CurioCategory is a data-layer singleton with [CurioCategory.isHidden]
 * defaulting to false. We mirror it via a local [mutableStateListOf] so
 * toggling visibility is reactive in the UI without mutating the source
 * list directly.
 */
@Composable
fun ManageCategoriesScreen(navController: NavController) {
    val items = remember {
        mutableStateListOf<CurioCategory>().apply { addAll(CurioCategories.all) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // ── Top bar ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CurioBackButton(onClick = { navController.popBackStack() })
            Text(
                text = "Manage categories",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // ── Helper text ───────────────────────────────────────────────────
        Text(
            text = "Hidden categories won't show in Spin, Category Picker, or Cabinet. " +
                  "Past entries in hidden categories are kept and reappear when you re-enable them.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // ── List ──────────────────────────────────────────────────────────
        ScreenEntrance {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items, key = { it.id }) { category ->
                    CategoryRow(
                        category = category,
                        isFirst = items.firstOrNull()?.id == category.id,
                        isLast = items.lastOrNull()?.id == category.id,
                        onMoveUp = { moveCategory(items, category.id, -1) },
                        onMoveDown = { moveCategory(items, category.id, +1) },
                        onVisibilityToggle = { visible ->
                            val index = items.indexOfFirst { it.id == category.id }
                            if (index >= 0) {
                                items[index] = category.copy(isHidden = !visible)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(
    category: CurioCategory,
    isFirst: Boolean,
    isLast: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onVisibilityToggle: (Boolean) -> Unit
) {
    val hiddenAlpha by animateFloatAsState(
        targetValue = if (category.isHidden) 0.45f else 1f,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 380f),
        label = "hiddenAlpha"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .alpha(hiddenAlpha),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Reorder stepper + drag handle (visual stand-in) ───────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.size(width = 40.dp, height = 56.dp)
            ) {
                ReorderButton(
                    glyph = CurioIcons.KeyboardArrowUp,
                    enabled = !isFirst,
                    onClick = onMoveUp
                )
                CurioIcon(
                    name = CurioIcons.DragHandle,
                    contentDescription = "Drag to reorder",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    size = 20.dp
                )
                ReorderButton(
                    glyph = CurioIcons.KeyboardArrowDown,
                    enabled = !isLast,
                    onClick = onMoveDown
                )
            }

            Spacer(Modifier.size(12.dp))

            // ── Category icon dot ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(category.tint, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CurioIcon(
                    name = category.iconGlyph,
                    contentDescription = null,
                    tint = if (category.isHidden) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    size = 22.dp
                )
            }

            Spacer(Modifier.size(12.dp))

            // ── Name + status ──────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.displayName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                AnimatedVisibility(visible = category.isHidden) {
                    Text(
                        text = "Hidden",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Visibility toggle ──────────────────────────────────────────
            Switch(
                checked = !category.isHidden,
                onCheckedChange = { newVisible -> onVisibilityToggle(newVisible) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

@Composable
private fun ReorderButton(glyph: String, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = if (enabled) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        },
        modifier = Modifier.size(20.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            CurioIcon(
                name = glyph,
                contentDescription = null,
                tint = if (enabled) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                },
                size = 16.dp
            )
        }
    }
}


/**
 * Move the category matching [id] by [delta] positions in the list.
 * No-op if at the boundary or if the id is not found.
 */
private fun moveCategory(
    list: MutableList<CurioCategory>,
    id: CategoryId,
    delta: Int
) {
    val current = list.indexOfFirst { it.id == id }
    if (current < 0) return
    val target = (current + delta).coerceIn(0, list.lastIndex)
    if (target == current) return
    val moved = list.removeAt(current)
    list.add(target, moved)
}
