# PRE_CI_REPORT — Phase 1.5 Pre-CI Verification

**Status:** Audit filesystem + konfigurasi sebelum first GitHub Actions build.
**Tanggal:** 2026-08-08
**Scope:** Audit only. Tidak ada perubahan fitur, tidak ada build lokal, tidak ada push, tidak ada Phase 2.

---

## 1. Project Memory Files

| File | Status |
|---|---|
| `AGENTS.md` | Ada — dibaca. Source of truth rules dikonfirmasi. |
| `PROJECT_STATE.md` | Ada — dibaca. Fase saat ini: "Phase 1 complete — awaiting first CI build". |
| `DECISIONS.md` | Ada — dibaca. D001–D008 tercatat. |
| `TODO.md` | Ada — dibaca. CI checkpoint saat ini belum green. |

Aturan memory (read-before-work, update after milestone) dicatat dan akan diikuti untuk pekerjaan selanjutnya.

---

## 2. Audit Filesystem — `MergedProject/E-Ujian_RE_JADX`

**KESIMPULAN: masih ada, tetapi hanya stub kosong (0 file).**

- Lokasi: `MergedProject/E-Ujian_RE_JADX/`
- Isi (recursive):
  ```
  E-Ujian_RE_JADX\sources
  E-Ujian_RE_JADX\sources\com            (EMPTY DIR)
  E-Ujian_RE_JADX\sources\id
  E-Ujian_RE_JADX\sources\id\eujian
  E-Ujian_RE_JADX\sources\id\eujian\cbt
  E-Ujian_RE_JADX\sources\id\eujian\cbt\screenpilot   (EMPTY DIR)
  ```
- Total: **0 file, 6 direktori kosong, 0 byte.**
- **Fungsi aktual:** tidak ada. Ini sisa folder kosong (bukan material referensi).
- **Material referensi JADX asli tetap utuh di luar repo:**
  `C:\Users\Administrator\Downloads\AntiGravityIDE\E-Ujian_RE_JADX` = **3.913 file, ~37,2 MB** (tidak dipindah/diubah).

**Rekomendasi:** tidak dihapus sekarang (sesuai instruksi "jangan hapus dulu"); dicatat sebagai kandidat penghapusan karena tidak dipakai build dan kosong.

---

## 3. Audit Filesystem — androidTest & Lokasi Package Lama

- `androidTest` ada di:
  `app/src/androidTest/java/com/example/ExampleInstrumentedTest.kt`
- **Masalah:** path folder masih `com/example` (lama), sedangkan `package` di dalam file sudah `id.eujian.cbt.screenpilot`.
  - Mismatch folder ↔ package (tidak memengaruhi compile Gradle karena hanya test instrumented yang tidak dijalankan di CI `testDebugUnitTest`, tapi menyesatkan).
- Tidak ada folder package produksi `com.example` tersisa di `app/src/main` atau `app/src/test`.
  - `app/src/main/java` → 32 file (semua di `id.eujian.cbt.screenpilot.*`)
  - `app/src/test/java/id/eujian/cbt/screenpilot/` → 15 file (semua package baru)
  - `app/src/androidTest/java/com/example/` → 1 file (path lama, package baru)

---

## 4. Git Status

- **Bukan repository git** (`MergedProject` tidak punya `.git`).
- `git status` / `git diff` **tidak dapat dijalankan**.
- `DAFTAR file berubah`: tidak bisa dihitung via git. Perubahan Fase 1 terdokumentasi di `PHASE1_REPORT.md`; backup pre-fase ada di `MergedProject_backup_phase1`.

---

## 5. Grep / Static Checks

