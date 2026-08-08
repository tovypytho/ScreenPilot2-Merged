# GIT_BASELINE_REPORT — Phase 1.7 Local Git Baseline

**Tanggal:** 2026-08-08
**Commit:** `8aabf1a` — "chore: establish standalone ScreenPilot baseline"
**Branch:** `main`

## 1. Pre-Commit Verifications

### 1.1 Environment
| Item | Status |
|---|---|
| Git version | 2.55.0.windows.3 |
| user.name | tovypytho |
| user.email | tommyirvan303@gmail.com |
| Working dir | `C:\Users\Administrator\Downloads\AntiGravityIDE\MergedProject` |

### 1.2 Repository Init
- `git init -b main` executed.
- No prior `.git` directory existed.
- Branch `main` confirmed as default.

### 1.3 .gitignore Verification (`git check-ignore`)

Ignored (correctly caught):
- `.gradle/`
- `build/` / `**/build/`
- `local.properties`
- `secrets.properties`
- `*.apk`, `*.aab`, `*.ap_`, `*.dex`
- `*.jks`, `*.keystore`
- `.idea/`, `*.iml`
- `captures/`, `.externalNativeBuild/`, `.cxx/`, `.kotlin/`

Trackable (correctly exempted / not ignored):
- `gradle.properties`
- `gradle/wrapper/gradle-wrapper.properties`
- `gradle/wrapper/gradle-wrapper.jar`
- `gradlew`, `gradlew.bat`
- `gradle/libs.versions.toml`

Result: **PASS** — no global `*.properties` ignore; wrapper artifacts kept.

## 2. Staged File Audit

Total staged files: **93** (13,317 insertions).

### 2.1 Secrets Check
- Searched for API key patterns, `secret`, `password`, `token`, `credential`, `private key`, `apikey`.
- Matches found are all **expected**:
  - `KeyStoreHelper.kt` — Java `javax.crypto.SecretKey` type references (no literal secrets).
  - Test files (`ExampleRobolectricTest.kt`, `KeyStoreHelperTest.kt`, `FloatingButtonVisualFeedbackTest.kt`, `ProviderGatewayTest.kt`, `StagedCaptureTest.kt`) — `SecretKeySpec` with dummy zero-byte keys (test-only).
  - `build.gradle.kts` / `libs.versions.toml` — references to `secrets-gradle-plugin` (Google's library, config only).
  - Documentation files — descriptive mentions of secrets/key storage.
- **No actual secret values** committed.

Result: **PASS** — no secrets.

### 2.2 local.properties
- `local.properties` NOT staged.
Result: **PASS**.

### 2.3 APK / AAB / Binary Build Outputs
- Searched staged filenames for `.apk`, `.aab`, `.ap_`, `.dex`, `.jks`, `.keystore`.
- None found.
Result: **PASS**.

### 2.4 Build Outputs / Generated Directories
- Searched staged filenames for `.gradle/`, `build/`, `app/build/`, `.kotlin/`, `.idea/`, `captures/`.
- None found.
Result: **PASS**.

### 2.5 JADX Evidence / Decompiled Material / .class Files
- Searched staged filenames for `E-Ujian_RE_JADX`, `jadx`, `decompil`, `.class`.
- None found.
- External evidence `AntiGravityIDE/E-Ujian_RE_JADX` (3,913 files / ~37 MB) remains untouched and outside this repository.
Result: **PASS**.

### 2.6 Unexpected Generated Files
- No `*.log`, no `captures/` outputs, no `.DS_Store`, no `metadata.xml` from build tools.
- `metadata.json` (project metadata, not build-generated) is staged — expected.
Result: **PASS**.

## 3. Commit Summary

```
[main (root-commit) 8aabf1a] chore: establish standalone ScreenPilot baseline
 93 files changed, 13317 insertions(+)
```

Staged file breakdown:
- `.github/workflows/android-build.yml`
- `.gitignore`
- `AGENTS.md`, `DECISIONS.md`, `FINAL_REPO_CHECK.md`, `LICENSE`, `PHASE1_REPORT.md`, `PRE_CI_REPORT.md`, `PROJECT_STATE.md`, `README.md`, `SCREENPILOT_ANDROID15_NOTIFICATION_FIX.md`, `SCREENPILOT_FINAL_EDIT_REPORT.md`, `SCREENPILOT_MULTISELECT_FEATURE_REPORT.md`, `TODO.md`, `VALIDATION_NOTES.md`
- `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`
- `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.jar`, `gradle/wrapper/gradle-wrapper.properties`
- `gradlew`, `gradlew.bat`
- `metadata.json`
- `app/build.gradle.kts`, `app/proguard-rules.pro`, `app/src/main/AndroidManifest.xml`
- `app/src/main/java/id/eujian/cbt/screenpilot/` — 32 Kotlin production files
- `app/src/main/res/` — drawables, mipmaps, values, xml (backup/data-extraction rules, launcher icons, theme)
- `app/src/test/java/id/eujian/cbt/screenpilot/` — 14 test Kotlin files + `screenshots/greeting.png`
- `app/src/androidTest/java/id/eujian/cbt/screenpilot/ExampleInstrumentedTest.kt`

## 4. Conclusion

All audit gates **PASS**. The standalone ScreenPilot baseline is committed to local `main`.

### Not yet done (pending explicit instruction / CI green):
- No GitHub remote added.
- No push performed.
- No local Android build.
- Phase 2 (CaptureProvider abstraction) not started.
