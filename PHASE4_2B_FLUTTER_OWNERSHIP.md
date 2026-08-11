# PHASE4_2B_FLUTTER_OWNERSHIP.md

## Scope

Phase 4.2B is a **decision-only ownership checkpoint** following the Phase 4.2A compatibility proof.

No production Android/Flutter code, Gradle configuration, target Flutter assets, DEX, or native libraries are introduced by this checkpoint.

## Inputs

Authoritative project evidence:

- `PHASE4_EUJIAN_INVENTORY.md`
- `PHASE4_INTEGRATION_DESIGN.md`
- `PHASE4_2A_COMPATIBILITY_PROOF.md`
- known-green Phase-3 runtime bridge
- CI run #23 (`31459422945`, commit `395909c`) — compile, 163 unit tests, `assembleDebug`, and `lintDebug` all GREEN

## Decision

**Phase 4.2B COMPLETE — retain ScreenPilot's current Flutter ownership.**

ScreenPilot keeps exactly one integrated Flutter application/library ownership domain:

- Android application owner: ScreenPilot (`id.eujian.cbt.screenpilot`)
- Flutter module owner: `flutter_test_host`
- Flutter toolchain: stable 3.44.9
- host dependency model: generated AAR consumed from the local Maven repository
- current UI engine: cached `FlutterEngine` id `screenpilot_capture_host`
- ScreenPilot capture channel: `id.eujian.cbt.screenpilot/capture`
- capture abstraction owner: `CaptureProviderRegistry` / `CaptureProvider` / `CaptureBridge`

The target opaque Flutter application is **not** a second ownership domain inside ScreenPilot.

## 1. Ownership matrix

| Surface | Current owner | Phase 4.2B decision |
|---|---|---|
| Android application / launcher | ScreenPilot | KEEP |
| Flutter library/application bundle | `flutter_test_host` AAR | KEEP |
| Flutter engine generation | Flutter 3.44.9 | KEEP |
| Cached FlutterEngine | ScreenPilot `MainActivity` | KEEP |
| ScreenPilot MethodChannel | `CaptureBridge` | KEEP |
| CaptureProvider registry | ScreenPilot | KEEP |
| Target `flutter_assets` | external/read-only evidence | DO NOT PACKAGE |
| Target `libapp.so` | external/read-only evidence | DO NOT PACKAGE |
| Target `libflutter.so` | external/read-only evidence | DO NOT PACKAGE |
| Target DEX/JADX/smali | external/read-only evidence | DO NOT PACKAGE |
| Target native `.so` set | external/read-only evidence | DO NOT PACKAGE |

## 2. Why ownership does not change

Phase 4.2A established an evidence-backed NO-GO for opaque bundle mixing:

- target Flutter runtime evidence maps to the Flutter 3.38.3 / Dart 3.10.1 generation;
- ScreenPilot uses Flutter 3.44.9;
- target `libapp.so` is a precompiled AOT application image and is not proven compatible with the ScreenPilot engine;
- two independent `flutter_assets` / `libapp.so` / `libflutter.so` ownership domains cannot be treated as a supported in-process composition.

Therefore Phase 4.2B does not authorize an implementation change. The correct action is to preserve the known-green ownership domain.

## 3. Architecture option status

### Option A — one authorized Flutter source/module

**CONDITIONAL FUTURE PATH.**

If an authorized source/module becomes available, integration must be a controlled ownership migration/rebuild under one pinned Flutter toolchain. It is not a plan to keep two opaque Flutter application bundles side-by-side.

### Option B — multiple FlutterEngine instances

**SUPPORTED PATTERN WITHIN ONE INTEGRATED FLUTTER LIBRARY.**

This does not change the application/library ownership decision and does not solve opaque-bundle collisions.

### Option C — two independent opaque Flutter bundles in one APK

**REJECTED.**

No Phase 4 implementation may depend on this architecture.

### Option D — isolated project-owned compatibility/test harness

**CURRENT EXECUTABLE PATH.**

This remains the active path because it preserves the Phase-3 Flutter AAR, CaptureProvider abstraction, and known-green runtime behavior.

## 4. Native ownership boundary

No target native library is added in Phase 4.2B.

The exact producer/version of the final ScreenPilot APK's `libc++_shared.so` remains **UNVERIFIED** because the APK artifact bytes were not supplied as local evidence for this checkpoint.

This does not block the ownership decision because there is no second native producer.

Before any future second native producer is considered:

1. inspect a CI-built merged APK/AAR native tree;
2. establish the exact producer/version/ABI of the shipped `libc++_shared.so`;
3. prove compatibility with all consumers;
4. do not use `pickFirst`, overwrite, or equivalent packaging workarounds as a substitute for compatibility evidence.

## 5. Capture ownership boundary

Knowledge of a target plugin, MethodChannel name, or PlatformView id does not transfer Android `View` ownership to ScreenPilot.

`CaptureProviderRegistry` may only expose a provider backed by a project-owned/authorized capture surface.

The current registry API is intentionally minimal:

- `set(provider)`
- `get()`
- `clear()`

Before adding another project-owned WebView surface, the next design must account for provider lifecycle ownership so that disposal of an older surface cannot accidentally clear a newer provider.

No registry refactor is performed in Phase 4.2B.

## 6. Security/integrity boundary

Phase 4.2B does not depend on, preserve, reproduce, or design around:

- license/integrity bypasses;
- package/certificate identity spoofing;
- gate-result spoofing;
- `FLAG_SECURE` bypass;
- server/backend security circumvention.

Security-adjacent artifacts remain inventory/forensic evidence only.

## 7. CI / baseline status

CI run #23 (`31459422945`) for commit `395909c` is GREEN:

- Kotlin compile: PASS
- unit tests: 163 / 163 PASS
- `assembleDebug`: PASS
- `lintDebug`: PASS
- debug APK artifact: produced

The recurring KSP/AWT background exception remains known nonfatal tooling noise because the requested Gradle tasks complete successfully.

## 8. Exit criteria

Phase 4.2B is complete when all of the following are recorded:

- [x] ScreenPilot remains the single Android application owner.
- [x] `flutter_test_host` remains the single integrated Flutter application/library owner.
- [x] Target opaque Flutter/native/DEX artifacts remain excluded from production packaging.
- [x] Option D remains the current executable path.
- [x] Option A remains conditional on authorized source/module + one pinned toolchain.
- [x] `libc++_shared.so` producer attribution remains a prerequisite only before a future second native producer.
- [x] No production code/build configuration changes are justified by this checkpoint.

## 9. Next milestone

**Phase 4.3A — Project-owned WebView provider lifecycle design (DESIGN ONLY).**

Purpose:

- design an explicit lifecycle contract for registering a project-owned Flutter PlatformView/WebView with the existing capture abstraction;
- determine whether `CaptureProviderRegistry` needs owner/token/handle semantics before a second project-owned surface exists;
- preserve the current internal off-screen WebView debug provider and Phase-3 bridge behavior;
- define tests for registration, replacement, stale-dispose, Activity teardown, Flutter reopen, and provider-unavailable states.

Phase 4.3A must remain project-owned/authorized and must not use reflection, cross-process View access, Accessibility scraping, MediaProjection workarounds, or security-control bypasses.

No Phase 4.3 implementation is authorized by this document.
