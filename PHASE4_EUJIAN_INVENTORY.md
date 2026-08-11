# PHASE4_EUJIAN_INVENTORY.md

## Scope

Static inventory and compatibility gate for E-Ujian v3.1.2 material in `AntiGravityIDE.zip`, cross-checked against the supplied forensic comparison report.

This phase does **not** copy E-Ujian binaries/assets into `MergedProject`, does not alter security/integrity controls, and does not modify ScreenPilot production code.

## Executive conclusion

**Recommended canonical evidence source**

- Binary/provenance baseline: `AntiGravityIDE/eujian_apk_orisplitapk no merge/`
- Equivalent duplicate raw set: `AntiGravityIDE/eujian_apk/` (all six APK hashes match the no-merge set exactly)
- Readable original Android manifest/code:
  - nested raw Apktool output: `AntiGravityIDE/eujian_apk/eujian_apk, apktool.zip/decompiled_base/`
  - raw JADX: `AntiGravityIDE/baseapkjadxori/`
- Original native libraries: `split_config.arm64_v8a.apk/lib/arm64-v8a/`
- Original Flutter assets: `base.apk/assets/flutter_assets/`
- `ORI AntiSplit`: useful only as a merged navigation/code convenience baseline; not authoritative for signature/provenance.
- RE folders/APKs: diff targets, **not** canonical integration sources.
- `AntiGravityIDE/E-Ujian_RE_JADX/` is especially unsuitable as a pristine source because its manifest already contains ScreenPilot components.

## 1. Candidate source classification

| Candidate | Classification | Use |
|---|---|---|
| `eujian_apk_orisplitapk no merge/` | RAW Play/ADB split set | **Canonical binary provenance** |
| `eujian_apk/` | Same RAW split bytes + nested Apktool output | Canonical binary + readable convenience |
| `baseapkjadxori/` | JADX of RAW base | Original Android-code navigation |
| `E-Ujian_v3.1.2_antisplit_decompiled ori PS adb ...` | Rebuilt AntiSplit | Semantic code convenience only |
| `E-Ujian_v3.1.2_decompiled RE ...` | Modified/repacked RE | Diff target only |
| `E-Ujian_RE_JADX/` | Modified/merged artifact | **Do not use as pristine baseline** |
| `E-Ujian_v3.1.2 MOD.apk` | Modified artifact | Diff/runtime evidence only |
| `E-Ujian_v3.1.2_antisplit.apk` | Rebuilt merged APK | Convenience only |

### Why `E-Ujian_RE_JADX` is not canonical

Its manifest reports package `id.eujian.cbt.lynix`, compile SDK 34, and contains both:

- `id.exambro.cbt.MainActivity`
- `id.eujian.cbt.screenpilot.MainActivity`
- `id.eujian.cbt.screenpilot.service.ScreenCaptureService`

It therefore already contains ScreenPilot integration state and cannot represent pristine upstream E-Ujian.

## 2. RAW split hashes

The two raw folders (`eujian_apk/` and `eujian_apk_orisplitapk no merge/`) match byte-for-byte for all six APKs.

| APK | Size | SHA-256 |
|---|---:|---|
| `base.apk` | 5,458,192 | `6e228c07001789e109238b49cd23971c9ecf9597fa27efe0b40c2bba5c34d143` |
| `split_config.arm64_v8a.apk` | 23,024,602 | `31fd8288a9e22943a423b7c16a529f311009cac7ceaebe9e29a94a1149e760e1` |
| `split_config.en.apk` | 37,273 | `f207927546d26fe1d7ded10e6fd57e97972016747f1b5f0a491b2e98104ef103` |
| `split_config.in.apk` | 24,985 | `ebd58f3ff9261e570a8c650c4a7ed47ef810ac48563bb62d78e99e192b22449f` |
| `split_config.ms.apk` | 24,985 | `4d3b71cbdf9ea13d5a1f62c5c0298bbfe5ce216495f683a5b757501f00f2753a` |
| `split_config.xxhdpi.apk` | 86,702 | `8b83d0f200880fa805ace8a242256c12801e33bc5ed18729dbc5191a908d1fbe` |

## 3. Original package/build metadata

RAW base:

