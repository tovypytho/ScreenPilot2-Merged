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
Phase 1 is GREEN. Phase 2 baseline CI is GREEN, but runtime smoke testing exposed an internal-debug startup issue. The current checkpoint is to verify the corrective internal-provider patch in GitHub Actions and then prove on-device that a fresh internal session starts without MediaProjection and exports the marked project-owned WebView image to `Pictures/ScreenPilotDebug`. Do not begin Phase 3 before that checkpoint is GREEN.
