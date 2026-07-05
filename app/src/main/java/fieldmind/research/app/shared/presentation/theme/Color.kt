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

// ============================================
// Midnight Flora — Sophisticated deep greens + warm neutrals
// Premium nature aesthetic. Forest emerald with warm amber accents
// on off-white / near-black backgrounds.
// ============================================

// Light
val FloraPrimaryLight = Color(0xFF1A6B4C)
val FloraOnPrimaryLight = Color(0xFFFFFFFF)
val FloraPrimaryContainerLight = Color(0xFFA8E8C8)
val FloraOnPrimaryContainerLight = Color(0xFF002110)

val FloraSecondaryLight = Color(0xFF5B6770)
val FloraOnSecondaryLight = Color(0xFFFFFFFF)
val FloraSecondaryContainerLight = Color(0xFFD0DCE3)
val FloraOnSecondaryContainerLight = Color(0xFF0D1A24)

val FloraTertiaryLight = Color(0xFFB8860B) // Dark goldenrod
val FloraOnTertiaryLight = Color(0xFFFFFFFF)
val FloraTertiaryContainerLight = Color(0xFFFFEAA0)
val FloraOnTertiaryContainerLight = Color(0xFF2C1700)

val FloraErrorLight = Color(0xFFBA1A1A)
val FloraOnErrorLight = Color(0xFFFFFFFF)
val FloraErrorContainerLight = Color(0xFFFFDAD6)
val FloraOnErrorContainerLight = Color(0xFF410002)

val FloraBackgroundLight = Color(0xFFF8F6F2) // Warm off-white
val FloraOnBackgroundLight = Color(0xFF1C1B19)
val FloraSurfaceLight = Color(0xFFF8F6F2)
val FloraOnSurfaceLight = Color(0xFF1C1B19)
val FloraSurfaceVariantLight = Color(0xFFE0E2E8)
val FloraOnSurfaceVariantLight = Color(0xFF4A4A4E)

val FloraOutlineLight = Color(0xFF8A8A8E)
val FloraOutlineVariantLight = Color(0xFFD0D2D8)

val FloraSurfaceContainerLowestLight = Color(0xFFFDFBFA)
val FloraSurfaceContainerLowLight = Color(0xFFF5F1EA)
val FloraSurfaceContainerLight = Color(0xFFEDE8DF)
val FloraSurfaceContainerHighLight = Color(0xFFE5DED2)
val FloraSurfaceContainerHighestLight = Color(0xFFDBD4C6)

val FloraSurfaceDimLight = Color(0xFFD8D3C8)
val FloraSurfaceBrightLight = Color(0xFFF8F6F2)

val FloraInverseSurfaceLight = Color(0xFF2D2D2E)
val FloraInverseOnSurfaceLight = Color(0xFFF2F0EA)
val FloraInversePrimaryLight = Color(0xFF8DD5A8)

// Dark
val FloraPrimaryDark = Color(0xFF7DCDA0)
val FloraOnPrimaryDark = Color(0xFF00391E)
val FloraPrimaryContainerDark = Color(0xFF005230)
val FloraOnPrimaryContainerDark = Color(0xFFA8E8C8)

val FloraSecondaryDark = Color(0xFFA8B8C0)
val FloraOnSecondaryDark = Color(0xFF1A2A34)
val FloraSecondaryContainerDark = Color(0xFF3A4A54)
val FloraOnSecondaryContainerDark = Color(0xFFD0DCE3)

val FloraTertiaryDark = Color(0xFFF0C860)
val FloraOnTertiaryDark = Color(0xFF492C00)
val FloraTertiaryContainerDark = Color(0xFF694200)
val FloraOnTertiaryContainerDark = Color(0xFFFFEAA0)

val FloraErrorDark = Color(0xFFFFB4AB)
val FloraOnErrorDark = Color(0xFF690005)
val FloraErrorContainerDark = Color(0xFF93000A)
val FloraOnErrorContainerDark = Color(0xFFFFDAD6)

