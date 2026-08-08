# PROJECT_STATE.md — Current Project State

Last updated: 2026-08-08

## Current Phase
**Phase 1 GREEN. Phase 2 final GREEN at commit `d50a817` / CI run #12 (`12`). Runtime smoke verified on device: a fresh internal debug session starts without MediaProjection consent or force-close, and the bubble capture exports the marked project-owned WebView PNG to `Pictures/ScreenPilotDebug`. Next milestone: Phase 3 (Flutter test host).**

## Completed
- Standalone Android/Kotlin baseline established and pushed to GitHub.
- Namespace/applicationId aligned to `id.eujian.cbt.screenpilot`.
- Phase 1 GitHub Actions compile, unit test, assembleDebug, lint, and artifact upload verified GREEN.
- Phase 2 `CaptureProvider`, `CaptureResult`, `WebViewCaptureProvider`, and `CaptureProviderRegistry` implemented.
- Internal provider and MediaProjection are explicit capture-source modes.
- `FakeCaptureProvider` is test-only; bitmap tests run with Robolectric.
- Project-owned debug asset is `app/src/debug/assets/capture_test.html`.
- Phase 2 baseline CI run `31248721397` is GREEN.
- Runtime debug trigger exists and normal ScreenPilot features were reported working.
- Corrective fix chain for the internal-debug startup issue landed and is CI GREEN: `12f1587` (decouple internal capture + debug WebView export), `7c99e84` (register offscreen WebView provider without view attachment), `d50a817` (close internal debug button click lambda).
- Final CI run #12 (`d50a817`) is GREEN: compile, unit tests, assembleDebug, lint, artifact upload.
- On-device runtime smoke test passed: fresh internal session without MediaProjection consent or force-close; bubble capture produced `Pictures/ScreenPilotDebug/capture_test_*.png` containing `SP-WEBVIEW-2026-08`.

## Runtime Finding and Corrective Work (RESOLVED)
- Fresh internal-debug start could exit/force-close while starting internal mode after a previously authorized MediaProjection session did not, indicating accidental dependency on the mediaProjection foreground-service path.
- Corrective patch removes foreground MediaProjection promotion from `ACTION_START_INTERNAL_CAPTURE`; normal `ACTION_START` MediaProjection behavior is retained.
- Internal debug mode is Activity-scoped, `START_NOT_STICKY`, overlay/provider guarded, and mixed sessions are rejected.
- Debug WebView viewport is fixed at 1080×1920 physical pixels instead of density-scaled dimensions.
- Successful internal WebView captures are additionally exported on Android 10+ to `Pictures/ScreenPilotDebug/capture_test_*.png` via MediaStore for direct visual verification.
- `capture_test.html` includes marker `SP-WEBVIEW-2026-08` to prove the exported image came from the project-owned WebView.
- Off-screen WebView registration issue (provider stayed null because `View.post` waits for view attachment) fixed by posting through `Handler(Looper.getMainLooper())` with a re-measured 1080×1920 layout, guarded by a Compose readiness state `internalCaptureProviderReady`.
- Button shows `Debug: Loading Internal Test…` (disabled) until the provider is ready, then `Debug: Start Internal Capture` (enabled).

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
No open Phase 2 blockers. Residual items: the debug harness is debug-build-only and covered by a `BuildConfig.DEBUG` guard; the MediaStore export only runs on API 29+. Phase 3 introduces the Flutter toolchain on CI, which is the next integration risk to manage.

## Next Milestone (Phase 3 — Flutter Test Host)
Phase 2 is final GREEN. Phase 3 begins:
1. Create a minimal Flutter test host proving Flutter↔Kotlin communication (MethodChannel/native bridge).
2. Exchange status/settings/events only for allowed/project-owned content.
3. Keep CI reproducible and GREEN; write `PHASE3_REPORT.md`.
