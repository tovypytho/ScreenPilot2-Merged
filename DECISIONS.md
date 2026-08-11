# DECISIONS.md — Architecture Decision Log

## D001 — Standalone clean baseline
Accepted. `MergedProject` must compile as a standalone Android/Kotlin app before Flutter integration.

## D002 — JADX is reference-only
Accepted. Decompiled/JADX files are for analysis and do not belong in normal Gradle production `sourceSets`.

## D003 — Canonical namespace
Accepted. Use `id.eujian.cbt.screenpilot` for production ScreenPilot code and standalone identity.

## D004 — GitHub Actions first
Accepted. Primary build environment is GitHub Actions using JDK 21, Gradle 9.3.1, AGP 9.1.1, Kotlin 2.2.10, compileSdk 36 (targetSdk remains 35).

## D005 — Mandatory phase checkpoints
Accepted. Do not start a later phase until the previous phase is reviewed and CI is green.

## D006 — Capture abstraction
Accepted. ScreenPilot capture logic depends on a `CaptureProvider` abstraction. Internal-provider mode is explicitly limited to project-owned/allowed test content via a project-owned WebView. MediaProjection remains a separate legacy capture source; `INTERNAL_PROVIDER` must not silently fall back to MediaProjection when its provider is absent or fails.

## D006a — WebView capture provider
Accepted. `WebViewCaptureProvider` implements `CaptureProvider` by rendering a `WebView`'s content via `Canvas`/bitmap. Uses `Dispatchers.Main.immediate` for UI-thread-safe `webView.draw()`. Project-owned HTML asset (`capture_test.html`) serves as the test surface.

## D006b — Fake capture provider
Accepted. `FakeCaptureProvider` returns a solid-color dummy `Bitmap` for unit testing without real rendering. Test-only convenience, not used in production.

## D006c — Internal debug session is not a MediaProjection foreground session
Accepted. The project-owned internal WebView test harness is Activity-scoped and must start without MediaProjection consent, `MediaProjection`, `VirtualDisplay`, `ImageReader`, or `FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION`. Normal MediaProjection activation keeps its existing foreground-service behavior. Mixed internal/MediaProjection sessions are rejected.

## D006d — Scoped debug capture evidence
Accepted. On Android 10+, successful `INTERNAL_PROVIDER` captures may write an additional diagnostic PNG via MediaStore to `Pictures/ScreenPilotDebug`. This export is debug verification only, uses no broad storage permission, never captures the device display, and must not duplicate normal MediaProjection screenshots.

## D006e — Off-screen provider registration uses the main Looper
Accepted. The debug WebView is intentionally off-screen and never attached to a ViewRoot, so `View.post` may queue forever. Provider registration after `onPageFinished` must post through `Handler(Looper.getMainLooper())`, re-measure/layout the fixed 1080×1920 viewport, and only then set `CaptureProviderRegistry`. A Compose readiness state gates the debug button (`Debug: Loading Internal Test…` → `Debug: Start Internal Capture`).

## D007 — Flutter integration starts with a test host
Accepted and completed in Phase 3.1. Prove Flutter↔Kotlin communication with a small test host before any larger authorized integration; the AAR-based test host is now the known-green rollback baseline.

## D008 — Security boundaries remain intact
Accepted. Do not add production behavior intended to bypass protected content, verification gates, licensing, signature/integrity checks, or related controls.

## D009 — Flutter–Kotlin bridge via MethodChannel
Accepted. Phase 3.2 ships a MethodChannel bridge (`id.eujian.cbt.screenpilot/capture`, method `capture`) between the Flutter test host and the Kotlin capture layer. Kotlin (`CaptureBridge.setup`) registers the channel against the cached FlutterEngine in `MainActivity.openFlutterTest()` and answers `capture` by delegating to `CaptureProviderRegistry.get()` — never falling back to MediaProjection — exporting the PNG to `cacheDir` and replying `{ok, path, width, height}`. CI run #19 GREEN and the on-device capture test passed, so the bridge is ready for Phase 4 integration through supported/authorized interfaces only.