val FloraBackgroundDark = Color(0xFF0E0E10) // Near-black
val FloraOnBackgroundDark = Color(0xFFE4E2DE)
val FloraSurfaceDark = Color(0xFF0E0E10)
val FloraOnSurfaceDark = Color(0xFFE4E2DE)
val FloraSurfaceVariantDark = Color(0xFF1E1E20)
val FloraOnSurfaceVariantDark = Color(0xFFC4C6C8)

val FloraOutlineDark = Color(0xFF8E9094)
val FloraOutlineVariantDark = Color(0xFF3A3A3E)

val FloraSurfaceContainerLowestDark = Color(0xFF141416)
val FloraSurfaceContainerLowDark = Color(0xFF1A1A1C)
val FloraSurfaceContainerDark = Color(0xFF222224)
val FloraSurfaceContainerHighDark = Color(0xFF2A2A2C)
val FloraSurfaceContainerHighestDark = Color(0xFF323234)

val FloraSurfaceDimDark = Color(0xFF0E0E10)
val FloraSurfaceBrightDark = Color(0xFF3A3A3C)

val FloraInverseSurfaceDark = Color(0xFFF2F0EA)
val FloraInverseOnSurfaceDark = Color(0xFF2D2D2E)
val FloraInversePrimaryDark = Color(0xFF1A6B4C)

// ============================================
// Noir Amethyst — Deep violet-black moody luxury
// Premium tech/creative aesthetic. Deep violet with amethyst glow.
// ============================================

// Light
val AmethystPrimaryLight = Color(0xFF5B3E96)
val AmethystOnPrimaryLight = Color(0xFFFFFFFF)
val AmethystPrimaryContainerLight = Color(0xFFEADDFF)
val AmethystOnPrimaryContainerLight = Color(0xFF21005D)

val AmethystSecondaryLight = Color(0xFF6B5E7A)
val AmethystOnSecondaryLight = Color(0xFFFFFFFF)
val AmethystSecondaryContainerLight = Color(0xFFF3E8FF)
val AmethystOnSecondaryContainerLight = Color(0xFF1D192B)

val AmethystTertiaryLight = Color(0xFFD4726A)
val AmethystOnTertiaryLight = Color(0xFFFFFFFF)
val AmethystTertiaryContainerLight = Color(0xFFFFE0DC)
val AmethystOnTertiaryContainerLight = Color(0xFF3E1515)

val AmethystErrorLight = Color(0xFFBA1A1A)
val AmethystOnErrorLight = Color(0xFFFFFFFF)
val AmethystErrorContainerLight = Color(0xFFFFDAD6)
val AmethystOnErrorContainerLight = Color(0xFF410002)

val AmethystBackgroundLight = Color(0xFFFCFAFF) // Crisp white-violet
val AmethystOnBackgroundLight = Color(0xFF1C1B1F)
val AmethystSurfaceLight = Color(0xFFFCFAFF)
val AmethystOnSurfaceLight = Color(0xFF1C1B1F)
val AmethystSurfaceVariantLight = Color(0xFFE8E0F0)
val AmethystOnSurfaceVariantLight = Color(0xFF4A4555)

val AmethystOutlineLight = Color(0xFF7C7588)
val AmethystOutlineVariantLight = Color(0xFFCEC4D8)

val AmethystSurfaceContainerLowestLight = Color(0xFFFFFFFF)
val AmethystSurfaceContainerLowLight = Color(0xFFF5F0FC)
val AmethystSurfaceContainerLight = Color(0xFFEFE8F5)
val AmethystSurfaceContainerHighLight = Color(0xFFE8E0EE)
val AmethystSurfaceContainerHighestLight = Color(0xFFE0D6E6)

val AmethystSurfaceDimLight = Color(0xFFDCD2E2)
val AmethystSurfaceBrightLight = Color(0xFFFCFAFF)

