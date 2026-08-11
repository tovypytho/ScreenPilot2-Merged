# PHASE4_INTEGRATION_DESIGN.md

Phase 4.2 — Authorized Flutter Integration Architecture Design (DESIGN ONLY)

Inventory authority: `PHASE4_EUJIAN_INVENTORY.md` (Phase 4.1, AUDIT PASS).
Baseline: Phase 3 GREEN — ScreenPilot Android shell + Flutter module via `flutter_debug:1.0` / `flutter_release:1.0` AAR, CaptureProvider/CaptureProviderRegistry/CaptureBridge GREEN.

This document is a **design**, not an implementation. No assets, `.so`, DEX, smali, source, or Gradle changes are made in this phase.

---

## 0. Non-negotiable constraints

- One Android application owner: `id.eujian.cbt.screenpilot`.
- One launcher owner: `id.eujian.cbt.screenpilot.MainActivity`.
- ScreenPilot capture abstraction (`CaptureProvider`, `CaptureProviderRegistry`, `CaptureBridge`) and its channel contract are immutable in Phase 4.2 design.
- CaptureProvider may only ever reference a `WebView` owned by the integrated/project-owned application context.
- The following are **REJECTED — SECURITY/INTEGRITY BOUNDARY** and no design below depends on them:
  - gate/license spoofing (GateCheckValues, GateMethodChannelHandler result forgery, PairIP bypass)
  - package/certificate identity spoofing
  - disabling licensing/integrity checks or exam/security policy
  - FLAG_SECURE bypass
  - server/backend gate circumvention
  - reflection into other applications, Accessibility scraping, cross-process View access, MediaProjection workarounds for capture
- RE artifact changes to licensing/gate state/FLAG_SECURE/lifecycle are recorded **only as forensic compatibility facts** (see §11).

---

## 1. Flutter application strategy

E-Ujian is itself a Flutter application bundle (`assets/flutter_assets/`, `libapp.so`, `libflutter.so`) inside a host whose Android side (Activities, MethodChannel handlers, GateJniBridge, PairIP Application) lives in its DEX. ScreenPilot already owns a Flutter application via AAR. Four options are analyzed:

- **A** = Replace Phase-3 test host with ONE authorized Flutter module
- **B** = Multiple FlutterEngine instances using ONE integrated Flutter library
- **C** = Two independent/opaque Flutter application bundles in one APK
- **D** = Isolated application/test harness

### OPTION A — Replace Phase-3 test host with ONE authorized Flutter module

The ScreenPilot shell remains the host. The Phase 3 test module (`flutter_test_host`) is replaced by a **project-owned/authorized** Flutter module that becomes the single Flutter application surface.

| Aspect | Analysis |
|---|---|
| What is lost from test host | The test host is a debug-only counter + capture button surface (`lib/main.dart`). Nothing functional is lost; its role is superseded by the new module. The AAR wiring itself is unchanged. |
| What stays reusable | All Kotlin capture code: `CaptureBridge` (Kotlin-side channel registration), `CaptureProviderRegistry`, `WebViewCaptureProvider`, `CaptureProvider` — none depend on test-host Dart code. The cached-engine launch pattern (`openFlutterTest()`) stays. |
| Engine ownership | Single owner: the ScreenPilot shell owns exactly **one cached `FlutterEngine`** (created in `MainActivity`, cached via `FlutterEngineCache`, torn down on finish). This is an **architectural ownership decision** for the current ScreenPilot surface, not a Flutter limitation. Flutter itself supports multiple `FlutterEngine` instances; multiple engines do **not** imply multiple independent Flutter libraries/application bundles. With one Flutter module there is one `libapp.so` and one `libflutter.so` — no naming collision possible. |
| Plugin registration | Via the module's AAR `GeneratedPluginRegistrant`; plugins are resolved at `flutter build aar` time. Any plugin must be declared in the module's `pubspec.yaml` and be compatible with the pinned Flutter SDK (3.44.9). |
| Lifecycle | Unchanged from Phase 3: cached engine + `FlutterActivity.withCachedEngine`, engine destroy only when `isFinishing`. |
| Migration risk | LOW **only if** the new module source is project-owned and built with the same pinned Flutter 3.44.9 toolchain. If the module source is not available, this option cannot be exercised. |
| Rollback strategy | Trivial: the test-host source is committed; revert to it (or keep it as a separate module) and CI re-runs the proven AAR flow. |

