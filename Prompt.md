# Prompt.md — Running Request Log

## Latest Request — Fix CI compile failures (3 root causes)

### Status: ✅ Complete (committed & pushed)

### Summary
CI (BOM 2026.05.01) failed with compile errors across three files. Fixed all
without removing any features:

1. **SaveCaptureScreen.kt:171** — smart cast on the delegated `editingEntry`
   property (`if (editingEntry != null) { editingEntry.copy(...) }`) is
   illegal for delegated properties. Fix: capture a stable local
   (`val existingEntry = editingEntry`) and branch on that. Behavior
   identical; no other smart-cast reliance on `editingEntry` remains
   (all other usages are safe-call/let-param/local-capture).
2. **GalleryWallFormat.kt:25,287** — `androidx.compose.foundation.shape.
   RectangleShape` is not in the resolved Compose BOM. Fix: drop the import,
   use `RoundedCornerShape(0.dp)` (visually identical rectangle) for the
   full-screen canvas shape.
3. **CurioColors.kt:173–210** — `Color.toArgb()`, `.hue`, `.saturation`,
   `.lightness` are not in the resolved Compose BOM. Fix:
   - Dedupe via `accents.distinct()` (Color is a value class with
     value-based equality) instead of the toArgb round-trip.
   - `hslBlend` rewritten with local `toHsl`/`fromHsl` (standard RGB↔HSL
     math) using only bedrock `Color.red/green/blue` + `Color(r,g,b)`
     Float constructor; keeps shortest-hue-path + saturation boost, so the
     premium mixed-deck behavior is preserved. `Color.hsl` was confirmed
     available but fromHsl avoids all version-API risk.

### Validation
- Braces: SaveCaptureScreen 66/66, GalleryWallFormat 117/117, CurioColors 15/15
- Zero remaining `toArgb`/`hue`/`saturation`/`lightness` (code) and
  zero `RectangleShape` refs
- Code review: clean verdict, no blocking issues

### Prior work (this session)
- Mood board pin-to-front + clear board — committed `f128ddfd`
- Edit mood board reuses saved entry-id watermark seed — committed `c6262518`
- Category pickers tap/long-press multi-select — committed `395f0abf`
- Premium mixed-deck colors — committed `01414c20`
