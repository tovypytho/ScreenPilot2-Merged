# PHASE2_REPORT.md — Capture Abstraction

**Date:** 2026-08-08
**Phase:** Phase 2 — Capture Abstraction
**Status:** **FINAL GREEN** — implementation complete, corrective fix chain CI GREEN (run #12 / commit `d50a817`), on-device runtime smoke test passed.

## 1. Verified Baseline

GitHub Actions run `31248721397` for commit `69db963` completed successfully:

- `compileDebugKotlin`: PASS
- `testDebugUnitTest`: PASS
- `assembleDebug`: PASS
- `lintDebug`: PASS
- debug APK artifact: PASS

The runtime smoke test then exposed behavior that CI cannot detect:

- debug trigger is visible and the normal ScreenPilot feature set works;
- starting internal mode from a fresh app can cause the app/process to exit;
- starting internal mode after a MediaProjection session is already active does not reproduce that failure, which indicates accidental coupling to the MediaProjection foreground-service path;
- the debug button itself only starts internal mode; the floating bubble remains the actual capture trigger.

Therefore Phase 2 is **not final GREEN yet**.

## 2. Architecture Already Present

- `CaptureProvider` + sealed `CaptureResult`.
- `WebViewCaptureProvider` using `WeakReference<WebView>` and `Dispatchers.Main.immediate`.
- `CaptureProviderRegistry` for provider injection without holding an Activity directly.
- explicit capture source modes: `MEDIA_PROJECTION` and `INTERNAL_PROVIDER`.
- `ACTION_START_INTERNAL_CAPTURE` for project-owned test content.
- MediaProjection health checks are skipped when the source is `INTERNAL_PROVIDER`.
- `FakeCaptureProvider` is test-only under `src/test`.
- `capture_test.html` is debug-only under `app/src/debug/assets/`.

## 3. Runtime Corrective Patch Prepared in This Revision

### 3.1 Internal mode is decoupled from MediaProjection foreground service

`ACTION_START_INTERNAL_CAPTURE` no longer promotes the service with `FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION`. The debug Activity starts this explicit service with `startService()` and the internal branch returns `START_NOT_STICKY`. It does not initialize MediaProjection, VirtualDisplay, or ImageReader.

Normal `ACTION_START` continues to use the existing MediaProjection foreground-service path.

### 3.2 Mixed-session protection

The debug trigger refuses to start while normal ScreenPilot capture is active. The service also rejects an internal start if another capture session is already active, and rejects MediaProjection activation while an internal debug session is active.

### 3.3 Overlay/provider readiness guards

The Activity checks overlay permission and provider readiness before starting internal mode. The service repeats those checks defensively.

### 3.4 Activity-scoped lifecycle

The internal provider owns a MainActivity WebView. The Activity stops an internal debug service during destruction, clears the provider registry, and destroys the WebView. The service records that Activity-scoped shutdown as graceful and also stops internal mode if the Activity task is removed. Normal MediaProjection behavior is not intentionally changed.

### 3.5 Deterministic WebView viewport

The debug WebView now uses a fixed **1080×1920 physical-pixel** viewport. It is no longer multiplied by display density, avoiding extremely large bitmaps on xxhdpi/xxxhdpi devices.

The debug WebView is intentionally **off-screen and never attached to a ViewRoot**, and uses a software layer to make direct `WebView.draw(Canvas)` verification more deterministic.

### 3.6 Off-screen provider registration (runtime fix, commit `7c99e84`)

The first runtime test of the decoupled patch surfaced a second, more specific bug: the debug button always reported `Internal test page is still loading` even after the page finished. Root cause: provider registration was deferred with `wv.post { ... }` after `onPageFinished`; because the WebView is not attached to a ViewRoot, `View.post` runnables can remain queued waiting for attachment forever, so `CaptureProviderRegistry` stayed null.

Fix in `MainActivity.kt` only:

- registration now posts through `Handler(Looper.getMainLooper()).post { ... }`, which does not depend on view attachment;
- the callback verifies the Activity is still alive and `captureWebView === wv`, then re-measures and re-lays out the WebView to the fixed 1080×1920 viewport;
- only when `wv.width > 0 && wv.height > 0` does it set `CaptureProviderRegistry.set(WebViewCaptureProvider(wv))` and flip the Compose readiness state `internalCaptureProviderReady.value = true`;
- the debug button is disabled with label `Debug: Loading Internal Test…` until the provider is ready, then enabled with `Debug: Start Internal Capture`;
- `onDestroy()` resets `internalCaptureProviderReady.value = false` and clears the registry.

A first version of this fix (commit `7c99e84`) was missing one closing brace on the button's `onClick` lambda and failed CI run #11 with cascading parser errors. That was fixed in commit `d50a817` (run #12 GREEN).

### 3.7 Visual verification export

A successful `INTERNAL_PROVIDER` capture exports an additional PNG through scoped-storage MediaStore to:

`Pictures/ScreenPilotDebug/capture_test_yyyyMMdd_HHmmss_SSS.png`

On a typical device this appears under:

`/storage/emulated/0/Pictures/ScreenPilotDebug/`

Properties:

- Android 10+ only for this public debug export;
- no `MANAGE_EXTERNAL_STORAGE`;
- no new broad storage permission;
- source bitmap is the result of `webView.draw(Canvas)`, not a display screenshot;
- export is only executed for the internal debug provider; normal MediaProjection screenshots are not duplicated here;
- success/failure is logged and surfaced with a Toast;
- API 28 returns a non-fatal unsupported result rather than adding legacy storage permission.

The test HTML now contains a prominent marker: `SP-WEBVIEW-2026-08`, making it easy to distinguish a true WebView-provider capture from a screen screenshot.

## 4. Corrective Fix Chain and CI Evidence

| Commit | Purpose | CI run | Result |
|---|---|---|---|
| `12f1587` | Decouple internal capture from MediaProjection FGS; add `DebugCaptureExporter` + test; scoped-storage PNG export | #10 | GREEN |
| `7c99e84` | Register off-screen WebView provider via main Looper instead of `View.post`; readiness state + button labels | #11 | FAILED (missing `}` on `onClick` lambda) |
| `d50a817` | Close internal debug button click lambda (syntax fix) | #12 | GREEN |

Final CI run #12 (commit `d50a817`): `compileDebugKotlin`, `testDebugUnitTest`, `assembleDebug`, `lintDebug`, and debug APK artifact upload all PASS.

## 5. On-Device Runtime Smoke Test (PASSED)

Performed after CI run #12 went GREEN, exactly as specified:

1. fresh-open the app without activating normal ScreenPilot — no force-close;
2. debug button initially shows `Debug: Loading Internal Test…` (disabled), then transitions to `Debug: Start Internal Capture` (enabled) once the provider is ready — without activating normal ScreenPilot;
3. press `Debug: Start Internal Capture` — no MediaProjection consent dialog, no force-close;
4. floating bubble appears; Toast confirms internal capture ready;
5. tap the bubble once — capture runs;
6. `Pictures/ScreenPilotDebug/capture_test_*.png` exists and visibly contains `SCREENPILOT INTERNAL WEBVIEW TEST` and `SP-WEBVIEW-2026-08`.

Phase 2 runtime checkpoint is therefore **GREEN** and Phase 3 may begin.

## 6. Files Changed by the Corrective Revision

- `app/src/main/java/id/eujian/cbt/screenpilot/MainActivity.kt`
- `app/src/main/java/id/eujian/cbt/screenpilot/service/ScreenCaptureService.kt`
- `app/src/main/java/id/eujian/cbt/screenpilot/capture/DebugCaptureExporter.kt` (new)
- `app/src/debug/assets/capture_test.html`
- `app/src/test/java/id/eujian/cbt/screenpilot/capture/DebugCaptureExporterTest.kt` (new)
- project state/decision/TODO/handoff documentation
