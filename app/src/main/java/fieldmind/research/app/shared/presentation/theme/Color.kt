package fieldmind.research.app.ui.theme

import androidx.compose.ui.graphics.Color

// Material Design 3 Color System - Light Theme
// Primary palette derived from seed #6750A4 (matches app logo exactly)
val PrimaryLight = Color(0xFF6750A4) // M3 baseline purple — matches logo stroke color
val OnPrimaryLight = Color(0xFFFFFFFF) // White text on primary
val PrimaryContainerLight = Color(0xFFEADDFF) // Light purple container
val OnPrimaryContainerLight = Color(0xFF21005D) // Dark text on primary container

// Secondary color palette — neutral violet
val SecondaryLight = Color(0xFF625B71) // Neutral gray-purple for balance
val OnSecondaryLight = Color(0xFFFFFFFF) // White text on secondary
val SecondaryContainerLight = Color(0xFFE8DEF8) // Light gray-purple container
val OnSecondaryContainerLight = Color(0xFF1D192B) // Dark text on secondary container

// Tertiary color palette — rosy pink (matches logo center pill gradient)
val TertiaryLight = Color(0xFF7D5260) // Rosy pink accent
val OnTertiaryLight = Color(0xFFFFFFFF) // White text on tertiary
val TertiaryContainerLight = Color(0xFFFFD8E4) // Light pink container (matches logo)
val OnTertiaryContainerLight = Color(0xFF31111D) // Dark text on tertiary container

// Error color palette
val ErrorLight = Color(0xFFB3261E) // Standard Material error red
val OnErrorLight = Color(0xFFFFFFFF) // White text on error
val ErrorContainerLight = Color(0xFFF9DEDC) // Light red container
val OnErrorContainerLight = Color(0xFF410E0B) // Dark text on error container

// Background and surface colors
val BackgroundLight = Color(0xFFFAF5FF) // Softened — subtle purple tint, less stark white
val OnBackgroundLight = Color(0xFF1C1B1F) // Dark text on background
val SurfaceLight = Color(0xFFFAF5FF) // Surface same as background
val OnSurfaceLight = Color(0xFF1C1B1F) // Dark text on surface
val SurfaceVariantLight = Color(0xFFE4DCF0) // Slightly richer purple-gray surface variant
val OnSurfaceVariantLight = Color(0xFF49454F) // Medium gray text

// Outline colors for borders and dividers
val OutlineLight = Color(0xFF79747E) // Medium gray outline
val OutlineVariantLight = Color(0xFFCAC4D0) // Light gray outline variant

// Surface containers for different elevation levels
val SurfaceContainerLowestLight = Color(0xFFF5EFFE) // Lowest elevation — faint purple tint
val SurfaceContainerLowLight = Color(0xFFF0E9F9) // Low elevation
val SurfaceContainerLight = Color(0xFFEAE3F3) // Medium elevation
val SurfaceContainerHighLight = Color(0xFFE4DDED) // High elevation
val SurfaceContainerHighestLight = Color(0xFFDED6E7) // Highest elevation

// Inverse colors for special cases
val InverseSurfaceLight = Color(0xFF313033) // Dark surface for light theme
val InverseOnSurfaceLight = Color(0xFFF4EFF4) // Light text on inverse surface
val InversePrimaryLight = Color(0xFFD0BCFF) // Light primary on dark surface

// Material Design 3 Color System - Dark Theme
// Primary palette derived from seed #6750A4 (matches app logo exactly)
val PrimaryDark = Color(0xFFD0BCFF) // Light purple for dark theme (M3 baseline)
val OnPrimaryDark = Color(0xFF381E72) // Dark text on primary
val PrimaryContainerDark = Color(0xFF4F378B) // Medium purple container
val OnPrimaryContainerDark = Color(0xFFEADDFF) // Light text on primary container

// Secondary color palette — neutral violet
val SecondaryDark = Color(0xFFCCC2DC) // Light gray-purple for balance
val OnSecondaryDark = Color(0xFF332D41) // Dark text on secondary
val SecondaryContainerDark = Color(0xFF4A4458) // Medium gray-purple container
val OnSecondaryContainerDark = Color(0xFFE8DEF8) // Light text on secondary container

