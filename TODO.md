# TODO.md — Project Roadmap

## Current: Phase 2 — Capture Abstraction
- [x] Backup pre-Phase-1 MergedProject
- [x] Remove `../../E-Ujian_RE_JADX` Gradle sourceSets
- [x] Align namespace/applicationId
- [x] Migrate production `com.example.*` type references
- [x] Update test/androidTest namespaces
- [x] Use local ScreenPilot AndroidManifest
- [x] Static verification
- [x] Create `PHASE1_REPORT.md`
- [x] Add project memory files (AGENTS.md, PROJECT_STATE.md, DECISIONS.md, TODO.md)
- [x] Ensure `.gitignore` excludes `.gradle/`, `build/`, APKs, local SDK config, and secrets
- [x] Review `.github/workflows/android-build.yml` (compatible with JDK21/Gradle 9.3.1/AGP 9.1.1/compileSdk35)
- [x] Audit filesystem (E-Ujian_RE_JADX stub = empty, androidTest path, not a git repo)
- [x] Create `PRE_CI_REPORT.md`
- [x] Remove empty internal stub `MergedProject/E-Ujian_RE_JADX/` (confirmed 0 files; external evidence untouched)
- [x] Move androidTest to `app/src/androidTest/java/id/eujian/cbt/screenpilot/`; remove legacy `com/example` dirs
- [x] Audit `.gitignore` final contents (no global `*.properties`; wrapper/gradlew/toml kept)
- [x] Final static verification (all PASS)
- [x] Create `FINAL_REPO_CHECK.md`
- [x] `git init -b main`
- [x] Verify `.gitignore` via `git check-ignore`
- [x] Verify required build files are trackable
- [x] `git add .` (93 files staged)
- [x] Audit staged files (secrets, local.properties, APK/AAB, build outputs, .gradle, JADX/evidence, generated files — all clean)
- [x] Commit "chore: establish standalone ScreenPilot baseline" (commit 8aabf1a)
- [x] Update PROJECT_STATE.md
- [x] Update TODO.md
- [x] Create `GIT_BASELINE_REPORT.md`
- [x] Push Phase-1 baseline to GitHub
- [x] CI #1: compile OK (2 fixes: package refs, coreKtx version)
- [x] Mark Phase-1 CI checkpoint GREEN
- [x] Define `CaptureProvider` interface + `CaptureResult` sealed class
- [x] Implement `WebViewCaptureProvider` (WebView → Bitmap via Canvas)
- [x] Create `FakeCaptureProvider` for unit tests
- [x] Create `app/src/main/assets/capture_test.html`
- [x] Wire `WebViewCaptureProvider` in `MainActivity.onCreate()`
- [x] Inject captureProvider via registry into ScreenCaptureService; fallback to MediaProjection for MEDIA_PROJECTION source only
- [x] Add CaptureSource enum + ACTION_START_INTERNAL_CAPTURE (no MediaProjection for internal mode)
- [x] Health watcher skips MediaProjection checks in INTERNAL_PROVIDER mode
- [x] Create CaptureProviderRegistry (thread-safe provider holder)
- [x] WebViewCaptureProvider uses WeakReference
- [x] WebView viewport: explicit measure/layout (1080x1920) in MainActivity
- [x] Lifecycle-aware registration in WebViewClient.onPageFinished; onDestroy clears registry
- [x] Move capture_test.html to src/debug/assets/
- [x] Move FakeCaptureProvider to src/test/
- [x] Create CaptureProviderTest.kt
- [x] Commit "feat: CaptureProvider abstraction with WebView capture" (commit dc5c7b3)
- [x] Commit "fix: correct CaptureProvider lifecycle and internal capture wiring" (commit 411c812)
- [x] Create PHASE2_REPORT.md
- [x] Run CI for Phase 2 (GREEN: compile + 164 tests + assembleDebug)
- [x] Write `PHASE2_REPORT.md` (updated)
- [ ] Runtime smoke test: verify internal WebView capture without MediaProjection (debug harness)
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
