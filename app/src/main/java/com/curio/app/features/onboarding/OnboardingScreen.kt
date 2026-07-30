package com.curio.app.features.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.MorphEntrance
import com.curio.app.ui.components.StaggeredEntrance
import com.curio.app.ui.components.StaggeredItem
import com.curio.app.ui.theme.CurioGradients
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import kotlinx.coroutines.launch

/**
 * First-launch onboarding — see CURIO_SPEC.md §2 (v2).
 *
 * Upgraded with:
 *  - MorphEntrance for each slide content on page change
 *  - Staggered entrance for headline + subtext within each slide
 *  - Enhanced illustration block with breathing gradient
 */
@Composable
fun OnboardingScreen(navController: NavController) {
    val pagerState = rememberPagerState(pageCount = { OnboardingSlides.size })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isLastSlide = pagerState.currentPage == OnboardingSlides.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                MorphEntrance {
                    OnboardingSlide(slide = OnboardingSlides[pageIndex])
                }
            }
        }

        // ── Page dots ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            OnboardingSlides.forEachIndexed { index, _ ->
                val selected = pagerState.currentPage == index
                PageDot(
                    selected = selected,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } }
                )
            }
        }

        // ── Bottom controls ────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = { finishOnboarding(context, navController) }) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = {
                    if (isLastSlide) {
                        finishOnboarding(context, navController)
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = if (isLastSlide) "Let's go" else "Next",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun OnboardingSlide(slide: OnboardingSlideData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ── Illustration glyph block ───────────────────────────────────────
        Box(
            modifier = Modifier
                .size(220.dp)
                .background(
                    Brush.horizontalGradient(CurioGradients.WildcardGradientStops),
                    shape = RoundedCornerShape(48.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            CurioIcon(
                name = slide.glyph,
                contentDescription = null,
                tint = Color.White,
                size = 96.dp
            )
        }

        Spacer(Modifier.height(32.dp))

        StaggeredEntrance(staggerDelayMs = 60) {
            StaggeredItem(index = 0) {
                Text(
                    text = slide.headline,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(12.dp))

            StaggeredItem(index = 1) {
                Text(
                    text = slide.subtext,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PageDot(selected: Boolean, onClick: () -> Unit) {
    val size = if (selected) 12.dp else 8.dp
    val color = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    }
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .size(size)
            .scale(if (selected) 1.2f else 1f)
            .background(color, shape = CircleShape)
            .clickable(onClick = onClick)
    )
}

private fun finishOnboarding(context: Context, navController: NavController) {
    CurioOnboardingState.markComplete(context)
    navController.navigate(CurioRoutes.HOME) {
        popUpTo(CurioRoutes.ONBOARDING) { inclusive = true }
    }
}

private data class OnboardingSlideData(
    val glyph: String,
    val headline: String,
    val subtext: String
)

private val OnboardingSlides = listOf(
    OnboardingSlideData(
        glyph = CurioIcons.Casino,
        headline = "Spin into something new",
        subtext = "Curio hands you a topic you didn't know you wanted opened."
    ),
    OnboardingSlideData(
        glyph = CurioIcons.AutoAwesome,
        headline = "Go explore it, your way",
        subtext = "Listen, read, watch, scroll — wherever your curiosity wants to roam."
    ),
    OnboardingSlideData(
        glyph = CurioIcons.Inventory2,
        headline = "Save it your way too",
        subtext = "Voice notes, written reviews, moodboards, journal entries — pick the format that fits."
    )
)

object CurioOnboardingState {
    private const val PREFS = "curio_onboarding"
    private const val KEY_COMPLETE = "complete"

    fun isComplete(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_COMPLETE, false)

    fun markComplete(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_COMPLETE, true)
            .apply()
    }

    fun reset(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_COMPLETE, false)
            .apply()
    }
}
