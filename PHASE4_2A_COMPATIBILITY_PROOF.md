# PHASE4_2A_COMPATIBILITY_PROOF.md

## Status

**Phase 4.2A compatibility proof: COMPLETE — NO-GO for opaque in-process bundle integration.**

This is an evidence-only compatibility result. No E-Ujian asset, DEX, native library, JADX/smali source, manifest component, or security/integrity modification is copied into ScreenPilot.

## Scope and evidence

Evidence used:

- Canonical RAW E-Ujian split set from `AntiGravityIDE.zip`:
  - `eujian_apk_orisplitapk no merge/base.apk`
  - `eujian_apk_orisplitapk no merge/split_config.arm64_v8a.apk`
- Original/read-only JADX evidence under `baseapkjadxori/` for Android embedding and plugin registration.
- ScreenPilot repository at commit `99e2cc2`.
- CI run #22 (`31456927418`) for the Phase-4 checkpoint commit.
- Official Flutter release metadata used only to correlate the engine revision embedded in the target binary with a Flutter/Dart release.

Security/integrity boundary remains unchanged: licensing/gate/signature behavior, `FLAG_SECURE`, server/backend policy, and related controls are inventory-only and are not modified, bypassed, or used as compatibility requirements.

## 1. CI baseline before compatibility decision

CI run #22 checks out commit `99e2cc2` and remains GREEN:

- compileDebugKotlin: GREEN
- testDebugUnitTest: GREEN
- assembleDebug: GREEN
- lintDebug: GREEN
- unit-test report: 163 tests, 0 failures, 0 errors, 0 skipped

Artifacts were produced by CI. The APK artifact bytes themselves were not included in the local evidence set used for this proof, so final packaged-native producer attribution remains intentionally deferred (see §7).

## 2. Target Flutter engine / Dart provenance

### Binary evidence

RAW `split_config.arm64_v8a.apk` contains:

- `libflutter.so`
  - ELF64 / little-endian / AArch64 / DYN
  - GNU Build ID: `2bb32ee9cefc5994a5c518a602828f719dc7f816`
  - printable engine revision observed in the binary: `13e658725ddaa270601426d1485636157e38c34c`
- `libapp.so`
  - ELF64 / little-endian / AArch64 / DYN
  - GNU Build ID: `4f1bdaed500008c905f0f0e738b55d35`
  - exports the standard precompiled Dart snapshot symbols (`_kDartVmSnapshotData`, `_kDartIsolateSnapshotData`, `_kDartVmSnapshotInstructions`, `_kDartIsolateSnapshotInstructions`).

### Release correlation

Official Flutter release history identifies Flutter `3.38.3` as the release whose final release commit updates `bin/internal/engine.version` immediately after commit `13e658725ddaa270601426d1485636157e38c34c`. The same revision is embedded as a printable revision in the target `libflutter.so`.

Flutter `3.38.3` release notes also record the Dart SDK bump to **Dart 3.10.1**.

### Provenance verdict

**HIGH confidence:** the supplied target Flutter runtime is from the Flutter **3.38.3 / Dart 3.10.1 generation**, with engine-artifact revision evidence `13e658725ddaa270601426d1485636157e38c34c`.

The mapping is evidence-based correlation of the embedded revision plus official release metadata; it does not claim source-level reproducibility of the proprietary application.

## 3. Dart AOT / ScreenPilot engine compatibility

ScreenPilot Phase 3 is built with Flutter stable **3.44.9**. The target precompiled application is tied to the older 3.38.3-generation runtime evidence above.

A precompiled Flutter/Dart Android application image (`libapp.so`) must be treated as an atomic pair with the compatible Flutter/Dart engine/toolchain that produced it. There is no evidence that the target `libapp.so` is compatible with ScreenPilot's Flutter 3.44.9 engine, and the identified release generations are different.

### Compatibility verdict

**NO-GO:** do not pair the target `libapp.so` or `flutter_assets` with ScreenPilot's Flutter 3.44.9 `libflutter.so`.

Also do not replace ScreenPilot's engine with the target opaque `libflutter.so`; doing so would invalidate the known-green Phase-3 AAR ownership/toolchain and still would not provide the target Android-side plugin/DEX contracts.

A future authorized in-process path remains possible only through **Option A** from `PHASE4_INTEGRATION_DESIGN.md`: obtain authorized Flutter source/module and rebuild the single integrated Flutter application with one pinned toolchain.

## 4. Android Flutter embedding generation

Original RAW/JADX manifest evidence contains:

```xml
<meta-data
    android:name="flutterEmbedding"
    android:value="2"/>
```

The original launcher `id.exambro.cbt.MainActivity` extends `io.flutter.embedding.android.FlutterActivity` and overrides `configureFlutterEngine(FlutterEngine)`.

**Verdict: Flutter Android embedding v2 is confirmed.**

This confirms the general engine/plugin lifecycle model, but does not make the opaque application bundle portable into ScreenPilot.

## 5. Plugin registration inventory

Original `io.flutter.plugins.GeneratedPluginRegistrant` registers eight Android plugins:

