# Weather Animation System — DOX Rail

## Purpose

This directory contains the FieldMind physics-based weather animation system,
replacing the legacy static weather scene with real-time 3D physics simulations.

## Architecture Overview

```
AnimatedWeatherScene.kt        ← Public API (maintains backward compat)
    ↓ orchestrates
WeatherPhysicsEngine.kt        ← Core physics: Newtonian mechanics, 3D depth, forces
AtmosphericChemistry.kt        ← Sky color: Rayleigh/Mie scattering, chemical air masses
WeatherEffects.kt              ← Rendering: rain, snow, clouds, fog, lightning, etc.
```

## File Responsibilities

### `WeatherPhysicsEngine.kt`
- `FrameClock` — frame-rate-independent deltaTime with 50ms cap
- `DepthLayer` — 3D parallax depth layers (SKY_BACKGROUND → UI_OVERLAY)
- `PhysicsBody` — position, velocity, acceleration, mass, charge, temperature
- `ForceAccumulator` — gravity, wind, drag (quadratic), buoyancy, turbulence, Coriolis
- `PhysicsBodyPool` — pre-allocated object pool (zero GC pressure)
- `PhysicsScene` — top-level orchestrator: resize, update(dt), applyGust, spawnBurst, clear

### `AtmosphericChemistry.kt`
- `SolarPosition` — zenith angle, path length from hour of day
- `SkyColorModel` — Rayleigh (λ⁻⁴) + Mie (λ⁻²) scattering, ozone Chappuis band, water vapor
- `ChemicalAirMass` — 7 preset air masses (pristine, maritime, urban, desert, arctic, etc.)
- `LightningSpectrum` — N₂⁺/O emission lines, blackbody color at 28,000K
- `WeatherPaletteGenerator` — generates `WeatherPalette` from physics + chemistry

### `WeatherEffects.kt`
- `RainSystem` — spawns drops via physics pool, draws with motion blur, splash on ground
- `SnowSystem` — 6-fold crystal symmetry, wobble, sparkle
- `CloudSystem` — 5 morphologies (cirrus, cumulus, stratus, cumulonimbus, altocumulus)
- `FogSystem` — multi-layer sine-band density field
- `LightningSystem` — fractal branching with stepped leaders, flash overlay
- `RainbowSystem` — primary + secondary arc (ROYGBIV + reversed)
- `StarSystem` — twinkling stars with color variation
- `GroundSystem` — procedural terrain with horizon, hill noise

### `AnimatedWeatherScene.kt`
- Maintains the exact same public API as the original:
  `fun AnimatedWeatherScene(weatherCode, temperature, sunrise, sunset, modifier, compact, forceNight, showCloudAnimation)`
- Frame loop: `LaunchedEffect` + `withFrameNanos` for deltaTime-accurate simulation
- Touch interaction: tap creates splashes/wind gusts/lightning
- Static fallback for preview/inspection mode

## Key Design Decisions

1. **Single physics pool** — All precipitation (rain, snow, splash) shares one `PhysicsBodyPool` to prevent memory fragmentation
2. **Frame-loop architecture** — Physics updates happen in a `LaunchedEffect` + `withFrameNanos` loop, rendering happens in `Canvas` draw. Separated update/render for clean architecture
3. **Chemistry-based colors** — Sky colors come from Rayleigh/Mie scattering physics, not hand-picked gradients. 7 chemical air masses for different weather conditions
4. **3D depth via parallax** — `DepthLayer` enum controls parallax offset, perspective scale, and atmospheric haze for each layer
5. **Object pooling** — `PhysicsBodyPool` pre-allocates all bodies. `borrow()` recycles dead bodies, eliminating GC during simulation
6. **Velocity Verlet integration** — More stable than Euler for spring-like forces (turbulence, buoyancy)

## WMO Weather Code Mapping

| Code Range | Effect | Cloud Coverage |
|---|---|---|
| 0-1 | Clear/sunny | 0-25% |
| 2-3 | Cloudy | 85-100% |
| 45-48 | Fog | 0% (fog rendered) |
| 51-67 | Rain | 90% |
| 71-86 | Snow | 70% |
| 95+ | Thunderstorm | 100% + lightning |

## Performance Notes

- Target: 60 FPS on mid-range devices (Snapdragon 7xx+)
- Compact mode halves particle counts and simplifies cloud rendering
- `PhysicsBodyPool` size of 256 handles rain + splash simultaneously
- Canvas-based rendering avoids composable recomposition overhead
- Adaptive sub-stepping in `PhysicsScene.update()` prevents instability at <30 FPS
