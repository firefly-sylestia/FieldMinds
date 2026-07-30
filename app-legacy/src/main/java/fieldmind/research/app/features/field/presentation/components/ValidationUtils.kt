package fieldmind.research.app.features.field.presentation.components

/**
 * Pre-defined validation functions for common field types.
 * Use these with RequiredFieldState to add inline validation.
 */
object FieldValidators {

    /** Required field — must not be blank. */
    fun required(label: String = "This field"): (String) -> String? = { value ->
        if (value.isBlank()) "$label is required" else null
    }

    /** Email format validation. */
    fun email(): (String) -> String? = { value ->
        if (value.isBlank()) null
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()) "Invalid email format"
        else null
    }

    /** URL format validation. */
    fun url(): (String) -> String? = { value ->
        if (value.isBlank()) null
        else if (!android.util.Patterns.WEB_URL.matcher(value).matches()) "Invalid URL format"
        else null
    }

    /** Minimum length validation. */
    fun minLength(min: Int, label: String = "Input"): (String) -> String? = { value ->
        if (value.isNotBlank() && value.length < min) "$label must be at least $min characters"
        else null
    }

    /** Maximum length validation. */
    fun maxLength(max: Int, label: String = "Input"): (String) -> String? = { value ->
        if (value.length > max) "$label must be at most $max characters"
        else null
    }

    /** Numeric value validation. */
    fun numeric(label: String = "Value"): (String) -> String? = { value ->
        if (value.isNotBlank() && value.toDoubleOrNull() == null) "$label must be a number"
        else null
    }

    /** Positive number validation. */
    fun positiveNumber(label: String = "Value"): (String) -> String? = { value ->
        when {
            value.isBlank() -> null
            value.toDoubleOrNull() == null -> "$label must be a number"
            value.toDouble() <= 0 -> "$label must be positive"
            else -> null
        }
    }

    /** Compose multiple validators — all must pass. */
    fun all(vararg validators: (String) -> String?): (String) -> String? = { value ->
        validators.firstNotNullOfOrNull { it(value) }
    }
}