- package: `id.eujian.cbt`
- versionName: `3.1.2`
- versionCode: `30`
- minSdk: `24`
- targetSdk: `36`
- compileSdk: `36`
- application class: `com.pairip.application.Application`
- launcher: `id.exambro.cbt.MainActivity`

Current ScreenPilot host:

- applicationId/namespace: `id.eujian.cbt.screenpilot`
- minSdk: `28`
- targetSdk: `35`
- compileSdk: `36`
- Flutter test host: prebuilt AAR (`flutter_debug:1.0`, `flutter_release:1.0`)
- Flutter SDK recorded by generated module state: `3.44.9`

These are **not** drop-in-equivalent runtime configurations.

## 4. Original manifest component inventory

Important RAW components:

### Activities
- `id.exambro.cbt.MainActivity` — launcher
- `io.flutter.plugins.urllauncher.WebViewActivity`
- `com.google.android.gms.common.api.GoogleApiActivity`
- `com.pairip.licensecheck.LicenseActivity`

### Services
- CameraX metadata holder
- ML Kit component discovery
- Google DataTransport backend discovery
- Google DataTransport JobInfo scheduler

### Providers
- ML Kit init provider
- AndroidX Startup provider

### Receivers
- AndroidX Profile Installer receiver
- DataTransport alarm scheduler receiver

### Notable permissions
- `SYSTEM_ALERT_WINDOW`
- `ACCESS_NOTIFICATION_POLICY`
- location permissions
- Bluetooth permissions
- `CAMERA`
- `RECORD_AUDIO`
- `INTERNET`
- `ACCESS_NETWORK_STATE`
- `com.android.vending.CHECK_LICENSE`

The original manifest also contains Play split/stamp metadata. These are distribution/provenance properties and should not be treated as generic mergeable application configuration.

## 5. Flutter asset inventory

Original source: `base.apk/assets/flutter_assets/`

Total: **13 files, 1,391,277 bytes**

| Path | Size | SHA-256 |
|---|---:|---|
| `AssetManifest.bin` | 494 | `0d94b97a5fb0401f5dee81624a8c08bda73b2aa7a84113288bea776e75b88fd0` |
| `FontManifest.json` | 208 | `cd7e03645bc44b2dd47b7cb626f51c4ecbf55a197ab77241628b47ac165fbe21` |
| `NativeAssetsManifest.json` | 45 | `9548a31e4a048135c1d94f919328bfb62ae2c7bb3cab96557c7941daa97776cb` |
| `NOTICES.Z` | 105,385 | `bbb38d295bf66159243fc17a77dc6c4cf25d79e8ef0caef5795746dc5f6afddc` |
| `assets/images/auth-hero.jpg` | 37,017 | `75b419139e08b361f658b0c57339e8dbc962673847da080e00d2e924f291736b` |
| `assets/images/e-ujian-new.png` | 2,019 | `bf11276bf034d0a375d875fb21da25e14a49245a5c7b819b723458d964a6d05a` |
| `assets/images/icon.png` | 47,003 | `99da73f79c359290e60e1cac518f4d59d960bbc650bcf563356e149d9666d852` |
| `assets/images/setup_wizard_illustration.png` | 304,882 | `7c2ac387433c944085460298068f66fd0970872f67a575dcc85d3b4a998965ba` |
| `assets/sounds/notification.wav` | 592,852 | `f71c299ba06fbd65df767a29093562ca66d390d4861e04106d393e69521b3246` |
| `fonts/MaterialIcons-Regular.otf` | 5,056 | `128638e12392e787b6e5c04549940b0b865781f0d4295318deb48c9a2da3eeee` |
| `packages/cupertino_icons/assets/CupertinoIcons.ttf` | 257,628 | `67c44fe9183b002e79dde7f6977e2988661c9a3e4a3c5fce968787efdbed823c` |
| `shaders/ink_sparkle.frag` | 21,320 | `e5e61d50c9549bc35382bbbd1c9548d46c4fb9ddf40c3a698415ac7e005516c8` |
| `shaders/stretch_effect.frag` | 17,368 | `3707b8a75ae51668d7bcf4fe72a7b6ac95055555a8afc1723da74ff5eded1378` |

All 13 files were directly compared against `E-Ujian_RE_JADX/resources/assets/flutter_assets/` and match byte-for-byte.

## 6. Native ABI/library inventory

Original source: `split_config.arm64_v8a.apk/lib/arm64-v8a/`

