# PHASE2_REPORT.md — Capture Abstraction

**Date:** 2026-08-08
**Phase:** Phase 2 — Capture Abstraction (CI GREEN, runtime smoke test pending)
**Commit:** `dc5c7b3` (initial) → `411c812` (lifecycle fix) → `8e32b6a` (test runner fix)

### CI Status
- `compileDebugKotlin`: SUCCESS
- `testDebugUnitTest`: SUCCESS (164 tests, 0 failures)
- `assembleDebug`: SUCCESS

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

## 5. CI Fix — Lifecycle Correction (commit `411c812`)

**Error:** `MainActivity.kt:156:30 — Unresolved reference 'captureProvider'`
**Root cause:** `captureProvider` was an instance property on `ScreenCaptureService`, accessed as `ScreenCaptureService.captureProvider` (static-like access on an instance var).

### Changes applied (no large refactor):

#### 5.1 CaptureProviderRegistry
- New `object CaptureProviderRegistry` — thread-safe holder with `@Volatile` backing field.
- `set(provider)`, `get()`, `clear()` — stores only `CaptureProvider?`, never `Activity`/`WebView` directly.

#### 5.2 WebViewCaptureProvider
- Uses `WeakReference(webView)` instead of strong reference — avoids leaks.
- `capture()` unchanged behavior: `withContext(Dispatchers.Main.immediate)`, width/height check, `Canvas`/`draw`.

#### 5.3 ScreenCaptureService
- Removed `var captureProvider: CaptureProvider?`.
- Import changed: `CaptureProvider` → `CaptureProviderRegistry`.
- `captureScreen()`: now calls `CaptureProviderRegistry.get()`; for `INTERNAL_PROVIDER` source, no fallback to MediaProjection on failure.
- Added `CaptureSource` enum: `MEDIA_PROJECTION`, `INTERNAL_PROVIDER`.
- Added `ACTION_START_INTERNAL_CAPTURE` — starts foreground + floating button, sets `isServiceActive=true`, does NOT call `initializeMediaProjection()`.
- `currentCaptureSource` defaults to `MEDIA_PROJECTION` (existing behavior preserved).

#### 5.4 Health Watcher
- `runHealthCheck()`: MediaProjection/virtualDisplay/imageReader null checks only run when `currentCaptureSource == MEDIA_PROJECTION`. INTERNAL mode skips these (they are legitimately null).

#### 5.5 MainActivity
- Removed `ScreenCaptureService.captureProvider = ...` (compile error).
- Uses `CaptureProviderRegistry.set(WebViewCaptureProvider(wv))` in `WebViewClient.onPageFinished()`.
- Added `onDestroy()` → `CaptureProviderRegistry.clear()` + `webView.destroy()`.

#### 5.6 WebView Viewport
- `WebView` is not attached to Compose hierarchy → uses explicit `measure()`/`layout()` with fixed 1080×1920 (density-adjusted) viewport → guarantees non-zero dimensions.

#### 5.7 Asset & Test Relocation
- `capture_test.html` moved from `src/main/assets/` → `src/debug/assets/` (test harness only).
- `FakeCaptureProvider` moved from `src/main/` → `src/test/` (test-only).
- Added `CaptureProviderTest.kt` — verifies `FakeCaptureProvider` returns `Success` with correct dimensions (default and custom).

## 6. Next Steps

- [x] Run CI to verify compile + tests (GREEN)
- [ ] Runtime smoke: verify internal WebView capture works without MediaProjection (debug harness)
- [ ] Only after CI green: start Phase 3 (Flutter test host)