**Feasibility:** requires authorized Flutter source + same pinned toolchain. Not exercisable with opaque E-Ujian binaries.

### OPTION B — Multiple FlutterEngine instances using ONE integrated Flutter library

Flutter supports multiple `FlutterEngine` instances in one process (already used via `FlutterEngineCache`). It is important to distinguish:

- **multiple FlutterEngine instances of ONE Flutter library** — supported, useful (e.g., background engine + UI engine), each with its own Dart entrypoint. All engines share the same `libflutter.so`, the same `assets/flutter_assets/` namespace, and the same engine version.
- **two independent Flutter application bundles (each with own `flutter_assets/`, `libapp.so`, `libflutter.so`) in one APK** — that is OPTION C below, NOT a multi-engine pattern, and is NOT supported at the packaging level:
  - `assets/flutter_assets/AssetManifest.bin` is a single APK asset path; two bundles cannot coexist.
  - `libapp.so` is a single native library name; two copies cannot coexist (and one `libapp.so` is only valid against the exact engine that compiled it).
  - `libflutter.so` is likewise a single name; the AAR engine and a foreign engine cannot both ship.

**Conclusion:** multi-engine does **not** solve the two-independent-bundles collision (OPTION C). For the artifacts we hold, OPTION B is **not applicable** as a collision solution; it is only a valid pattern within a single project-owned Flutter module.

### OPTION C — Two independent/opaque Flutter application bundles in one APK

Placing E-Ujian's opaque Flutter bundle (`flutter_assets/`, `libapp.so`, `libflutter.so`) **and** ScreenPilot's own Flutter module in the same APK:

- Single APK `assets/flutter_assets/` namespace — one `AssetManifest.bin` cannot serve two bundles.
- Single `lib/<abi>/` namespace — one `libapp.so`, one `libflutter.so`; two copies of either name are impossible.
- E-Ujian `libapp.so` ↔ engine pairing is unproven (see §2); E-Ujian plugin registrant lives in its DEX, so channels/plugins would be missing.
- Copying E-Ujian's Android-side surfaces (Activities, PairIP Application, GateJniBridge, gate MethodChannel handlers) to compensate brings security-controlled behavior into our process — **REJECTED — SECURITY/INTEGRITY BOUNDARY**.

**Conclusion:** OPTION C is **REJECTED** (not feasible at the packaging level; not designable without security compromise).

### OPTION D — Isolated application / test harness

ScreenPilot and authorized target content are **not** packaged as two opaque Flutter apps in one bundle.

| Aspect | Analysis |
|---|---|
| Isolation boundary | Separate APK/process identity. E-Ujian raw split set remains read-only evidence (never merged). ScreenPilot stays a standalone app with its own Flutter module. |
| Process/application ownership | Each surface owns its process; no shared in-process native state, no shared `flutter_assets`. |
| IPC implications | Communication is explicit and narrow (explicit intents with package visibility, file/URI exchange via `FileProvider`). No cross-process View access. |
| CaptureProvider limitation | `CaptureProviderRegistry` is an in-process singleton; it can only ever capture WebViews inside the ScreenPilot process. This is a **feature** of the design (ownership constraint), not a gap. |
| Testing value | HIGH: the harness validates the full capture pipeline against project-owned content (off-screen WebView + `WebViewCaptureProvider` + `CaptureBridge`) without any bundle mixing. |

### RECOMMENDATION

**CURRENT EXECUTABLE PATH: OPTION D — isolated compatibility/test harness.**
It is the only option executable now, preserves every Phase-3 guarantee, and requires no security compromise.

**FUTURE AUTHORIZED IN-PROCESS PATH: OPTION A — single authorized Flutter source/module**, only after the §2 compatibility gate passes and authorized source is available. Until then, A is documented but not exercisable.

**Explicit:** with the current opaque artifacts, **no supported in-process E-Ujian bundle integration has been established** (OPTION C is rejected).

