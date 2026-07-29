package fieldmind.research.app.features.field.presentation.screens
import fieldmind.research.app.ui.theme.CuteCardDefaults

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import fieldmind.research.app.features.field.data.security.LockSecurityPolicy
import fieldmind.research.app.features.field.data.settings.FieldMindSettings
import fieldmind.research.app.features.field.presentation.components.FieldMindIcons
import fieldmind.research.app.features.field.presentation.components.FieldMindLogo
import fieldmind.research.app.features.field.presentation.theme.FieldMindTheme
import fieldmind.research.app.shared.presentation.components.icons.Icon
import fieldmind.research.app.shared.presentation.components.icons.MaterialSymbolIcon
// ══════════════════════════════════════════════════════════════════════
//  App Lock / Privacy Gate
// ══════════════════════════════════════════════════════════════════════

/**
 * Full-screen lock that guards the entire FieldMind app when privacy lock is enabled.
 *
 * Unlike the previous implementation, this composable always renders [content] behind
 * the lock overlay. Pre-composing the app content (NavHost, all tab screens, database
 * flows) ensures instant transition on unlock — no composition, layout, or initial
 * database query delay. The only thing that changes on unlock is the visibility of
 * the [LockGate] overlay.
 *
 * Supports:
 * - Device credential (PIN/pattern/password via system)
 * - Biometric (fingerprint, face)
 * - In-app 4-6 digit PIN as fallback when no device lock is set
 */
@Composable
fun FieldMindAppLock(
    settings: FieldMindSettings,
    isUnlocked: Boolean,
    isDecoyMode: Boolean,
    onUnlock: () -> Unit,
    onDecoyUnlock: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val privacyEnabled by settings.privacyLockEnabled.collectAsState()
    val appPinEnabled by settings.appPinEnabled.collectAsState()
    val appPinHash by settings.appPinHash.collectAsState()
    val hasPin = appPinEnabled && appPinHash.isNotBlank()

    // Decoy mode takes full screen (no real content visible)
    if (isDecoyMode) {
        DecoyAppContent(onExitDecoy = { })
        return
    }

    val showLock = (privacyEnabled || hasPin) && !isUnlocked

    // ── Pre-compose content behind lock overlay ──
    // The app content (NavHost, tab screens, database flows) is always rendered,
    // just hidden behind the lock gate. When the user authenticates, [showLock]
    // flips to false and the overlay disappears — revealing already-composed
    // content instantly, with no composition or query delay.
    Box(Modifier.fillMaxSize()) {
        content()

        if (showLock) {
            LockGate(
                settings = settings,
                onUnlock = onUnlock,
                onDecoyUnlock = onDecoyUnlock
            )
        }
    }
}

/**
 * Lock screen overlay — biometric/PIN gate rendered on top of the pre-composed
 * app content. Uses [Box.fillMaxSize] with the app's background color so the
 * content underneath is completely hidden until the user authenticates.
 */
