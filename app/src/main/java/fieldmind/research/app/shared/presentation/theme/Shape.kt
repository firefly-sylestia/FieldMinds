package fieldmind.research.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Material Design 3 Expressive Shape System
// Uses more organic, rounded shapes for expressive design language
val Shapes = Shapes(
    // ── Bouncy, cute, pill-like shapes ──
    // Extra rounded corners for a friendly, approachable feel.
    
    // Extra small components (small chips, badges)
    extraSmall = RoundedCornerShape(16.dp),
    
    // Small components (chips, small buttons)
    small = RoundedCornerShape(20.dp),
    
    // Medium components (buttons, cards, FABs)
    medium = RoundedCornerShape(24.dp),
    
    // Large components (sheets, dialogs, large cards)
    large = RoundedCornerShape(32.dp),
    
    // Extra large components (full-width modals, prominent surfaces)
    extraLarge = RoundedCornerShape(40.dp)
)


