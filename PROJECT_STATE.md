# PROJECT_STATE.md — Current Project State

Last updated: 2026-08-08

## Current Phase
**Phase 1 CI verified GREEN. Phase 2 (Capture Abstraction) in progress — CaptureProvider, WebViewCaptureProvider, registry, internal capture mode, and unit tests implemented.**

## Completed
- Backup of pre-Phase-1 MergedProject created.
- Removed Gradle `sourceSets` references to `../../E-Ujian_RE_JADX`.
- Returned to standard `app/src/main/{java,res,AndroidManifest.xml}` layout.
- Aligned namespace/applicationId to `id.eujian.cbt.screenpilot`.
- Migrated production type references from legacy `com.example.*`.
- Updated test/androidTest namespaces.
- Confirmed local ScreenPilot manifest is used.
- Confirmed JADX evidence remains outside the build.
- `PHASE1_REPORT.md` created.
- Added project memory files: `AGENTS.md`, `PROJECT_STATE.md`, `DECISIONS.md`, `TODO.md`.
- Created `.gitignore` (Gradle/build/local/SDK/secrets ignored; wrapper jar/properties + gradlew + version catalog kept).
- Audited `.github/workflows/android-build.yml` (compatible with JDK 21 / Gradle 9.3.1 / AGP 9.1.1 / compileSdk 35 / testDebugUnitTest / assembleDebug / artifact upload; risk notes only, not edited).
- `PRE_CI_REPORT.md` created.
- Phase 1.6: removed empty internal stub `MergedProject/E-Ujian_RE_JADX/` (reconfirmed 0 files). External evidence `AntiGravityIDE/E-Ujian_RE_JADX` (3,913 files) untouched.
- Phase 1.6: moved `app/src/androidTest/java/com/example/ExampleInstrumentedTest.kt` → `app/src/androidTest/java/id/eujian/cbt/screenpilot/ExampleInstrumentedTest.kt` (package `id.eujian.cbt.screenpilot`). Removed empty legacy `com/example` directories.
- Phase 1.6: audited `.gitignore` final contents (no global `*.properties`; wrapper properties/gradlew/catalog kept).
- Phase 1.6: `FINAL_REPO_CHECK.md` created.
- Phase 1.7: `git init -b main` — local repository initialized.
- Phase 1.7: verified `.gitignore` rules via `git check-ignore` — build outputs, secrets, local.properties, .gradle/ all correctly ignored; required build files (gradlew, gradle-wrapper.jar/properties, libs.versions.toml, gradle.properties) correctly trackable.
- Phase 1.7: staged all 93 files with `git add .`.
- Phase 1.7: audited staged files for secrets, local.properties, APK/AAB, build outputs, .gradle, JADX/evidence, .class files — all clean.
- Phase 1.7: committed baseline `8aabf1a` — "chore: establish standalone ScreenPilot baseline".
- Phase 1.7: `GIT_BASELINE_REPORT.md` created.

## Phase 2 — Capture Abstraction
- Created `CaptureProvider` interface + `CaptureResult` sealed class in `id.eujian.cbt.screenpilot.capture` package.
- Implemented `WebViewCaptureProvider` — renders `WebView` content to `Bitmap` via `Canvas` on `Dispatchers.Main.immediate`.
- Created `CaptureProviderRegistry` — thread-safe object holder for injectable provider lifecycle.
- Created `FakeCaptureProvider` for unit tests — returns solid-color dummy `Bitmap`.
- Injected provider via registry into `ScreenCaptureService`; `captureScreen()` uses `CaptureProviderRegistry.get()`; added `CaptureSource` enum + `ACTION_START_INTERNAL_CAPTURE` for provider-only mode (no MediaProjection).
- Health watcher skips MediaProjection/virtualDisplay/imageReader null checks when `CaptureSource.INTERNAL_PROVIDER`.
- Created `app/src/debug/assets/capture_test.html` — test-only HTML (heading, radio buttons, checkboxes, table, placeholder).
- Wired `WebViewCaptureProvider` in `MainActivity.onCreate()` via lifecycle-aware `WebViewClient.onPageFinished`; WebView explicitly measured (1080x1920) for non-zero viewport.
- Moved `FakeCaptureProvider` from `src/main/` → `src/test/`; added `CaptureProviderTest.kt` unit test.
- Commit `dc5c7b3` — "feat: CaptureProvider abstraction with WebView capture".
- Commit `411c812` — "fix: correct CaptureProvider lifecycle and internal capture wiring" (CI fix: registry pattern + WeakReference + internal mode + test relocation).

## Static Verification
Passed:
- no `../../` build dependency
- no production `com.example.*` type references
- standard sourceSets only
- local AndroidManifest present
- required manifest resources present
- dependency aliases resolve
- 32 production Kotlin files
- no `E-Ujian_RE_JADX` inside `MergedProject`
- no legacy `com/example` package directories anywhere under `app/src`
- namespace/applicationId = `id.eujian.cbt.screenpilot`
- memory/report files present; Gradle wrapper files present
- no secrets or build artifacts in staged files
- no local.properties, APK/AAB, .gradle, JADX evidence, or generated files staged

## Remaining Risk
No actual CI compile/test/build has yet verified the Phase-2 state. GitHub Actions must compile, test, and assemble the APK.

## Do Not Start Yet
Until GitHub Actions is green:
- no Flutter test host
- no MethodChannel bridge
- no GateProvider work
- no large storage/API refactor

## Next Milestone
1. Run GitHub Actions to verify Phase 2 compile + unit tests
2. Confirm `assembleDebug` succeeds
3. Start Phase 3 (Flutter test host) only after Phase 2 CI is green

Only after CI is green should the next phase begin.
