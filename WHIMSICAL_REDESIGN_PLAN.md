# 🌿 FieldMind Whimsical Redesign Plan

## Vision
A **cozy nature journal meets whimsical explorer** aesthetic — Studio Ghibli meets an antique naturalist's sketchbook. Warm, textured, and full of delightful surprises.

## Implementation Phases

### Phase 1: JournalStyle Theming Infrastructure ✅ STARTING HERE
**Goal:** Create the foundation that enables all 4 journal aesthetics with settings toggles.

**Files to create/modify:**
- `app/src/main/java/fieldmind/research/app/shared/presentation/theme/JournalStyle.kt` (NEW)
- Modify `FieldMindSettings.kt` - Add `journalStyle`, `backgroundStyle`, `microDelightIntensity` settings
- Modify `FieldMindTheme.kt` - Wire journal style into theme application
- Modify `FieldMindSettingsScreen.kt` - Add Journal Style picker to Appearance settings

**4 Journal Style presets:**
1. **Victorian Naturalist** - Parchment tones, copperplate aesthetic, scientific illustration vibes, formal serif typography, ornate borders
2. **Explorer's Sketchbook** - Cream paper, pencil-sketch textures, field notes charm, irregular hand-drawn card borders, practical stains
3. **Modern Bullet Journal** - Dot-grid textures, neatly hand-lettered headings, washi tape accents, sticker elements, clean organization
4. **Ghibli Storybook** - Watercolor washes, soft dreamy edges, whimsical characters, warm fantasy tones, soft glow effects

**Settings:**
- `journalStyle` (String) - "Victorian" / "Sketchbook" / "BulletJournal" / "Ghibli"
- `backgroundAnimation` (String) - "Static" / "Gentle" / "Full"
- `microDelightIntensity` (String) - "Minimal" / "Normal" / "Maximum"
- `navBarStyle` (String) - "Modern" / "Nature" / "Journal"

### Phase 2: Immersive Time-of-Day Background System
**Goal:** Replace flat backgrounds with animated nature scenes that change with time and weather.

**Components:**
- `AnimatedBackgroundScene.kt` - Canvas-based scene composable
- 4 time states: Dawn (golden+mist) / Day (dappled sun) / Evening (amber+fireflies) / Night (stars+moon)
- Weather integration: rain falls on scene, clouds drift, fog mist
- Each journal style gets different color treatment

### Phase 3: Card & Component Aesthetic Overhaul
**Goal:** Every card, button, and surface reflects the chosen journal aesthetic.

**Key changes:**
- Card composables get journal-style parameters (paper texture, border style, corner treatment)
- Navigation bar: animated nature-themed active indicator
- Sketch-like irregular card borders for Sketchbook style
- Watercolor wash backgrounds for Ghibli style
- Parchment gradients for Victorian style
- Dot-grid subtle overlay for Bullet Journal style

### Phase 4: Whimsical Micro-Delights
**Goal:** Surprise and joy throughout interactions.

**Delights per intensity:**
- **Minimal:** Subtle press haptics, gentle transitions
- **Normal:** Add celebration overlays, streak bird flocks, goal vine growth
- **Maximum:** Butterflies on capture, fireflies at night, leaf showers on refresh, ambient nature sounds, seasonal effects

### Phase 5: Magical Onboarding
**Goal:** Make first-run feel like opening a magical journal.

- Full-screen illustrated welcome (one question at a time, not a form page)
- Animated nature scene background
- Name appears in calligraphy on journal cover
- Each step has a charming illustration

---

## Architecture Decisions

### JournalStyle data class
```kotlin
data class JournalStyle(
    val name: String,
    val displayName: String,
    val colors: JournalColorPalette,
    val typography: JournalTypography,
    val cardStyle: JournalCardStyle,
    val backgroundTexture: JournalTexture,
    val ornamentStyle: JournalOrnament
)
```

### How it integrates
- `FieldMindTheme` observes the `journalStyle` setting (via StateFlow)
- A `LocalJournalStyle` CompositionLocal provides the active style to all composables
- Each card/surface/component reads `LocalJournalStyle.current` to adapt its rendering
- Setting changes trigger animated transitions (existing animated color scheme system supports this)

### File organization
- `theme/JournalStyle.kt` - Main JournalStyle class and presets
- `theme/journal/` - Subfolder for each journal style's specific config
  - `VictorianJournal.kt`
  - `SketchbookJournal.kt`  
  - `BulletJournal.kt`
  - `GhibliJournal.kt`
