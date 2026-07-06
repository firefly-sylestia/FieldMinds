package fieldmind.research.app.features.field.data.security

/** Shared, testable lock-security rules used by settings, lock UI, and smoke tests. */
object LockSecurityPolicy {
    const val FAILED_UNLOCK_THRESHOLD: Int = 5

    fun pinLengthForLabel(label: String): Int = when (label) {
        "5 digits" -> 5
        "6 digits" -> 6
        else -> 4
    }

    fun failedUnlockCooldownMs(setting: String): Long = when (setting) {
        "30 Second Cooldown" -> 30_000L
        "5 Minute Cooldown" -> 300_000L
        else -> 0L
    }

    fun shouldTriggerFailedPolicy(failedAttempts: Int): Boolean = failedAttempts >= FAILED_UNLOCK_THRESHOLD

    fun shouldRequireBiometricsAfterFailure(
        failedAttempts: Int,
        settingEnabled: Boolean,
        deviceAuthAvailable: Boolean
    ): Boolean = settingEnabled && deviceAuthAvailable && shouldTriggerFailedPolicy(failedAttempts)

    fun isExactPinLength(pin: String, pinLengthLabel: String): Boolean = pin.length == pinLengthForLabel(pinLengthLabel)
}
