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
Phase 1 GREEN. Phase 2 final GREEN. Phase 3 COMPLETE and runtime-verified. Phase 4.1 inventory, Phase 4.2 architecture, Phase 4.2A compatibility proof, Phase 4.2B Flutter ownership, and Phase 4.3A provider lifecycle design are COMPLETE. Current Flutter ownership remains the `flutter_test_host` AAR on Flutter 3.44.9 with cached engine `screenpilot_capture_host`; opaque target Flutter/native/DEX artifacts are not production inputs. NEXT: Phase 4.3B owner-aware `CaptureProviderRegistry` implementation using owner-scoped registration handles and stale-dispose-safe newest-live selection. Preserve the security boundary and existing no-fallback capture semantics. See `PROJECT_STATE.md`, `PHASE4_2B_FLUTTER_OWNERSHIP.md`, and `PHASE4_3A_PROVIDER_LIFECYCLE_DESIGN.md`.