@Composable
private fun LockGate(
    settings: FieldMindSettings,
    onUnlock: () -> Unit,
    onDecoyUnlock: (() -> Unit)? = null
) {
    val privacyEnabled by settings.privacyLockEnabled.collectAsState()
    val appPinEnabled by settings.appPinEnabled.collectAsState()
    val appPinHash by settings.appPinHash.collectAsState()
    val appPinLength by settings.appPinLength.collectAsState()
    val pinRequiredLength = LockSecurityPolicy.pinLengthForLabel(appPinLength)
    val decoyEnabled by settings.decoyPinEnabled.collectAsState()
    val decoyPinHash by settings.decoyPinHash.collectAsState()
    val hasPin = appPinEnabled && appPinHash.isNotBlank()
    val hasDecoy = decoyEnabled && decoyPinHash.isNotBlank()

    val context = LocalContext.current
    val keyguard = remember(context) { context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager }
    val biometricManager = remember(context) { BiometricManager.from(context) }
    val deviceAuthenticators = BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
    val hasDeviceAuth = biometricManager.canAuthenticate(deviceAuthenticators) == BiometricManager.BIOMETRIC_SUCCESS
    val hasBiometric = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
    val hasDeviceCredential = keyguard.isDeviceSecure
    var usePinLock by rememberSaveable { mutableStateOf(false) }
    var pin by rememberSaveable { mutableStateOf("") }
    var pinError by rememberSaveable { mutableStateOf(false) }
    var pinAttempts by rememberSaveable { mutableIntStateOf(0) }
    var pinLockedUntil by rememberSaveable { mutableLongStateOf(0L) }
    var biometricRequiredAfterFailure by rememberSaveable { mutableStateOf(false) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(pinLockedUntil) {
        while (pinLockedUntil > System.currentTimeMillis()) {
            now = System.currentTimeMillis()
            kotlinx.coroutines.delay(1_000L)
        }
        now = System.currentTimeMillis()
    }
    val isPinLocked = pinLockedUntil > now
    val cooldownRemainingSeconds = ((pinLockedUntil - now).coerceAtLeast(0L) + 999L) / 1000L
    var authAttempted by remember { mutableStateOf(false) }

    val unlockLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            onUnlock()
        } else {
            // Device auth failed, fall back to PIN if available
            if (hasPin) usePinLock = true
        }
    }

    // Keep a ref to the current BiometricPrompt so we can cancel it before retrying.
    // No re-entrancy flag: BiometricPrompt cancels the prior session internally and
    // a flag-based guard could deadlock if a callback was dropped
    // (rapid finger lift, system-back cancel, or OEM prompt dismissal).
    var currentBiometricPrompt by remember { mutableStateOf<BiometricPrompt?>(null) }

    fun startBiometricAuth() {
        // Cancel any prior prompt; BiometricPrompt serializes overlapping authenticate() calls.
        currentBiometricPrompt?.cancelAuthentication()

        authAttempted = true
        if (!biometricRequiredAfterFailure) usePinLock = false
        val activity = context as? FragmentActivity
        if (hasDeviceAuth && activity != null) {
            val executor = ContextCompat.getMainExecutor(context)
            val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    currentBiometricPrompt = null
                    biometricRequiredAfterFailure = false
                    onUnlock()
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    currentBiometricPrompt = null
                    if (hasPin) usePinLock = true
                }
                override fun onAuthenticationFailed() {
                    pinError = true
                }
            })
            currentBiometricPrompt = prompt
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("FieldMind Privacy Lock")
                .setSubtitle("Authenticate to access your research data")
                .setAllowedAuthenticators(
                    deviceAuthenticators
                )
                .build()
            prompt.authenticate(promptInfo)
            return
        }
        // Fallback paths: no biometric prompt was shown — nothing to cancel or reset
        if (hasDeviceCredential) {
            val intent = keyguard.createConfirmDeviceCredentialIntent(
                "FieldMind Privacy Lock",
                "Authenticate to access your research data"
            )
            if (intent != null) {
                unlockLauncher.launch(intent)
            } else if (hasPin) {
                usePinLock = true
            }
        } else if (hasPin) {
            usePinLock = true
        }
    }

    // Try biometric/device auth first, then fall back to PIN.
    // Uses LaunchedEffect(Unit) so the prompt fires each time LockGate is
    // composed — covers both initial lock and auto-lock from backgrounding.
    LaunchedEffect(Unit) {
        if (privacyEnabled) {
            authAttempted = false
            startBiometricAuth()
        }
    }

    // Consume all touches on the lock overlay so the pre-composed content
    // underneath never receives accidental taps through the background.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.88f),
            shape = CuteCardDefaults.DialogShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Lock icon
                Box(Modifier.size(64.dp).clip(CuteCardDefaults.Shape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                    Icon(FieldMindIcons.Lock, null, tint = MaterialTheme.colorScheme.primary, size = 32.dp)
                }

                Text("FieldMind Locked", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)

                Text(
                    "Your research data is protected by privacy lock. Authenticate to continue.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                // PIN input (for in-app PIN mode). Uses an app-rendered numpad so the
                // device keyboard is never opened for unlock.
                if (usePinLock && hasPin) {
                    Text("Enter PIN ($pinRequiredLength digits)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    PinProgressDots(length = pinRequiredLength, filled = pin.length, isError = pinError)
                    if (pinError) {
                        Text("Incorrect PIN. Try again.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                    if (biometricRequiredAfterFailure) {
                        Text(
                            "Biometric or device unlock is required after repeated failures.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        val remaining = (LockSecurityPolicy.FAILED_UNLOCK_THRESHOLD - pinAttempts).coerceAtLeast(0)
                        if (pinAttempts > 0) {
                            Text(
                                "$pinAttempts/${LockSecurityPolicy.FAILED_UNLOCK_THRESHOLD} failed attempts • $remaining remaining",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                        FieldMindPinNumpad(
                            enabled = !isPinLocked,
                            onDigit = { digit ->
                                if (isPinLocked || pin.length >= pinRequiredLength) return@FieldMindPinNumpad
                                pinError = false
                                val nextPin = pin + digit
                                pin = nextPin
                                if (nextPin.length == pinRequiredLength) {
                                    when {
                                        hasDecoy && settings.verifyDecoyPin(nextPin) -> {
                                            pinAttempts = 0
                                            pin = ""
                                            onDecoyUnlock?.invoke()
                                        }
                                        settings.verifyAppPin(nextPin) -> {
                                            pinAttempts = 0
                                            biometricRequiredAfterFailure = false
                                            pin = ""
                                            onUnlock()
                                        }
                                        else -> {
                                            pinAttempts++
                                            pinError = true
                                            pin = ""
                                            if (LockSecurityPolicy.shouldTriggerFailedPolicy(pinAttempts)) {
                                                val requireBiometric = LockSecurityPolicy.shouldRequireBiometricsAfterFailure(
                                                    failedAttempts = pinAttempts,
                                                    settingEnabled = settings.failedUnlockRequireBiometrics.value,
                                                    deviceAuthAvailable = hasDeviceAuth || hasDeviceCredential
                                                )
                                                val cooldownMs = LockSecurityPolicy.failedUnlockCooldownMs(settings.failedUnlockCooldown.value)
                                                if (settings.failedUnlockPanicLock.value) {
                                                    settings.performPanicLockReset()
                                                }
                                                if (requireBiometric) {
                                                    biometricRequiredAfterFailure = true
                                                    startBiometricAuth()
                                                }
                                                if (cooldownMs > 0L) {
                                                    pinLockedUntil = System.currentTimeMillis() + cooldownMs
                                                }
                                                pinAttempts = 0
                                            }
                                        }
                                    }
                                }
                            },
                            onBackspace = {
                                if (!isPinLocked && pin.isNotEmpty()) {
                                    pin = pin.dropLast(1)
                                    pinError = false
                                }
                            },
                            onClear = {
                                if (!isPinLocked) {
                                    pin = ""
                                    pinError = false
                                }
                            }
                        )
                    }
                }

                // ── Forgot PIN — recovery button ──
                if (usePinLock && hasPin) {
                    var showForgotDialog by remember { mutableStateOf(false) }

                    TextButton(
                        onClick = { showForgotDialog = true },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(MaterialSymbolIcon("help_outline"), null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Forgot PIN?",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (showForgotDialog) {
                        AlertDialog(
                            onDismissRequest = { showForgotDialog = false },
                            icon = { Icon(MaterialSymbolIcon("lock_open"), null, tint = MaterialTheme.colorScheme.primary, size = 28.dp) },
                            title = { Text("Forgot your PIN?") },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        "If you've forgotten your app PIN, you can recover access using your device's built-in security.",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (hasDeviceAuth || hasDeviceCredential) {
                                        HorizontalDivider()
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(MaterialSymbolIcon("fingerprint"), null, tint = MaterialTheme.colorScheme.primary, size = 20.dp)
                                            Column(Modifier.weight(1f)) {
                                                Text("Use device authentication", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                                Text("Verify with your fingerprint, face, or device PIN to regain access without losing data.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                    HorizontalDivider()
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(MaterialSymbolIcon("settings_backup_restore"), null, tint = MaterialTheme.colorScheme.error, size = 20.dp)
                                        Column(Modifier.weight(1f)) {
                                            Text("Emergency reset", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                            Text("Disable the app PIN and privacy lock. Your research data will not be affected.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                if (hasDeviceAuth || hasDeviceCredential) {
                                    Button(
                                        onClick = {
                                            showForgotDialog = false
                                            usePinLock = false
                                            startBiometricAuth()
                                        },
                                        shape = CuteCardDefaults.ButtonShape
                                    ) { Text("Use device auth") }
                                }
                            },
                            dismissButton = {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(
                                        onClick = {
                                            showForgotDialog = false
                                            // Emergency reset: clear PIN, privacy lock, and all other security settings
                                            settings.performPanicLockReset()
                                            settings.setPrivacyLockEnabled(false)
                                            pin = ""
                                            pinError = false
                                            onUnlock()
                                        },
                                        shape = CuteCardDefaults.ButtonShape,
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) { Text("Emergency reset") }
                                    TextButton(
                                        onClick = { showForgotDialog = false },
                                        shape = CuteCardDefaults.ButtonShape
                                    ) { Text("Cancel") }
                                }
                            }
                        )
                    }
                }

                if (usePinLock && isPinLocked) {
                    Text(
                        "Too many attempts. Try again in ${cooldownRemainingSeconds}s.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (hasPin) {
                        OutlinedButton(
                            onClick = { usePinLock = true },
                            modifier = Modifier.weight(1f),
                            shape = CuteCardDefaults.ShapeCompact
                        ) {
                            Text(if (usePinLock) "Using PIN" else "Use PIN")
                        }
                    }
                    if (hasDeviceAuth || hasDeviceCredential) {
                        Button(
                            onClick = { startBiometricAuth() },
                            modifier = Modifier.weight(1f),
                            shape = CuteCardDefaults.ShapeCompact
                        ) {
                            Text(if (hasBiometric) "Retry biometric" else "Retry device lock")
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun PinProgressDots(length: Int, filled: Int, isError: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(length) { index ->
            Box(
                Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isError -> MaterialTheme.colorScheme.error
                            index < filled -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outlineVariant
                        }
                    )
            )
        }
    }
}

@Composable
private fun FieldMindPinNumpad(
    enabled: Boolean,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("Clear", "0", "⌫")
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { key ->
                    val action = {
                        if (enabled) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            when (key) {
                                "Clear" -> onClear()
                                "⌫" -> onBackspace()
                                else -> onDigit(key)
                            }
                        }
                    }
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .clip(CuteCardDefaults.ButtonShape)
                            .clickable(
                                enabled = enabled,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = action
                            ),
                        shape = CuteCardDefaults.ButtonShape,
                        color = if (enabled) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(key, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════
//  Decoy App Content — clean, empty FieldMind (no data visible)
// ══════════════════════════════════════════════════════════════════════

/**
 * Displayed when the user enters the decoy PIN. Shows a clean, empty version
 * of FieldMind with no real data. The user must restart the app to return
 * to the real lock screen.
 */
@Composable
fun DecoyAppContent(
    onExitDecoy: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize().statusBarsPadding().background(MaterialTheme.colorScheme.background).systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Decoy brand icon
            FieldMindLogo(
                size = 80.dp,
                modifier = Modifier.clip(CuteCardDefaults.Shape).background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f))
            )

            Text(
                "Welcome to FieldMind",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                "A clean version of the app is ready. There is no data to display yet — start observing to build your research notebook.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            // Empty state illustration
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CuteCardDefaults.Shape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(MaterialSymbolIcon("empty_dashboard"), null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), size = 48.dp)
                    Text(
                        "No observations yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "This is a fresh start. Observations, notes, and research will appear here once you begin.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "Tap the + button below to start your first observation.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}