---

## 2. Flutter version / AOT compatibility gate

**Do not assume** E-Ujian `libapp.so` (BuildID `4f1bdaed500008c905f0f0e738b55d35`) is compatible with the ScreenPilot Flutter 3.44.9 engine (AAR engine, `libflutter.so` BuildID `2bb32ee9cefc5994a5c518a602828f719dc7f816`).

Compatibility gate (must all pass before ANY engine/AOT mixing — none may be assumed):

1. **Engine/AOT provenance** — determine the exact Flutter engine commit + Dart SDK version that produced E-Ujian's `libapp.so`/`libflutter.so` (e.g., engine artifact metadata, `kernel_blob`, AOT snapshot layout, string table markers). Provenance from an opaque bundle is not self-evident.
2. **Original build toolchain known?** — If the E-Ujian toolchain/version cannot be identified from evidence, compatibility is **unproven**.
3. **Dart AOT snapshot compatibility** — `libapp.so` embeds a Dart AOT snapshot whose layout is bound to a specific engine version; the engine and app image must originate from the same Flutter release.
4. **Asset layout compatibility** — `AssetManifest.bin` (binary manifest format), shaders, and `NOTICES.Z` layout evolve across Flutter releases; the consuming engine must support the asset version present.
5. **Plugin embedding generation** — E-Ujian's `GeneratedPluginRegistrant` lives in its DEX, not in the assets; channels/plugins registered there are absent if only assets + `.so` are copied.
6. **Android embedding version** — confirm E-Ujian uses Flutter v2 embedding (FlutterEngine/FlutterActivity model); older embedding would change integration entirely.

**Gate rule: if compatibility cannot be proven → DO NOT MIX ENGINE + AOT ARTIFACTS.**

---

## 3. Native library strategy

From the inventory (all ELF64/LSB/AArch64/DYN), the 8 libraries split into three categories:

### A. Flutter runtime/application binaries — `libflutter.so`, `libapp.so`

- Both are already provided by ScreenPilot's Flutter AAR path (engine artifact from `download.flutter.io` + the module's own `libapp.so`).
- Collision: an APK has a single `lib/<abi>/` namespace — one `libflutter.so`, one `libapp.so`. Two copies with the same name are impossible; and a foreign `libapp.so` is invalid against a different engine.
- Ownership decision: **single owner = ScreenPilot's Flutter module + pinned engine.** E-Ujian's `libflutter.so`/`libapp.so` pair is atomic (each is meaningless without its matched engine) and is **never packaged** in this phase.
- **No packaging of two copies under the same name is designed.**

### B. Generic / plugin native dependencies

Examples from inventory:

| Library | Character |
|---|---|
| `libbarhopper_v3.so` | Barhopper OCR runtime (Google ML-Kit family) — pulled by an ML-Kit plugin in E-Ujian's DEX |
| `libimage_processing_util_jni.so` | JNI utility tied to E-Ujian Java code (`System.loadLibrary`) |
| `libsurface_util_jni.so` | Same character: JNI wrapper over project-owned/plugin Java |
| `libc++_shared.so` | C++ STL runtime shared library |

- These `.so` files are dead without their matching Java/plugin classes. Copying them alone yields nothing runnable.
- **`libc++_shared.so` dependency ownership analysis (evidence-based at design time):**
  - Current ownership **candidate** = Flutter AAR / transitive dependency graph (bundled by the AAR flow).
  - Exact producer of `libc++_shared.so` in the final ScreenPilot APK = **UNVERIFIED at design time** — the merged AAR/APK native tree has not been audited in this phase.
  - Before any second native producer is ever added, implementation must inspect the merged native artifacts and establish the exact producer, version, and ABI of the shipped copy.
  - **No `pickFirst`/overwrite/packaging workaround is recommended** until compatibility between the shipped copy and all consumers is proven.
- Conclusion (unchanged): **E-Ujian's `libc++_shared.so` MUST NOT be copied now.**

### C. Custom application JNI libraries — `libexambro_gate.so`, `libdatastore_shared_counter.so`

Inventory-only. If a future authorized source integration ever needs them, the **compatibility checklist** (not design of security logic) is:

