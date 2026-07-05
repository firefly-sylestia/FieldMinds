# 🌸 Cutifying FieldMind — Comprehensive Beautification Plan

**Last Updated:** 2026-07-01  
**Status:** Phases 0–3 Complete (Pastel Theme, Cute Cards, Staggered Animations, Mini-Cards)

---

## Vision Statement

Transform FieldMind from a functional research tool into a **delightful, cozy, aesthetically beautiful app** that sparks joy every time you open it — without sacrificing usability or performance. The app should feel like a warm, thoughtfully designed field journal, not a sterile database.

---

## ✅ Completed Phases

### Phase 0: Foundation — Soft Shadows & Cute Cards (v0.23.0)

**Files:** `CuteThemeConfig.kt`, `FieldMindComponents.kt`, 15 screen files

| What | Details |
|------|---------|
| **CuteThemeConfig.kt** | Central theme config with `CuteElevations` (5 plush tiers), `CuteShadows`, `CuteCardDefaults` |
| **Plush elevations** | plushTier1 (2dp) → plushTier5 (12dp) for consistent depth everywhere |
| **Standard cards elevated** | All cards now 4dp shadow — cards float above background |
| **Cute card shapes** | `CuteCardDefaults.Shape` (32dp radius), `ShapeCompact` (24dp), `ShapeHero` (36dp) |
| **cuteShadow modifier** | `Modifier.cuteShadow()` for adding soft shadows to any composable |
| **Surface colors** | Cards use `surface` color for clean, bright faces; backgrounds use `surfaceContainerLow` |
| **Expressive press** | Cards lift + scale on press (1.5dp lift, 0.985 scale) |

### Phase 1: Pastel Color Theme (v0.24.0)

**Files:** `Color.kt`, `Theme.kt`, `FieldMindTheme.kt`, `MainActivity.kt`, `FieldMindSettingsScreen.kt`, `CuteThemeConfig.kt`

