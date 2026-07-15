# FieldMind v3 Changelog

> **Since v3.2.26.10** — 47 commits across design, animation, camera, compass, crash recovery, and UX.

---

## 🎨 Design System Overhaul

### CuteCardDefaults — Global Shape Unification
All 200+ hardcoded `RoundedCornerShape(...)` calls across the entire app have been replaced with centralized design tokens from `CuteCardDefaults`. Every card, chip, button, sheet, and icon box now uses a single consistent shape language.

- **Core shapes unified:** `Shape` (32dp), `ShapeCompact` (24dp), `ChipShape` (18dp), `ShapeHero`, `ButtonShape`, `FieldShape`, `ProgressBarShape` (10dp)
- **Corner radii increased** ~6-8dp across all tokens for a rounder, cuter overall look
- **PR #211 proportions restored** — core shapes anchored at 32/24/36dp
- **4 journal styles stripped** and unified around the single cute rounded design language — color identity preserved via warmth tints
- **Journal system fully purged** — `JournalCard.kt`, `JournalStyle.kt`, `MicroDelightIntensity`, `NavBarStyle`, and all per-style texture/ornament/divider drawing code removed (~350+ lines)
- **`journalBorderStroke` retired** — returns null, removing the last journal visual artifact

---

## ✨ Animation Engine v2

### Telegram-Inspired Spring Physics
The entire animation system was rewritten with runtime-tunable springs modeled after Telegram's bouncy, snappy feel. All `FieldMindMotion` specs are now `@Composable` readers of `AnimationConfig` for live tuning from Developer Settings.

- **Damping defaults** ~50% snappier (0.65 damping, 350 stiffness)
- **Runtime tuning sliders** in Developer Settings for entrance, swipe-back, tab, morph, and side-swipe springs
- **`LocalAnimationsEnabled`** composition local now actually works — toggle animations globally off
- **`isReduceMotion()`** respects the system animator duration scale

### Morph & Shape Animations
Powered by `androidx.graphics.shapes`, field cards and UI elements can smoothly morph between geometric shapes with spring-driven interpolation.

- **`MorphPolygonShape`** — clips any composable to a morphing polygon (circle ↔ rounded rect, star ↔ hexagon, etc.)
- **`MorphTransition`** composable — interactive tap-to-morph containers with spring animation
- **5 predefined morph pairs** with live preview in Developer Settings
- **Runtime morph tuning** — damping and stiffness sliders

### Expressive Motion System
Six new animation modifiers and composables for polished micro-interactions:

| Effect | Description |
|---|---|
| `sideReveal()` | Slide in from Start/End/Top/Bottom with spring |
| `morphShape()` | Animate corner radius between two values |
| `shimmer()` | Sweeping highlight for loading placeholders |
| `pulse()` | Gentle breathing scale for attention badges |
| `pageFlip()` | 3D card rotation around Y-axis |
| `ConfettiOverlay` | Particle burst celebration on triggers |

### Predictive Back (Android 13+)
iOS/Telegram-style peek animations when using the system back gesture. The current screen scales down to reveal **real** composable content from the previous screen behind it — not mock placeholders.

- `PredictiveBackHandler` drives the same peek/scrim/scale transforms as manual edge-swipe
- `PeekContentHolder` / `LocalPeekContentHolder` preserves previous screen state via `Key` composition
- Configurable scale minimum and scrim alpha

### Swipe Actions
- **SwipeBackHost** — edge-swipe-to-go-back with real previous-screen preview, scrim, and spring snap-back
- **SwipeActionHost** — Telegram-style item-level horizontal swipe to reveal action buttons behind
- **Center-swipe support** — optional swipe-from-anywhere mode (not just edge)
- Configurable thresholds, reveal distances, and spring physics

---

## 📷 Camera V2 Rewrite

Complete rewrite of the in-app camera experience:

- **6 bugs fixed** — duplicate `onPhotoCaptured` calls, double-shadow rendering, modifier duplication
- **Clean Z-layering** with unified cute rounded design language
- **No more system camera intent** — full CameraX integration with capture, preview, and lifecycle management

---

## 🧭 Compass & Level Polish

Major visual upgrade to the compass and bubble level tools:

- **Liquid glass bubble level** — glassmorphic bubble that mimics a real physical level
- **Glassmorphic tilt gauge** — smooth gradient-rendered linear gauge
- **Hysteresis auto-mode** — intelligent switching between compass and level modes
- **Upgraded compass styling** — cardinal labels and chart axes now rendered with Compose `TextMeasurer.drawText` instead of native `android.graphics.Paint`
- **LinearTiltGauge** — extracted `MaterialTheme.colorScheme.error` outside Canvas lambda for correctness

---

## 🛡️ Crash Recovery

The crash recovery screen is now **reliable**:

- **Native Android Views** replace Compose UI in `:crash_process` to prevent `painterResource` failures during crash rendering
- **`Application.onCreate` guarded** in crash process for safe initialization
- **Confirmation dialog** before disabling security lock
- **Crash recovery theme isolated** from main UI theme to prevent resource coupling
- **5 CI compilation errors resolved** — LayoutParams scoping, `setTypeface` overload ambiguity, dp references

---

## 🔧 UX Improvements

- **AMOLED dark mode** — auto-switches to true black (`#000000`) on AMOLED displays
- **NavBarStyle** blob color now actually changes per selected style
- **Premium screen transitions** with scale+fade animation
- **Achievements expand/collapse** — restored full-row tap target with `Row.clickable`
- **Restore from backup** — one-tap card on Backup & Restore screen auto-launches file picker
- **PdfViewer icon fix** — replaced invalid `"description_off"` symbol with `"description"`

---

## ⚡ Performance & Cleanup

- **Sound effects system removed** — deleted `FieldMindSoundManager` + 9 sound assets (~700 KB APK savings)
- **Journal system purged** — `JournalCard.kt`, `JournalStyle.kt`, and all dormant drawing code stripped (~350 lines)
- **Dead analysis markdown files removed** — fresh UI/UX + functionality analysis added
- **Stale imports and dead code cleaned** across 35+ files

---

## 🔨 Build & CI

- **graphics-shapes 1.0.1 API migration** — fixed `asPath`, `circle`/`rectangle`/`star` signatures, added `regularPolygon` vertex-computation helper
- **Kotlin 2.3.21 + Compose BOM 2026.05.01** compatibility maintained
- **Multiple CI compilation fixes** — import resolution, `setTypeface` overload, `RippleTheme` API removal, `BorderStroke` imports
- **`dp20` out-of-scope fix** in CrashActivity
- **`tabEntranceSpring()` → `spring()` migration** for removed API

---

## 📦 Version Info

| Property | Value |
|---|---|
| Previous tag | `v3.2.26.10` |
| Commits | 47 |
| Lines changed | ~15,000+ across 200+ files |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 37 |
| Compose BOM | 2026.05.01 |
| Kotlin | 2.3.21 |

---

*Generated from `git log v3.2.26.10..HEAD` on the `finetune` branch.*