val AmethystInverseSurfaceLight = Color(0xFF2D2B33)
val AmethystInverseOnSurfaceLight = Color(0xFFF0ECF5)
val AmethystInversePrimaryLight = Color(0xFFCFBFFF)

// Dark
val AmethystPrimaryDark = Color(0xFFC4A5FF)
val AmethystOnPrimaryDark = Color(0xFF2C1B69)
val AmethystPrimaryContainerDark = Color(0xFF4A2E8A)
val AmethystOnPrimaryContainerDark = Color(0xFFEADDFF)

val AmethystSecondaryDark = Color(0xFFB0A0C8)
val AmethystOnSecondaryDark = Color(0xFF2A2440)
val AmethystSecondaryContainerDark = Color(0xFF423A58)
val AmethystOnSecondaryContainerDark = Color(0xFFF3E8FF)

val AmethystTertiaryDark = Color(0xFFF0A098)
val AmethystOnTertiaryDark = Color(0xFF4E2828)
val AmethystTertiaryContainerDark = Color(0xFF6A3A3A)
val AmethystOnTertiaryContainerDark = Color(0xFFFFE0DC)

val AmethystErrorDark = Color(0xFFFFB4AB)
val AmethystOnErrorDark = Color(0xFF690005)
val AmethystErrorContainerDark = Color(0xFF93000A)
val AmethystOnErrorContainerDark = Color(0xFFFFDAD6)

val AmethystBackgroundDark = Color(0xFF0D0B12) // Deep dark purple-black
val AmethystOnBackgroundDark = Color(0xFFE6E0EE)
val AmethystSurfaceDark = Color(0xFF0D0B12)
val AmethystOnSurfaceDark = Color(0xFFE6E0EE)
val AmethystSurfaceVariantDark = Color(0xFF1E1A28)
val AmethystOnSurfaceVariantDark = Color(0xFFC8C0D0)

val AmethystOutlineDark = Color(0xFF9088A0)
val AmethystOutlineVariantDark = Color(0xFF3A3448)

val AmethystSurfaceContainerLowestDark = Color(0xFF12101A)
val AmethystSurfaceContainerLowDark = Color(0xFF181620)
val AmethystSurfaceContainerDark = Color(0xFF201E28)
val AmethystSurfaceContainerHighDark = Color(0xFF282630)
val AmethystSurfaceContainerHighestDark = Color(0xFF302E38)

val AmethystSurfaceDimDark = Color(0xFF0D0B12)
val AmethystSurfaceBrightDark = Color(0xFF383640)

val AmethystInverseSurfaceDark = Color(0xFFF0ECF5)
val AmethystInverseOnSurfaceDark = Color(0xFF2D2B33)
val AmethystInversePrimaryDark = Color(0xFF5B3E96)

// ============================================
// Warm Terrain — Earthy tones, grounded premium
// Brown/sage/terracotta — Aesop-inspired natural luxury.
// ============================================

// Light
val TerrainPrimaryLight = Color(0xFF8B6B4A)
val TerrainOnPrimaryLight = Color(0xFFFFFFFF)
val TerrainPrimaryContainerLight = Color(0xFFF0DFD0)
val TerrainOnPrimaryContainerLight = Color(0xFF2C1808)

val TerrainSecondaryLight = Color(0xFF6B7D6B)
val TerrainOnSecondaryLight = Color(0xFFFFFFFF)
val TerrainSecondaryContainerLight = Color(0xFFD8E8D8)
val TerrainOnSecondaryContainerLight = Color(0xFF0C1F13)

val TerrainTertiaryLight = Color(0xFFC07050)
val TerrainOnTertiaryLight = Color(0xFFFFFFFF)
val TerrainTertiaryContainerLight = Color(0xFFFFE0D0)
val TerrainOnTertiaryContainerLight = Color(0xFF3E1515)

val TerrainErrorLight = Color(0xFFBA1A1A)
val TerrainOnErrorLight = Color(0xFFFFFFFF)
val TerrainErrorContainerLight = Color(0xFFFFDAD6)
val TerrainOnErrorContainerLight = Color(0xFF410002)

