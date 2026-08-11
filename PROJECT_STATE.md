# PROJECT_STATE.md — Current Project State

Last updated: 2026-08-09 (Phase 4.1 inventory + Phase 4.2 architecture design COMPLETE)

## Current Phase
**Phase 1 GREEN. Phase 2 final GREEN. Phase 3 COMPLETE and runtime-verified. Phase 4.1 inventory is COMPLETE/AUDIT PASS (`PHASE4_EUJIAN_INVENTORY.md`), and Phase 4.2 architecture design is COMPLETE (`PHASE4_INTEGRATION_DESIGN.md`) with no production-source changes. Current executable path = Option D, an isolated project-owned compatibility/test harness. Future authorized in-process path = Option A, a single authorized Flutter source/module, only after the compatibility gate passes. NEXT: Phase 4.2A compatibility proof (design/evidence only; no opaque bundle mixing and no security/integrity bypass).**

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

### Phase 3.1 — Minimal Flutter Test Host (COMPLETE, GREEN)
Goal (no MethodChannel, no capture yet): prove a Flutter module builds and displays from MainActivity.
- `flutter_test_host/` module created via `flutter create --template=module --org id.eujian.cbt` (Flutter 3.44.9). Committed source: `lib/main.dart` (default counter app), `pubspec.yaml`, `pubspec.lock`, `.metadata`, `analysis_options.yaml`, `test/widget_test.dart`, `.gitignore`, `README.md`.
- **`.android/` is gitignored + untracked** (generated, machine-specific `local.properties`). Verified in flutter_tools source that `flutter build aar` auto-regenerates `.android/` on a fresh checkout (module `existsSync()` returns true; `_shouldRegenerateFromTemplate()` regenerates when missing).
- **Integration = AAR flow** (NOT Flutter Gradle plugin):
  - CI: `flutter pub get` → `flutter precache --android` → `flutter build aar --debug` in module dir. Output: `flutter_test_host/build/host/outputs/repo` (gitignored).
  - Host: `settings.gradle.kts` `dependencyResolutionManagement` adds `maven { url = uri("flutter_test_host/build/host/outputs/repo") }` + `maven { url = uri("https://storage.googleapis.com/download.flutter.io") }` (engine artifacts).
  - `app/build.gradle.kts`: `debugImplementation("id.eujian.cbt.flutter_test_host:flutter_debug:1.0")` + `releaseImplementation("...flutter_release:1.0")` (version = module gradle `version = "1.0"`, NOT 1.0.0; no profile — CI only builds debug).
  - `MainActivity.kt`: debug-only button "Open Flutter Test" → FlutterActivity (Phase 3.2 switched to a cached-engine launch — see below).
  - Manifest: `io.flutter.embedding.android.FlutterActivity` declared with standard configChanges; `themes.xml` has `LaunchTheme` (white window background).
  - Host `build.gradle.kts`/catalog: NO Flutter plugin entries; `android-library` alias added then removed again (not needed in AAR flow).
- **Failure history (do not repeat):**
  - CI #14 (`6b447f9`): Flutter Gradle plugin direct integration → FAILED (Flutter Gradle plugin conflicts with AGP 9.1.1/Kotlin 2.2.10 KotlinAndroidExtension). Abandoned.
  - CI #15 (`6f55d51`): AAR integration v1 → FAILED at Compile ScreenPilot: AAR version was actually `1.0` not `1.0.0` (and missing engine repo).
  - CI #16 (`7d8dcfc`): version aligned to `1.0`, profileImplementation removed, `download.flutter.io` repo added → FAILED: AAR built by flutter tool with default `compileSdk 36` → host must compile against ≥36.
  - CI #17 (`fe57637`): `compileSdk = 36` (targetSdk stays 35) → **GREEN**. Run: `31268201223`.
- On-device smoke test PASSED (fresh app → "Open Flutter Test" → Flutter screen opened without crash; no MediaProjection/capture involved).
- `git log` key commits (newest): `f4256a7` → `fe57637` → `7d8dcfc` → `6f55d51` → `6b447f9` → `6862dc8` → `d50a817`.

### Phase 3.2 — MethodChannel Bridge (COMPLETE, GREEN)
Goal: Flutter requests a capture through Kotlin and receives the PNG path of the result.
- `CaptureBridge.kt` (new, `capture/` package): MethodChannel `id.eujian.cbt.screenpilot/capture`, method `capture`. `CaptureProviderRegistry.get()` → `{ok:false,error:"provider_unavailable"}` if absent; `Success` → PNG to `context.cacheDir` (`capture_<yyyyMMdd_HHmmss_SSS>.png`) → `{ok:true,path,width,height}`; `Denied` → `{ok:false,error:"capture_denied"}`; `Error` → `{ok:false,error:<message>}`. Handler runs on `Dispatchers.Main.immediate` scope; PNG write on `Dispatchers.IO`; result reply on the platform main thread.
- `flutter_test_host/lib/main.dart`: MethodChannel + state `_imagePath/_status/_dimensions`; "Capture via Bridge" button; `Image.file(File(path))` with status and dimensions rendered below the image.
- `MainActivity.kt`: `openFlutterTest()` creates + caches a FlutterEngine (`FlutterEngineCache`, id `screenpilot_capture_host`), calls `CaptureBridge.setup(engine, this)` before launch, then starts `FlutterActivity.withCachedEngine(...)`. Engine is torn down in `onDestroy()` only when `isFinishing` (config-change safe).
- CI run #19 (`31292591345`, commit `f4256a7`) GREEN: Flutter AAR build → compileDebugKotlin → unit tests → assembleDebug → lint.
- On-device test PASSED (user-verified): fresh app → "Open Flutter Test" → "Capture via Bridge" → PNG path + dimensions returned and image displayed.

