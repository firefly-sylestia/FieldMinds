package com.curio.app.features.lightbox

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.curio.app.ui.theme.CurioGradients
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons

/**
 * Lightbox — full-screen image viewer — see CURIO_SPEC.md §13.2.
 *
 * Per spec §3 (Home "Recently explored") + §10 (Entry Detail): topic
 * image tap opens a full-screen viewer with pinch-zoom. Per §13.2:
 * swipe-down or tap to dismiss.
 *
 * Phase 0 placeholder: instead of a real image loader (Coil / Glide land
 * with the asset pipeline in the data layer phase), the lightbox renders
 * a branded gradient placeholder with the [imageUrl] rendered as the
 * label. The pinch-to-zoom + pan gesture still works over the
 * placeholder so the interaction is full-featured.
 *
 * The close button (top-right) is the only tap-to-dismiss target to
 * avoid Compose gesture conflict between `detectTransformGestures` and
 * `detectTapGestures`. Long-press / swipe-down dismissal can be wired
 * alongside the data loader phase if it becomes important.
 */
@Composable
fun LightboxScreen(imageUrl: String, navController: NavController) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 380f),
        label = "lightboxScale"
    )
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 380f),
        label = "lightboxOffsetX"
    )
    val animatedOffsetY by animateFloatAsState(
        targetValue = offsetY,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 380f),
        label = "lightboxOffsetY"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            // Pinch + pan to zoom (1x to 4x, capped offset)
            .pointerInput(imageUrl) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 4f)
                    offsetX = (offsetX + pan.x).coerceIn(-600f, 600f)
                    offsetY = (offsetY + pan.y).coerceIn(-600f, 600f)
                }
            }
    ) {
        // ── Image surface (gradient placeholder for now) ─────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            PlaceholderImageCard(
                imageUrl = imageUrl,
                scale = animatedScale,
                offsetX = animatedOffsetX,
                offsetY = animatedOffsetY
            )
        }

        // ── Top-right close button ───────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 0.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                onClick = {
                    scale = 1f
                    offsetX = 0f
                    offsetY = 0f
                    navController.popBackStack()
                },
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.55f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CurioIcon(
                        name = CurioIcons.Close,
                        contentDescription = "Close lightbox",
                        tint = Color.White,
                        size = 22.dp
                    )
                }
            }
        }

        // ── Bottom helper strip ──────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = imageUrl,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = Color.White.copy(alpha = 0.65f)
            )
            Text(
                text = "Pinch to zoom · Close button to dismiss",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.40f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun PlaceholderImageCard(
    imageUrl: String,
    scale: Float,
    offsetX: Float,
    offsetY: Float
) {
    val derivedTitle = remember(imageUrl) {
        imageUrl.substringAfterLast('/')
            .replace('-', ' ')
            .takeIf { it.isNotBlank() } ?: imageUrl
    }
    Box(
        modifier = Modifier
            .size(width = 320.dp, height = 360.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.verticalGradient(CurioGradients.WildcardGradientStops))
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offsetX,
                translationY = offsetY
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CurioIcon(
                name = CurioIcons.AutoAwesome,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                size = 96.dp
            )
            Text(
                text = derivedTitle.replaceFirstChar(Char::uppercase),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                modifier = Modifier.padding(top = 12.dp, start = 16.dp, end = 16.dp)
            )
            Text(
                text = "Image placeholder",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.80f),
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