val TerrainBackgroundLight = Color(0xFFFAF6F0) // Cream
val TerrainOnBackgroundLight = Color(0xFF1C1A16)
val TerrainSurfaceLight = Color(0xFFFAF6F0)
val TerrainOnSurfaceLight = Color(0xFF1C1A16)
val TerrainSurfaceVariantLight = Color(0xFFE6E2D8)
val TerrainOnSurfaceVariantLight = Color(0xFF4A4844)

val TerrainOutlineLight = Color(0xFF7C7870)
val TerrainOutlineVariantLight = Color(0xFFD0CCC2)

val TerrainSurfaceContainerLowestLight = Color(0xFFFFFEF8)
val TerrainSurfaceContainerLowLight = Color(0xFFF3EFE6)
val TerrainSurfaceContainerLight = Color(0xFFEBE7DD)
val TerrainSurfaceContainerHighLight = Color(0xFFE4DFD4)
val TerrainSurfaceContainerHighestLight = Color(0xFFDCD7CA)

val TerrainSurfaceDimLight = Color(0xFFD8D4C8)
val TerrainSurfaceBrightLight = Color(0xFFFAF6F0)

val TerrainInverseSurfaceLight = Color(0xFF2D2B28)
val TerrainInverseOnSurfaceLight = Color(0xFFF0ECE6)
val TerrainInversePrimaryLight = Color(0xFFD4BFA0)

// Dark
val TerrainPrimaryDark = Color(0xFFD4B896)
val TerrainOnPrimaryDark = Color(0xFF3E2A18)
val TerrainPrimaryContainerDark = Color(0xFF5E4232)
val TerrainOnPrimaryContainerDark = Color(0xFFF0DFD0)

val TerrainSecondaryDark = Color(0xFFA0B8A0)
val TerrainOnSecondaryDark = Color(0xFF142A1A)
val TerrainSecondaryContainerDark = Color(0xFF3A523A)
val TerrainOnSecondaryContainerDark = Color(0xFFD8E8D8)

val TerrainTertiaryDark = Color(0xFFE8A080)
val TerrainOnTertiaryDark = Color(0xFF4A2420)
val TerrainTertiaryContainerDark = Color(0xFF683A34)
val TerrainOnTertiaryContainerDark = Color(0xFFFFE0D0)

val TerrainErrorDark = Color(0xFFFFB4AB)
val TerrainOnErrorDark = Color(0xFF690005)
val TerrainErrorContainerDark = Color(0xFF93000A)
val TerrainOnErrorContainerDark = Color(0xFFFFDAD6)

val TerrainBackgroundDark = Color(0xFF100E0C) // Deep espresso
val TerrainOnBackgroundDark = Color(0xFFE4E0D8)
val TerrainSurfaceDark = Color(0xFF100E0C)
val TerrainOnSurfaceDark = Color(0xFFE4E0D8)
val TerrainSurfaceVariantDark = Color(0xFF1E1C18)
val TerrainOnSurfaceVariantDark = Color(0xFFC4C0B8)

val TerrainOutlineDark = Color(0xFF8E8A80)
val TerrainOutlineVariantDark = Color(0xFF3A3832)

val TerrainSurfaceContainerLowestDark = Color(0xFF141210)
val TerrainSurfaceContainerLowDark = Color(0xFF1A1816)
val TerrainSurfaceContainerDark = Color(0xFF22201C)
val TerrainSurfaceContainerHighDark = Color(0xFF2A2824)
val TerrainSurfaceContainerHighestDark = Color(0xFF32302C)

val TerrainSurfaceDimDark = Color(0xFF100E0C)
val TerrainSurfaceBrightDark = Color(0xFF383634)

val TerrainInverseSurfaceDark = Color(0xFFF0ECE6)
val TerrainInverseOnSurfaceDark = Color(0xFF2D2B28)
val TerrainInversePrimaryDark = Color(0xFF8B6B4A)
