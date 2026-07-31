# Midnight Signal icon and palette rebrand

## Request

The user rejected the pastel discovery-wheel icon and requested a completely new icon design and fully changed palette—not a recolor.

## Decision

User selected **Midnight Signal**: deep navy/ink foundations, electric blue primary signal, orange energy accent, mint aperture highlight, and a genuinely new angular portal/beacon mark.

## Completed implementation

- Replaced Android adaptive launcher background/foreground with an angular open portal, mint aperture, orange spark, and dedicated monochrome themed-icon mask.
- Replaced the web SVG with matching angular geometry and updated the web CSS/Tailwind palettes, dark mode, shadows, and documentation.
- Replaced Compose palette tokens and dark surfaces while preserving source-compatible token names across existing screens.
- Replaced the splash auto-awesome glyph with the native multi-color launcher vector.
- Updated XML bootstrap resources, CURIO_SPEC, CURIO_DATA_PLAN, app/web ownership docs, and the store changelog.
- Corrected Material primary text contrast for the new signal-blue controls.

## Validation

- XML/SVG parsing, source assertions, brace checks, CSS balance, stale-reference scan, and `git diff --check` are planned.
- Gradle compilation/build/lint/test are not run locally because the repository explicitly forbids Android build commands; CI remains the compiler check.

## Status

- Implementation complete; final review and static validation in progress.