- Java class contract (the Java classes that declare `System.loadLibrary` and JNI methods)
- `System.loadLibrary` ownership (who loads, when)
- exported JNI symbol set / class-name encoding (`Java_<pkg>_<class>_<method>`)
- ABI (arm64-v8a)
- `DT_NEEDED` closure (see §12)
- lifecycle / load ordering (system libs first; no load-time gate side effects)

`libexambro_gate.so` is a security-adjacent custom gate JNI. **No design changes its behavior; it is recorded only as forensic inventory.**

---

## 4. Manifest merge strategy

Principles:

- **ONE application owner** (`id.eujian.cbt.screenpilot`) and **ONE launcher owner** (`id.eujian.cbt.screenpilot.MainActivity`).
- ScreenPilot manifest is never replaced wholesale by E-Ujian's.
- Play distribution/signing/split-stamp metadata are distribution properties, not generic manifest components to copy.
- Security/licensing components are recorded, never modified or imported.

Component matrix (E-Ujian inventory vs ScreenPilot):

| Component | Source | Needed for authorized integration? | Conflict? | Resolution strategy |
|---|---|---|---|---|
| `id.eujian.cbt.screenpilot.MainActivity` (launcher) | ScreenPilot | Yes | None | Keep as sole launcher |
| `io.flutter.embedding.android.FlutterActivity` | ScreenPilot | Yes | None (same class) | Keep; declared once |
| `.service.ScreenCaptureService` (FGS mediaProjection) | ScreenPilot | Yes | None | Keep |
| `id.exambro.cbt.MainActivity` | E-Ujian | No (opaque, launcher of other app) | Package collision domain | Do not import |
| `com.pairip.application.Application` | E-Ujian | No — security boundary | Application class is single-owner | Record only; not imported |
| `com.pairip.licensecheck.LicenseActivity` | E-Ujian | No — security boundary | None | Record only; not imported |
| `io.flutter.plugins.urllauncher.WebViewActivity` | E-Ujian | Only with url_launcher plugin + its DEX | Plugin Activity | Skip unless authorized plugin integration |
| `com.google.android.gms.common.api.GoogleApiActivity` | E-Ujian | Only with GMS deps | Play-services dependency | Skip |
| CameraX metadata holder / ML Kit discovery | E-Ujian | Only with those plugins | Provider/service | Skip |
| DataTransport providers/receivers | E-Ujian | Only with those plugins | Provider/receiver | Skip |
| AndroidX Startup provider / Profile Installer receiver | generic | Only with those libs | None | Decide at integration, not now |
| Permissions (`SYSTEM_ALERT_WINDOW`, `ACCESS_NOTIFICATION_POLICY`, location, Bluetooth, `CAMERA`, `RECORD_AUDIO`, `INTERNET`, `ACCESS_NETWORK_STATE`, `CHECK_LICENSE`, …) | E-Ujian | Only the minimum actually exercised by integrated code | None | Merge per-principle-of-least-privilege; **never `CHECK_LICENSE`** |
| Play split/stamp metadata | E-Ujian | No | None | Do not copy |

---

## 5. WebView / CaptureProvider architecture

Ownership rule (non-negotiable): `CaptureProvider` receives only a `WebView` owned by the integrated/project-owned application context. The debug off-screen WebView + `capture_test.html` pattern from Phase 2 remains the model.

**Forbidden by design:** reflection into other apps, Accessibility scraping, cross-process View access, MediaProjection workaround capture, FLAG_SECURE bypass.

Authorized registration hook:

```
WebView / PlatformView creation
        ↓
project-owned registration hook
        ↓
CaptureProviderRegistry
        ↓
WebViewCaptureProvider
        ↓
CaptureBridge
        ↓
Flutter caller
```

### Primary design — Android Flutter plugin wrapper (host-side, project-owned)

