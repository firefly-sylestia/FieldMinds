package com.curio.app.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.View.MeasureSpec
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewTreeLifecycleOwner
import java.io.File
import java.io.FileOutputStream

/**
 * Captures the [card] composable as a PNG bitmap and launches an
 * [Intent.ACTION_SEND] chooser to share it.
 *
 * The composable is rendered off-screen in a temporary [ComposeView] that
 * is given its own [LifecycleOwner] so Compose can drive composition.
 * After a layout pass, the view is drawn to a [Bitmap], saved as PNG in
 * the app's share cache directory, and shared via [FileProvider].
 *
 * Old share images are cleaned up before creating a new one.
 *
 * @param context   Android context (Activity recommended for lifecycle).
 * @param cardSize  Fixed dimensions for the rendered card (e.g. 400×400 dp).
 * @param authority FileProvider authority string (usually `package.fileprovider`).
 * @param card      @Composable lambda that renders the self-contained share card.
 * @param onShared  Optional callback invoked after the chooser is launched.
 */
fun shareComposableCard(
    context: Context,
    cardSize: DpSize,
    authority: String,
    card: @Composable () -> Unit,
    onShared: () -> Unit = {}
) {
    val widthPx  = dpToPx(context, cardSize.width)
    val heightPx = dpToPx(context, cardSize.height)

    // Clean up old share PNGs so the cache doesn't grow unbounded.
    val shareDir = File(context.cacheDir, "share").apply { mkdirs() }
    shareDir.listFiles()?.forEach { it.delete() }

    // Create a synthetic LifecycleOwner so ComposeView has the lifecycle
    // it needs to drive composition even though it's off-screen.
    val lifecycleOwner = SyntheticLifecycleOwner()

    val composeView = ComposeView(context).apply {
        // Provide lifecycle before setting content — required for composition.
        ViewTreeLifecycleOwner.set(this, lifecycleOwner)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        setContent { card() }

        layoutParams = ViewGroup.LayoutParams(widthPx, heightPx)
        val widthSpec  = MeasureSpec.makeMeasureSpec(widthPx,  MeasureSpec.EXACTLY)
        val heightSpec = MeasureSpec.makeMeasureSpec(heightPx, MeasureSpec.EXACTLY)
        measure(widthSpec, heightSpec)
        layout(0, 0, widthPx, heightPx)
    }

    // Move lifecycle to STARTED so Compose begins composition.
    lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
    lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

    // Post the capture to run after the layout/render pass completes.
    composeView.post {
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        // Fill white background so transparent areas don't show as black.
        canvas.drawColor(android.graphics.Color.WHITE)
        composeView.draw(canvas)

        // Save PNG to share cache.
        val file = File(shareDir, "curio_share_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, fos)
        }
        bitmap.recycle()

        // Clean up the synthetic lifecycle.
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

        // Build and launch share intent.
        val uri = FileProvider.getUriForFile(context, authority, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share your Curio card"))
        onShared()
    }
}

private fun dpToPx(context: Context, dp: Dp): Int =
    (dp.value * context.resources.displayMetrics.density).toInt()

/**
 * Minimal [LifecycleOwner] for off-screen [ComposeView] instances.
 *
 * Compose requires a lifecycle to drive recomposition. This synthetic owner
 * provides the minimum lifecycle events needed to compose a view tree,
 * capture it to a bitmap, and tear it down.
 */
private class SyntheticLifecycleOwner : LifecycleOwner {
    private val registry = LifecycleRegistry(this)

    override val lifecycle: Lifecycle get() = registry

    fun handleLifecycleEvent(event: Lifecycle.Event) {
        registry.handleLifecycleEvent(event)
    }
}
