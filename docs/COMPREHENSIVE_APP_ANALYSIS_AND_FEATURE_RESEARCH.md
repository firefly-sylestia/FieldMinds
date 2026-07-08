# FieldMind — Comprehensive App Analysis & Feature Research

**Generated:** 2026-07-08
**Methodology:** Full codebase audit + web research on competitor apps, field research trends, AI/ML integration patterns, and offline-first best practices

---

## Table of Contents

1. [Current App Feature Inventory](#1-current-app-feature-inventory)
2. [Architecture Overview](#2-architecture-overview)
3. [Competitor Analysis](#3-competitor-analysis)
4. [Feature Gap Analysis](#4-feature-gap-analysis)
5. [Web Research Findings — Field Research Trends](#5-web-research-findings)
6. [AI/ML Integration Opportunities](#6-aiml-integration-opportunities)
7. [New Feature Recommendations](#7-new-feature-recommendations)
8. [UX & Polish Improvements](#8-ux--polish-improvements)
9. [Prioritized Roadmap](#9-prioritized-roadmap)
10. [Open Questions for Users](#10-open-questions-for-users)

---

## 1. Current App Feature Inventory

### Core Research Features

| Feature | Status | Notes |
|---------|--------|-------|
| Observation capture | ✅ Complete | Multi-field form with weather, GPS, evidence attachments, timer |
| Research sessions | ✅ Complete | Timed observation sessions with start/end/duration tracking |
| Species identification | ✅ Complete | Camera capture, API-based ID, catalog browsing, taxonomic browser |
| Project management | ✅ Complete | Full CRUD, folders, relations, settings |
| Notes & canvas | ✅ Complete | Rich text notes + canvas editor with figures |
| Questions & hypotheses | ✅ Complete | Full lifecycle with confidence tracking, evidence linking |
| Sources & bibliography | ✅ Complete | Citation management, PDF viewer |
| Reports | ✅ Complete | Markdown + structured reports with all sections |
| Data records (8 tools) | ✅ Complete | Counter, Measurement, Weather Log, Checklist, Event Log, Site Log, Comparison, Species |
| Tasks | ✅ Complete | Full task management with due dates, checklists, subtasks |
| Flashcards | ✅ Complete | SM-2 spaced repetition, auto-generation |
| Weather tracking | ✅ Complete | 7 weather providers, weather catalog, auto-capture |
| Map & location | ✅ Complete | Offline Maplibre maps, GPS tracking, geo-fencing |
| Media & evidence | ✅ Complete | Photo, audio, video capture with gallery |

### Learning & Knowledge

| Feature | Status | Notes |
|---------|--------|-------|
| Learn library | ✅ Complete | Curated curriculum, milestones |
| Learn reader | ✅ Complete | In-app article reader |
| Flashcards | ✅ Complete | SM-2 algorithm |
| Progress tracking | ✅ Complete | Observation streaks, statistics |

### AI Features

| Feature | Status | Notes |
|---------|--------|-------|
| Gemini integration | ✅ Complete | Research assistant, analysis |
| OpenAI integration | ✅ Complete | Alternative AI provider |
| Auto-generation (questions, flashcards, patterns) | ✅ Complete | AI-powered content generation |
| Species image analysis | ✅ Complete | Camera → API → identification |
| Local ML model support | 🟡 Partial | Downloaded model support, TFLite inference is TODO |

### Backup & Export

| Feature | Status | Notes |
|---------|--------|-------|
| FieldMind Archive (.fieldmind) | ✅ Complete | Full backup/restore with media |
| JSON export | ✅ Complete | Archive format |
| CSV export | ✅ Complete | Observations export |
| HTML export | ✅ Complete | PDF-ready HTML |
| PDF export | ✅ Complete | Simple PDF generation |
| Markdown export | ✅ Complete | Single observation share |
| Export history | ✅ Complete | Tracks last 50 exports |
| Auto-backup | ✅ Complete | WorkManager-based, configurable interval |
| Encrypted backups | ✅ Complete | AES-256-GCM encryption |
| Export privacy controls | ✅ Complete | GPS precision, media exclusion |
| Cross-reference preservation | ✅ Complete | All entity relationships backed up |

### Security & Privacy

| Feature | Status | Notes |
|---------|--------|-------|
| PIN lock | ✅ Complete | Configurable length, decoy PIN |
| Biometric lock | ✅ Complete | Fingerprint + face unlock |
| Auto-lock timeout | ✅ Complete | Immediate to 15 minutes |
| Screen capture protection | ✅ Complete | Prevents screenshots |
| Privacy typing | ✅ Complete | Hides text in fields |
| App preview mode | ✅ Complete | Normal, Blur, Privacy Screen |
| Clipboard auto-cleanup | ✅ Complete | On background + after export |
| Cooldown after failures | ✅ Complete | Configurable lockout |
| Panic lock | ✅ Complete | Wipes data after max failures |
| Require biometrics after failure | ✅ Complete | |

### Festive & Theming

| Feature | Status | Notes |
|---------|--------|-------|
| Material You | ✅ Complete | Dynamic color from wallpaper |
| Light/Dark/System theme | ✅ Complete | |
| Card gradient styles | ✅ Complete | Multiple gradient options |
| Entity color overrides | ✅ Complete | Per-category custom colors |
| Animation tuning | ✅ Complete | Spring physics customization |
| Christmas effects | ✅ Complete | Snowfall, decorations |
| Festive greetings | ✅ Complete | Splash messages |
| **Halloween effects** | ❌ TODO | Placeholder only |
| **Valentine's effects** | ❌ TODO | Placeholder only |

### Settings (100+ individual settings)

| Category | Items | Status |
|----------|-------|--------|
| Profile | Name, role, focus | ✅ Complete |
| Appearance | Theme, dynamic color, gradients, entity colors, fonts | ✅ Complete |
| Capture | Default category, confidence, location mode, media | ✅ Complete |
| AI | Provider, API keys, models, confirm before save, send attachments | ✅ Complete |
| Weather | Provider, API keys, display toggles (7 items), refresh interval, units | ✅ Complete |
| Map | Map type, show location | ✅ Complete |
| Security | PIN, biometrics, timeout, screen capture, clipboard, decoy, cooldown | ✅ Complete |
| Backup | Auto-backup, interval, retention, folder, encryption, GPS privacy | ✅ Complete |
| Units | Temperature, distance, wind speed, time, date | ✅ Complete |
| Species ID | API key, offline-first, model URL | ✅ Complete |
| Field mode | Default session, auto-start timer, spacing | ✅ Complete |
| Developer | Mode, weather test panel, debug logging, data integrity | ✅ Complete |
| Animation tuning | 8 spring parameter sliders | ✅ Complete |
| Screen visibility | Per-screen nav bar toggles | ✅ Complete |

---

## 2. Architecture Overview

### Stack

```
Kotlin 2.3.x + Jetpack Compose + Material3 (Expressive)
├── Room (KSP) — SQLite database with 30+ entity types
├── Compose Navigation — 80+ registered destinations
├── Kotlin Coroutines + StateFlow — Async & state
├── WorkManager — Background jobs (backup, reminders, streaks)
├── Maplibre GL — Offline maps
├── Coil — Image loading
├── Retrofit + OkHttp — API clients (weather, species, AI)
├── Haze — Glassmorphism effects
└── Modular by feature (no DI framework)
```

### Screen Count: 80+ registered routes
- 5 bottom tabs (Today, Capture, Projects, Insights, Library)
- 30+ settings pages
- 8 data tool screens
- 10+ detail/edit screens
- 5+ creation screens (New Observation, Note, Source, etc.)
- 10+ utility screens (Map, Weather, Backup, Compass, Timer, etc.)

---

## 3. Competitor Analysis

Based on web research, the field research data collection landscape in 2026 is dominated by these categories:

### Category 1: Purpose-Built Research Platforms

| App | Primary Use | Pricing | Key Features FieldMind Lacks |
|-----|-------------|---------|------------------------------|
| **KoboToolbox** | Academic/NGO research | Free (self-host) | Multi-user sync, server-based collaboration, XLSForm support |
| **ODK Collect** | Rigorous field research | Free (open source) | Complex form logic, longitudinal case management, server sync |
| **Epicollect5** | Simple field projects | Free | Easy web form builder, project sharing, cloud sync |
| **CommCare** | Case management | Paid | Longitudinal tracking, decision support, multi-user workflows |

### Category 2: GIS & Mapping

| App | Primary Use | Pricing | Key Features FieldMind Lacks |
|-----|-------------|---------|------------------------------|
| **ArcGIS Field Maps** | Enterprise GIS | Paid | Full GIS integration, custom map layers, field crew management |
| **QField** | QGIS fieldwork | Free | QGIS project sync, digitizing, offline geopackages |
| **Fulcrum** | Commercial field ops | Paid | AI validation, enterprise dashboards, automated insights |

### Category 3: Citizen Science

| App | Primary Use | Pricing | Key Features FieldMind Lacks |
|-----|-------------|---------|------------------------------|
| **iNaturalist** | Biodiversity crowdsourcing | Free | Global community, AI species ID suggestions, public observation network |
| **eBird** | Bird observations | Free | Standardized protocols, global database, checklists |
| **Merlin Bird ID** | Bird identification | Free | Sound ID, photo ID, bird pack downloads |

### FieldMind's Competitive Advantages

| Advantage | Details |
|-----------|---------|
| **Offline-first by design** | All features work without internet — competitors like Fulcrum need periodic sync |
| **Comprehensive feature set** | Observations + species + weather + maps + flashcards + learning in one app |
| **Privacy & security** | PIN/biometric lock, decoy mode, panic lock, clipboard cleanup — unmatched by competitors |
| **Local AI** | Gemini/OpenAI integration + local ML model support |
| **No subscription** | Free open-source with no paid tiers unlike Fulcrum ($20+/month) |
| **Backup ecosystem** | Encrypted archives, auto-backup, export history |
| **Animation & polish** | Haze glassmorphism, spring physics, liquid nav bar — far beyond typical field tools |

---

## 4. Feature Gap Analysis

### Critical Gaps (Data Loss Risk)

| Gap | Impact | Current Workaround | Effort |
|-----|--------|--------------------|--------|
| **No multi-device sync** | Data tied to one device | Manual backup/restore | Very High |
| **No cloud backup** | Device loss = total data loss | SAF folder + manual copy | High |
| **No team/collaboration** | Can't share projects with team | Export/share manually | High |
| **No server component** | No web dashboard | N/A | Very High |

### Medium Gaps (User Experience)

| Gap | Impact | Current Workaround | Effort |
|-----|--------|--------------------|--------|
| **No CSV/XLSX file import** | Can't import data from other tools | Manual entry | Medium |
| **No form builder** | Can't customize data collection forms | Fixed schema | High |
| **No observation templates** | Repetitive entry | Default values | Low |
| **No advanced statistics** | Basic counts only | Manual analysis | Medium |
| **No data visualization** | No charts/graphs for data records | Export to external tools | Medium |
| **No notification system** | No reminders for follow-ups | None | Low |

### Minor Gaps (Polish)

| Gap | Impact | Effort |
|-----|--------|--------|
| **No dark mode toggle per-screen** | Minor | Low |
| **No batch operations** | Can't select multiple entities | Medium |
| **No custom fields on observations** | Fixed schema | High |
| **No observation templates** | Repetitive entry | Low |
| **No sharing via QR code** | Can't quickly share entities | Low |
| **No bulk import from other apps** | Migration barrier | Medium |
| **No reading list/bookmarks** | Save articles for later | Low |
| **No offline Wikipedia integration** | Quick species reference | Low |

### TODO/Placeholder Issues (From QA Audit)

| ID | Issue | Status |
|----|-------|--------|
| Q1 | Halloween & Valentine's effects | ❌ Not implemented |
| Q4 | FigureSidePanel AI interpretation placeholder | ❌ Not implemented |
| Q5 | MediaGallery audio/video player placeholder | ❌ Not implemented |
| Q6 | Timer UNUSED_EXPRESSION suppression | ❌ Still suppressed |
| Q8 | Deprecated FieldMindCameraCapture | ❌ Still referenced |
| Q9 | Deprecated FieldMindMotion functions | ❌ Still present |
| Q10 | exportProgress not displayed as determinate | ❌ Still indeterminate |
| Q11 | DevFullAppTestRunner test names mismatch | ❌ Not renamed |
| Q12 | Weather database retry UI missing | ❌ Not added |
| Q13 | Open-Meteo commercial auth parameter untested | ❌ Untested |
| Q27 | Compass tool tips static/non-contextual | ❌ Not fixed |
| Q28 | Past sessions hardcoded to 10 | ❌ Not fixed |
| Q29 | Weather widget text contrast | ❌ Not fixed |

---

## 5. Web Research Findings

### Field Research App User Demands (2026)

Based on analysis of field research forums, Reddit (r/ecology, r/UXResearch), and review sites:

| User Need | Demand Level | Current FieldMind Coverage |
|-----------|-------------|---------------------------|
| Reliable offline functionality | 🟢 Critical | ✅ Excellent — fully offline |
| GPS & mapping | 🟢 Critical | ✅ Good — Maplibre offline maps |
| Photo/audio/video attachments | 🟢 Critical | ✅ Complete |
| Species identification | 🟢 Critical | ✅ Multiple AI providers |
| Data export (CSV, GIS) | 🟢 High | ✅ Multiple formats |
| **Cloud sync / backup** | 🟢 Critical | ❌ Local-only |
| **Team collaboration** | 🟢 High | ❌ Not available |
| Form customization | 🟡 Medium | ❌ Fixed schema |
| Statistics & visualization | 🟡 Medium | ❌ Basic only |
| Cross-platform (iOS/Web) | 🟡 Medium | ❌ Android only |

### Industry Trends

1. **Edge AI is the future** — Running ML models on-device for species ID, image analysis, and voice transcription without internet is the #1 request from field researchers
2. **Offline-first with smart sync** — Field researchers expect full functionality offline + automatic sync when connected
3. **Multi-modal data collection** — Combining photos, GPS traces, audio notes, weather data, and structured forms into unified observations
4. **Predictive analytics** — Using historical data to predict species movements, weather patterns, phenology
5. **Integration with existing tools** — QGIS, ArcGIS, R/Python for analysis

---

## 6. AI/ML Integration Opportunities

### Immediate Opportunities (Low Effort, High Impact)

| Opportunity | Description | Effort | Impact |
|-------------|-------------|--------|--------|
| **Species ID from existing photos** | Run on-device MLKit object detection on photos in MediaGallery | Low | Medium |
| **Auto-tagging observations** | Use Gemini to suggest tags based on observation content | Low | Medium |
| **Voice transcription in capture** | Real-time speech-to-text for observation notes | Low | Medium |
| **Smart observation suggestions** | \"Other users observing [species] nearby\" patterns | Low | Low |

### Medium-Term Opportunities

| Opportunity | Description | Effort | Impact |
|-------------|-------------|--------|--------|
| **FigureSidePanel AI analysis** | Wire placeholder to Gemini/OpenAI for real image description | Medium | High |
| **Auto-categorization** | ML-based categorization of observations from subject/notes | Medium | Medium |
| **Predictive phenology** | Predict when species will be observable based on weather patterns | High | High |
| **Anomaly detection** | Flag unusual observations (outlier timings, locations, species) | Medium | Medium |
| **Image similarity search** | Find visually similar observations using pHash | Medium | Low |

### Long-Term Opportunities

| Opportunity | Effort | Impact |
|-------------|--------|--------|
| On-device species classifier (TFLite) | High | Very High |
| Custom training from user's observations | Very High | High |
| Multi-modal search (text + image + location) | High | High |

---

## 7. New Feature Recommendations

### P0 — Critical (Data Safety)

#### 1. Cloud Backup (Google Drive)
- **Why:** Device loss = total data loss for local-only backups
- **Implementation:** Google Drive REST API via `play-services-auth`, OAuth `drive.file` scope
- **UX:** "Back up to Drive" in Backup tab, auto-backup with network constraint
- **Estimates:** 40-60 hours
- **Dependency:** `com.google.android.gms:play-services-auth`

#### 2. Multi-Device Sync Foundation
- **Why:** Users want data on phone + tablet
- **Approach:** WebSocket/HTTP server or P2P (nearby devices)
- **MVP:** Manual sync trigger, conflict resolution dialog
- **Estimates:** 60-100 hours

### P1 — High Value

#### 3. CSV/XLSX Data Import
- **Why:** Users have existing data in spreadsheets
- **Implementation:** OpenCSV or kotlin-csv parser, column mapping UI
- **UX:** Import tab → pick CSV → map columns → preview → import
- **Estimates:** 15-25 hours

#### 4. Observation Templates
- **Why:** Field researchers often repeat the same observation patterns
- **Implementation:** Save/load template with pre-filled fields
- **UX:** Template picker in capture screen, template manager in settings
- **Estimates:** 8-12 hours

#### 5. Advanced Data Visualization
- **Why:** Researchers want insights without exporting to external tools
- **Implementation:** Charts for data records (line, bar, scatter)
- **UX:** Chart view in DataToolsHub and detail screens
- **Estimates:** 20-30 hours

### P2 — Polish & UX

#### 6. Quick Capture Widget
- **Why:** Placeholder in widget dashboard — needs real implementation
- **Implementation:** Glance widget with one-tap observation capture
- **Estimates:** 8-12 hours

#### 7. QR Code Sharing
- **Why:** Quick entity sharing between devices
- **Implementation:** QR code generation for observations, projects, species
- **Estimates:** 5-10 hours

#### 8. Reading List / Bookmarks
- **Why:** Users save articles in Learn reader but can't bookmark
- **Implementation:** Bookmark Entity + bookmark manager in Library
- **Estimates:** 4-8 hours

#### 9. Dark Mode Per-Screen
- **Why:** Users override theme for specific screens
- **Implementation:** Per-screen theme toggle in overflow menu
- **Estimates:** 3-6 hours

### P3 — Seasonal & Fun

#### 10. Halloween Effects
- **Why:** Placeholder since initial development
- **Implementation:** Falling orange leaves, bat silhouettes, dark overlay
- **Estimates:** 4-6 hours

#### 11. Valentine's Effects
- **Why:** Placeholder since initial development
- **Implementation:** Floating hearts, rose petals, pink gradient
- **Estimates:** 4-6 hours

---

## 8. UX & Polish Improvements

### Visual Consistency

| Issue | Current | Target | Effort |
|-------|---------|--------|--------|
| MediaGallery audio/video | Comment placeholder only | ExoPlayer Media3 integration | 8-12h |
| Compass tips card | Static bullets | Contextual tips (calibration/interference) | 2-4h |
| Weather widget contrast | Hardcoded text color | Adaptive contrast from scene | 2-3h |
| Timer UNUSED_EXPRESSION | Suppressed warning | Clean up | 1h |

### Feature Completion

| Issue | Current State | Target | Effort |
|-------|---------------|--------|--------|
| FigureSidePanel interpretation | \"This image appears to contain...\" | AI-generated description via Gemini | 6-10h |
| Weather database retry | None on failed fetch | Retry button per entry | 2-4h |
| Past sessions limit | Hardcoded 10 | \"Show more\" button | 1-2h |
| DevFullAppTestRunner names | Misleading labels | Rename to match behavior | 1h |

### Technical Debt

| Issue | Current State | Target | Effort |
|-------|---------------|--------|--------|
| FieldMindCameraCapture | Deprecated, still referenced | Migrate to CameraV2, delete | 4-8h |
| FieldMindMotion deprecated fns | Binary compat, render nothing | Verify no callers, delete | 2-4h |
| exportProgress indeterminate | Progress set but not read | Wire to LinearProgressIndicator | 1-2h |

---

## 9. Prioritized Roadmap

### Sprint 1 — Foundation & Safety (2 weeks)
- [ ] **Q4** FigureSidePanel AI interpretation
- [ ] **Q5** MediaGallery audio/video player
- [ ] **Q27** Compass contextual tips
- [ ] **Q12** Weather database retry UI
- [ ] **Q13** Open-Meteo auth verification

### Sprint 2 — Data & Import (2 weeks)
- [ ] **F3** CSV/XLSX import
- [ ] **F4** Observation templates
- [ ] **Q8/Q9** Deprecated code removal
- [ ] **Q10** Fix exportProgress display

### Sprint 3 — Visualization & Export (2 weeks)
- [ ] **F5** Data visualization (charts)
- [ ] **F2** Cloud backup (Google Drive)
- [ ] **F7** QR code sharing

### Sprint 4 — Polish & Seasonal (2 weeks)
- [ ] **F10/F11** Halloween + Valentine's effects
- [ ] **F6** Quick capture widget
- [ ] **F8** Reading list/bookmarks
- [ ] **Q6** Timer cleanup
- [ ] **Q11** Test names correction

### Sprint 5+ — Major Features
- [ ] **F1** Multi-device sync
- [ ] On-device TFLite species classifier
- [ ] iOS companion app
- [ ] Web dashboard

---

## 10. Open Questions for Users

Based on the comprehensive analysis, here are questions for the product direction:

1. **How important is multi-device sync vs. new features?** Building sync would require a server component — is that the priority, or should features like CSV import and cloud backup come first?

2. **What's the target user segment?** The app currently serves both casual citizen scientists and professional researchers. Should we optimize for one segment? Professional researchers want form customization and GIS integration; casual users want simplicity and quick entry.

3. **Is an iOS/Web version on the roadmap?** This would fundamentally affect technology choices (KMP vs. separate codebase).

4. **How much AI is too much?** The app already has Gemini/OpenAI integration. Should we invest in on-device ML (TFLite) for privacy-conscious users, or focus on server-side AI features?

5. **Should the app remain free and open-source?** This affects monetization (if any) and which cloud services we can integrate.

---

## Appendix: Web Research Sources

| Source | URL | Key Takeaways |
|--------|-----|---------------|
| SafetyCulture — 8 Best Data Collection Apps 2026 | [safetyculture.com](https://safetyculture.com/apps/data-collection-app) | Competitor landscape |
| SurveyCTO — Data Collection Buyer's Guide | [surveycto.com](https://www.surveycto.com/resources/guides/data-collection-software-buyers-guide/) | Feature requirements by user segment |
| Felt — 8 Best Field Data Collection Apps | [felt.com](https://felt.com/blog/field-data-collection-app) | GIS integration needs |
| Fulcrum — AI-driven field data collection | [fulcrumapp.com](https://www.fulcrumapp.com/blog/ai-driven-field-data-collection-tools-techniques-and-trends/) | AI/ML trends in field research |
| CodeBranch — Species ID with AI | [codebranch.co](https://codebranch.co/blog/species-identification-and-monitoring-with-ai-a-revolution-in-conservation/) | Conservation AI |
| Dovetail — AI for qualitative research | [dovetail.com](https://dovetail.com/ux/ai-for-qualitative-data-analysis/) | NLP/AI for research |
| Teamscope — 8 Apps for Data Collection | [teamscopeapp.com](https://www.teamscopeapp.com/mobile-data-collection-guide/7-mobile-data-collection-apps-for-field-research) | Academic research tools |
| iNaturalist | [inaturalist.org](https://www.inaturalist.org/) | Citizen science AI ID |
| Fulcrum — Best Practices | [fulcrumapp.com](https://www.fulcrumapp.com/blog/best-practices-for-creating-mobile-apps-for-data-collection/) | Mobile field UX patterns |
| ODK — Open Data Kit | [getodk.org](https://getodk.org/) | Gold standard for field research |
| Kobo Toolbox | [kobotoolbox.org](https://www.kobotoolbox.org/) | NGO/academic standard |
| r/ecology — Field data tools | Reddit | Community preferences |
| r/UXResearch — Field research UX | Reddit | Professional researcher needs |

---

*End of analysis. Generated from comprehensive codebase audit + web research. This document should be re-verified when new features are implemented or when the codebase undergoes significant changes.*
