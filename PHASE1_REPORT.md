# PHASE1_REPORT — Baseline ScreenPilot Standalone

**Tanggal:** 2026-08-08
**Status:** Selesai (static verification pass)
**Tujuan:** Membuat baseline ScreenPilot yang self-contained dan compile-ready, tanpa fitur baru, tanpa menyentuh security/gate/licensing E-Ujian.

---

## 1. Ringkasan

Fase 1 menghilangkan seluruh ketergantungan build terhadap hasil decompile `../../E-Ujian_RE_JADX`, menjadikan `app/src/main/{java,res,AndroidManifest.xml}` sebagai satu-satunya sumber, dan memindahkan semua referensi tipe `com.example.*` di main source ke namespace resmi `id.eujian.cbt.screenpilot.*`.

Tidak ada file evidence E-Ujian yang dipindah/diubah. Tidak ada fitur baru (CaptureProvider, WebView test, Flutter bridge/host, GateProvider) yang ditambahkan.

---

## 2. Perubahan yang Dilakukan

### 2.1 Backup
- Dibuat `C:\Users\Administrator\Downloads\AntiGravityIDE\MergedProject_backup_phase1` berisi snapshot kondisi MergedProject sebelum perubahan.

### 2.2 `app/build.gradle.kts`
- **Dihapus seluruh blok `sourceSets { named("main") { ... } }`** yang sebelumnya merujuk:
  - `../../E-Ujian_RE_JADX/sources` (javaSrcDirs)
  - `../../E-Ujian_RE_JADX/resources/res` (resSrcDirs)
  - `../../E-Ujian_RE_JADX/resources/assets` (assetsSrcDirs)
  - `../../E-Ujian_RE_JADX/resources/lib` (jniLibsSrcDirs)
  - `../../E-Ujian_RE_JADX/resources/AndroidManifest.xml` (manifestSrcFile)
- Gradle kini kembali ke konvensi default: `src/main/java`, `src/main/res`, `src/main/AndroidManifest.xml`.
- **applicationId diubah** dari `com.aistudio.screenpilot.wpxvzl` → `id.eujian.cbt.screenpilot` (selaras namespace).
- `namespace` tetap `id.eujian.cbt.screenpilot`.
- versionCode/versionName diselaraskan menjadi `1` / `1.0`.
- Tidak ada perubahan pada signingConfig, compileOptions, buildTypes, atau dependencies yang aktif.

### 2.3 Main source — alihkan tipe `com.example.*`
Semua referensi tipe di `app/src/main` dialihkan ke namespace resmi:

- `service/ScreenCaptureService.kt`
  - `com.example.data.AnswerPopupStyle` → `id.eujian.cbt.screenpilot.data.AnswerPopupStyle`
  - `com.example.data.PopupBackgroundTheme`, `PopupTextColorMode`, `PopupStyle`, `PopupFontWeight` → `id.eujian.cbt.screenpilot.data.*`
  - `com.example.data.GallerySaveResult` → `id.eujian.cbt.screenpilot.data.GallerySaveResult`
  - `com.example.data.savePreparedJpegToGallery` → `id.eujian.cbt.screenpilot.data.savePreparedJpegToGallery`
  - `com.example.notification.EssayAnswerNotificationManager` → `id.eujian.cbt.screenpilot.notification.*`
  - `com.example.service.ResponseParser`, `FailoverDecision`, `FailoverAction`, `ScreenCaptureService` → `id.eujian.cbt.screenpilot.service.*`
- `MainActivity.kt`
  - `com.example.service.ResponseParser`, `FailoverDecision`, `FailoverAction`, `ScreenCaptureService` → `id.eujian.cbt.screenpilot.service.*`
  - `com.example.notification.EssayAnswerNotificationManager` → `id.eujian.cbt.screenpilot.notification.*`

Implementasi yang dipakai adalah yang sudah ada di production main source (`data/ImageUtils.kt`, `data/AnswerPopupStyle.kt`, dst). **Tidak ada mock/fake/stub yang disalin dari test ke main.**

Konstanta string action (`const val ACTION_START = "com.example.action.START"`, dst.) sengaja DIBIARKAN — nilainya string literal, bukan referensi tipe.

### 2.4 Test source — namespace diselaraskan
14 file test dipindah dari `app/src/test/java/com/example/**` ke `app/src/test/java/id/eujian/cbt/screenpilot/**` dan seluruh `package`/`import` `com.example.*` dialihkan ke `id.eujian.cbt.screenpilot.*` agar resolusi tipe konsisten dengan main source yang sudah berganti namespace.

### 2.5 androidTest
`app/src/androidTest/java/com/example/ExampleInstrumentedTest.kt` dipindah/update: package dan assertion `assertEquals("com.example", ...)` → `id.eujian.cbt.screenpilot`.

