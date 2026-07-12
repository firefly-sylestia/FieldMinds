package fieldmind.research.app.features.field.presentation.screens
import fieldmind.research.app.ui.theme.CuteCardDefaults

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt
import fieldmind.research.app.features.field.data.settings.*
import fieldmind.research.app.features.field.presentation.components.FieldMindIcons
import fieldmind.research.app.features.field.presentation.components.FieldMindLogo
import fieldmind.research.app.features.field.presentation.components.FieldMindMotion
import fieldmind.research.app.features.field.presentation.components.expressivePress
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import fieldmind.research.app.ui.theme.CuteElevations
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon

// ══════════════════════════════════════════════════════════════════════
//  Onboarding Entry Point - Fresh 5-Step Wizard
// ══════════════════════════════════════════════════════════════════════

@Composable
fun FieldMindOnboardingScreen(
    settings: FieldMindSettings,
    onFinish: () -> Unit
) {
    var currentPage by remember { mutableIntStateOf(0) }

    var profileName by remember { mutableStateOf("") }
    var profileRole by remember { mutableStateOf("Field learner") }
    var interests by remember { mutableStateOf(UserInterests()) }
    var onboardingFrequency by remember { mutableStateOf("A few times a week") }
    var onboardingLayoutStyle by remember { mutableStateOf("Guided journal") }

    var cameraGranted by remember { mutableStateOf(false) }
    var locationGranted by remember { mutableStateOf(false) }
    var audioGranted by remember { mutableStateOf(false) }
    var notificationGranted by remember { mutableStateOf(false) }

    var selectedTheme by remember { mutableStateOf("System") }
    var useDynamicColors by remember { mutableStateOf(false) }
    var tempUnit by remember { mutableStateOf("Celsius") }
    var distanceUnit by remember { mutableStateOf("km") }
    var timeFormat by remember { mutableStateOf("24h") }
    var dailyGoal by remember { mutableIntStateOf(1) }

    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        cameraGranted = granted
    }
    val locationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        locationGranted = result.values.any { it }
    }
    val audioLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        audioGranted = granted
    }
    val notificationLauncher = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            notificationGranted = granted
        }
    } else null

    LaunchedEffect(Unit) {
        cameraGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        locationGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        audioGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            notificationGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            notificationGranted = true
        }
    }

    fun finishOnboarding() {
        settings.setProfileName(profileName)
        settings.setProfileRole(profileRole)
        settings.setProfileFocus(
            buildList {
                if (interests.zoology.isNotEmpty()) add("Zoology: ${interests.zoology.joinToString { it.displayName }}")
                if (interests.botany.isNotEmpty()) add("Botany: ${interests.botany.joinToString { it.displayName }}")
                if (interests.ecologyEnvironment) add("Ecology & Environment")
                if (interests.astronomy) add("Astronomy")
                if (interests.geology) add("Geology")
                interests.customInterests.forEach { add(it) }
            }.joinToString(", ").ifEmpty { "Wildlife & ecology" }
        )
        settings.setUserInterests(interests)
        val derivedVis = ScreenVisibility.fromInterests(interests)
        settings.setScreenVisibility(derivedVis)
        settings.setThemeMode(selectedTheme)
        settings.setDynamicColorEnabled(useDynamicColors)
        settings.setTempUnit(tempUnit)
        settings.setDistanceUnit(distanceUnit)
        settings.setTimeFormat(timeFormat)
        settings.setDailyObservationGoal(dailyGoal)
        settings.setMediaAttachmentsEnabled(cameraGranted)
        settings.setAudioRecordingEnabled(audioGranted)
        settings.setOnboardingFrequency(onboardingFrequency)
        settings.setOnboardingLayoutStyle(onboardingLayoutStyle)
        onFinish()
    }

    BackHandler(enabled = currentPage > 0) {
        currentPage--
    }

    val systemDark = isSystemInDarkTheme()
    val isDarkTheme = when (selectedTheme) {
        "Dark" -> true
        "Light" -> false
        else -> systemDark
    }

    FieldMindTheme(darkTheme = isDarkTheme, dynamicColor = useDynamicColors) {
    BoxWithConstraints(Modifier.fillMaxSize().statusBarsPadding()) {
        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                val direction = if (targetState > initialState) 1 else -1
                (slideInHorizontally(
                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                    initialOffsetX = { direction * it / 3 }
                ) + fadeIn(tween(300)))
                    .togetherWith(
                        slideOutHorizontally(
                            animationSpec = tween(300),
                            targetOffsetX = { -direction * it / 4 }
                        ) + fadeOut(tween(200))
                    )
            },
            label = "onboardingPage"
        ) { page ->
            when (page) {
                0 -> OnboardingWelcomePage(
                    name = profileName,
                    onNameChange = { profileName = it },
                    role = profileRole,
                    onRoleChange = { profileRole = it },
                    frequency = onboardingFrequency,
                    onFrequencyChange = { onboardingFrequency = it },
                    onNext = { currentPage = 1 }
                )
                1 -> OnboardingInterestsPage(
                    interests = interests,
                    onInterestsChange = { interests = it },
                    onNext = { currentPage = 2 },
                    onBack = { currentPage = 0 }
                )
                2 -> OnboardingPermissionsPage(
                    cameraGranted = cameraGranted,
                    locationGranted = locationGranted,
                    audioGranted = audioGranted,
                    notificationGranted = notificationGranted,
                    onRequestCamera = { cameraLauncher.launch(Manifest.permission.CAMERA) },
                    onRequestLocation = { locationLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) },
                    onRequestAudio = { audioLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    onRequestNotification = {
                        if (notificationLauncher != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    onNext = { currentPage = 3 },
                    onBack = { currentPage = 1 }
                )
                3 -> OnboardingThemePage(
                    selectedTheme = selectedTheme,
                    useDynamicColors = useDynamicColors,
                    tempUnit = tempUnit,
                    distanceUnit = distanceUnit,
                    timeFormat = timeFormat,
                    dailyGoal = dailyGoal,
                    layoutStyle = onboardingLayoutStyle,
                    onLayoutStyleChange = { onboardingLayoutStyle = it },
                    onThemeChange = { selectedTheme = it },
                    onDynamicColorsChange = { useDynamicColors = it },
                    onTempUnitChange = { tempUnit = it },
                    onDistanceUnitChange = { distanceUnit = it },
                    onTimeFormatChange = { timeFormat = it },
                    onDailyGoalChange = { dailyGoal = it },
                    onNext = { currentPage = 4 },
                    onBack = { currentPage = 2 }
                )
                4 -> OnboardingReviewPage(
                    profileName = profileName,
                    profileRole = profileRole,
                    interests = interests,
                    cameraGranted = cameraGranted,
                    locationGranted = locationGranted,
                    audioGranted = audioGranted,
                    selectedTheme = selectedTheme,
                    dailyGoal = dailyGoal,
                    layoutStyle = onboardingLayoutStyle,
                    frequency = onboardingFrequency,
                    onFinish = { finishOnboarding() },
                    onBack = { currentPage = 3 },
                    onEditPage = { currentPage = it }
                )
            }
        }
    }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Page Indicator Dots
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun PageIndicator(current: Int, total: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { i ->
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(if (i == current) 24.dp else 8.dp)
                    .graphicsLayer { alpha = if (i == current) 1f else 0.4f }
                    .background(
                        if (i <= current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                        RoundedCornerShape(8.dp)
                    )
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Screen 0: Welcome, Identity & Frequency
// ══════════════════════════════════════════════════════════════════════

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun OnboardingWelcomePage(
    name: String,
    onNameChange: (String) -> Unit,
    role: String,
    onRoleChange: (String) -> Unit,
    frequency: String,
    onFrequencyChange: (String) -> Unit,
    onNext: () -> Unit
) {
    val roles = listOf("Field learner", "Citizen scientist", "Biology student", "Educator", "Professional researcher", "Hobbyist naturalist", "Conservationist")
    val frequencies = listOf("Daily", "A few times a week", "Weekends", "Spontaneously")
    var roleExpanded by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { showContent = true }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(Modifier.size(280.dp).offset(x = 60.dp, y = (-80).dp)
            .graphicsLayer { alpha = 0.08f }
            .background(Brush.radialGradient(listOf(MaterialTheme.colorScheme.primary, Color.Transparent)), CircleShape))
        Box(Modifier.size(200.dp).offset(x = (-40).dp, y = 100.dp)
            .graphicsLayer { alpha = 0.06f }
            .background(Brush.radialGradient(listOf(MaterialTheme.colorScheme.tertiary, Color.Transparent)), CircleShape))

        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f).padding(top = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AnimatedVisibility(visible = showContent,
                    enter = fadeIn(tween(400)) + scaleIn(initialScale = 0.8f, animationSpec = tween(400))) {
                    Box(Modifier.size(72.dp).clip(CuteCardDefaults.Shape)
                        .background(Brush.linearGradient(
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer))),
                        contentAlignment = Alignment.Center) {
                        FieldMindLogo(size = 64.dp)
                    }
                }
                AnimatedVisibility(visible = showContent,
                    enter = fadeIn(FieldMindMotion.expressiveFloat) + slideInVertically { it / 3 }) {
                    Text("Welcome to\nFieldMind",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.ExtraBold, lineHeight = 40.sp))
                }
                AnimatedVisibility(visible = showContent,
                    enter = fadeIn(FieldMindMotion.expressiveFloat) + slideInVertically { it / 4 }) {
                    Text("Your personal field research companion.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(8.dp))

                AnimatedVisibility(visible = showContent,
                    enter = fadeIn(FieldMindMotion.expressiveFloat) + slideInVertically { it / 4 }) {
                    OutlinedTextField(value = name, onValueChange = onNameChange,
                        label = { Text("Your name (optional)") },
                        placeholder = { Text("e.g. Alex") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = CuteCardDefaults.FieldShape, singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                        leadingIcon = { Icon(FieldMindIcons.User, null, size = 20.dp) })
                }

                AnimatedVisibility(visible = showContent,
                    enter = fadeIn(FieldMindMotion.expressiveFloat) + slideInVertically { it / 5 }) {
                    ExposedDropdownMenuBox(expanded = roleExpanded,
                        onExpandedChange = { roleExpanded = it }) {
                        OutlinedTextField(value = role, onValueChange = {}, readOnly = true,
                            label = { Text("I am a…") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = roleExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = CuteCardDefaults.FieldShape,
                            leadingIcon = { Icon(FieldMindIcons.School, null, size = 20.dp) })
                        ExposedDropdownMenu(expanded = roleExpanded,
                            onDismissRequest = { roleExpanded = false }) {
                            roles.forEach { option ->
                                DropdownMenuItem(text = { Text(option) },
                                    onClick = { onRoleChange(option); roleExpanded = false })
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 16.dp)) {
                Text("How often do you go out?",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)
                Row(Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    frequencies.forEach { freq ->
                        val isSelected = frequency == freq
                        Surface(onClick = { onFrequencyChange(freq) },
                            shape = MaterialTheme.shapes.medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.12f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = if (isSelected) BorderStroke(1.5.dp,
                                MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier.weight(1f)) {
                            Text(freq, modifier = Modifier.padding(vertical = 10.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center)
                        }
                    }
                }
                PageIndicator(current = 0, total = 5)
                Button(onClick = onNext,
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                        .expressivePress(scaleDown = 0.96f),
                    shape = CuteCardDefaults.FieldShape) {
                    Text("Get started", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(Modifier.size(8.dp))
                    Icon(FieldMindIcons.Forward, null, size = 20.dp)
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Screen 1: Fields of Interest
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun OnboardingInterestsPage(
    interests: UserInterests,
    onInterestsChange: (UserInterests) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    var customInput by remember { mutableStateOf("") }
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { showContent = true }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Spacer(Modifier.height(8.dp))
                AnimatedVisibility(visible = showContent,
                    enter = fadeIn() + slideInVertically { it / 3 }) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("What do you study?",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold)
                        Text("Select all that apply.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                AnimatedVisibility(visible = showContent,
                    enter = fadeIn(FieldMindMotion.expressiveFloat)) {
                    InterestChipGroup(title = "Zoology",
                        icon = FieldMindIcons.Nature,
                        accent = FieldMindTheme.colors.observation,
                        options = ZoologySubfield.all().toList(),
                        selectedOptions = interests.zoology.toList(),
                        onToggle = { field ->
                            val current = interests.zoology.toMutableSet()
                            if (field in current) current.remove(field) else current.add(field)
                            onInterestsChange(interests.copy(zoology = current))
                        })
                }

                AnimatedVisibility(visible = showContent,
                    enter = fadeIn(FieldMindMotion.expressiveFloat)) {
                    InterestChipGroup(title = "Botany",
                        icon = FieldMindIcons.Nature,
                        accent = FieldMindTheme.colors.data,
                        options = BotanySubfield.all().toList(),
                        selectedOptions = interests.botany.toList(),
                        onToggle = { field ->
                            val current = interests.botany.toMutableSet()
                            if (field in current) current.remove(field) else current.add(field)
                            onInterestsChange(interests.copy(botany = current))
                        })
                }

                AnimatedVisibility(visible = showContent,
                    enter = fadeIn(FieldMindMotion.expressiveFloat)) {
                    OtherFieldsSection(interests = interests,
                        onInterestsChange = onInterestsChange)
                }

                AnimatedVisibility(visible = showContent,
                    enter = fadeIn(FieldMindMotion.expressiveFloat)) {
                    CustomInterestSection(interests = interests,
                        onInterestsChange = onInterestsChange,
                        customInput = customInput,
                        onCustomInputChange = { customInput = it })
                }
            }

            Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = onBack,
                    shape = CuteCardDefaults.ShapeCompact) { Text("Back") }
                PageIndicator(current = 1, total = 5,
                    modifier = Modifier.weight(1f))
                Button(onClick = onNext,
                    shape = CuteCardDefaults.ShapeCompact,
                    modifier = Modifier.height(48.dp)) { Text("Continue") }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun <T> InterestChipGroup(
    title: String, icon: MaterialSymbolIcon, accent: Color,
    options: List<T>, selectedOptions: List<T>, onToggle: (T) -> Unit
) where T : Enum<T> {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, tint = accent, size = 20.dp)
            Text(title, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = accent)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { field ->
                val isSelected = field in selectedOptions
                val fieldName = (field as? ZoologySubfield)?.displayName
                    ?: (field as? BotanySubfield)?.displayName ?: field.name
                Surface(onClick = { onToggle(field) },
                    shape = CuteCardDefaults.ButtonShape,
                    color = if (isSelected) accent.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceContainerHighest,
                    border = if (isSelected) BorderStroke(1.5.dp,
                        accent.copy(alpha = 0.4f)) else null) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(if (isSelected) FieldMindIcons.Check
                            else FieldMindIcons.Add, null,
                            tint = if (isSelected) accent
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            size = 16.dp)
                        Text(fieldName,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold
                                else FontWeight.Normal)
                    }
                }
            }
        }
    }
}

@Composable
private fun OtherFieldsSection(
    interests: UserInterests,
    onInterestsChange: (UserInterests) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Other fields", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        val otherFields = listOf(
            Triple("Ecology", FieldMindIcons.Weather,
                FieldMindTheme.colors.categorical[3]),
            Triple("Astronomy", FieldMindIcons.MoonFull,
                FieldMindTheme.colors.categorical[4]),
            Triple("Geology", FieldMindIcons.Rock,
                FieldMindTheme.colors.categorical[9]))
        otherFields.forEach { (label, icon, accent) ->
            val isSelected = when (label) {
                "Ecology" -> interests.ecologyEnvironment
                "Astronomy" -> interests.astronomy
                "Geology" -> interests.geology
                else -> false
            }
            Surface(onClick = {
                onInterestsChange(when (label) {
                    "Ecology" -> interests.copy(
                        ecologyEnvironment = !isSelected)
                    "Astronomy" -> interests.copy(
                        astronomy = !isSelected)
                    "Geology" -> interests.copy(
                        geology = !isSelected)
                    else -> interests
                })
            }, shape = CuteCardDefaults.ShapeCompact,
                color = if (isSelected) accent.copy(alpha = 0.12f)
                    else MaterialTheme.colorScheme.surfaceContainerHigh,
                border = if (isSelected) BorderStroke(1.5.dp,
                    accent.copy(alpha = 0.5f)) else null,
                modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(36.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(accent.copy(
                            if (isSelected) 0.22f else 0.1f)),
                        contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = accent, size = 20.dp)
                    }
                    Text(label, style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f))
                    if (isSelected)
                        Icon(FieldMindIcons.Check, null,
                            tint = accent, size = 22.dp)
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun CustomInterestSection(
    interests: UserInterests,
    onInterestsChange: (UserInterests) -> Unit,
    customInput: String,
    onCustomInputChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Custom interests",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(value = customInput,
            onValueChange = onCustomInputChange,
            label = { Text("Add custom field") },
            placeholder = { Text("e.g. Mycology, Entomology…") },
            modifier = Modifier.fillMaxWidth(),
            shape = CuteCardDefaults.ShapeCompact, singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                if (customInput.isNotBlank()) {
                    onInterestsChange(interests.copy(
                        customInterests = interests.customInterests + customInput.trim()))
                    onCustomInputChange("")
                }
            }))
        if (interests.customInterests.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                interests.customInterests.forEach { item ->
                    InputChip(selected = true,
                        onClick = {
                            onInterestsChange(interests.copy(
                                customInterests = interests.customInterests - item))
                        },
                        label = { Text(item,
                            style = MaterialTheme.typography.labelMedium) },
                        trailingIcon = { Icon(FieldMindIcons.Close,
                            null, size = 16.dp) },
                        shape = MaterialTheme.shapes.medium)
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Screen 2: Permissions
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun OnboardingPermissionsPage(
    cameraGranted: Boolean, locationGranted: Boolean,
    audioGranted: Boolean, notificationGranted: Boolean,
    onRequestCamera: () -> Unit, onRequestLocation: () -> Unit,
    onRequestAudio: () -> Unit, onRequestNotification: () -> Unit,
    onNext: () -> Unit, onBack: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { showContent = true }

    Box(Modifier.fillMaxSize().background(
        MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)
                .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Spacer(Modifier.height(8.dp))
                AnimatedVisibility(visible = showContent,
                    enter = fadeIn() + slideInVertically { it / 3 }) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Permissions",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold)
                        Text("Grant later from Settings.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                val items = listOf(
                    PermItem("Camera", "Photos of observations",
                        FieldMindIcons.Camera,
                        FieldMindTheme.colors.observation,
                        cameraGranted, onRequestCamera),
                    PermItem("Location", "Auto-tag GPS + weather",
                        FieldMindIcons.Location,
                        MaterialTheme.colorScheme.primary,
                        locationGranted, onRequestLocation),
                    PermItem("Microphone", "Hands-free audio notes",
                        FieldMindIcons.Mic,
                        FieldMindTheme.colors.question,
                        audioGranted, onRequestAudio),
                    PermItem("Notifications", "Reminders & streaks",
                        FieldMindIcons.Notifications,
                        FieldMindTheme.colors.warning,
                        notificationGranted, onRequestNotification))

                items.forEach { item ->
                    AnimatedVisibility(visible = showContent,
                        enter = fadeIn(FieldMindMotion.expressiveFloat)
                            + slideInVertically { it / 3 }) {
                        PermissionCard(item)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            Row(Modifier.fillMaxWidth()
                .padding(top = 12.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = onBack,
                    shape = CuteCardDefaults.ShapeCompact) { Text("Back") }
                PageIndicator(current = 2, total = 5,
                    modifier = Modifier.weight(1f))
                Button(onClick = onNext,
                    shape = CuteCardDefaults.ShapeCompact,
                    modifier = Modifier.height(48.dp)) { Text("Continue") }
            }
        }
    }
}

private data class PermItem(
    val title: String, val description: String,
    val icon: MaterialSymbolIcon, val accent: Color,
    val granted: Boolean, val onRequest: () -> Unit
)

@Composable
private fun PermissionCard(item: PermItem) {
    Card(shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.granted) item.accent.copy(
                alpha = 0.08f)
                else MaterialTheme.colorScheme.surfaceContainerHigh),
        border = if (item.granted) BorderStroke(1.dp,
            item.accent.copy(alpha = 0.3f)) else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(44.dp).clip(CuteCardDefaults.ButtonShape)
                .background(if (item.granted) item.accent.copy(
                    alpha = 0.2f) else item.accent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center) {
                Icon(item.icon, null,
                    tint = if (item.granted) item.accent
                        else item.accent.copy(alpha = 0.7f),
                    size = 22.dp)
            }
            Column(Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold)
                    if (item.granted) {
                        Surface(shape = CuteCardDefaults.ChipShape,
                            color = item.accent.copy(alpha = 0.15f)) {
                            Text("Granted",
                                style = MaterialTheme.typography.labelSmall,
                                color = item.accent,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(
                                    horizontal = 8.dp, vertical = 2.dp))
                        }
                    }
                }
                Text(item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (!item.granted) {
                Button(onClick = item.onRequest,
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(
                        horizontal = 16.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = item.accent)) {
                    Text("Grant") }
            } else {
                Icon(FieldMindIcons.Check, null,
                    tint = item.accent, size = 24.dp)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Screen 3: Theme, Layout & Quick Settings
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun OnboardingThemePage(
    selectedTheme: String, useDynamicColors: Boolean,
    tempUnit: String, distanceUnit: String, timeFormat: String,
    dailyGoal: Int,
    layoutStyle: String, onLayoutStyleChange: (String) -> Unit,
    onThemeChange: (String) -> Unit,
    onDynamicColorsChange: (Boolean) -> Unit,
    onTempUnitChange: (String) -> Unit,
    onDistanceUnitChange: (String) -> Unit,
    onTimeFormatChange: (String) -> Unit,
    onDailyGoalChange: (Int) -> Unit,
    onNext: () -> Unit, onBack: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { showContent = true }

    Box(Modifier.fillMaxSize().background(
        MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)
                .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Spacer(Modifier.height(8.dp))
                AnimatedVisibility(visible = showContent,
                    enter = fadeIn() + slideInVertically { it / 3 }) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Make it yours",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold)
                        Text("Choose your look, layout, and goal.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                AnimatedVisibility(visible = showContent,
                    enter = fadeIn(FieldMindMotion.expressiveFloat)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Home screen style",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                        Row(Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf("Simple", "Guided journal",
                                "Data-focused").forEach { style ->
                                val sel = layoutStyle == style
                                Surface(onClick = {
                                    onLayoutStyleChange(style) },
                                    shape = CuteCardDefaults.ShapeCompact,
                                    color = if (sel) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceContainerHighest,
                                    border = if (sel) BorderStroke(2.dp,
                                        MaterialTheme.colorScheme.primary) else null,
                                    modifier = Modifier.weight(1f)
                                        .height(72.dp)) {
                                    Column(Modifier.fillMaxSize()
                                        .padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center) {
                                        Icon(when (style) {
                                            "Simple" -> FieldMindIcons.Check
                                            "Guided journal" -> FieldMindIcons.Article
                                            else -> FieldMindIcons.Data
                                        }, null,
                                            tint = if (sel) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                            size = 24.dp)
                                        Text(style,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (sel) FontWeight.Bold
                                                else FontWeight.Normal)
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(Modifier.padding(vertical = 2.dp))

                AnimatedVisibility(visible = showContent,
                    enter = fadeIn(FieldMindMotion.expressiveFloat)) {
                    ThemeSelector(selectedTheme = selectedTheme,
                        onThemeChange = onThemeChange)
                }

                AnimatedVisibility(visible = showContent,
                    enter = fadeIn(FieldMindMotion.expressiveFloat)) {
                    Row(Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text("Use device colors",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold)
                            Text("Match your wallpaper palette",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = useDynamicColors,
                            onCheckedChange = onDynamicColorsChange)
                    }
                }

                HorizontalDivider(Modifier.padding(vertical = 2.dp))

                AnimatedVisibility(visible = showContent,
                    enter = fadeIn(FieldMindMotion.expressiveFloat)) {
                    UnitsSelector(tempUnit = tempUnit,
                        onTempUnitChange = onTempUnitChange,
                        distanceUnit = distanceUnit,
                        onDistanceUnitChange = onDistanceUnitChange,
                        timeFormat = timeFormat,
                        onTimeFormatChange = onTimeFormatChange)
                }

                HorizontalDivider(Modifier.padding(vertical = 2.dp))

                AnimatedVisibility(visible = showContent,
                    enter = fadeIn(FieldMindMotion.expressiveFloat)) {
                    DailyGoalSlider(dailyGoal = dailyGoal,
                        onDailyGoalChange = onDailyGoalChange)
                }
                Spacer(Modifier.height(8.dp))
            }

            Row(Modifier.fillMaxWidth()
                .padding(top = 12.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = onBack,
                    shape = CuteCardDefaults.ShapeCompact) { Text("Back") }
                PageIndicator(current = 3, total = 5,
                    modifier = Modifier.weight(1f))
                Button(onClick = onNext,
                    shape = CuteCardDefaults.ShapeCompact,
                    modifier = Modifier.height(48.dp)) { Text("Continue") }
            }
        }
    }
}

@Composable
private fun ThemeSelector(selectedTheme: String,
    onThemeChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Theme", style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("System", "Light", "Dark").forEach { theme ->
                val sel = selectedTheme == theme
                Surface(onClick = { onThemeChange(theme) },
                    shape = CuteCardDefaults.ShapeCompact,
                    color = when {
                        sel && theme == "Dark" -> Color(0xFF1A1A2E)
                        sel && theme == "Light" -> Color(0xFFF5F0E8)
                        sel -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceContainerHighest
                    },
                    border = if (sel) BorderStroke(2.dp,
                        MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier.weight(1f).height(72.dp)) {
                    Column(Modifier.fillMaxSize().padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                        Icon(when (theme) {
                            "System" -> FieldMindIcons.Settings
                            "Light" -> FieldMindIcons.Weather
                            else -> FieldMindIcons.MoonFull
                        }, null,
                            tint = if (sel) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            size = 24.dp)
                        Text(theme,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (sel) FontWeight.Bold
                                else FontWeight.Normal)
                    }
                }
            }
        }
    }
}

@Composable
private fun UnitsSelector(
    tempUnit: String, onTempUnitChange: (String) -> Unit,
    distanceUnit: String, onDistanceUnitChange: (String) -> Unit,
    timeFormat: String, onTimeFormatChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Units & format",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                Text("Temperature",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                SegBtn(listOf("Celsius", "Fahrenheit"),
                    tempUnit, onTempUnitChange)
            }
            Column(Modifier.weight(1f)) {
                Text("Distance",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                SegBtn(listOf("km", "mi"),
                    distanceUnit, onDistanceUnitChange)
            }
        }
        Row(Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Text("Time",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            SegBtn(listOf("24h", "12h"),
                timeFormat, onTimeFormatChange)
        }
    }
}

@Composable
private fun SegBtn(options: List<String>, selected: String,
    onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
        options.forEachIndexed { index, option ->
            val sel = selected == option
            val first = index == 0
            val last = index == options.lastIndex
            Surface(onClick = { onSelect(option) },
                shape = if (first && last) MaterialTheme.shapes.medium
                    else if (first) RoundedCornerShape(
                        topStart = 16.dp, bottomStart = 20.dp)
                    else if (last) RoundedCornerShape(
                        topEnd = 16.dp, bottomEnd = 20.dp)
                    else RoundedCornerShape(0.dp),
                color = if (sel) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceContainerHighest,
                border = BorderStroke(0.5.dp,
                    MaterialTheme.colorScheme.outlineVariant)) {
                Text(option,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (sel) FontWeight.Bold
                        else FontWeight.Normal,
                    color = if (sel) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(
                        horizontal = 16.dp, vertical = 10.dp))
            }
        }
    }
}

@Composable
private fun DailyGoalSlider(dailyGoal: Int,
    onDailyGoalChange: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically) {
            Text("Daily observation goal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("$dailyGoal / day",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold)
        }
        Slider(value = dailyGoal.toFloat(),
            onValueChange = { onDailyGoalChange(it.roundToInt()) },
            valueRange = 0f..10f, steps = 9,
            modifier = Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text("None",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("10/day",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Screen 4: Review & Done (no extended tour)
// ══════════════════════════════════════════════════════════════════════

@Composable
private fun OnboardingReviewPage(
    profileName: String, profileRole: String,
    interests: UserInterests,
    cameraGranted: Boolean, locationGranted: Boolean,
    audioGranted: Boolean,
    selectedTheme: String, dailyGoal: Int,
    layoutStyle: String, frequency: String,
    onFinish: () -> Unit, onBack: () -> Unit,
    onEditPage: (Int) -> Unit
) {
    var showContent by remember { mutableStateOf(false) }
    val grantedCount = listOf(cameraGranted, locationGranted,
        audioGranted).count { it }
    LaunchedEffect(Unit) { showContent = true }

    Box(Modifier.fillMaxSize().background(
        MaterialTheme.colorScheme.background)) {
        Box(Modifier.size(200.dp).offset(x = (-60).dp, y = 200.dp)
            .graphicsLayer { alpha = 0.05f }
            .background(Brush.radialGradient(
                listOf(MaterialTheme.colorScheme.primary,
                    Color.Transparent)), CircleShape))

        Column(Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)
                .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Spacer(Modifier.height(16.dp))
                AnimatedVisibility(visible = showContent,
                    enter = fadeIn() + scaleIn(initialScale = 0.9f)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()) {
                        Box(Modifier.size(80.dp).background(
                            Brush.linearGradient(
                                listOf(Color(0xFF1F6B4C),
                                    Color(0xFF4CAF50))),
                            CuteCardDefaults.ShapeHero),
                            contentAlignment = Alignment.Center) {
                            Icon(FieldMindIcons.Check, null,
                                tint = Color.White, size = 44.dp)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("You're all set!",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center)
                        Text("Your companion is ready.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                AnimatedVisibility(visible = showContent,
                    enter = fadeIn(FieldMindMotion.expressiveFloat)
                        + slideInVertically { it / 4 }) {
                    Card(shape = CuteCardDefaults.Shape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = CuteElevations.clickableTier)) {
                        Column(Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            RevRow("Name",
                                profileName.ifBlank { "Not set" },
                                onClick = { onEditPage(0) })
                            HorizontalDivider()
                            RevRow("Role", profileRole,
                                onClick = { onEditPage(0) })
                            HorizontalDivider()
                            RevRow("Interests",
                                buildList {
                                    if (interests.zoology.isNotEmpty())
                                        add("${interests.zoology.size} zoology")
                                    if (interests.botany.isNotEmpty())
                                        add("${interests.botany.size} botany")
                                    if (interests.ecologyEnvironment)
                                        add("Ecology")
                                    if (interests.astronomy)
                                        add("Astronomy")
                                    if (interests.geology)
                                        add("Geology")
                                    interests.customInterests.forEach { add(it) }
                                }.joinToString(", ")
                                    .ifEmpty { "Not specified" },
                                onClick = { onEditPage(1) })
                            HorizontalDivider()
                            RevRow("Permissions",
                                "$grantedCount/4 granted",
                                onClick = { onEditPage(2) })
                            HorizontalDivider()
                            RevRow("Theme", selectedTheme,
                                onClick = { onEditPage(3) })
                            HorizontalDivider()
                            RevRow("Layout", layoutStyle,
                                onClick = { onEditPage(3) })
                            HorizontalDivider()
                            RevRow("Frequency", frequency,
                                onClick = { onEditPage(0) })
                            HorizontalDivider()
                            RevRow("Daily goal", "$dailyGoal/day",
                                onClick = { onEditPage(3) })
                        }
                    }
                }
            }

            AnimatedVisibility(visible = showContent,
                enter = fadeIn(FieldMindMotion.expressiveFloat)) {
                Column(Modifier.fillMaxWidth()
                    .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onBack,
                            shape = CuteCardDefaults.ShapeCompact,
                            modifier = Modifier.weight(1f)) {
                            Text("Back") }
                    }
                    Button(onClick = onFinish,
                        modifier = Modifier.fillMaxWidth()
                            .height(54.dp)
                            .expressivePress(scaleDown = 0.96f),
                        shape = CuteCardDefaults.FieldShape) {
                        Text("Start exploring",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun RevRow(label: String, value: String,
    onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(Modifier.weight(1f)) {
            Text(label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold)
        }
        TextButton(onClick = onClick,
            contentPadding = PaddingValues(
                horizontal = 8.dp, vertical = 4.dp)) {
            Text("Edit",
                style = MaterialTheme.typography.labelLarge)
        }
    }
}