| Pemeriksaan | Hasil |
|---|---|
| `../../` di Gradle files (`settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `libs.versions.toml`, `gradle.properties`) | **PASS** — tidak ada |
| Produksi `com.example.*` type reference di `app/src/main` | **PASS** — tidak ada (hanya konstanta string `"com.example.action.*"` yang valid di `ScreenCaptureService.kt`) |
| `sourceSets` custom di `app/build.gradle.kts` | **PASS** — blok `sourceSets { main { .. } }` sudah dihapus; Gradle pakai konvensi default |
| `AndroidManifest.xml` | **PASS** — `app/src/main/AndroidManifest.xml` lokal dipakai, merujuk resource yang ada (`@xml/data_extraction_rules`, `@xml/backup_rules`, `@mipmap/ic_launcher`, `@string/app_name`, `@style/Theme.MyApplication`) |
| namespace | `id.eujian.cbt.screenpilot` |
| applicationId | `id.eujian.cbt.screenpilot` |
| minSdk / targetSdk / compileSdk | 28 / 35 / 35 |

---

## 6. Audit `.gitignore`

`MergedProject/.gitignore` **tidak ada sebelumnya** → **dibuat** dalam fase ini (satu-satunya perubahan file selain report).

Isi sesuai aturan minimum:

- Ignored: `.gradle/`, `build/`, `**/build/`, `.kotlin/`, `local.properties`, `*.apk`, `*.aab`, `*.ap_`, `*.dex`, keystore/signing (`*.keystore`, `*.jks`, `my-upload-key.*`), `*.properties` (secrets, dikecualikan khusus untuk wrapper), IDE/local (`/.idea`, `*.iml`, `.DS_Store`, `captures/`, `.externalNativeBuild/`, `.cxx/`).
- **Jangan di-ignore (harus di-commit):**
  - `!gradle/wrapper/gradle-wrapper.jar`
  - `!gradlew`
  - `!gradlew.bat`
  - `!gradle/libs.versions.toml`
  - `!gradle/wrapper/gradle-wrapper.properties` *(ditambahkan agar `*.properties` global tidak meng-ignore wrapper properties)*

**CATATAN:** negasi `!gradle/wrapper/gradle-wrapper.properties` bekerja karena parent `gradle/` tidak di-ignore secara global; file ini akan ter-commit.

---

## 7. Audit Workflow `.github/workflows/android-build.yml`

File ada (60 baris). Kompatibilitas:

| Kriteria | Requirement | Workflow | Kompatibel |
|---|---|---|---|
| JDK | 21 | `setup-java@v4`, temurin, `'21'` | ✅ |
| Gradle wrapper | 9.3.1 | `gradle/actions/setup-gradle@...` (v4.4.2, commit-pinned) | ✅ |
| AGP | 9.1.1 | via `libs.versions.toml` (`agp = "9.1.1"`) | ✅ |
| Kotlin | 2.2.10 | `kotlin = "2.2.10"` di toml | ✅ |
| compileSdk 35 | build-tools/platform 35 | Diinstal otomatis oleh AGP saat `compileDebugKotlin`/`assembleDebug` | ✅ (perlu setup SDK, lihat catatan) |
| Runner | Ubuntu | `ubuntu-latest` | ✅ (tidak ter-pin; baca catatan) |
| `testDebugUnitTest` | ya | Step "Run unit tests" → `:app:testDebugUnitTest --rerun-tasks` | ✅ |
| `assembleDebug` | ya | Step "Build debug APK" → `:app:assembleDebug --rerun-tasks` | ✅ |
| APK artifact upload | ya | `actions/upload-artifact@v4`, path `app/build/outputs/apk/debug/app-debug.apk`, `if-no-files-found: error` | ✅ |
| Test/lint report upload | ya | `actions/upload-artifact@v4`, `if: always()` | ✅ |

### Catatan risiko workflow (tidak diedit, hanya dilaporkan)
1. **`runs-on: ubuntu-latest`** tidak deterministic. Disarankan pin ke `ubuntu-24.04` di fase berikutnya (bukan sekarang — audit only).
2. **SDK tidak diinstal eksplisit.** Runner GitHub menyediakan Android SDK, tetapi versi/komponen bisa berubah. Risiko rendah karena AGP mendownload platform otomatis bila lisensi diterima; runner default biasanya sudah accept-license. Bila `lintDebug`/`assemble` butuh komponen tambahan, bisa gagal. Disarankan `android-actions/setup-android` atau `sdkmanager` install di fase berikutnya.
3. **`lintDebug` default `abortOnError=true`** — pada proyek hasil migrasi bisa memblokir build jika ada lint errors. Disarankan dipertimbangkan `lint { abortOnError = false }` atau step lint non-blocking.
4. **`--rerun-tasks`** di semua step membuat build lebih lambat (3x recompile). Untuk CI checkpoint pertama OK (memaksa kompilasi penuh deterministic), tetapi bisa dioptimalkan nanti.
5. **Trigger:** `workflow_dispatch` + `push: branches: [main]`. Bila repo baru diinisialisasi, pastikan branch default `main`.
6. **Reproducibility action:** `setup-gradle` di-pin ke commit SHA (baik). `checkout@v6` dan `upload-artifact@v4` memakai major version tag (acceptable).

---

## 8. Build Lokal

**TIDAK dijalankan** (sesuai instruksi; GitHub Actions adalah primary build environment).

---

## 9. Ringkasan Status Verifikasi

| Item | Status |
|---|---|
| Memory files dibaca | ✅ |
| `E-Ujian_RE_JADX` internal = stub kosong (0 file) | ⚠️ catatan, tidak dihapus |
| Reference JADX eksternal utuh (3.913 file / 37 MB) | ✅ |
| androidTest path lama `com/example` (package sudah baru) | ⚠️ kosmetik |
| Git repo | ❌ bukan git repo (belum `git init`) |
| `../../` di Gradle | ✅ bersih |
| `com.example.*` type di main | ✅ bersih |
| sourceSets default | ✅ |
| Manifest lokal + resource ada | ✅ |
| namespace/applicationId selaras | ✅ |
| `.gitignore` | ✅ dibuat & diverifikasi |
| Workflow kompatibel JDK21/Gradle9.3.1/AGP9.1.1/compileSdk35/Ubuntu/test/assemble/upload | ✅ (dengan catatan risiko) |

## 10. Blocker / Action Needed Sebelum Push & CI

1. **Inisialisasi git** di `MergedProject` (saat ini bukan repo). Commit baseline Fase 1 + `.gitignore` + memory files + report.
2. **Push** ke GitHub (branch `main`) — belum dilakukan.
3. (Opsional) Pin `ubuntu-24.04` + install SDK eksplisit + pertimbangkan lint non-blocking — direkomendasikan di fase berikutnya, bukan sekarang.
4. (Opsional) Bersihkan stub `MergedProject/E-Ujian_RE_JADX/` (kosong) dan rapikan path folder `app/src/androidTest/java/.../screenpilot/` — keputusan terpisah.

**Berhenti di sini. Tidak ada push. Tidak ada Phase 2.**
