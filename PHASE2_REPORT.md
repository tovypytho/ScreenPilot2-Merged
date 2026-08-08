# PHASE2_REPORT.md — Capture Abstraction

**Date:** 2026-08-08
**Phase:** Phase 2 — Capture Abstraction (in progress)
**Commit:** `dc5c7b3`

---

## 1. Objective

Make ScreenPilot capable of capturing internal WebView content for analysis **without requiring MediaProjection**. Introduce a `CaptureProvider` abstraction so capture sources are injectable and testable.

## 2. Changes Made

### 2.1 New Package: `id.eujian.cbt.screenpilot.capture`
- **`CaptureProvider.kt`** — Defines `CaptureResult` sealed class and `CaptureProvider` interface.
  - `CaptureResult`: `Success(Bitmap)`, `Denied`, `Error(message)`
  - `interface CaptureProvider { suspend fun capture(): CaptureResult }`

- **`WebViewCaptureProvider.kt`** — Implements `CaptureProvider`.
  - Constructor takes a `WebView`.
  - `capture()` runs on `Dispatchers.Main.immediate`.
  - Checks `webView.width > 0 && height > 0` before rendering.
  - Creates an `ARGB_8888` bitmap, draws via `Canvas(webView.draw(canvas))`.
  - Returns `Success(bitmap)` or `Error(message)`.

- **`FakeCaptureProvider.kt`** — For unit tests.
  - Returns a solid-color dummy `Bitmap`.
  - Configurable width/height/color.
  - Runs on `Dispatchers.Default`.

### 2.2 ScreenCaptureService.kt (modified)
- Added `import id.eujian.cbt.screenpilot.capture.CaptureProvider` and `CaptureResult`.
- Added `var captureProvider: CaptureProvider? = null` — externally injectable.
- Modified `captureScreen()`:
  - If `captureProvider != null`, delegates to the provider.
  - Returns `Bitmap` from `Success`, logs and returns `null` for `Denied`/`Error`.
  - If `captureProvider == null`, falls back to the existing MediaProjection path.

### 2.3 MainActivity.kt (modified)
- Added imports: `android.webkit.WebView`, `WebViewCaptureProvider`, `ScreenCaptureService`.
- In `onCreate()`, created a `WebView(this)`, disabled JavaScript, loaded `file:///android_asset/capture_test.html`, and set `ScreenCaptureService.captureProvider = WebViewCaptureProvider(captureWebView)`.

### 2.4 Assets
- Created `app/src/main/assets/capture_test.html` — project-owned test HTML with:
  - Heading and paragraphs
  - Radio button options (single answer)
  - Checkbox options (multi-select)
  - Table with topic/level data
  - Placeholder image
  - No external resources (offline-safe)

## 3. Static Verification

- [x] `CaptureProvider` interface has suspend `capture(): CaptureResult`
- [x] `CaptureResult` is a sealed class with `Success`, `Denied`, `Error`
- [x] `WebViewCaptureProvider` uses `Dispatchers.Main.immediate`
- [x] `WebViewCaptureProvider` checks width/height > 0
- [x] `ScreenCaptureService.captureProvider` is injectable (`var`, nullable)
- [x] Fallback to MediaProjection when provider is null
- [x] HTML asset is project-owned, no external URLs
- [x] No refactor of `MainActivity` or `ScreenCaptureService` beyond provider injection
- [x] compileSdk/targetSdk unchanged (35)
- [x] No dependency changes
- [x] AndroidManifest permissions unchanged (INTERNET already present)

## 4. Commit History

```
dc5c7b3 feat: CaptureProvider abstraction with WebView capture
8208694 fix: align AndroidX Core with compileSdk 35
815bbf7 fix: resolve migrated package references in UI code
843c5b4 docs: update project state and TODO for git baseline
8aabf1a chore: establish standalone ScreenPilot baseline
```

## 5. Next Steps

- [ ] Add unit tests for `CaptureProvider` / `WebViewCaptureProvider` / `FakeCaptureProvider`
- [ ] Route capture pipeline through the abstraction in key call sites
- [ ] Run CI to verify compile + test
- [ ] Only after CI green: start Phase 3 (Flutter test host)