// Tertiary color palette — rosy pink (matches logo center pill gradient)
val TertiaryDark = Color(0xFFEFB8C8) // Rosy pink for dark theme
val OnTertiaryDark = Color(0xFF492532) // Dark text on tertiary
val TertiaryContainerDark = Color(0xFF633B48) // Medium rose container
val OnTertiaryContainerDark = Color(0xFFFFD8E4) // Light text on tertiary container

// Error color palette
val ErrorDark = Color(0xFFF2B8B5) // Light red for dark theme
val OnErrorDark = Color(0xFF601410) // Dark text on error
val ErrorContainerDark = Color(0xFF8C1D18) // Medium red container
val OnErrorContainerDark = Color(0xFFF9DEDC) // Light text on error container

// Background and surface colors
val BackgroundDark = Color(0xFF141218) // Slightly deeper than M3 baseline for better depth
val OnBackgroundDark = Color(0xFFE6E1E5) // Light text on background
val SurfaceDark = Color(0xFF141218) // Surface same as background
val OnSurfaceDark = Color(0xFFE6E1E5) // Light text on surface
val SurfaceVariantDark = Color(0xFF49454F) // Medium gray surface variant
val OnSurfaceVariantDark = Color(0xFFCAC4D0) // Light gray text

// Outline colors for borders and dividers
val OutlineDark = Color(0xFF938F99) // Light gray outline
val OutlineVariantDark = Color(0xFF49454F) // Medium gray outline variant

// Surface containers for different elevation levels
val SurfaceContainerLowestDark = Color(0xFF1C1922) // Lowest elevation — slightly brighter than background
val SurfaceContainerLowDark = Color(0xFF25222C) // Low elevation — visibly distinct from bg
val SurfaceContainerDark = Color(0xFF2E2B36) // Medium elevation
val SurfaceContainerHighDark = Color(0xFF383540) // High elevation
val SurfaceContainerHighestDark = Color(0xFF43404A) // Highest elevation

// Inverse colors for special cases
val InverseSurfaceDark = Color(0xFFE6E1E5) // Light surface for dark theme
val InverseOnSurfaceDark = Color(0xFF313033) // Dark text on inverse surface
val InversePrimaryDark = Color(0xFF6750A4) // Dark primary on light surface (= logo color)

// ============================================
// Custom Color Scheme Presets
// ============================================

// Warm Theme - Sunset colors
val WarmPrimaryLight = Color(0xFFFF6B35)
val WarmOnPrimaryLight = Color(0xFFFFFFFF)
val WarmPrimaryContainerLight = Color(0xFFFFDDD2)
val WarmOnPrimaryContainerLight = Color(0xFF3E0400)

val WarmSecondaryLight = Color(0xFFF7931E)
val WarmOnSecondaryLight = Color(0xFFFFFFFF)
val WarmSecondaryContainerLight = Color(0xFFFFDDB6)
val WarmOnSecondaryContainerLight = Color(0xFF2C1600)

val WarmTertiaryLight = Color(0xFFFFC857)
val WarmOnTertiaryLight = Color(0xFF432A0D)
val WarmTertiaryContainerLight = Color(0xFFFFE8B6)
val WarmOnTertiaryContainerLight = Color(0xFF261900)

val WarmPrimaryDark = Color(0xFFFFB59A)
val WarmOnPrimaryDark = Color(0xFF5F1500)
val WarmPrimaryContainerDark = Color(0xFFC84520)
val WarmOnPrimaryContainerDark = Color(0xFFFFDDD2)

val WarmSecondaryDark = Color(0xFFFFD499)
val WarmOnSecondaryDark = Color(0xFF4A2800)
val WarmSecondaryContainerDark = Color(0xFFD97E00)
val WarmOnSecondaryContainerDark = Color(0xFFFFDDB6)

val WarmTertiaryDark = Color(0xFFFFE099)
val WarmOnTertiaryDark = Color(0xFF442B00)
val WarmTertiaryContainerDark = Color(0xFFFFA91F)
val WarmOnTertiaryContainerDark = Color(0xFFFFE8B6)

