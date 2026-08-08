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
Accepted. ScreenPilot capture logic depends on a `CaptureProvider` abstraction. Initially operates only on project-owned/allowed test content via a project-owned WebView loading local HTML assets. MediaProjection remains as the fallback path when no provider is injected.

## D006a — WebView capture provider
Accepted. `WebViewCaptureProvider` implements `CaptureProvider` by rendering a `WebView`'s content via `Canvas`/bitmap. Uses `Dispatchers.Main.immediate` for UI-thread-safe `webView.draw()`. Project-owned HTML asset (`capture_test.html`) serves as the test surface.

## D006b — Fake capture provider
Accepted. `FakeCaptureProvider` returns a solid-color dummy `Bitmap` for unit testing without real rendering. Test-only convenience, not used in production.

## D007 — Flutter integration starts with a test host
Planned for Phase 3. Prove Flutter↔Kotlin communication with a small test host before any larger authorized integration.

## D008 — Security boundaries remain intact
Accepted. Do not add production behavior intended to bypass protected content, verification gates, licensing, signature/integrity checks, or related controls.
