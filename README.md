# Focus (working name)

An Android app for people who work from home: it shows your real phone habits, adds gentle friction
to distracting apps, and helps you do focus sessions. Personal-use MVP. No sign-in, no backend,
no network permission. Everything stays on the phone.

## Getting the APK

Every push runs the **Build APK** workflow (`.github/workflows/build.yml`).
Open the repo's **Actions** tab, pick the latest run, and download from **Artifacts**:

- `focusapp-debug-N`: debug build
- `focusapp-release-N`: smaller, minified build (recommended)

Unzip it and install the `.apk` on your phone (allow "Install unknown apps" for your browser or file manager).
Both builds share one application ID and are signed with the committed `app/debug.keystore`, so a new build
installs over the old one and keeps your data. That key isn't secret, which is fine for personal sideloading.
A Play Store release would need its own private upload key.

## Tech

Kotlin, Jetpack Compose (Material 3 styled with the v1 tokens), single activity with Compose Navigation,
MVVM with coroutines/Flow, manual DI (`di/AppContainer.kt`), DataStore for settings, and Room for history
(milestone 7). minSdk 26, targetSdk 36. Fonts (Space Grotesk, Inter) are bundled under SIL OFL.

```
app/src/main/java/io/github/satz93/focusapp/
  FocusApplication.kt, MainActivity.kt
  di/            AppContainer (manual DI)
  data/settings/ UserSettings, SettingsRepository (DataStore), GuardConfig (tunable constants)
  ui/theme/      FocusColors (design tokens), typography, shapes
  ui/components/ buttons, cards, step indicator, onboarding scaffold
  ui/navigation/ routes + NavHost (onboarding graph, home/progress, focus timer)
  ui/onboarding, ui/home, ui/focus, ui/progress
```

## Plan

| # | Milestone | Status |
|---|-----------|--------|
| 1 | Project setup, theme/tokens, navigation skeleton, DataStore settings, CI APK build | Done |
| 2 | Usage access permission, unlock + per-app usage queries, onboarding 1–4 with real data | |
| 3 | Onboarding 5–7 (goal, focus target, distracting apps), saving settings | |
| 4 | Home with live data, focus timer (foreground service + notification), session logging, session complete | |
| 5 | Accessibility service: foreground-app detection, notch timer overlay, limit-reached sheet (nudge/lock) | |
| 6 | Focus guard: block overlay, unlock challenge (wait + typed sentence, no paste), broken sessions | |
| 7 | Room history + daily WorkManager snapshot, Progress Day/Week (Compose Canvas charts) | |
| 8 | Polish: empty states, permission-revoked handling, README policy notes | |

Not every screen exists yet. Screens from later milestones are placeholders that say which milestone builds them,
so the whole flow can be clicked through from milestone 1 onward.

## Decisions so far

- **Manual DI instead of Hilt.** The app is small, and this avoids annotation processing and keeps builds fast.
- **String routes** for navigation (no serialization plugin needed).
- **No pickup/unlock limit.** Unlocks are shown for awareness only, as the brief says.
- **Open questions are constants** in `GuardConfig`: challenge wait escalation (30s → 60s → 2 min per day),
  "End session early" also needs the challenge (yes), 5-minute unlock, waking hours (16).
- **Auto Backup** includes DataStore settings and the Room database (`res/xml/*backup*`).

## Play Store policy note

The app will use an **AccessibilityService** to detect which app is in the foreground and to draw overlays,
plus the **Usage Access** special permission. That's fine for personal sideloaded use. Google Play restricts
Accessibility API use to apps whose core purpose is helping people with disabilities, unless the app goes
through a declaration and review process, so a Play release would need either that review or a switch to
usage-stats polling for detection. This will be documented properly in milestone 8.