// Cool Theme - Ocean colors
val CoolPrimaryLight = Color(0xFF1E88E5)
val CoolOnPrimaryLight = Color(0xFFFFFFFF)
val CoolPrimaryContainerLight = Color(0xFFD1E4FF)
val CoolOnPrimaryContainerLight = Color(0xFF001D36)

val CoolSecondaryLight = Color(0xFF00897B)
val CoolOnSecondaryLight = Color(0xFFFFFFFF)
val CoolSecondaryContainerLight = Color(0xFFB2DFDB)
val CoolOnSecondaryContainerLight = Color(0xFF00201D)

val CoolTertiaryLight = Color(0xFF80DEEA)
val CoolOnTertiaryLight = Color(0xFF003640)
val CoolTertiaryContainerLight = Color(0xFFB2EBF2)
val CoolOnTertiaryContainerLight = Color(0xFF002025)

val CoolPrimaryDark = Color(0xFF90CAF9)
val CoolOnPrimaryDark = Color(0xFF003258)
val CoolPrimaryContainerDark = Color(0xFF004A77)
val CoolOnPrimaryContainerDark = Color(0xFFD1E4FF)

val CoolSecondaryDark = Color(0xFF4DB6AC)
val CoolOnSecondaryDark = Color(0xFF003731)
val CoolSecondaryContainerDark = Color(0xFF005048)
val CoolOnSecondaryContainerDark = Color(0xFFB2DFDB)

val CoolTertiaryDark = Color(0xFF4DD0E1)
val CoolOnTertiaryDark = Color(0xFF00363D)
val CoolTertiaryContainerDark = Color(0xFF004F58)
val CoolOnTertiaryContainerDark = Color(0xFFB2EBF2)

// Forest Theme - Nature green
val ForestPrimaryLight = Color(0xFF2E7D32)
val ForestOnPrimaryLight = Color(0xFFFFFFFF)
val ForestPrimaryContainerLight = Color(0xFFC8E6C9)
val ForestOnPrimaryContainerLight = Color(0xFF0D5016)

val ForestSecondaryLight = Color(0xFF558B2F)
val ForestOnSecondaryLight = Color(0xFFFFFFFF)
val ForestSecondaryContainerLight = Color(0xFFDCEDC8)
val ForestOnSecondaryContainerLight = Color(0xFF1B5E20)

val ForestTertiaryLight = Color(0xFF9CCC65)
val ForestOnTertiaryLight = Color(0xFF2E5016)
val ForestTertiaryContainerLight = Color(0xFFE7F5E1)
val ForestOnTertiaryContainerLight = Color(0xFF223608)

val ForestPrimaryDark = Color(0xFF81C784)
val ForestOnPrimaryDark = Color(0xFF0D5016)
val ForestPrimaryContainerDark = Color(0xFF1B5E20)
val ForestOnPrimaryContainerDark = Color(0xFFC8E6C9)

val ForestSecondaryDark = Color(0xFFAED581)
val ForestOnSecondaryDark = Color(0xFF1B5E20)
val ForestSecondaryContainerDark = Color(0xFF33691E)
val ForestOnSecondaryContainerDark = Color(0xFFDCEDC8)

val ForestTertiaryDark = Color(0xFFDCE775)
val ForestOnTertiaryDark = Color(0xFF3F5100)
val ForestTertiaryContainerDark = Color(0xFF5A7700)
val ForestOnTertiaryContainerDark = Color(0xFFE7F5E1)

// Rose Theme - Pink elegance
val RosePrimaryLight = Color(0xFFE91E63)
val RoseOnPrimaryLight = Color(0xFFFFFFFF)
val RosePrimaryContainerLight = Color(0xFFF8BBD0)
val RoseOnPrimaryContainerLight = Color(0xFF3E001D)

val RoseSecondaryLight = Color(0xFFC2185B)
val RoseOnSecondaryLight = Color(0xFFFFFFFF)
val RoseSecondaryContainerLight = Color(0xFFFFCDD2)
val RoseOnSecondaryContainerLight = Color(0xFF300016)

val RoseTertiaryLight = Color(0xFFF8BBD0)
val RoseTertiaryLight2 = Color(0xFFFF80AB)
val RoseOnTertiaryLight = Color(0xFF5C002E)
val RoseTertiaryContainerLight = Color(0xFFFFE0EC)
val RoseOnTertiaryContainerLight = Color(0xFF31000F)

