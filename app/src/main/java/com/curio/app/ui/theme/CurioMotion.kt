package com.curio.app.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

/**
 * Curio's motion design tokens — see CURIO_SPEC.md section 0.5.
 *
 * Centralizes the spring specs + duration constants that the rest of the app
 * uses for animations, so every transition uses the same easing vocabulary.
 *
 * Three named spring presets (per M3 expressive motion spec):
 *
 *  - [Springs.Snappy] — high stiffness, no overshoot. For small UI changes
 *    that should feel decisive (chip selection, drawer toggles, modal
 *    mounts). Use for anything where overshoot would feel jittery.
 *
 *  - [Springs.Bouncy] — lower stiffness, ~55% damping ratio. For reward
 *    moments + playful arrivals. Things should overshoot and settle like
 *    a gummy bounce. Use for the dial settling on the Spin screen,
 *    the entry card mounting, the Save success animation.
 *
 *  - [Springs.Deliberate] — moderate stiffness, slight overshoot. For
 *    bigger elements moving larger distances (screen transitions, sheet
 *    mounts). Slower than Snappy but more controlled than Bouncy.
 *
 * Duration tokens in milliseconds:
 *
 *  - [Durations.Quick]      — 150ms (chip toggles, button presses)
 *  - [Durations.Standard]   — 300ms (default transitions)
 *  - [Durations.Deliberate] — 500ms (larger movements)
 *  - [Durations.SpinMin]    — 2500ms (low end of The Spin rotation)
 *  - [Durations.SpinMax]    — 3500ms (high end of The Spin rotation)
 *  - [Durations.Confetti]   — 600ms (reward burst lifetime)
 *  - [Durations.RevealHold] — 400ms (pause after landing before nav to Reveal)
 */
object CurioMotion {

    object Springs {
        /** No overshoot, fast — chip toggles, button presses, drawer mounts. */
        val Snappy: SpringSpec<Float> = spring(
            dampingRatio = 1.0f,
            stiffness = 1800f
        )

        /** ~55% damping ratio, medium stiffness — gummy-bounce overshoot for rewards. */
        val Bouncy: SpringSpec<Float> = spring(
            dampingRatio = 0.55f,
            stiffness = 380f
        )

        /** ~85% damping ratio, slower stiffness — controlled overshoot for big transitions. */
        val Deliberate: SpringSpec<Float> = spring(
            dampingRatio = 0.85f,
            stiffness = 250f
        )

        /** Used by SpinScreen's dial rotation — strong overshoot for the "settling" feel. */
        val WheelLanding: SpringSpec<Float> = spring(
            dampingRatio = 0.40f,
            stiffness = 80f
        )
    }

    object Durations {
        const val Quick: Int = 150
        const val Standard: Int = 300
        const val Deliberate: Int = 500

        /** The Spin rotation window (per section 5: 2.5 to 3.5 seconds). */
        const val SpinMin: Int = 2500
        const val SpinMax: Int = 3500

        /** Confetti / sparkle burst lifetime (per section 0.5: ~600ms total). */
        const val Confetti: Int = 600

        /** Pause between Spin landing and auto-navigation to Topic Reveal. */
        const val RevealHold: Int = 400
    }

    /** Particle count for the confetti burst (per section 0.5: 6 to 10 tiny shapes). */
    const val ConfettiParticleCount: Int = 8

    /** Default dial wedge count (per section 5: 6 to 8 visual segments). */
    const val DialWedgeCount: Int = 6

    /** Number of full rotations per Spin (per section 5: 3 to 5). */
    const val MinSpinTurns: Int = 3
    const val MaxSpinTurns: Int = 5
}