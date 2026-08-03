package com.curio.app.features.onboarding

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.curio.app.data.AppPreferences
import com.curio.app.navigation.CurioRoutes
import com.curio.app.ui.components.MorphEntrance
import com.curio.app.ui.theme.CurioGradients
import com.curio.app.ui.theme.CurioIcon
import com.curio.app.ui.theme.CurioIcons
import kotlinx.coroutines.launch

/**
 * First-launch onboarding — see CURIO_SPEC.md §2 (v2).
 *
 * Upgraded with:
 *  - MorphEntrance for each slide content on page change
 *  - Headline + subtext render at once within each slide (no stagger)
 *  - Enhanced illustration block with breathing gradient
 */
@Composable
fun OnboardingScreen(navController: NavController) {
    val pagerState = rememberPagerState(pageCount = { OnboardingSlides.size + 1 })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isLastSlide = pagerState.currentPage == OnboardingSlides.size

    // ── Setup-step permission state ───────────────────────────────────
    var notificationGranted by remember { mutableStateOf(hasNotificationPermission(context)) }
    var micGranted by remember { mutableStateOf(hasMicPermission(context)) }
    // "Display over other apps" — special access for the floating explore
    // bubble. No runtime dialog on Android 10+, so "Allow" opens the system
    // settings page; the ON_RESUME observer picks up the grant on return.
    var overlayGranted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    // "Want the daily shuffle reminder on?" — only reachable once
    // notifications are granted; applied to prefs the moment it flips.
    var reminderWanted by rememberSaveable { mutableStateOf(false) }

    val requestNotifications = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationGranted = granted
        // If they asked for the reminder before granting, it lands now.
        if (granted && reminderWanted) {
            AppPreferences.setReminderEnabled(context, true)
        }
    }
    val requestMic = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> micGranted = granted }
    // The result callback is empty on purpose: `StartActivityForResult`
    // fires while the settings page is still open (permission not yet
    // granted), so the ON_RESUME observer above is the real source of truth.
    val requestOverlay = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ -> }

    fun openOverlaySettings() {
        runCatching {
            requestOverlay.launch(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
            )
        }
    }

    // Re-read permission state when returning from the system Settings
    // screen — users can flip grants mid-session and the cards should
    // reflect reality the moment they come back.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationGranted = hasNotificationPermission(context)
                micGranted = hasMicPermission(context)
                overlayGranted = Settings.canDrawOverlays(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
                if (pageIndex == OnboardingSlides.size) {
                    // Final step: permission setup, not an intro slide.
                    SetupSlide(
                        notificationGranted = notificationGranted,
                        micGranted = micGranted,
                        overlayGranted = overlayGranted,
                        reminderWanted = reminderWanted,
                        onReminderChange = { wanted ->
                            reminderWanted = wanted
                            AppPreferences.setReminderEnabled(context, wanted)
                        },
                        onRequestNotifications = {
                            requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
                        },
                        onRequestMic = {
                            requestMic.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        onRequestOverlay = { openOverlaySettings() }
                    )
                } else {
                    MorphEntrance {
                        OnboardingSlide(slide = OnboardingSlides[pageIndex])
                    }
                }
            }
        }

        // ── Page dots (empty on the final setup step — keeps layout stable) ─
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            if (!isLastSlide) {
                OnboardingSlides.forEachIndexed { index, _ ->
                    val selected = pagerState.currentPage == index
                    PageDot(
                        selected = selected,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } }
                    )
                }
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

        Text(
            text = slide.headline,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = slide.subtext,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SetupSlide(
    notificationGranted: Boolean,
    micGranted: Boolean,
    overlayGranted: Boolean,
    reminderWanted: Boolean,
    onReminderChange: (Boolean) -> Unit,
    onRequestNotifications: () -> Unit,
    onRequestMic: () -> Unit,
    onRequestOverlay: () -> Unit
) {
    // Centered when the content fits, scrollable on very small screens —
    // the Box centers the scrollable column as a whole, so short content
    // stays vertically centered like the intro slides.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        // ── Illustration glyph block ───────────────────────────────────
        Box(
            modifier = Modifier
                .size(140.dp)
                .background(
                    Brush.horizontalGradient(CurioGradients.WildcardGradientStops),
                    shape = RoundedCornerShape(44.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            CurioIcon(
                name = CurioIcons.Settings,
                contentDescription = null,
                tint = Color.White,
                size = 64.dp
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Make Curio yours",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Grant what you like — you can change it anytime in Settings.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        // ── Notifications ─────────────────────────────────────────────
        PermissionCard(
            glyph = CurioIcons.Notifications,
            title = "Notifications",
            subtitle = "Explore-session timer & reminders, plus the daily shuffle nudge",
            granted = notificationGranted,
            onRequest = onRequestNotifications
        )

        // Ask whether the daily shuffle reminder should be on — only once
        // notifications are actually granted (it can't work without them).
        if (notificationGranted) {
            ReminderRow(
                reminderWanted = reminderWanted,
                onReminderChange = onReminderChange
            )
        }

        Spacer(Modifier.height(12.dp))

        // ── Microphone ────────────────────────────────────────────────
        PermissionCard(
            glyph = CurioIcons.Mic,
            title = "Microphone",
            subtitle = "Voice notes (Sound Bite) & voice attachments in your journal",
            granted = micGranted,
            onRequest = onRequestMic
        )

        Spacer(Modifier.height(12.dp))

        // ── Display over other apps (floating explore bubble) ─────────
        PermissionCard(
            glyph = CurioIcons.BubbleChart,
            title = "Display over other apps",
            subtitle = "Floating explore bubble while you research a topic",
            granted = overlayGranted,
            onRequest = onRequestOverlay
        )
        }
    }
}

@Composable
private fun PermissionCard(
    glyph: String,
    title: String,
    subtitle: String,
    granted: Boolean,
    onRequest: () -> Unit
) {
    val accent = MaterialTheme.colorScheme.primary
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (granted) accent.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
                contentAlignment = Alignment.Center
            ) {
                CurioIcon(
                    name = glyph,
                    contentDescription = null,
                    tint = if (granted) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    size = 20.dp
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (granted) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CurioIcon(
                        name = CurioIcons.Check,
                        contentDescription = null,
                        tint = accent,
                        size = 16.dp
                    )
                    Text(
                        "Granted",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = accent
                    )
                }
            } else {
                Button(
                    onClick = onRequest,
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        "Allow",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun ReminderRow(
    reminderWanted: Boolean,
    onReminderChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 4.dp, top = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CurioIcon(
            name = CurioIcons.Schedule,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            size = 18.dp
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Daily shuffle reminder",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "A gentle nudge to discover something new",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Switch(checked = reminderWanted, onCheckedChange = onReminderChange)
    }
}

/** POST_NOTIFICATIONS is a no-op below API 33 — treated as granted. */
private fun hasNotificationPermission(context: Context): Boolean =
    Build.VERSION.SDK_INT < 33 ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

private fun hasMicPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
        PackageManager.PERMISSION_GRANTED

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
        // launchSingleTop dedups the replay path: onboarding is pushed on
        // top of an existing HOME, so without it [HOME, ONBOARDING] → pops
        // onboarding → pushes a second HOME and back walks Home twice.
        launchSingleTop = true
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
        headline = "Shuffle into something new",
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
