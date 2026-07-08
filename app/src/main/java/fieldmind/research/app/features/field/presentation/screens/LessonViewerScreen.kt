package fieldmind.research.app.features.field.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fieldmind.research.app.features.field.data.learn.*
import fieldmind.research.app.features.field.presentation.components.BackButton
import fieldmind.research.app.features.field.presentation.components.FieldMindIcons
import fieldmind.research.app.features.field.presentation.components.StandardScreenHeader
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon
import kotlinx.coroutines.launch

/**
 * Full-screen lesson viewer that renders an [AppLesson] with sections,
 * steps, callouts, examples, and key takeaways.
 */
@Composable
fun LessonViewerScreen(
    lesson: AppLesson,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Header ──
        StandardScreenHeader(
            title = lesson.title,
            subtitle = "${lesson.level} · ${lesson.estimatedTime}",
            icon = MaterialSymbolIcon(lesson.iconName),
            trailing = {
                BackButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(20.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                    contentDescription = "Back"
                )
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Summary card ──
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        icon = MaterialSymbolIcon(lesson.iconName),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        size = 32.dp,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .padding(10.dp)
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            lesson.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LevelBadge(lesson.level)
                            InfoChip2(lesson.estimatedTime, icon = MaterialSymbolIcon("schedule"))
                        }
                    }
                }
            }

            // ── Lesson sections ──
            lesson.sections.forEachIndexed { index, section ->
                LessonSectionBlock(section, index)
            }

            // ── Divider before takeaways ──
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // ── Key Takeaways ──
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            icon = MaterialSymbolIcon("key"),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            size = 20.dp
                        )
                        Text(
                            "Key Takeaways",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    lesson.keyTakeaways.forEach { takeaway ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "✓",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                takeaway,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // ── Practice Challenge (if present) ──
            if (lesson.practiceChallenge.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                icon = MaterialSymbolIcon("emoji_objects"),
                                contentDescription = null,
                                tint = FieldMindTheme.colors.flashcard,
                                size = 22.dp
                            )
                            Text(
                                "Practice Challenge",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = FieldMindTheme.colors.flashcard
                            )
                        }
                        Text(
                            lesson.practiceChallenge,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // ── Bottom spacer ──
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LevelBadge(level: String) {
    val (bgColor, textColor) = when (level.lowercase()) {
        "beginner" -> FieldMindTheme.colors.positive.copy(alpha = 0.12f) to FieldMindTheme.colors.positive
        "intermediate" -> FieldMindTheme.colors.flashcard.copy(alpha = 0.12f) to FieldMindTheme.colors.flashcard
        "advanced" -> FieldMindTheme.colors.warning.copy(alpha = 0.12f) to FieldMindTheme.colors.warning
        else -> MaterialTheme.colorScheme.surfaceContainerHighest to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgColor
    ) {
        Text(
            level,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun InfoChip2(text: String, icon: MaterialSymbolIcon? = null) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 12.dp)
            }
            Text(
                text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Section renderers
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun LessonSectionBlock(section: LessonSection, index: Int) {
    when (section) {
        is TextSection -> TextSectionBlock(section)
        is StepSection -> StepSectionBlock(section)
        is BulletSection -> BulletSectionBlock(section)
        is CalloutSection -> CalloutSectionBlock(section)
        is ExampleSection -> ExampleSectionBlock(section)
        is CodeBlockSection -> CodeBlockSectionBlock(section)
        is ComparisonSection -> ComparisonSectionBlock(section)
    }
}

@Composable
private fun TextSectionBlock(section: TextSection) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (section.heading.isNotBlank()) {
            Text(
                section.heading,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        // Parse **bold** and *italic* markers
        val annotated = buildAnnotatedString {
            var remaining = section.body
            while (remaining.isNotEmpty()) {
                val boldStart = remaining.indexOf("**")
                val italicStart = remaining.indexOf("*")
                val firstMarker = listOfNotNull(
                    boldStart.takeIf { it >= 0 }?.let { it to "bold" },
                    italicStart.takeIf { it >= 0 && it != boldStart }?.let { it to "italic" }
                ).minByOrNull { it.first }

                if (firstMarker == null) {
                    append(remaining)
                    break
                }

                // Text before marker
                if (firstMarker.first > 0) {
                    append(remaining.substring(0, firstMarker.first))
                }

                val markerLen = if (firstMarker.second == "bold") 2 else 1
                val endMarker = remaining.indexOf(
                    if (firstMarker.second == "bold") "**" else "*",
                    firstMarker.first + markerLen
                )
                if (endMarker < 0) {
                    append(remaining.substring(firstMarker.first))
                    break
                }

                val inner = remaining.substring(firstMarker.first + markerLen, endMarker)
                if (firstMarker.second == "bold") {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(inner) }
                } else {
                    withStyle(SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)) { append(inner) }
                }
                remaining = remaining.substring(endMarker + markerLen)
            }
        }
        Text(
            annotated,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun StepSectionBlock(section: StepSection) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (section.heading.isNotBlank()) {
            Text(
                section.heading,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        section.steps.forEachIndexed { idx, step ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Numbered circle
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "${idx + 1}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        step,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun BulletSectionBlock(section: BulletSection) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (section.heading.isNotBlank()) {
            Text(
                section.heading,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        section.items.forEach { item ->
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text(
                    "•",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp
                )
                Text(
                    item,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
private fun CalloutSectionBlock(section: CalloutSection) {
    val (bgColor, accentColor, icon) = when (section.calloutType) {
        "tip" -> Triple(
            FieldMindTheme.colors.flashcard.copy(alpha = 0.08f),
            FieldMindTheme.colors.flashcard,
            MaterialSymbolIcon("lightbulb")
        )
        "warning" -> Triple(
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.error,
            MaterialSymbolIcon("warning")
        )
        "example" -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
            MaterialTheme.colorScheme.tertiary,
            MaterialSymbolIcon("spa")
        )
        else -> Triple(
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
            MaterialTheme.colorScheme.secondary,
            MaterialSymbolIcon("info")
        )
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = bgColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, size = 18.dp)
                Text(
                    section.calloutType.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor
                )
                if (section.heading.isNotBlank() && section.heading != section.calloutType.replaceFirstChar { it.uppercase() }) {
                    Text(
                        "· ${section.heading}",
                        style = MaterialTheme.typography.labelMedium,
                        color = accentColor.copy(alpha = 0.8f)
                    )
                }
            }
            Text(
                section.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun ExampleSectionBlock(section: ExampleSection) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Scenario
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    icon = MaterialSymbolIcon("preview"),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    size = 18.dp
                )
                Text(
                    "Scenario",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                section.scenario,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )

            if (section.goodExample.isNotBlank()) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        icon = MaterialSymbolIcon("check_circle"),
                        contentDescription = null,
                        tint = FieldMindTheme.colors.positive,
                        size = 18.dp
                    )
                    Text(
                        "Good",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = FieldMindTheme.colors.positive
                    )
                }
                Text(
                    section.goodExample,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
            }

            if (section.badExample.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        icon = MaterialSymbolIcon("cancel"),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        size = 18.dp
                    )
                    Text(
                        "Avoid",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Text(
                    section.badExample,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun CodeBlockSectionBlock(section: CodeBlockSection) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (section.heading.isNotBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    icon = MaterialSymbolIcon("code"),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    size = 16.dp
                )
                Text(
                    section.heading,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                section.code,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(14.dp)
            )
        }
    }
}

@Composable
private fun ComparisonSectionBlock(section: ComparisonSection) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (section.heading.isNotBlank()) {
            Text(
                section.heading,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Column headers
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = FieldMindTheme.colors.positive.copy(alpha = 0.1f),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    section.leftLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = FieldMindTheme.colors.positive,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    section.rightLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        // Rows
        section.rows.forEach { (left, right) ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        left,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        lineHeight = 18.sp
                    )
                    Box(
                        Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    )
                    Text(
                        right,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