val RosePrimaryDark = Color(0xFFF48FB1)
val RoseOnPrimaryDark = Color(0xFF560027)
val RosePrimaryContainerDark = Color(0xFFC2185B)
val RoseOnPrimaryContainerDark = Color(0xFFF8BBD0)

val RoseSecondaryDark = Color(0xFFFFAB91)
val RoseOnSecondaryDark = Color(0xFF5F000A)
val RoseSecondaryContainerDark = Color(0xFFAD1457)
val RoseOnSecondaryContainerDark = Color(0xFFFFCDD2)

val RoseTertiaryDark = Color(0xFFFF80AB)
val RoseOnTertiaryDark = Color(0xFF5C002E)
val RoseTertiaryContainerDark = Color(0xFFD81B60)
val RoseOnTertiaryContainerDark = Color(0xFFFFE0EC)

// Monochrome Theme - Minimalist grayscale
val MonoPrimaryLight = Color(0xFF424242)
val MonoOnPrimaryLight = Color(0xFFFFFFFF)
val MonoPrimaryContainerLight = Color(0xFFE0E0E0)
val MonoOnPrimaryContainerLight = Color(0xFF1C1C1C)

val MonoSecondaryLight = Color(0xFF616161)
val MonoOnSecondaryLight = Color(0xFFFFFFFF)
val MonoSecondaryContainerLight = Color(0xFFEEEEEE)
val MonoOnSecondaryContainerLight = Color(0xFF2C2C2C)

val MonoTertiaryLight = Color(0xFF9E9E9E)
val MonoOnTertiaryLight = Color(0xFF1C1C1C)
val MonoTertiaryContainerLight = Color(0xFFF5F5F5)
val MonoOnTertiaryContainerLight = Color(0xFF1C1C1C)

val MonoPrimaryDark = Color(0xFFBDBDBD)
val MonoOnPrimaryDark = Color(0xFF1C1C1C)
val MonoPrimaryContainerDark = Color(0xFF424242)
val MonoOnPrimaryContainerDark = Color(0xFFE0E0E0)

val MonoSecondaryDark = Color(0xFF9E9E9E)
val MonoOnSecondaryDark = Color(0xFF2C2C2C)
val MonoSecondaryContainerDark = Color(0xFF616161)
val MonoOnSecondaryContainerDark = Color(0xFFEEEEEE)

val MonoTertiaryDark = Color(0xFF757575)
val MonoOnTertiaryDark = Color(0xFFEEEEEE)
val MonoTertiaryContainerDark = Color(0xFF424242)
val MonoOnTertiaryContainerDark = Color(0xFFF5F5F5)

// ============================================
// Pastel Theme - Soft, adorable pastels
// ============================================

// Light Pastel — lavender, blush, mint on warm white
val PastelPrimaryLight = Color(0xFFB39DDB) // Soft lavender
val PastelOnPrimaryLight = Color(0xFFFFFFFF)
val PastelPrimaryContainerLight = Color(0xFFEDE7F6) // Very light lavender
val PastelOnPrimaryContainerLight = Color(0xFF2C1B4D)

val PastelSecondaryLight = Color(0xFFF8BBD0) // Blush pink
val PastelOnSecondaryLight = Color(0xFF3E1F2E) // Deep rose for contrast — white fails WCAG on this bright blush
val PastelSecondaryContainerLight = Color(0xFFFCE4EC) // Very light pink
val PastelOnSecondaryContainerLight = Color(0xFF3E1F2E)

val PastelTertiaryLight = Color(0xFFA5D6A7) // Soft mint
val PastelOnTertiaryLight = Color(0xFFFFFFFF)
val PastelTertiaryContainerLight = Color(0xFFE8F5E9) // Very light mint
val PastelOnTertiaryContainerLight = Color(0xFF1B3E1D)

val PastelErrorLight = Color(0xFFEF9A9A) // Soft coral
val PastelOnErrorLight = Color(0xFFFFFFFF)
val PastelErrorContainerLight = Color(0xFFFFEBEE) // Very light coral
val PastelOnErrorContainerLight = Color(0xFF3E1515)

