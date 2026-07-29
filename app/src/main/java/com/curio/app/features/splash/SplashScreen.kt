package com.curio.app.features.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.theme.CurioColors
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import com.curio.app.ui.theme.CurioTheme
import kotlinx.coroutines.delay

/**
 * Splash screen — see CURIO_SPEC.md §13.1.
 *
 * The first thing the user sees on app launch. Covers the gap between
 * process start and MainActivity being ready. In the placeholder phase
 * there is no real async work to wait for (no Room DB, no SharedPreferences
 * reads yet), so this just delays ~800ms then routes to Home.
 *
 * Phase 3+ will replace the LaunchedEffect body with:
 *   - init Room DB
 *   - read `onboardingComplete` flag from DataStore
 *   - if false → `CurioRoutes.ONBOARDING`
 *   - else    → `CurioRoutes.HOME`
 *
 * Visual:
 *   [ Curio logomark ]
 *       Curio        (geom, 36sp, heavy)
 *   · · · (subtle 3-dot pulse)
 *
 * No back button. No interaction. Max 800ms before auto-dismiss.
 */
@Composable
fun SplashScreen(navController: NavHostController) {
    var pulseIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        // 3-dot pulse loop — pure visual
        while (true) {
            pulseIndex = (pulseIndex + 1) % 3
            delay(200)
        }
    }

    LaunchedEffect(Unit) {
        // Spec: max 800ms before auto-dismiss. Phase 3+ replaces this with
        // the real init logic.
        delay(800)
        navController.navigate(CurioRoutes.HOME) {
            popUpTo(CurioRoutes.SPLASH) { inclusive = true }
        }
    }

    CurioTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Logomark — large auto_awesome Material Symbols glyph in primary
                    CurioIcon(
                        name = CurioIcons.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        size = 96.dp
                    )

                    Text(
                        text = "Curio",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(Modifier.height(8.dp))

                    // 3-dot loader, geom colored, pulses in sequence
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) { index ->
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        if (index == pulseIndex)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
