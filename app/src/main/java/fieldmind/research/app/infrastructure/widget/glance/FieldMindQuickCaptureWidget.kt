package fieldmind.research.app.infrastructure.widget.glance

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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

private val WIDGET_SURFACE_LOW = ColorProvider(Color(0xFFF7FBF7), Color(0xFF17211B))
private val WIDGET_SURFACE = ColorProvider(Color(0xFFEAF3EC), Color(0xFF202A23))
private val WIDGET_SURFACE_HIGH = ColorProvider(Color(0xFFE1ECE4), Color(0xFF2A342D))

private val BRAND_PRIMARY = Color(0xFF1F6B4C)
private val BRAND_ACCENT = Color(0xFF1F6B4C)

/**
 * FieldMind Quick Capture Widget — 2×1 cells
 * Premium glassmorphic design with FieldMind brand colors.
 * One-tap access to observation capture with elegant branding.
 */
class FieldMindQuickCaptureWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val currentSize = LocalSize.current
            GlanceTheme { QuickCaptureUi(currentSize) }
        }
    }

    @Composable
    private fun QuickCaptureUi(size: DpSize) {
        val isWide = size.width.value.toInt() >= 180

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(32.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            // ── Glassmorphic layered background ──
            Box(modifier = GlanceModifier.fillMaxSize().background(WIDGET_SURFACE_LOW).cornerRadius(32.dp)) { }
            Box(modifier = GlanceModifier.fillMaxSize().background(WIDGET_SURFACE).cornerRadius(32.dp)) { }
            // ── Brand accent top bar ──
            Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(ColorProvider(BRAND_ACCENT)).cornerRadius(1.5f.dp)) { }

            Box(modifier = GlanceModifier.fillMaxSize().padding(14.dp)) {
                if (isWide) {
                    Row(modifier = GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                        // Brand icon pill
                        Box(
                            modifier = GlanceModifier.size(48.dp)
                                .background(ColorProvider(BRAND_PRIMARY.copy(alpha = 0.12f)))
                                .cornerRadius(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(provider = ImageProvider(R.drawable.ic_notification), contentDescription = "Capture", modifier = GlanceModifier.size(26.dp))
                        }
                        Spacer(GlanceModifier.width(14.dp))
                        Column(modifier = GlanceModifier.defaultWeight()) {
                            Text("Quick Observe", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GlanceTheme.colors.onSurface))
                            Spacer(GlanceModifier.height(3.dp))
                            Text("Tap to capture", style = TextStyle(fontSize = 11.sp, color = GlanceTheme.colors.onSurfaceVariant))
                        }
                        // Brand green dot
                        Box(modifier = GlanceModifier.size(8.dp).background(ColorProvider(BRAND_PRIMARY)).cornerRadius(4.dp)) { }
                    }
                } else {
                    Column(modifier = GlanceModifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = GlanceModifier.size(40.dp)
                                .background(ColorProvider(BRAND_PRIMARY.copy(alpha = 0.12f)))
                                .cornerRadius(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(provider = ImageProvider(R.drawable.ic_notification), contentDescription = "Capture", modifier = GlanceModifier.size(22.dp))
                        }
                        Spacer(GlanceModifier.height(8.dp))
                        Text("Observe", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GlanceTheme.colors.onSurface))
                    }
                }
            }
        }
    }
}
