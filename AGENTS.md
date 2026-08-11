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
- compileSdk 36, targetSdk 35, minSdk 28.
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
Phase 1 GREEN. Phase 2 final GREEN. Phase 3 COMPLETE and runtime-verified. Phase 4.1 through Phase 4.3A are COMPLETE. Phase 4.3B owner-aware registry code is implemented and awaiting CI/device validation. `CaptureProviderRegistry` ownership now uses `register(provider) -> CaptureProviderRegistration` with newest-live selection/restoration; `MainActivity` owns/closes only its internal-WebView registration. `CaptureBridge` and `ScreenCaptureService` must remain read-only `get()` consumers, and `INTERNAL_PROVIDER` must not fall back to MediaProjection. Do not introduce opaque target Flutter/native/DEX artifacts. Next action: static review + GitHub Actions, then repeat the project-owned Phase-2/Phase-3 device smoke gates before closing 4.3B.