## D010 — Canonical E-Ujian evidence is the RAW split set
Accepted. Phase 4.1 uses the RAW Play/ADB split set as provenance authority: `base.apk` for original Android/DEX/manifest evidence and `split_config.arm64_v8a.apk` for original native ARM64 libraries. AntiSplit is navigation convenience only; RE/MOD/JADX trees are comparison evidence and must not be treated as pristine upstream or copied into production source.

## D011 — Do not package two opaque Flutter application bundles in one APK
Accepted. ScreenPilot must have one integrated Flutter application/library ownership domain. Multiple `FlutterEngine` instances are allowed only as a pattern within that one integrated Flutter library; they do not solve collisions between independent `flutter_assets`, `libapp.so`, or `libflutter.so`. Two opaque Flutter bundles in one APK are rejected.

## D012 — Phase 4 current path is isolated/project-owned; future in-process path requires authorized source
Accepted. The current executable Phase 4 path is Option D: an isolated project-owned compatibility/test harness that preserves the Phase-3 Flutter AAR and capture abstraction. A future in-process path may use Option A only with authorized Flutter source/module and only after engine/Dart/AOT/embedding/plugin compatibility is proven. No design depends on licensing/gate/signature spoofing, `FLAG_SECURE` bypass, or server/backend gate circumvention.

## D013 — Native duplicate resolution requires merged-artifact evidence
Accepted. The exact producer/version of `libc++_shared.so` in the final ScreenPilot APK is unverified at Phase 4.2 design time. Do not add E-Ujian's copy and do not use `pickFirst`, overwrite, or equivalent packaging workarounds until a CI-built merged artifact establishes the shipped producer/version and compatibility with all consumers.

## D014 — Opaque target Flutter bundle is incompatible with the current ScreenPilot engine ownership
Accepted. Phase 4.2A evidence identifies the target runtime as the Flutter 3.38.3 / Dart 3.10.1 generation (embedded engine revision evidence `13e658725ddaa270601426d1485636157e38c34c`, Android embedding v2), while ScreenPilot's known-green AAR flow uses Flutter 3.44.9. The target precompiled `libapp.so`/`flutter_assets` must not be paired with ScreenPilot's engine and the target opaque `libflutter.so` must not replace the ScreenPilot engine. Opaque in-process bundle integration is therefore NO-GO. Option D remains executable; Option A remains the only future authorized in-process path and requires authorized source/module rebuilt under one pinned toolchain.

## D015 — Plugin/channel inventory does not transfer View ownership
Accepted. Phase 4.2A confirms eight target Android plugins and identifies `plugins.flutter.io/webview` as the explicit WebView PlatformView factory id in the original plugin registration path. Knowledge of plugin/channel names does not grant ScreenPilot ownership of a target WebView. CaptureProvider registration remains limited to a WebView explicitly created/owned by project-authorized integration code in the same process.

## D016 — Retain ScreenPilot as the single Flutter application ownership domain
Accepted. Phase 4.2B closes the ownership checkpoint with no production implementation change. ScreenPilot keeps `flutter_test_host` as its single integrated Flutter application/library, built by the known-green Flutter 3.44.9 AAR flow and launched through the cached `screenpilot_capture_host` engine. Target opaque `flutter_assets`, `libapp.so`, `libflutter.so`, DEX/JADX/smali, and native libraries remain outside production packaging. Option D remains the executable path. Option A is a future ownership migration only if authorized source/module becomes available and is rebuilt under one pinned toolchain; it is not a two-bundle coexistence plan.

## D017 — A second project-owned WebView requires owner-aware provider lifecycle semantics
Accepted as a design prerequisite for Phase 4.3. The current `CaptureProviderRegistry` exposes only global `set/get/clear` operations and does not identify which surface owns the active registration. Before a second project-owned WebView/PlatformView is introduced, the lifecycle design must prevent disposal of an older surface from clearing a newer provider registration. Phase 4.3A will define the ownership/registration contract first; no registry refactor is performed by the Phase 4.2B checkpoint.