val PastelBackgroundLight = Color(0xFFFFF8FB) // Warm pink-white
val PastelOnBackgroundLight = Color(0xFF2D2D2D) // Soft dark gray
val PastelSurfaceLight = Color(0xFFFFF8FB)
val PastelOnSurfaceLight = Color(0xFF2D2D2D)
val PastelSurfaceVariantLight = Color(0xFFF3E5F5) // Very light lavender-pink
val PastelOnSurfaceVariantLight = Color(0xFF5D4E5E) // Muted mauve-gray

val PastelOutlineLight = Color(0xFFD7C4D7) // Soft mauve
val PastelOutlineVariantLight = Color(0xFFEBE0EB) // Lighter mauve

val PastelSurfaceContainerLowestLight = Color(0xFFFFF0F5) // Lavender blush
val PastelSurfaceContainerLowLight = Color(0xFFFDE8F0) // Light pink
val PastelSurfaceContainerLight = Color(0xFFFCE4EC) // Pink
val PastelSurfaceContainerHighLight = Color(0xFFF8D7E3) // Slightly deeper pink
val PastelSurfaceContainerHighestLight = Color(0xFFF0CCD8) // Deeper pink

val PastelInverseSurfaceLight = Color(0xFF2D2D2D)
val PastelInverseOnSurfaceLight = Color(0xFFFFF8FB)
val PastelInversePrimaryLight = PastelPrimaryLight

// Dark Pastel — deep warm tones with glowing pastels
val PastelPrimaryDark = Color(0xFFD1C4E9) // Soft light lavender
val PastelOnPrimaryDark = Color(0xFF2C1B4D)
val PastelPrimaryContainerDark = Color(0xFF6A4E94) // Muted purple
val PastelOnPrimaryContainerDark = Color(0xFFEDE7F6)

val PastelSecondaryDark = Color(0xFFF8BBD0) // Warm blush
val PastelOnSecondaryDark = Color(0xFF3E1F2E)
val PastelSecondaryContainerDark = Color(0xFF6D3A4D) // Muted rose
val PastelOnSecondaryContainerDark = Color(0xFFFCE4EC)

val PastelTertiaryDark = Color(0xFFA5D6A7) // Soft mint glow
val PastelOnTertiaryDark = Color(0xFF1B3E1D)
val PastelTertiaryContainerDark = Color(0xFF3E6B40) // Muted green
val PastelOnTertiaryContainerDark = Color(0xFFE8F5E9)

val PastelErrorDark = Color(0xFFEF9A9A) // Soft coral
val PastelOnErrorDark = Color(0xFF3E1515)
val PastelErrorContainerDark = Color(0xFF6E2C2C) // Muted maroon
val PastelOnErrorContainerDark = Color(0xFFFFEBEE)

val PastelBackgroundDark = Color(0xFF1A1423) // Deep warm purple-black
val PastelOnBackgroundDark = Color(0xFFE8E0EB) // Light lavender-gray
val PastelSurfaceDark = Color(0xFF1A1423)
val PastelOnSurfaceDark = Color(0xFFE8E0EB)
val PastelSurfaceVariantDark = Color(0xFF3D2E40) // Muted purple-gray
val PastelOnSurfaceVariantDark = Color(0xFFCBB4CF) // Light mauve

val PastelOutlineDark = Color(0xFF8A7A8E) // Muted mauve outline
val PastelOutlineVariantDark = Color(0xFF3D2E40) // Darker variant

val PastelSurfaceContainerLowestDark = Color(0xFF221C2A) // Deepest — brighter than bg
val PastelSurfaceContainerLowDark = Color(0xFF2B2430) // Low — visibly distinct
val PastelSurfaceContainerDark = Color(0xFF342C3A) // Medium
val PastelSurfaceContainerHighDark = Color(0xFF3E3542) // High
val PastelSurfaceContainerHighestDark = Color(0xFF473E4C) // Highest

val PastelInverseSurfaceDark = Color(0xFFFFF8FB)
val PastelInverseOnSurfaceDark = Color(0xFF2D2D2D)
val PastelInversePrimaryDark = PastelPrimaryDark
