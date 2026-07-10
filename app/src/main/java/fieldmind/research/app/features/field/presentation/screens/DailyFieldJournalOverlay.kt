package fieldmind.research.app.features.field.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fieldmind.research.app.features.field.data.settings.FieldMindSettings
import fieldmind.research.app.features.field.presentation.components.*
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * A beautiful half-sheet journal overlay that greets the user with
 * a time-adaptive message, quick capture input, category chips,
 * and a streak display. Shows once per day after onboarding.
 */
@Composable
fun DailyFieldJournalOverlay(
    settings: FieldMindSettings,
    streakCount: Int = 0,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    var quickText by remember { mutableStateOf("") }
    val profileName by settings.profileName.collectAsState()
    val celebrationState = rememberCelebrationState()

    LaunchedEffect(Unit) {
        visible = true
    }

    // Spring animation for the overlay
    val offsetAnim by animateFloatAsState(
        targetValue = if (visible) 0f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "journalOffset"
    )

    // ── Streak milestone celebration ──
    val isStreakMilestone = streakCount in setOf(1, 7, 14, 30, 60, 100, 365)
    LaunchedEffect(streakCount) {
        if (isStreakMilestone) {
            // Small delay so the overlay animation plays first, then sparkles
            kotlinx.coroutines.delay(400)
            celebrationState.trigger(CelebrationVariant.GENTLE_SPARKLE)
        }
    }

    val greeting = getTimeBasedGreeting()
    val icon = getTimeBasedIcon()

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f * offsetAnim))) {
        // Tap background to dismiss
        Box(Modifier.fillMaxSize().clickable { visible = false; onDismiss() })

        // Celebration overlay for streak milestones
        CelebrationOverlay(celebrationState = celebrationState)

        // Journal sheet slides up from bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .align(Alignment.BottomCenter)
                .graphicsLayer {
                    translationY = (1f - offsetAnim) * size.height * 0.15f
                    alpha = offsetAnim
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ),
                        RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
                    )
                    .statusBarsPadding()
            ) {
                // Handle bar
                Box(
                    Modifier.fillMaxWidth().padding(top = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier
                            .width(40.dp).height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                    )
                }

                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Spacer(Modifier.height(8.dp))

                    // Greeting section
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Time-based icon with gradient background
                        Box(
                            Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = when (getTimeOfDay()) {
                                            "morning" -> listOf(Color(0xFFFFB74D), Color(0xFFFF8A65))
                                            "afternoon" -> listOf(Color(0xFF4FC3F7), Color(0xFF29B6F6))
                                            "evening" -> listOf(Color(0xFF7E57C2), Color(0xFF5C6BC0))
                                            else -> listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                                        }
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, null, tint = Color.White, size = 28.dp)
                        }

                        Column(Modifier.weight(1f)) {
                            Text(
                                greeting,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                            Text(
                                getTimeBasedSuggestion(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Quick capture card
                    Card(
                        shape = RoundedCornerShape(30.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "What did you observe?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = quickText,
                                onValueChange = { quickText = it },
                                placeholder = {
                                    Text(
                                        "I saw…",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 60.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    if (quickText.isNotBlank()) {
                                        // TODO: Save quick observation
                                        quickText = ""
                                    }
                                })
                            )

                            // Category chips
                            Text(
                                "Category",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            val categories = listOf(
                                Triple("Bird", FieldMindIcons.Bird, FieldMindTheme.colors.observation),
                                Triple("Plant", FieldMindIcons.Plant, FieldMindTheme.colors.data),
                                Triple("Insect", FieldMindIcons.Insect, FieldMindTheme.colors.categorical[2]),
                                Triple("Weather", FieldMindIcons.Weather, FieldMindTheme.colors.categorical[3]),
                                Triple("Animal", FieldMindIcons.Animal, FieldMindTheme.colors.categorical[1])
                            )

                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                categories.take(3).forEach { (label, catIcon, accent) ->
                                    Surface(
                                        onClick = { /* TODO: quick-capture category */ },
                                        shape = RoundedCornerShape(22.dp),
                                        color = accent.copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, accent.copy(alpha = 0.3f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(catIcon, null, tint = accent, size = 16.dp)
                                            Text(
                                                label,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = accent
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                categories.drop(3).forEach { (label, catIcon, accent) ->
                                    Surface(
                                        onClick = { /* TODO: quick-capture category */ },
                                        shape = RoundedCornerShape(22.dp),
                                        color = accent.copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, accent.copy(alpha = 0.3f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(catIcon, null, tint = accent, size = 16.dp)
                                            Text(
                                                label,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = accent
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }

                    // Streak section
                    Card(
                        shape = RoundedCornerShape(30.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Streak flame icon
                            Box(
                                Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color(0xFFFF6F00).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    FieldMindIcons.Streak, null,
                                    tint = Color(0xFFFF6F00),
                                    size = 26.dp
                                )
                            }

                            Column(Modifier.weight(1f)) {
                                Text(
                                    if (streakCount > 0) "$streakCount-day streak!"
                                    else "Start your streak today",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    if (streakCount > 0)
                                        "Keep it going! Log your first observation."
                                    else
                                        "Log an observation each day to build momentum.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (streakCount > 0) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFFFF6F00).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        "$streakCount",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF6F00),
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Tips section
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                FieldMindIcons.Lightbulb, null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                size = 22.dp
                            )
                            Text(
                                getRandomTip(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }

                // Dismiss button
                Surface(
                    onClick = {
                        visible = false
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                        .height(54.dp)
                        .navigationBarsPadding(),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.primary,
                    tonalElevation = 0.dp
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            if (quickText.isNotBlank()) "Save & start exploring"
                            else "Start exploring",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

// ── Time-based helpers ──

private fun getTimeOfDay(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "morning"
        hour < 17 -> "afternoon"
        else -> "evening"
    }
}

private fun getTimeBasedGreeting(): String {
    return when (getTimeOfDay()) {
        "morning" -> "Good morning"
        "afternoon" -> "Good afternoon"
        "evening" -> "Good evening"
        else -> "Hello"
    }
}

private fun getTimeBasedIcon(): MaterialSymbolIcon {
    return when (getTimeOfDay()) {
        "morning" -> FieldMindIcons.Sunrise
        "afternoon" -> FieldMindIcons.Weather
        "evening" -> FieldMindIcons.MoonFull
        else -> FieldMindIcons.Weather
    }
}

private fun getTimeBasedSuggestion(): String {
    return when (getTimeOfDay()) {
        "morning" -> "A fresh start to your field day. What do you see?"
        "afternoon" -> "Perfect time to log your observations."
        "evening" -> "Time to reflect on today's findings."
        else -> "Ready to explore?"
    }
}

private fun getRandomTip(): String {
    val tips = listOf(
        "Try logging observations at different times of day to spot patterns.",
        "Use the camera to capture visual evidence of your findings.",
        "You can organize observations into projects for deeper analysis.",
        "Tag locations to build a spatial map of your research.",
        "Review your insights tab weekly to track emerging patterns.",
        "Flashcards help reinforce species identification over time.",
        "Record audio notes for hands-free observation logging."
    )
    return tips.random()
}

internal fun getTodayDateString(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return sdf.format(Date())
}

/**
 * Check if the journal overlay should be shown today.
 * Returns true if onboarding is complete, journal is enabled,
 * and the overlay hasn't been shown today yet.
 */
fun shouldShowJournalToday(settings: FieldMindSettings): Boolean {
    val lastDate = settings.journalLastShownDate.value
    val today = getTodayDateString()
    return settings.journalEnabled.value && lastDate != today
}
