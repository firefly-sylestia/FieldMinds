package com.curio.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.curio.app.data.CaptureRepository
import com.curio.app.data.CurioDatabase
import com.curio.app.data.CurioRepositoryHolder
import com.curio.app.data.TopicJsonLoader
import com.curio.app.infrastructure.CurioCrashReporter
import com.curio.app.navigation.CurioNavHost
import com.curio.app.ui.theme.CurioTheme

/**
 * Curio's single Activity — see CURIO_SPEC.md.
 *
 * Hosts the entire app via [CurioNavHost] inside [CurioTheme]. Edge-to-edge
 * is enabled so the splash background and bottom-nav surface extend behind
 * the system bars (M3 expressive + the spec's "warm cream" feel).
 *
 * Installs [TopicJsonLoader] before any Compose code runs so the loader
 * has access to the AssetManager. Topic JSONs are read lazily on first
 * access; SplashScreen additionally calls [TopicJsonLoader.preloadAll]
 * to warm the cache.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Wire the asset manager into the topic loader before any Compose
        // code runs. Must happen here (not in SplashScreen's LaunchedEffect)
        // because CurioNavHost routes are resolved synchronously on first
        // composition, before the splash coroutine has a chance to run.
        TopicJsonLoader.install(this)

        // Initialize crash reporter before anything else
        CurioCrashReporter.init(this)

        // Initialize Room database and repository singleton
        val db = CurioDatabase.getInstance(this)
        CurioRepositoryHolder.init(db.captureDao())

        setContent {
            CurioTheme {
                CurioNavHost()
            }
        }
    }
}