### Phase 4.1 — E-Ujian Inventory & Compatibility Gate (COMPLETE, AUDIT PASS)
- Canonical provenance is the RAW Play/ADB split set; `base.apk` is the Android/DEX/manifest reference and `split_config.arm64_v8a.apk` is the native reference. `E-Ujian_RE_JADX` is not pristine and remains comparison evidence only.
- Flutter bundle inventory: 13 files / 1,391,277 bytes. Native inventory: 8 ARM64 libraries / 22,954,080 bytes. RAW and RE/MOD native libraries are byte-identical in the audited set.
- No APK, DEX, `.so`, JADX/smali tree, or RE Flutter assets were copied into ScreenPilot.
- Full evidence and no-copy constraints are recorded in `PHASE4_EUJIAN_INVENTORY.md`.

### Phase 4.2 — Authorized Flutter Integration Architecture Design (COMPLETE, DESIGN ONLY)
- Option A: replace the Phase-3 test host with ONE authorized Flutter source/module — conditional future in-process path after compatibility proof.
- Option B: multiple `FlutterEngine` instances using ONE integrated Flutter library — supported pattern, but not a solution for two independent Flutter bundles.
- Option C: two independent/opaque Flutter bundles in one APK — REJECTED because of `flutter_assets` / `libapp.so` / `libflutter.so` ownership collisions and unproven AOT/engine compatibility.
- Option D: isolated project-owned compatibility/test harness — CURRENT executable/recommended path.
- `CaptureProvider`, `CaptureProviderRegistry`, and `CaptureBridge` remain unchanged; capture is limited to project-owned/authorized WebViews registered explicitly in-process.
- Exact producer/version of the final APK's `libc++_shared.so` remains UNVERIFIED at design time; no `pickFirst`/overwrite workaround is allowed before merged-artifact evidence exists.
- Security/integrity controls remain a hard boundary: no licensing/gate spoofing, package/certificate spoofing, `FLAG_SECURE` bypass, or server/backend gate circumvention is part of the design.
- Documentation closeout commit `9f0ce3c` passed CI run #21 (`31304311490`): compile, unit tests, assembleDebug, and lint all GREEN. The two Phase-4 checkpoint documents were inadvertently left untracked by that commit; this follow-up tracks them without changing production code.

## Remaining Risk
- AAR flow depends on CI regenerating `.android/` + Flutter toolchain each run; local dev cannot build the Flutter module (no Android SDK on this machine).
- `releaseImplementation` artifact (`flutter_release:1.0`) is declared but never built/resolved by CI (only debug AAR is produced) — revisit if release builds are needed.
- Bridge reply contract (`{ok,path,width,height}`) is currently a simple debug surface; any production-facing expansion must stay behind the authorized/project-owned capture boundary.
- E-Ujian's opaque `libapp.so` / Flutter engine provenance and Dart AOT compatibility with ScreenPilot Flutter 3.44.9 are unproven.
- The exact producer/version of `libc++_shared.so` in a final merged ScreenPilot APK has not yet been established from a CI-built artifact.

## Next Milestone (Phase 4.2A — compatibility proof, evidence only)
1. Establish, if possible from authorized evidence, the target Flutter engine/Dart SDK/AOT provenance and Android embedding generation.
2. Inventory target plugin/MethodChannel/EventChannel/PlatformView contracts only to the extent needed for compatibility planning; do not use this work to bypass licensing, gate, signature, `FLAG_SECURE`, or exam/security policy.
3. Do not mix foreign `flutter_assets`, `libapp.so`, `libflutter.so`, or native libraries into ScreenPilot during this proof.
4. If compatibility cannot be proven, record a NO-GO for in-process opaque-bundle integration and keep Option D as the executable path.
5. If a CI-built artifact becomes available for inspection, establish the exact producer/version of the shipped `libc++_shared.so` before any second native producer is considered.

## Documentation / working-artifact policy
- `PHASE4_EUJIAN_INVENTORY.md` and `PHASE4_INTEGRATION_DESIGN.md` are documentation checkpoint files and should be tracked.
- `CHATGPT_REVIEW.diff`, `PHASE2_RUNTIME_FIX_HANDOFF.md` remain local working artifacts and should not be staged unless explicitly requested.
- Everything under `flutter_test_host/.android/`, `flutter_test_host/build/`, `.dart_tool/`, `local.properties` remains generated/machine-specific.

## Static Verification (phase 1 baseline, still passing)
- no `../../` build dep; no production `com.example.*`; standard sourceSets; local manifest present; dependency aliases resolve; no JADX/legacy dirs in `app/src`; no secrets/build artifacts staged.
