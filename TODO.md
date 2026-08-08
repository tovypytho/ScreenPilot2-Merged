# TODO.md — Project Roadmap

## Current: Git Baseline (Phase 1.7)
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
- [ ] Push Phase-1 baseline to GitHub (after explicit instruction)
- [ ] Run GitHub Actions
- [ ] Fix compile errors if any
- [ ] Fix unit-test errors if any
- [ ] Confirm `assembleDebug` succeeds
- [ ] Download/test standalone APK
- [ ] Mark Phase-1 CI checkpoint GREEN

## Phase 2 — Capture Abstraction
Start only after Phase-1 CI is green.
- [ ] Define `CaptureProvider`
- [ ] Define `CaptureResult`
- [ ] Add project-owned dummy WebView page
- [ ] Implement allowed test WebView capture provider
- [ ] Add fake provider for unit tests
- [ ] Route analysis pipeline through abstraction
- [ ] Add tests
- [ ] Run CI
- [ ] Write `PHASE2_REPORT.md`

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