| # | Plugin | Android implementation evidence |
|---:|---|---|
| 1 | `audioplayers_android` | `xyz.luan.audioplayers.AudioplayersPlugin` |
| 2 | `mobile_scanner` | `dev.steenbakker.mobile_scanner.MobileScannerPlugin` |
| 3 | `path_provider_android` | `io.flutter.plugins.pathprovider.PathProviderPlugin` |
| 4 | `permission_handler_android` | `com.baseflow.permissionhandler.PermissionHandlerPlugin` |
| 5 | `shared_preferences_android` | `io.flutter.plugins.sharedpreferences.SharedPreferencesPlugin` |
| 6 | `url_launcher_android` | `io.flutter.plugins.urllauncher.UrlLauncherPlugin` |
| 7 | `volume_controller` | `com.kurenai7968.volume_controller.VolumeControllerPlugin` |
| 8 | `webview_flutter_android` | `io.flutter.plugins.webviewflutter.WebViewFlutterPlugin` |

The registrant lives in the Android DEX, not in `flutter_assets` or `libapp.so`. This independently confirms why copying only the Flutter asset/native payload is incomplete.

## 6. Channel and PlatformView compatibility inventory

### Custom host MethodChannels observed

Original `MainActivity` registers:

- `id.exambro.cbt/gate`
- `id.exambro.cbt/app_detection`

These names are recorded only as compatibility inventory. Their security/integrity behavior is explicitly outside integration scope.

### Plugin channels observed in original DEX/AOT evidence

Representative plugin contracts observed:

- mobile_scanner:
  - `dev.steenbakker.mobile_scanner/scanner/method`
  - `dev.steenbakker.mobile_scanner/scanner/event`
  - `dev.steenbakker.mobile_scanner/scanner/deviceOrientation`
- audioplayers:
  - `xyz.luan/audioplayers`
  - `xyz.luan/audioplayers.global`
  - `xyz.luan/audioplayers.global/events`
  - per-player `xyz.luan/audioplayers/events/...` family
- permission_handler:
  - `flutter.baseflow.com/permissions/methods`
- shared preferences / URL launcher / WebView evidence includes:
  - `plugins.flutter.io/shared_preferences`
  - `plugins.flutter.io/url_launcher`
  - `plugins.flutter.io/webview`
  - modern Pigeon API namespaces under `dev.flutter.pigeon.shared_preferences_android.*`, `dev.flutter.pigeon.url_launcher_android.*`, and `dev.flutter.pigeon.webview_flutter_android.*`

Flutter system channels are also present as expected (`flutter/platform`, `flutter/navigation`, `flutter/textinput`, `flutter/platform_views`, etc.); they are engine/framework infrastructure rather than target-specific integration contracts.

### PlatformView evidence

The original `WebViewFlutterPlugin` explicitly registers one platform-view factory:

```text
plugins.flutter.io/webview
```

A scan of original JADX Java sources found no other explicit `registerViewFactory(...)` call from the registered target plugins.

**Implication:** the target WebView is owned by the WebView Flutter plugin lifecycle. ScreenPilot cannot assume a reference to that WebView merely because both applications use Flutter. Any future authorized source-based integration would need an explicit project-owned registration hook at the WebView/plugin factory lifecycle, consistent with `PHASE4_INTEGRATION_DESIGN.md`.

## 7. `libc++_shared.so` producer status

Target RAW native inventory contains its own `arm64-v8a/libc++_shared.so`, but Phase 4 rules prohibit copying it into ScreenPilot.

CI run #22 produced a ScreenPilot APK artifact, but the actual APK artifact bytes were not included in the evidence set used for this proof. Therefore:

- exact producer/version of the `libc++_shared.so` shipped in the current ScreenPilot APK remains **UNVERIFIED**;
- no `pickFirst`, overwrite, or duplicate-native workaround is allowed;
- this is **not a blocker for the current NO-GO decision**, because no target native library is being introduced;
- producer attribution must be completed before any future authorized design introduces a second native producer.

## 8. Phase 4.2A decision matrix

| Question | Result |
|---|---|
| Target Flutter generation identified? | **YES — high confidence: 3.38.3 generation** |
| Target Dart generation identified? | **YES — Dart 3.10.1 release generation** |
| Android embedding generation identified? | **YES — embedding v2** |
| Plugin registration inventory available? | **YES — 8 registered Android plugins** |
| Custom host channels identified for compatibility planning? | **YES — names inventoried, behavior out of scope** |
| WebView PlatformView contract identified? | **YES — `plugins.flutter.io/webview`** |
| Target `libapp.so` proven compatible with ScreenPilot Flutter 3.44.9 engine? | **NO** |
| Two opaque Flutter bundles safe to package together? | **NO** |
| Exact ScreenPilot final `libc++_shared.so` producer proven? | **NO — deferred; no second producer allowed** |

## 9. Final verdict

### Opaque in-process integration

**NO-GO.**

Do not transplant target `flutter_assets`, `libapp.so`, `libflutter.so`, DEX, or native libraries into ScreenPilot in an attempt to run the opaque target application in the existing Flutter 3.44.9 process.

### Current executable path

Retain **Option D**: ScreenPilot remains the known-green standalone application with its project-owned compatibility/test harness and existing CaptureProvider/Registry/Bridge pipeline.

### Future authorized in-process path

Retain **Option A** only: if authorized Flutter source/module becomes available, rebuild a single integrated Flutter application using one pinned Flutter/Dart toolchain, then re-run manifest/plugin/native/capture gates from `PHASE4_INTEGRATION_DESIGN.md`.

## 10. Exit condition

Phase 4.2A is complete because the compatibility gate has an evidence-backed negative answer for opaque bundle mixing. No production code or build configuration change is required or justified by this result.
