# AGENTS.md — Project Rules

## Source of Truth
Before changing code, read `PROJECT_STATE.md`, `DECISIONS.md`, `TODO.md`, and the latest phase report. After meaningful work, update those files. Do not rely only on chat/session memory.

## Current Architecture
- Standalone Android/Kotlin ScreenPilot project.
- Production namespace/applicationId: `id.eujian.cbt.screenpilot`.
- Production source: `app/src/main/java`.
- Resources: `app/src/main/res`.
- Manifest: `app/src/main/AndroidManifest.xml`.
- JADX/decompiled material is reference-only and MUST NOT enter Gradle `sourceSets`.
- Do not reintroduce `../../E-Ujian_RE_JADX` or other paths outside the repository.
- Do not reintroduce legacy `com.example.*` production type/package references.

## Build
- Primary build environment: GitHub Actions.
- JDK 21.
- Gradle 9.3.1.
- AGP 9.1.1.
- Kotlin 2.2.10.
- compileSdk 35, targetSdk 35, minSdk 28.
- Avoid local Android builds unless explicitly requested.

## Workflow
- Work phase-by-phase.
- Do not combine multiple major phases in one edit.
- Inspect the real files before editing.
- Perform static verification after edits.
- If CI fails, fix the first real compile/test error before adding new features.
- Stop at phase checkpoints for review.

## Safety / Scope
- Keep security boundaries intact.
- Do not add production code intended to bypass protected-content capture, exam controls, licensing, signature/integrity checks, or verification gates.
- Test fakes/mocks must stay test-only and must not reproduce spoofed production identities.

## Current Checkpoint
Phase 1 GREEN. Phase 2 final GREEN (CI #12 / `d50a817`, runtime smoke verified). Phase 3.1 minimal Flutter test host is CI GREEN via AAR integration (`fe57637`, CI run #17 `31268201223`): host consumes `flutter_test_host` as a prebuilt AAR from `flutter build aar --debug` (local Maven repo + `download.flutter.io`), Flutter Gradle plugin is NOT used (conflicts with AGP 9.1.1), `.android/` is gitignored/untracked, `compileSdk=36`/`targetSdk=35`. NEXT: on-device smoke test — fresh app, tap debug button "Open Flutter Test", Flutter screen must open without crash. Only after that: Phase 3.2 (MethodChannel bridge). See PROJECT_STATE.md for full detail.
