package fieldmind.research.app.infrastructure.widget.glance

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import fieldmind.research.app.activities.MainActivity
import fieldmind.research.app.R

/**
 * Quick Capture Widget — 2×1 cells
 * Glassmorphic card with accent top bar, one-tap observation capture.
 * Matches the compass/level premium aesthetic.
 */
class FieldMindQuickCaptureWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val currentSize = LocalSize.current
            GlanceTheme {
                QuickCaptureUi(currentSize)
            }
        }
    }

    @Composable
    private fun QuickCaptureUi(size: DpSize) {
        val minWidth = size.width.value.toInt()
        val isWide = minWidth >= 180

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(32.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            // Glass background — surfaceVariant for depth, matching compass card style
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(GlanceTheme.colors.surfaceVariant)
                    .cornerRadius(32.dp)
            )
            // Accent top bar — primary purple stripe
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(ColorProvider(0xFF6750A4))
                    .cornerRadius(1.5f.dp),
                contentAlignment = Alignment.TopCenter
            )
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (isWide) {
                    Row(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon with primary container pill
                        Box(
                            modifier = GlanceModifier
                                .size(52.dp)
                                .background(GlanceTheme.colors.primaryContainer)
                                .cornerRadius(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_notification),
                                contentDescription = "Capture",
                                modifier = GlanceModifier.size(28.dp)
                            )
                        }
                        Spacer(GlanceModifier.width(14.dp))
                        Column(modifier = GlanceModifier.defaultWeight()) {
                            Text(
                                text = "Quick Observe",
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GlanceTheme.colors.onSurface
                                )
                            )
                            Spacer(GlanceModifier.height(3.dp))
                            Text(
                                text = "Tap to capture an observation",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = GlanceTheme.colors.onSurfaceVariant
                                )
                            )
                        }
                        // Accent dot
                        Box(
                            modifier = GlanceModifier
                                .size(8.dp)
                                .background(GlanceTheme.colors.primary)
                                .cornerRadius(4.dp)
                        )
                    }
                } else {
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = GlanceModifier
                                .size(44.dp)
                                .background(GlanceTheme.colors.primaryContainer)
                                .cornerRadius(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_notification),
                                contentDescription = "Capture",
                                modifier = GlanceModifier.size(24.dp)
                            )
                        }
                        Spacer(GlanceModifier.height(10.dp))
                        Text(
                            text = "Observe",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlanceTheme.colors.onSurface
                            )
                        )
                        Spacer(GlanceModifier.height(2.dp))
                        Text(
                            text = "Tap",
                            style = TextStyle(
                                fontSize = 10.sp,
                                color = GlanceTheme.colors.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}