Observed ABI matrix:

| ABI | Present |
|---|---|
| `arm64-v8a` | **Yes** |
| `armeabi-v7a` | No native split present in supplied RAW set |
| `x86` | No |
| `x86_64` | No |

Total native payload: **8 files, 22,954,080 bytes**.

| Library | Size | SHA-256 | ELF / Build ID |
|---|---:|---|---|
| `libapp.so` | 5,440,432 | `d569f7cf10700d5d90f706840aa1a5a95918f7053409f7fcf62dde8a5fc93f5b` | ELF64 AArch64 / `4f1bdaed500008c905f0f0e738b55d35` |
| `libbarhopper_v3.so` | 4,946,720 | `ec09597e9eb3aee0eb55ae3366da0d4ba416f5aa35a1af0b3811f50ff46743a1` | ELF64 AArch64 / `31faf13054616563bd553d8d71a935bb` |
| `libc++_shared.so` | 1,253,544 | `cd61762848882a16c8244c964a6f396c0caa0b440588a210ce9cc4ab0e6d9f0c` | ELF64 AArch64 / `7befe631535aa853c4f4ac1293e49dcea34c9b6e` |
| `libdatastore_shared_counter.so` | 7,112 | `d3e48717c9aa147e0ab21063ba0e8e0211cabf8bf40b222640829519edbf58e1` | ELF64 AArch64 / `17db37bd6770ac00dd2d1d2828839fd23a7959a3` |
| `libexambro_gate.so` | 160,912 | `9f8d252d606ff71cb62fdf76c3d1dd2fcf00f1b09f9408fe01997aa0dd5e47d2` | ELF64 AArch64 / `040f2d741849610befb22f73e0aa86807d6d73f5` |
| `libflutter.so` | 11,107,920 | `7a0047ec04562abf662cf2d1f36b5205d88de4b8c508b96e7637c8c5216d694c` | ELF64 AArch64 / `2bb32ee9cefc5994a5c518a602828f719dc7f816` |
| `libimage_processing_util_jni.so` | 32,544 | `f0e4611db0d6b68942d8ba6977519f3dd1cdbffa87b7fc16d8006696ba954af4` | ELF64 AArch64 / `3a1049a8184f35f8198fcaefd16045e2c7ef6abd` |
| `libsurface_util_jni.so` | 4,896 | `6cb2eac3c68e6a82ce2a5ea5aba86749eb8c609914392e125f6cdfbc208992f8` | ELF64 AArch64 / `80526e39fa0d29c1b7200db5e801b80fcb21d910` |

All eight were directly compared against `E-Ujian_RE_JADX/resources/lib/arm64-v8a/` and match byte-for-byte.

## 7. DT_NEEDED dependency map

- `libapp.so`
  - no DT_NEEDED entries observed
- `libbarhopper_v3.so`
  - `libjnigraphics.so`
  - `liblog.so`
  - `libdl.so`
  - `libm.so`
  - `libc.so`
- `libc++_shared.so`
  - `libc.so`
  - `libm.so`
  - `libdl.so`
- `libdatastore_shared_counter.so`
  - `libm.so`
  - `libdl.so`
  - `libc.so`
- `libexambro_gate.so`
  - `liblog.so`
  - `libm.so`
  - **`libc++_shared.so`**
  - `libdl.so`
  - `libc.so`
- `libflutter.so`
  - `libc.so`
  - `libdl.so`
  - `libm.so`
  - `libandroid.so`
  - `libEGL.so`
  - `libGLESv2.so`
  - `liblog.so`
  - `libjnigraphics.so`
- `libimage_processing_util_jni.so`
  - `liblog.so`
  - `libandroid.so`
  - `libjnigraphics.so`
  - `libm.so`
  - `libdl.so`
  - `libc.so`
- `libsurface_util_jni.so`
  - `libandroid.so`
  - `libm.so`
  - `libdl.so`
  - `libc.so`

## 8. Security/integrity observations — inventory only

The original application uses:

- `com.pairip.application.Application`
- `com.pairip.licensecheck.LicenseActivity`
- `com.android.vending.CHECK_LICENSE`
- custom `libexambro_gate.so`
- custom gate/app-detection MethodChannel/JNI boundary

