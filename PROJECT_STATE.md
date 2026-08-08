# PROJECT_STATE.md — Current Project State

Last updated: 2026-08-08 (night checkpoint, Phase 3.1)

## Current Phase
**Phase 1 GREEN. Phase 2 final GREEN (CI run #12 / `d50a817`, runtime smoke verified on device). Phase 3.1 (minimal Flutter test host) CI GREEN via AAR integration at commit `fe57637` / CI run #17 (`31268201223`). REMAINING for Phase 3.1: on-device smoke test — install APK, tap "Open Flutter Test" debug button, confirm Flutter screen opens without crash. Phase 3.2 (MethodChannel bridge) must NOT start before that device check passes.**

## Environment / Toolchain Facts (IMPORTANT — machine-specific)
- **Flutter SDK**: stable `3.44.9` cloned at `C:\flutter` (git clone --depth 1 -b stable). Not on PATH — call `C:\flutter\bin\flutter.bat`. First run auto-downloaded Dart SDK.
- **Local machine has NO Android SDK** (`ANDROID_HOME` unset) — `flutter build aar` locally fails with "No Android SDK found". Do NOT attempt local Android/Flutter Android builds; CI is the primary build environment (project rule).
- Root `local.properties` (`flutter.sdk=C:\\flutter`) exists locally but is **gitignored** — CI writes nothing to it anymore (AAR flow does not need it).
- Host toolchain (locked): AGP **9.1.1**, Gradle **9.3.1**, Kotlin **2.2.10**, compileSdk **36**, targetSdk **35**, minSdk **28**, coreKtx 1.16.0. GitHub Actions ubuntu-latest has Android SDK preinstalled (licenses accepted).

## Completed

### Phase 1 — Baseline (GREEN)
- Standalone Android/Kotlin baseline, namespace/applicationId `id.eujian.cbt.screenpilot`, GitHub push, CI compile/test/assemble/lint GREEN.

### Phase 2 — Capture Abstraction (FINAL GREEN)
- `CaptureProvider`, `CaptureResult`, `WebViewCaptureProvider`, `CaptureProviderRegistry`, explicit `INTERNAL_PROVIDER` vs `MEDIA_PROJECTION` sources, `FakeCaptureProvider` (test-only, Robolectric), debug-only `capture_test.html` with marker `SP-WEBVIEW-2026-08`.
- Fix chain: `12f1587` (decouple internal capture from MediaProjection FGS + `DebugCaptureExporter` scoped-storage PNG to `Pictures/ScreenPilotDebug`), `7c99e84` (register off-screen WebView provider via `Handler(Looper.getMainLooper())` + readiness state), `d50a817` (syntax fix). CI run #12 GREEN; device smoke passed (fresh session, no MediaProjection consent/FC, PNG with marker exported).
- Docs marked final GREEN at `6862dc8`.

### Phase 3.1 — Minimal Flutter Test Host (CI GREEN, device test pending)
Goal (no MethodChannel, no capture yet): prove a Flutter module builds and displays from MainActivity.
- `flutter_test_host/` module created via `flutter create --template=module --org id.eujian.cbt` (Flutter 3.44.9). Committed source: `lib/main.dart` (default counter app), `pubspec.yaml`, `pubspec.lock`, `.metadata`, `analysis_options.yaml`, `test/widget_test.dart`, `.gitignore`, `README.md`.
- **`.android/` is gitignored + untracked** (generated, machine-specific `local.properties`). Verified in flutter_tools source that `flutter build aar` auto-regenerates `.android/` on a fresh checkout (module `existsSync()` returns true; `_shouldRegenerateFromTemplate()` regenerates when missing).
- **Integration = AAR flow** (NOT Flutter Gradle plugin):
  - CI: `flutter pub get` → `flutter precache --android` → `flutter build aar --debug` in module dir. Output: `flutter_test_host/build/host/outputs/repo` (gitignored).
  - Host: `settings.gradle.kts` `dependencyResolutionManagement` adds `maven { url = uri("flutter_test_host/build/host/outputs/repo") }` + `maven { url = uri("https://storage.googleapis.com/download.flutter.io") }` (engine artifacts).
  - `app/build.gradle.kts`: `debugImplementation("id.eujian.cbt.flutter_test_host:flutter_debug:1.0")` + `releaseImplementation("...flutter_release:1.0")` (version = module gradle `version = "1.0"`, NOT 1.0.0; no profile — CI only builds debug).
  - `MainActivity.kt`: debug-only button "Open Flutter Test" → `FlutterActivity.createDefaultIntent(this)` (engine caching deliberately removed).
  - Manifest: `io.flutter.embedding.android.FlutterActivity` declared with standard configChanges; `themes.xml` has `LaunchTheme` (white window background).
  - Host `build.gradle.kts`/catalog: NO Flutter plugin entries; `android-library` alias added then removed again (not needed in AAR flow).
- **Failure history (do not repeat):**
  - CI #14 (`6b447f9`): Flutter Gradle plugin direct integration → FAILED (Flutter Gradle plugin conflicts with AGP 9.1.1/Kotlin 2.2.10 KotlinAndroidExtension). Abandoned.
  - CI #15 (`6f55d51`): AAR integration v1 → FAILED at Compile ScreenPilot: AAR version was actually `1.0` not `1.0.0` (and missing engine repo).
  - CI #16 (`7d8dcfc`): version aligned to `1.0`, profileImplementation removed, `download.flutter.io` repo added → FAILED: AAR built by flutter tool with default `compileSdk 36` → host must compile against ≥36.
  - CI #17 (`fe57637`): `compileSdk = 36` (targetSdk stays 35) → **GREEN**. Run: `31268201223`.
- `git log` key commits (newest): `fe57637` → `7d8dcfc` → `6f55d51` → `6b447f9` → `6862dc8` → `d50a817`.

## Remaining Risk
- Phase 3.1 device smoke test NOT yet done (no device evidence yet).
- AAR flow depends on CI regenerating `.android/` + Flutter toolchain each run; local dev cannot build the Flutter module (no Android SDK on this machine).
- `releaseImplementation` artifact (`flutter_release:1.0`) is declared but never built/resolved by CI (only debug AAR is produced) — revisit if release builds are needed.

## Next Milestone (Phase 3.1 finish → 3.2)
1. Install debug APK from CI #17 artifacts on a device.
2. Fresh app → debug button "Open Flutter Test" appears → tap → Flutter counter screen opens WITHOUT crash (no MediaProjection, no capture).
3. If green → start Phase 3.2: MethodChannel bridge (status/settings/events exchange) on project-owned content only. Write `PHASE3_REPORT.md`.
4. Phase 3.3 (capture via Flutter) and Phase 4 (authorized E-Ujian integration study) afterwards.

## Untracked / not committed (by design)
- `CHATGPT_REVIEW.diff`, `PHASE2_RUNTIME_FIX_HANDOFF.md` — working artifacts, never staged.
- Everything under `flutter_test_host/.android/`, `flutter_test_host/build/`, `.dart_tool/`, `local.properties` — generated/machine-specific.

## Static Verification (phase 1 baseline, still passing)
- no `../../` build dep; no production `com.example.*`; standard sourceSets; local manifest present; dependency aliases resolve; no JADX/legacy dirs in `app/src`; no secrets/build artifacts staged.
