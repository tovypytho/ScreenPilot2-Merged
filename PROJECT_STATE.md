# PROJECT_STATE.md — Current Project State

Last updated: 2026-08-08

## Current Phase
**Phase 1 GREEN. Phase 2 CI baseline GREEN at commit `69db963` / run `31248721397`, but runtime smoke exposed an internal-start lifecycle/foreground-service bug. A corrective internal-debug patch is prepared locally and requires a new CI run plus device smoke test. Phase 3 remains blocked.**

## Completed
- Standalone Android/Kotlin baseline established and pushed to GitHub.
- Namespace/applicationId aligned to `id.eujian.cbt.screenpilot`.
- Phase 1 GitHub Actions compile, unit test, assembleDebug, lint, and artifact upload verified GREEN.
- Phase 2 `CaptureProvider`, `CaptureResult`, `WebViewCaptureProvider`, and `CaptureProviderRegistry` implemented.
- Internal provider and MediaProjection are explicit capture-source modes.
- `FakeCaptureProvider` is test-only; bitmap tests run with Robolectric.
- Project-owned debug asset is `app/src/debug/assets/capture_test.html`.
- Phase 2 CI baseline run `31248721397` is GREEN.
- Runtime debug trigger exists and normal ScreenPilot features were reported working.

## Runtime Finding and Corrective Work
- Fresh internal-debug start could exit/force-close while starting internal mode after a previously authorized MediaProjection session did not, indicating accidental dependency on the mediaProjection foreground-service path.
- Corrective patch removes foreground MediaProjection promotion from `ACTION_START_INTERNAL_CAPTURE`; normal `ACTION_START` MediaProjection behavior is retained.
- Internal debug mode is Activity-scoped, `START_NOT_STICKY`, overlay/provider guarded, and mixed sessions are rejected.
- Debug WebView viewport is fixed at 1080×1920 physical pixels instead of density-scaled dimensions.
- Successful internal WebView captures are additionally exported on Android 10+ to `Pictures/ScreenPilotDebug/capture_test_*.png` via MediaStore for direct visual verification.
- `capture_test.html` includes marker `SP-WEBVIEW-2026-08` to prove the exported image came from the project-owned WebView.

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
The corrective runtime patch has not yet been verified by GitHub Actions or a device. The critical acceptance test is a fresh internal-provider session with no MediaProjection permission/session, followed by a bubble capture that produces `Pictures/ScreenPilotDebug/capture_test_*.png`.

## Do Not Start Yet
Until the corrective patch is CI GREEN and the runtime smoke test passes:
- no Flutter test host
- no MethodChannel bridge
- no Phase 3 integration work
- no broad storage/API refactor

## Next Milestone
1. Copy/review the corrective patch in the Git working tree.
2. Run GitHub Actions and require compile + unit tests + assembleDebug + lint GREEN.
3. Install the resulting debug APK.
4. Verify fresh internal capture works without MediaProjection and exports the marked WebView PNG.
5. Mark Phase 2 final GREEN only after that runtime evidence exists.
