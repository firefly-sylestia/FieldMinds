package fieldmind.research.app.features.field.presentation.components
import fieldmind.research.app.ui.theme.CuteCardDefaults

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * FieldMind Animated Splash Screen
 *
 * Displays a branded splash with the FieldMind logo animating in:
 * 1. Logo scales from 0.3 to 1.0 with a bouncy spring + fades in simultaneously
 * 2. Tagline fades in after the logo
 * 3. After [durationMs], calls [onSplashComplete]
 */
@Composable
fun FieldMindAnimatedSplash(
    durationMs: Int = 1800,
    onSplashComplete: () -> Unit
) {
    var phase by remember { mutableStateOf(0) }

    val logoScale by animateFloatAsState(
        targetValue = if (phase >= 1) 1f else 0.3f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 280f),
        label = "logoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (phase >= 1) 1f else 0f,
        animationSpec = tween(500),
        label = "logoAlpha"
    )
    val taglineAlpha by animateFloatAsState(
        targetValue = if (phase >= 2) 1f else 0f,
        animationSpec = tween(400),
        label = "taglineAlpha"
    )

    LaunchedEffect(Unit) {
        // Phase 1: Start logo scale + fade together
        phase = 1
        delay(600)

        // Phase 2: Fade in tagline
        phase = 2
        delay(400)

        // Wait for remaining duration
        delay((durationMs - 1000).toLong().coerceAtLeast(0L))
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated logo
            Box(
                modifier = Modifier
                    .scale(logoScale)
                    .alpha(logoAlpha)
            ) {
                FieldMindLogo(
                    size = 96.dp,
                    modifier = Modifier
                        .clip(CuteCardDefaults.ShapeHero)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        )
                )
            }

            Spacer(Modifier.height(24.dp))

            // Tagline
            Box(modifier = Modifier.alpha(taglineAlpha)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "FieldMind",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Observe. Question. Research clearly.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