### 2.6 Manifest
- `app/src/main/AndroidManifest.xml` (milik ScreenPilot standalone) tetap dipakai. Tanpa override `manifest.srcFile`, AGP otomatis memakai file lokal ini.
- Manifest berisi hanya komponen ScreenPilot: `MainActivity` (LAUNCHER) + `service.ScreenCaptureService` (mediaProjection) + permission yang dipakai kode.

---

## 3. Static Verification (status)

| # | Verifikasi | Metode | Status |
|---|---|---|---|
| 1 | Tidak ada `../../` di build files (settings/build.gradle/gradle.properties/libs.versions.toml) | Grep | PASS |
| 2 | Tidak ada referensi tipe `com.example.*` di `app/src/main` | Grep (regex non-action) | PASS |
| 3 | Konstanta string `"com.example.action.*"` masih ada (valid, bukan tipe) | Grep | PASS (expected) |
| 4 | sourceSets hanya milik project (default `src/main/{java,res,AndroidManifest.xml}`) | Cek `app/build.gradle.kts` | PASS |
| 5 | Manifest lokal dipakai & ada | `Test-Path app/src/main/AndroidManifest.xml` | PASS |
| 6 | Resource yang dirujuk manifest (`@xml/data_extraction_rules`, `@xml/backup_rules`, `@mipmap/ic_launcher`, `@string/app_name`, `@style/Theme.MyApplication`) ada | Cek `app/src/main/res/**` | PASS |
| 7 | Dependency consistency: semua `libs.*` di `app/build.gradle.kts` ada di `gradle/libs.versions.toml` | Script | PASS |
| 8 | Evidence E-Ujian_RE_JADX tidak dipindah/diubah | Test-Path sumber asli | PASS |
| 9 | Tidak ada duplikat JADX di dalam MergedProject | Grep | PASS |
| 10 | `app/src/main/java` = 32 file ScreenPilot | Count | PASS |
| 11 | `app/src/test` = 15 file (namespace baru) | Count | PASS |
| 12 | `app/src/androidTest` = 1 file (namespace baru) | Count | PASS |
| 13 | Tidak ada folder `com/example` tersisa di `app/src` | Count | PASS |

---

## 4. Hasil Akhir Struktur

```
MergedProject/
├── settings.gradle.kts          (tidak diubah)
├── build.gradle.kts             (tidak diubah)
├── gradle.properties            (tidak diubah)
├── gradle/
│   ├── libs.versions.toml       (tidak diubah)
│   └── wrapper/                 (tidak diubah)
├── gradlew / gradlew.bat        (tidak diubah)
├── PHASE1_REPORT.md             (baru)
├── app/
│   ├── build.gradle.kts         (diubah: hapus sourceSets ../../, applicationId diselaraskan)
│   ├── proguard-rules.pro       (tidak diubah)
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml            (dipakai: manifest ScreenPilot standalone)
│       │   ├── java/id/eujian/cbt/screenpilot/
│       │   │   ├── MainActivity.kt            (diubah: com.example.* → namespace resmi)
│       │   │   ├── data/      (10 file, tidak diubah)
│       │   │   ├── service/   (17 file, ScreenCaptureService.kt diubah)
│       │   │   ├── notification/ (1 file, tidak diubah)
│       │   │   └── ui/theme/  (3 file, tidak diubah)
│       │   └── res/           (dipakai: satu-satunya res tree)
│       ├── test/java/id/eujian/cbt/screenpilot/   (14+1 file, dipindah + namespace baru)
│       └── androidTest/java/id/eujian/cbt/screenpilot/ (1 file, di-update)
```

---

## 5. Catatan / Risiko Tersisa

1. **Build lokal tidak dijalankan** (sesuai instruksi — tidak ada SDK lokal). Kelulusan kompilasi harus diverifikasi lewat GitHub Actions.
2. Test source yang dipindah namespace-nya **belum diverifikasi compile** — referensi silang antar test perlu dijamin konsisten; ini akan terbukti saat `testDebugUnitTest` di CI.
3. Konstanta string `com.example.action.*` dipertahankan. Jika di kemudian hari dipakai sebagai intent action, nilainya tetap valid secara runtime; tidak ada resolusi tipe.
4. `gradle/libs.versions.toml` masih memuat library yang dikomentari (Firebase/Camera/Coil/Retrofit). Tidak aktif, dibiarkan untuk minimasi perubahan.
5. Dependencies aktif yang dipakai main source: Compose BOM, activity-compose, material-icons, material3, ui/graphics/tooling, core-ktx, datastore, lifecycle, room, coroutines, okhttp — semuanya ada di toml.

---

## 6. Berhenti Setelah Fase 1

Fase 2 (bridge Flutter, WebView test, CaptureProvider, GateProvider) **tidak dikerjakan**. Menunggu review Phase 1 dan keberhasilan build melalui GitHub Actions.
