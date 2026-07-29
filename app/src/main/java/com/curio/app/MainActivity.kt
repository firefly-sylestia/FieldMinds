package com.curio.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.curio.app.navigation.CurioNavHost
import com.curio.app.ui.theme.CurioTheme

/**
 * Curio's single Activity — see CURIO_SPEC.md.
 *
 * Hosts the entire app via [CurioNavHost] inside [CurioTheme]. Edge-to-edge
 * is enabled so the splash background and bottom-nav surface extend behind
 * the system bars (M3 expressive + the spec's "warm cream" feel).
 *
 * Phase 2 (current): simple NavHost → Splash → Home + stub screens for the
 * rest. App is data-free (no Room, no SharedPreferences wiring yet).
 *
 * Phase 3: ViewModels get injected at the NavHost level via
 * `viewModel()` call sites inside each Composable destination.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            CurioTheme {
                CurioNavHost()
            }
        }
    }
}