| What | Details |
|------|---------|
| **Pastel M3 palette** | Lavender primary (#B39DDB), blush secondary (#F8BBD0), mint tertiary (#A5D6A7) |
| **Warm light bg** | Pink-white (#FEF9F0) backgrounds for warm, cozy feel |
| **Deep warm dark** | Purple-black (#1A1625) backgrounds that glow in dark mode |
| **Pastel entity colors** | 12 research entity types get soft pastel tints — sage, sky, blush, lavender, rose, amber |
| **Pastel confidence colors** | Soft sage (sure), warm amber (guess), soft coral (verify) |
| **10 chart colors** | Pastel categorical chart colors for consistency |
| **10 color schemes** | Default, Pastel, Warm, Cool, Forest, Rose, Lavender, Mint, Ocean, Monochrome |
| **Settings picker** | Color scheme picker in Settings → Appearance |
| **Auto-switch entity colors** | Entity colors switch to pastel when Pastel scheme is active |

### Phase 2: Mini-Card Section Headers & Plush Chips (v0.24.1)

**Files:** `FieldMindComponents.kt`

| What | Details |
|------|---------|
| **SectionHeader → mini-card** | Wrapped in Surface with 2dp cuteShadow, tonal elevation |
| **Accent color strip** | 4dp wide left strip auto-detects entity color from title |
| **28dp rounded corners** | Plush, pill-like feel with proper padding |
| **InfoChip elevated** | 2dp soft shadow, `surfaceContainerLow` bg, tonal elevation |
| **ConfidenceChip elevated** | Same treatment, floats like tiny badge |
| **EntityBadge elevated** | Keeps semantic accent color, now floats with shadow |
| **TagChip** | Inherits InfoChip styling |

### Phase 3: Staggered Entrance Animations (v0.24.2)

**Files:** `FieldMindMotion.kt`, `FieldMindComponents.kt`, `ClickableCard.kt`

| What | Details |
|------|---------|
| **staggeredEntrance modifier** | `Modifier.staggeredEntrance(index, animate, offsetY)` |
| **Fade-in + slide-up** | Items start alpha=0, 20dp below → glide up + fade in |
| **Staggered cascade** | 80ms initial + 50ms per index delay |
| **ExpressiveFloat spring** | 0.94 damping, 180 stiffness for buttery motion |
| **Applied to all cards** | SectionHeader (animate=true default), EntityCard, ClickableCard, InfoCard, MetricTile |
| **Reduce-motion aware** | Respects system accessibility setting |
| **No layout shifts** | Purely visual via graphicsLayer (alpha + translationY) |

---

## 🚧 Phase 4: Micro-Interactions & Touch Feedback

### Priority: High — Effort: Medium

| Task | Description | File(s) | Status |
|------|-------------|---------|--------|
| 4a | **Ripple-less press feedback** — Replace default Material ripple with `expressiveCardPress` lift+scale everywhere | `ClickableCard.kt` ✅ (already done) | 🟢 Done |
| 4b | **Nav bar icon bounce** — Active tab icons pulse/spring when tapped | `FieldMindNavigation.kt` | ⚪ Planned |
| 4c | **Checkmark spring** — Checkmark icon does a tiny scale-bounce when toggled | `FieldMindComponents.kt` | ⚪ Planned |
| 4d | **Swipe-to-complete tasks** — Tasks can be swiped to mark complete with a satisfying snap animation | `FieldMindTasksScreen.kt` | ⚪ Planned |
| 4e | **Pull-to-refresh with cute animation** — Custom pull-down indicator (e.g., a leaf that shakes) | Home/Archive screens | ⚪ Planned |
| 4f | **Like/reaction heart animation** — Tapping a heart/favorite icon produces a tiny burst animation | Detail screens | ⚪ Planned |
| 4g | **Button press on Surface** — All `Surface(onClick=...)` should use `.pressScale()` for consistent feedback | Globally | 🔶 Partial (many done, audit needed) |

---

## 🚧 Phase 5: Card & Surface Polish

### Priority: High — Effort: Medium

| Task | Description | File(s) | Status |
|------|-------------|---------|--------|
| 5a | **Dialog cards** — All dialogs should use `CuteCardDefaults.Shape` + elevation presets | `FieldMindDialogs.kt` + others | ⚪ Planned |
| 5b | **Bottom sheets** — Rounded top corners (32dp) with handle indicator | All bottom sheets | ⚪ Planned |
| 5c | **SettingsGroupCard** — Add subtle `cuteShadow` for visual separation in settings | `FieldMindSettingsScreen.kt` | ⚪ Planned |
| 5d | **Entity cards in lists** — Verify all use same `RoundedCornerShape(34.dp)` + 4dp elevation | All screens | 🔶 Partial audit |
| 5e | **Empty states** — Use more elaborate illustrations or gradient backgrounds | `FieldMindComponents.kt` | ⚪ Planned |
| 5f | **Card border on select** — Use `accent.cardBorder()` instead of hardcoded alpha | `FieldMindComponents.kt` | ⚪ Planned |

---

## 🚧 Phase 6: Typography & Iconography

### Priority: Medium — Effort: Low

| Task | Description | File(s) | Status |
|------|-------------|---------|--------|
| 6a | **Geom font pass** — Verify Geom font is used consistently across all screens | All screens | 🔶 In progress |
| 6b | **Icon weight pass** — Use `filled=true` for active/selected icons, default for inactive | `FieldMindIcons.kt` | ⚪ Planned |
| 6c | **Icon animations** — Icons that transition between states (play→pause, expand→collapse) use spring animation | Misc | ⚪ Planned |
| 6d | **Logo polish** — App logo with gradient and soft glow on splash/onboarding | `FieldMindOnboardingScreen.kt` | ⚪ Planned |
| 6e | **Splash screen** — Animated splash with logo fade-in + scale | `FieldMindCrashActivity.kt` | ⚪ Planned |

---

## 🚧 Phase 7: Backgrounds & Atmosphere

### Priority: Medium — Effort: High

| Task | Description | File(s) | Status |
|------|-------------|---------|--------|
| 7a | **Subtle gradient backgrounds** — Each tab screen gets a subtle vertical gradient (light → slightly tinted) | All tab screens | ⚪ Planned |
| 7b | **Weather scene as home bg** — Animated weather particles behind semi-transparent home cards | `FieldMindHomeScreen.kt` | ⚪ Planned |
| 7c | **Glassmorphism** — Cards with semi-transparent `surface.copy(alpha=0.8f)` + blur for layering | All screens | ⚪ Planned |
| 7d | **Night mode ambience** — Darker, warmer backgrounds at night with subtle star/glow particles | `FieldMindTheme.kt` | ⚪ Planned |
| 7e | **Seasonal accents** — Subtle seasonal color shifts (pastel spring, warm autumn) | `Color.kt` | 🟡 Backlog |
| 7f | **Festive overlays** — Snowflakes in winter, cherry blossoms in spring, fireflies in summer | `FestiveTheme.kt` | 🟡 Backlog (exists as festive) |

---

## 🚧 Phase 8: Animation & Motion System

### Priority: Medium — Effort: Medium

| Task | Description | File(s) | Status |
|------|-------------|---------|--------|
| 8a | **Screen transition polish** — Ensure all route transitions use configurable springs | `FieldMindNavigation.kt` | 🔶 Partial (settings exist) |
| 8b | **Shared element transitions** — Card tap → detail screen with smooth position animation | All entity cards | ⚪ Planned |
| 8c | **Count-up numbers** — Metrics animate from 0 to final value on scroll-in | `MetricTile`, Insights | ⚪ Planned |
| 8d | **Skeleton shimmer** — Loading placeholders with gradient shimmer animation | All loading states | ⚪ Planned |
| 8e | **Chart entrance** — Chart bars/lines animate from zero on screen appear | All chart composables | ⚪ Planned |
| 8f | **List reorder animation** — Smooth `animateItemPlacement()` for filtered/sorted lists | All LazyColumns | ⚪ Planned |
| 8g | **Tab switch animation** — Current tab content slides out, new tab slides in | `FieldMindNavigation.kt` | 🔶 Exists, can polish |

---

## 🚧 Phase 9: Settings & Configuration UI

### Priority: Low — Effort: Low

| Task | Description | File(s) | Status |
|------|-------------|---------|--------|
| 9a | **Animation speed slider** — Global animation speed control in Appearance settings | `FieldMindSettingsScreen.kt` | 🟡 Backlog |
| 9b | **Reduce motion toggle** — In-app toggle for reduced motion (respects system too) | `FieldMindSettingsScreen.kt` | 🟡 Backlog |
| 9c | **Theme preview** — Live preview of color scheme before applying | `AppearanceSettingsPage` | 🟡 Backlog |
| 9d | **Font size/weight** — Customize body and heading font weights/sizes | `FieldMindSettingsScreen.kt` | 🟡 Backlog |
| 9e | **Corner radius preference** — Users can adjust global card roundness (default: 32dp) | `CuteThemeConfig.kt` | 🟡 Backlog |

---

## 🚧 Phase 10: Deep Visual Hierarchy

### Priority: Low — Effort: High

| Task | Description | File(s) | Status |
|------|-------------|---------|--------|
| 10a | **Depth map** — Ensure all surfaces use correct tonal elevations (0→3dp) for consistent hierarchy | All screens | 🔶 Partial audit |
| 10b | **Surface color audit** — Verify correct usage of `surface`, `surfaceContainerLow`, `surfaceContainerHigh` | All screens | 🔶 Partial audit |
| 10c | **Color contrast check** — WCAG AA compliance for all text-on-background combinations | Theme | ⚪ Planned |
| 10d | **Screenshot polish** — Every screen looks good in a screenshot (no clipped text, broken layouts) | All screens | ⚪ Planned |

---

## Implementation Principles

1. **Reuse existing systems** — Use `CuteElevations`, `CuteCardDefaults`, `cuteShadow()`, `staggeredEntrance()`, `expressiveCardPress()` — never hardcode values
2. **One file, one responsibility** — Theme config lives in `CuteThemeConfig.kt`, animations in `FieldMindMotion.kt`, colors in `Color.kt`
3. **Reduce-motion friendly** — Every animation must respect `FieldMindMotion.isReduceMotion()`
4. **Incremental rollout** — Each phase is independently shippable and adds delight without breaking existing functionality
5. **Consistent rounding** — All card corners should be 32-34dp; section headers 28dp; chips 999dp (pill)
6. **Spring > Tween** — Prefer spring animations (damping ~0.94, stiffness ~180) over tweens for organic feel

---

## How to Add a New Theme/Color Scheme

1. Add color constants in `Color.kt` (e.g., `PastelPrimaryLight`, `PastelPrimaryDark`, etc.)
2. Add case in `Theme.kt` → `getCustomColorScheme()`
3. Add entity colors in `FieldMindTheme.kt` (light + dark `FieldMindColors` objects)
4. Add option string to the picker in `FieldMindSettingsScreen.kt`
5. Reference in `CuteThemeConfig.kt` doc comment
6. Add changelog entry

---

## Design Tokens Reference

### Elevation Tiers (from `CuteElevations`)

| Token | Value | Usage |
|-------|-------|-------|
| `plushTier1` | 2dp | Background surfaces, low-focus info cards |
| `plushTier2` | 4dp | Standard card elevation (default) |
| `plushTier3` | 6dp | Featured cards, hero surfaces |
| `plushTier4` | 8dp | Dialogs, bottom sheets |
| `plushTier5` | 12dp | Modals, pickers, important overlays |

### Corner Radius Presets (from `CuteCardDefaults`)

| Token | Value | Usage |
|-------|-------|-------|
| `Shape` | 32dp | Standard cards |
| `ShapeCompact` | 24dp | Inline/compact cards |
| `ShapeHero` | 36dp | Hero/banner surfaces |
| SectionHeader | 28dp | Section header mini-cards |
| Chips/Badges | 999dp | Pill shapes (effectively circular) |

### Spring Animation Defaults

| Property | Value | Usage |
|----------|-------|-------|
| Damping ratio | 0.92–0.95 | Smooth, no bounce |
| Stiffness | 120–200 | Slow, elegant (lower = slower) |
| Stagger initial | 80ms | First item delay |
| Stagger per item | 50ms | Cascade increment |
| Max stagger | 500ms | Cap for long lists |

---

## Metrics & Goals

- **Card consistency:** 100% of cards use `CuteElevations` or `cuteShadow()` — no raw `shadow()` calls
- **Animation coverage:** Every interactive element has press feedback
- **Entrance animation:** Every scrollable list has staggered entrance on items
- **Dark mode parity:** Every visual enhancement works equally well in dark mode
- **Performance target:** <1ms animation overhead per frame (shimmer/skeletons excluded)
- **Test coverage:** All animation modifiers should work with reduce-motion enabled
