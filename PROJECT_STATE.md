# PROJECT_STATE.md — Current Project State

Last updated: 2026-08-08

## Current Phase
**Phase 1.7 (Git baseline) complete. Local Git repository initialized with standalone ScreenPilot baseline committed.**

## Completed
- Backup of pre-Phase-1 MergedProject created.
- Removed Gradle `sourceSets` references to `../../E-Ujian_RE_JADX`.
- Returned to standard `app/src/main/{java,res,AndroidManifest.xml}` layout.
- Aligned namespace/applicationId to `id.eujian.cbt.screenpilot`.
- Migrated production type references from legacy `com.example.*`.
- Updated test/androidTest namespaces.
- Confirmed local ScreenPilot manifest is used.
- Confirmed JADX evidence remains outside the build.
- `PHASE1_REPORT.md` created.
- Added project memory files: `AGENTS.md`, `PROJECT_STATE.md`, `DECISIONS.md`, `TODO.md`.
- Created `.gitignore` (Gradle/build/local/SDK/secrets ignored; wrapper jar/properties + gradlew + version catalog kept).
- Audited `.github/workflows/android-build.yml` (compatible with JDK 21 / Gradle 9.3.1 / AGP 9.1.1 / compileSdk 35 / testDebugUnitTest / assembleDebug / artifact upload; risk notes only, not edited).
- `PRE_CI_REPORT.md` created.
- Phase 1.6: removed empty internal stub `MergedProject/E-Ujian_RE_JADX/` (reconfirmed 0 files). External evidence `AntiGravityIDE/E-Ujian_RE_JADX` (3,913 files) untouched.
- Phase 1.6: moved `app/src/androidTest/java/com/example/ExampleInstrumentedTest.kt` → `app/src/androidTest/java/id/eujian/cbt/screenpilot/ExampleInstrumentedTest.kt` (package `id.eujian.cbt.screenpilot`). Removed empty legacy `com/example` directories.
- Phase 1.6: audited `.gitignore` final contents (no global `*.properties`; wrapper properties/gradlew/catalog kept).
- Phase 1.6: `FINAL_REPO_CHECK.md` created.
- Phase 1.7: `git init -b main` — local repository initialized.
- Phase 1.7: verified `.gitignore` rules via `git check-ignore` — build outputs, secrets, local.properties, .gradle/ all correctly ignored; required build files (gradlew, gradle-wrapper.jar/properties, libs.versions.toml, gradle.properties) correctly trackable.
- Phase 1.7: staged all 93 files with `git add .`.
- Phase 1.7: audited staged files for secrets, local.properties, APK/AAB, build outputs, .gradle, JADX/evidence, .class files — all clean.
- Phase 1.7: committed baseline `8aabf1a` — "chore: establish standalone ScreenPilot baseline".
- Phase 1.7: `GIT_BASELINE_REPORT.md` created.

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
No actual CI compile/test/build has yet verified the Phase-1 state. GitHub Actions must compile, test, and assemble the APK before Phase 2.

## Do Not Start Yet
Until GitHub Actions is green:
- no CaptureProvider
- no dummy WebView capture implementation
- no Flutter test host
- no MethodChannel bridge
- no GateProvider work
- no large storage/API refactor

## Next Milestone
1. Push Phase-1 baseline to GitHub (after explicit instruction)
2. Run GitHub Actions
3. Fix compile errors if any
4. Fix unit-test errors if any
5. Confirm `assembleDebug` succeeds
6. Download/test standalone APK
7. Mark Phase-1 CI checkpoint GREEN

Only after CI is green should Phase 2 begin.
