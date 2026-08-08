# TODO.md — Project Roadmap

## Current: Phase 2 — Capture Abstraction

### Phase 1 / baseline
- [x] Standalone cleanup, namespace migration, Git baseline, GitHub push
- [x] Phase 1 CI compile/test/assemble/lint checkpoint GREEN

### Phase 2 implementation and CI baseline
- [x] Define `CaptureProvider` + `CaptureResult`
- [x] Implement `WebViewCaptureProvider` for project-owned WebView content
- [x] Add `CaptureProviderRegistry` and WeakReference lifecycle
- [x] Keep `FakeCaptureProvider` test-only and add Robolectric bitmap tests
- [x] Add explicit `INTERNAL_PROVIDER` mode and preserve MediaProjection mode
- [x] Add debug-only `app/src/debug/assets/capture_test.html` harness
- [x] Phase 2 baseline CI GREEN (`31248721397`, commit `69db963`)

### Runtime corrective checkpoint
- [x] Perform first runtime smoke test
- [x] Record runtime issue: fresh internal start can exit/FC while a prior MediaProjection session masks the problem
- [x] Prepare corrective patch: internal debug start no longer claims MediaProjection foreground-service type
- [x] Add mixed-session protection
- [x] Add overlay-permission and provider-readiness guards
- [x] Make internal debug session Activity-scoped / `START_NOT_STICKY`
- [x] Fix debug WebView viewport to 1080×1920 physical pixels (no density multiplier)
- [x] Add MediaStore debug PNG export to `Pictures/ScreenPilotDebug` on API 29+
- [x] Add unmistakable HTML marker `SP-WEBVIEW-2026-08`
- [x] Add API-28 test proving no legacy public-storage permission is introduced
- [ ] Review corrective patch in Git working tree
- [ ] Push corrective patch and run GitHub Actions
- [ ] Confirm new CI compile + tests + assembleDebug + lint are GREEN
- [ ] Runtime smoke from fresh app: internal start without MediaProjection dialog or FC
- [ ] Tap bubble and confirm `Pictures/ScreenPilotDebug/capture_test_*.png` exists
- [ ] Confirm PNG contains `SCREENPILOT INTERNAL WEBVIEW TEST` / `SP-WEBVIEW-2026-08`
- [ ] Mark Phase-2 runtime checkpoint GREEN

## Phase 3 — Flutter Test Host
Start only after Phase 2 is green.
- [ ] Create minimal Flutter test host
- [ ] Define MethodChannel/native bridge contract
- [ ] Exchange status/settings/events between Flutter and Kotlin
- [ ] Test only on allowed/project-owned content
- [ ] Run CI
- [ ] Write `PHASE3_REPORT.md`

## Phase 4 — Authorized Integration Study
- [ ] Evaluate original/authorized source boundaries
- [ ] Keep protected/security-controlled surfaces protected
- [ ] Integrate only through supported/authorized interfaces
- [ ] Keep build reproducible and documented
