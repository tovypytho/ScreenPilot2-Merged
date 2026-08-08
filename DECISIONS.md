# DECISIONS.md — Architecture Decision Log

## D001 — Standalone clean baseline
Accepted. `MergedProject` must compile as a standalone Android/Kotlin app before Flutter integration.

## D002 — JADX is reference-only
Accepted. Decompiled/JADX files are for analysis and do not belong in normal Gradle production `sourceSets`.

## D003 — Canonical namespace
Accepted. Use `id.eujian.cbt.screenpilot` for production ScreenPilot code and standalone identity.

## D004 — GitHub Actions first
Accepted. Primary build environment is GitHub Actions using JDK 21, Gradle 9.3.1, AGP 9.1.1, Kotlin 2.2.10, compileSdk 35.

## D005 — Mandatory phase checkpoints
Accepted. Do not start a later phase until the previous phase is reviewed and CI is green.

## D006 — Capture abstraction
Planned for Phase 2. ScreenPilot capture logic should depend on a `CaptureProvider` abstraction and initially operate only on project-owned/allowed test content.

## D007 — Flutter integration starts with a test host
Planned for Phase 3. Prove Flutter↔Kotlin communication with a small test host before any larger authorized integration.

## D008 — Security boundaries remain intact
Accepted. Do not add production behavior intended to bypass protected content, verification gates, licensing, signature/integrity checks, or related controls.
