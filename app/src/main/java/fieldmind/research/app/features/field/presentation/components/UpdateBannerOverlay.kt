package fieldmind.research.app.features.field.presentation.components
import fieldmind.research.app.ui.theme.CuteCardDefaults

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fieldmind.research.app.infrastructure.updates.UpdateInfo
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon

/**
 * Top-slide-down banner shown when [info] describes a newer release.
 *
 * - Does **NOT** block the rest of the app — only the top edge is occluded.
 * - Auto-animates in on first composition.
 * - Three actions: **Update** (open release URL via callback), **Later**
 *   (set [onLater] so the overlay stops appearing for this tag), and an
 *   inline **What's new** affordance.
 *
 * The component owns its visibility animation; the caller's *showJournal*-style
 * "should this composable tree exist" flag should be left set until after
 * [onLater] / [onUpdate] fires — the parent handles the unmount just like with
 * the journal overlay.
 */
@Composable
fun UpdateBannerOverlay(
    info: UpdateInfo.UpdateAvailable,
    onUpdate: () -> Unit,
    onLater: () -> Unit,
    onOpenChangelog: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    // 0f = fully off-screen above the top edge, 1f = docked at the top.
    val offsetProgress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bannerOffset"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "bannerAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                // Map 0f..1f progress to -bannerHeight..0 dp translation. The banner
                // is roughly 80dp tall in practice; using -160dp guarantees it sits
                // fully above the status-bar area when collapsed.
                translationY = -160f.dp.toPx() * (1f - offsetProgress)
                this.alpha = alpha
            }
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 2.dp,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon = MaterialSymbolIcon("rocket_launch"),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        size = 20.dp
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    Column {
                        Text(
                            text = "Update available",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = info.versionName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            maxLines = 1
                        )
                    }
                }
                BannerActionButton(
                    label = "Update",
                    primary = true,
                    onClick = onUpdate
                )
                BannerActionButton(
                    label = "Later",
                    primary = false,
                    onClick = onLater
                )
                BannerActionButton(
                    label = "Notes",
                    primary = false,
                    onClick = onOpenChangelog
                )
            }
        }
    }
}

@Composable
private fun BannerActionButton(
    label: String,
    primary: Boolean,
    onClick: () -> Unit
) {
    val (bg, fg) = if (primary) {
        MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    }
    Surface(
        shape = CuteCardDefaults.ChipShape,
        color = bg,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = fg,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