A project-owned Kotlin `FlutterPlugin` (in the app module or the Flutter module's plugin dir) is registered against the cached `FlutterEngine` **before** the FlutterActivity attaches:

- obtains the engine's `PlatformViewRegistry` and registers a `PlatformViewFactory` that **constructs the WebView itself** (project-owned factory = the only creator),
- hands the constructed WebView to a project-owned `WebViewRegistry`/hook that installs `WebViewCaptureProvider` into `CaptureProviderRegistry` (WeakReference semantics preserved, main-Looper registration preserved),
- leaves `CaptureBridge` untouched (it already reads `CaptureProviderRegistry`).

Ownership: the shell owns both the factory and the WebView instance; the Flutter side only renders the platform view.

### Fallback design — application-owned WebView factory (explicit contract)

A project-owned `WebViewFactory` interface with a single factory method; the Flutter plugin receives WebViews **only** through this interface, never by constructing its own or receiving arbitrary views. Registration of the factory happens in `MainActivity`/Application init; the plugin calls it at PlatformView create time. Same downstream pipeline.

No reflection, no cross-process access in either design.

---

## 6. Plugin / MethodChannel inventory gate

Before any Flutter application replacement/integration, a mandatory inventory must be produced:

- MethodChannel names, EventChannel names, PlatformView factory names
- plugin classes and their registration path (`GeneratedPluginRegistrant` vs host-manual)
- ActivityAware plugins, permission-dependent plugins
- native plugin dependencies (each channel → its JNI lib)

Separate into:

| Owner | Known channels (today) |
|---|---|
| ScreenPilot | `id.eujian.cbt.screenpilot/capture` (capture) — **immutable in Phase 4.2** |
| Target Flutter app (E-Ujian) | unknown from opaque bundle; must be inventoried from authorized source or treated as forensic (DEX-decompiled) evidence only |

Collision matrix template (populated only after the gate inventory; empty now):

| channel | owner | conflict risk | resolution |
|---|---|---|---|
| (filled by inventory gate) | | | |

**Rule:** `CaptureBridge` channel `id.eujian.cbt.screenpilot/capture` does not change in this phase's design.

---

## 7. Testing / rollback strategy

Development isolation (recommendation, not created now):

- `main` — Phase 3 known-green baseline (unchanged).
- `phase4/integration-prototype` — experimental integration branch, branched from `main`.

Test gates — each gate is **STOP on failure**:

| Gate | Check |
|---|---|
| A | Packaging/static compatibility (no duplicate `.so`, no duplicate `flutter_assets`, manifest merge lint) |
| B | CI compile + unit tests (Phase 2/3 unit tests and lint remain regression gates) |
| C | Flutter host boots (FlutterActivity opens, first frame renders) |
| D | Plugin/channel initialization (all registered channels respond, no missing registrant) |
| E | Project-owned WebView renders (project-owned HTML/WebView factory produces a valid surface) |
| F | CaptureProvider receives the correct (project-owned) WebView reference |
| G | Capture result reaches Flutter through CaptureBridge (PNG path + dimensions contract intact) |
| H | Back/reopen/lifecycle regression (engine teardown only on finish; config-change safe) |

Existing Phase-2/Phase-3 tests (Robolectric unit tests, CI lint) are permanent regression gates.

---

## 8. Decision matrix

| Option | Technical feasibility | Risk | Phase-3 impact | Requires source? | Opaque binary mixing? | Recommended? |
|---|---|---|---|---|---|---|
| A. Replace test host with authorized Flutter source/module | Feasible with pinned 3.44.9 toolchain | Medium (engine/AOT coupling) | Low (AAR flow unchanged) | **Yes** | No | Conditional future path — only when authorized source exists and §2 gate passes |
| B. Multiple FlutterEngine instances using ONE integrated Flutter library | Feasible (one module, several engines) | Medium (memory, lifecycle) | Low | **Yes** | No | Supported pattern; **not** a solution for two independent bundles |
| C. Two independent/opaque Flutter application bundles in one APK | **Not feasible** — single `flutter_assets/` and single `libapp.so`/`libflutter.so` namespace; unproven engine/AOT pairing; DEX-bound plugins absent | Very high | High (baseline risk) | No | **Yes** | **REJECTED** |
| D. Isolated application/test harness | Feasible now | Low | Minimal | No | No | **CURRENT RECOMMENDED / executable path** |

---

## 9. Recommended architecture

**Design: isolated application/test harness (OPTION D from §1), keeping ScreenPilot as the sole packaged application.**

- ScreenPilot Android shell: unchanged.
- CaptureProvider / CaptureProviderRegistry / CaptureBridge: unchanged.
- CI baseline: unchanged.
- Flutter: existing AAR module (`flutter_debug:1.0`/`flutter_release:1.0`) remains the only Flutter application in the APK.
- E-Ujian raw split set: read-only evidence; nothing from `flutter_assets/`, `lib/*.so`, DEX, JADX, or smali is packaged.
- WebView registration follows §5 (host-side plugin wrapper as primary, explicit factory as fallback).
- **With the current opaque artifacts, no supported in-process E-Ujian bundle integration has been established.** The only executable path is the isolated harness (OPTION D); OPTION A is the future authorized in-process path, gated by §2 and source availability.

**Explicit statement of what cannot be done safely from the opaque bundle alone** (`flutter_assets + libapp.so + native .so`, without E-Ujian source/DEX):

- Cannot run E-Ujian's Flutter UI in-process without engine/AOT provenance (unproven pairing), plugin/channel completeness (registrant lives in DEX), and embedding compatibility.
- Cannot reach E-Ujian's Android-side features (Activities, MethodChannel handlers, GateJniBridge, PairIP Application/licensing, CameraX/ML Kit stack) without its DEX — and importing those surfaces brings security-controlled behavior into our process.
- Any "make it work anyway" path requires license/gate/FLAG_SECURE circumvention → **REJECTED — SECURITY/INTEGRITY BOUNDARY**.
- Therefore, with the current artifacts, the **only safe, testable integration is at the pipeline level against project-owned content** (the harness), not at the bundle level.

---

## 10. Implementation slices — DESIGN ONLY

| Slice | Goal | Entry condition | Exit condition |
|---|---|---|---|
| **4.2A compatibility proof** | Prove (or disprove) engine/AOT provenance for E-Ujian bundle | Authorized access to evidence or metadata enabling provenance determination | Compatibility gate (§2) answered with evidence, or permanently documented as unprovable |
| **4.2B Flutter ownership decision** | Confirm single-Flutter-module ownership; keep or replace test host | 4.2A result; authorized-source availability known | Explicit decision recorded in DECISIONS.md |
| **4.2C manifest/plugin prototype** | Prototype manifest merge matrix + plugin/channel inventory on a branch | 4.2B decision | Gate B green on `phase4/integration-prototype`; collision matrix filled |
| **4.2D project-owned WebView registration** | Implement §5 primary (or fallback) hook in prototype branch | 4.2C green | Gate C + E green (host boots, project-owned WebView renders) |
| **4.2E CaptureProvider runtime test** | Validate provider wiring + CaptureBridge end-to-end | 4.2D green | Gates F + G green on-device |
| **4.2F regression/cleanup** | Full regression, docs (PROJECT_STATE/DECISIONS/TODO), branch hygiene | 4.2E green | Gate H green; main unchanged; CI baseline intact |

No slice in this phase: no `git add/commit/push`, no production edits.

---

## 11. Forensic compatibility facts (RE artifact — record only)

The RE artifact (`E-Ujian_RE_JADX`, MOD APK) contains changes to licensing, gate-facing state, FLAG_SECURE behavior, and lifecycle/focus handling. These are recorded as forensic evidence that the RE artifact is **not** a pristine baseline and **not** integration instructions. No design in this document preserves, replicates, or depends on them.

## 12. Reference: native DT_NEEDED (from Phase 4.1 inventory)

- `libapp.so`: none observed
- `libbarhopper_v3.so`: libjnigraphics, liblog, libdl, libm, libc
- `libc++_shared.so`: libc, libm, libdl
- `libdatastore_shared_counter.so`: libm, libdl, libc
- `libexambro_gate.so`: liblog, libm, **libc++_shared**, libdl, libc
- `libflutter.so`: libc, libdl, libm, libandroid, libEGL, libGLESv2, liblog, libjnigraphics
- `libimage_processing_util_jni.so`: liblog, libandroid, libjnigraphics, libm, libdl, libc
- `libsurface_util_jni.so`: libandroid, libm, libdl, libc