The supplied RE report shows modifications around these boundaries in the RE artifact. Those modifications should be treated as forensic evidence, **not** as integration instructions.

No Phase 4 design should depend on disabling, spoofing, or neutralizing these controls.

## 9. Collision analysis against Phase 3

Current ScreenPilot already embeds its own Flutter module through:

- `flutter_debug:1.0`
- `flutter_release:1.0`
- Flutter SDK state `3.44.9`

Directly adding E-Ujian's prebuilt Flutter bundle into the same Android application creates high-risk namespace/packaging collisions.

### Definite/high-risk asset collisions

Both are Flutter application bundles and use canonical runtime paths such as:

- `assets/flutter_assets/AssetManifest.bin`
- `assets/flutter_assets/FontManifest.json`
- `assets/flutter_assets/NativeAssetsManifest.json`
- `assets/flutter_assets/NOTICES.Z`
- Material/Cupertino fonts
- Flutter shaders

The current test host also uses Material Icons and `cupertino_icons`.

**Conclusion:** do not copy E-Ujian `flutter_assets/` into `app/src/main/assets/flutter_assets/`.

### Native collisions / compatibility risks

High-risk names:

- `libflutter.so` — Flutter engine runtime; ScreenPilot's Flutter AAR already brings a Flutter engine.
- `libapp.so` — E-Ujian's Dart AOT application image; cannot safely be assumed compatible with ScreenPilot's Flutter 3.44.9 engine.
- `libc++_shared.so` — potential duplicate native runtime with transitive Android/Flutter dependencies.

Other E-Ujian native libraries have unique filenames, but several are coupled to Java/JNI/plugin code that is not present merely by copying `.so` files.

**Conclusion:** do not copy the entire E-Ujian `lib/arm64-v8a` directory into `app/src/main/jniLibs`.

## 10. Why “assets + native libs only” is not a runnable integration

The E-Ujian Flutter application is not self-contained in `flutter_assets + libapp.so`.

The RAW application also depends on Android-side components including:

- custom `id.exambro.cbt.MainActivity`
- Java/Kotlin MethodChannel handlers
- `GateJniBridge`
- PairIP Application/licensing components
- URL launcher Activity
- CameraX / ML Kit stack
- providers/services/permissions
- WebView/plugin registration
- custom JNI library linkage

Therefore copying only assets/native binaries can produce a packaging-successful APK that still fails at runtime due to plugin/channel/JNI/engine incompatibility.

## 11. Files that should NOT be copied in Phase 4.1

Do not place these in ScreenPilot production source yet:

- E-Ujian `assets/flutter_assets/**`
- `libapp.so`
- `libflutter.so`
- whole `lib/arm64-v8a/**`
- DEX files
- JADX `sources/**`
- Apktool `smali/**`
- RE `classes2.dex` / `com.dpmods.*`
- PairIP/gate modifications from the RE artifact
- RE manifest as a replacement/merge source

Also do not add `abiFilters` simply because the supplied native set is ARM64-only.

## 12. Recommended Phase 4.2 architecture

Preferred order:

1. **Keep Phase 3 GREEN baseline untouched.**
2. Treat RAW E-Ujian split set as read-only evidence.
3. If authorized source-level integration is available, integrate from source/build artifacts rather than transplanting an opaque second Flutter bundle.
4. If source-level integration is not available, use an **isolated compatibility prototype** rather than placing two Flutter applications into one engine/package.
5. Validate:
   - Flutter engine/AOT compatibility
   - plugin/MethodChannel inventory
   - JNI class/library contracts
   - ABI coverage
   - manifest component conflicts
   - lifecycle ownership
6. Preserve `CaptureProvider` as the ScreenPilot-side abstraction and test it against project-owned/authorized content.
7. Do not modify security/integrity controls as part of compatibility work.

### Recommended Phase 4.2 gate

Before any production copy, produce:

- Flutter engine compatibility determination
- complete plugin/channel inventory
- JNI method/class map for native libraries
- Android manifest merge matrix
- explicit decision: **replace** one Flutter app, **isolate** it, or use authorized source integration

## Phase 4.1 verdict

**Inventory gate: PASS. Direct-copy plan: REJECTED.**

The material is well enough characterized to proceed to a compatibility-design phase, but not to copy E-Ujian Flutter assets/native libraries into the current Phase-3 ScreenPilot APK.
