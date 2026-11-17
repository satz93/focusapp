# Focus (working name)

An Android app for people who work from home: it shows your real phone habits, adds gentle friction
to distracting apps, and helps you do focus sessions. Personal-use MVP. No sign-in, no backend,
no network permission. Everything stays on the phone.

## Getting the APK

Every push runs the **Build APK** workflow (`.github/workflows/build.yml`).
Open the repo's **Actions** tab, pick the latest run, and download `focusapp-N` from **Artifacts**.

Unzip it and install the `.apk` on your phone (allow "Install unknown apps" for your browser or file manager).
Builds are signed with the committed `app/debug.keystore`, so a new build installs over the old one and keeps
your data. That key isn't secret, which is fine for personal sideloading.
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
| 2 | Usage access permission, unlock + per-app usage queries, onboarding 1–4 with real data | Done |
| 3 | Onboarding 5–7 (goal, focus target, distracting apps), saving settings | |
| 4 | Home with live data, focus timer (foreground service + notification), session logging, session complete | Done |
| 5 | Accessibility service: foreground-app detection, notch timer overlay, limit-reached sheet (nudge/lock) | |
| 6 | Focus guard: block overlay, unlock challenge (wait + typed sentence, no paste), broken sessions | |
| 7 | Room history + daily WorkManager snapshot, Progress Day/Week (Compose Canvas charts) | Charts and session log done; daily snapshot to do |
| 8 | Polish: empty states, permission-revoked handling, README policy notes | |

Not every screen exists yet. Screens from later milestones are placeholders that say which milestone builds them,
so the whole flow can be clicked through from milestone 1 onward.

## Decisions so far

- **Manual DI instead of Hilt.** The app is small, and this avoids annotation processing and keeps builds fast.
- **String routes** for navigation (no serialization plugin needed).
- **No pickup/unlock limit.** Unlocks are shown for awareness only, as the brief says.
- **Open questions are constants** in `GuardConfig`: challenge wait escalation (30s → 60s → 2 min per day),
  "End session early" also needs the challenge (yes), 5-minute unlock, waking hours (16).
- **Unlock counting** (`data/usage/UsageMath.kt`): counts `KEYGUARD_HIDDEN` events (the lock screen
  was dismissed), which matches "pickups" and ignores the screen waking for notifications. Phones with no
  lock screen have no keyguard events, so it falls back to `SCREEN_INTERACTIVE`. Both need Android 9+;
  on Android 8 unlocks show as unavailable.
- **App time** comes from activity resume/pause events, closed when the screen turns off. The home screen,
  System UI and this app are excluded from "phone time".
- **Averages** use the last 7 full days with data (Android keeps detailed events for roughly a week).
  On a phone with no history yet, onboarding shows today so far.
- **Day parts**: morning 05:00–11:59, afternoon 12:00–16:59, evening 17:00–04:59.
- **Focus sessions have no daily cap.** The target is a goal: Home shows "Target hit · N extra" and extra dots,
  and every session is logged. Only completed sessions count toward the target; "End session early" is saved
  as ended early. Until the focus guard lands (milestone 6), ending early asks for a simple confirmation
  instead of the unlock challenge.
- **Timer**: a foreground service (`specialUse` type) keeps it alive with a countdown notification, and an
  exact alarm (`USE_EXACT_ALARM`) ends the session on time even while the phone sleeps. The running session is
  saved, so it survives the app process being killed. Pause/resume extends the end time.
- **Pickups during a session** are counted from usage events between the session's start and end.
- **Session length** defaults to the onboarding choice, and can be changed per session on the timer screen
  (25 / 45 / 55 min).
- **Auto Backup** includes DataStore settings and the Room database (`res/xml/*backup*`).

## Play Store policy note

The app uses **QUERY_ALL_PACKAGES** to show app names and icons; Play only allows that for certain app types.
`USE_EXACT_ALARM` is also limited on Play to alarm and timer apps.
It will also use an **AccessibilityService** to detect which app is in the foreground and to draw overlays,
plus the **Usage Access** special permission. That's fine for personal sideloaded use. Google Play restricts
Accessibility API use to apps whose core purpose is helping people with disabilities, unless the app goes
through a declaration and review process, so a Play release would need either that review or a switch to
usage-stats polling for detection. This will be documented properly in milestone 8.
