# AuraScan

AuraScan is an Android app for scanning **QR codes** and **barcodes** with the device camera. It uses [CameraX](https://developer.android.com/training/camerax) for preview and capture and [ML Kit Barcode Scanning](https://developers.google.com/ml-kit/vision/barcode-scanning) for decoding. The UI is built with **Jetpack Compose** and **Material 3**.

## Features

- **Splash screen** → **Home** with separate entry points for QR and barcode scanning  
- **Live camera** with framing guidance, back navigation, and **torch (flash)** toggle  
- **QR mode**: optimized for QR codes  
- **Barcode mode**: common retail and linear formats (EAN, UPC, Code 128/39/93, Codabar, ITF, Data Matrix, PDF417, and related types as configured in code)  
- **Result screen**: shows decoded payload and format; **copy to clipboard** and **open in browser** when the content is a usable URL  
- **Edge-to-edge** layout on supported devices  
- **AdMob** hooks are present in the repo but **disabled** by default so the app builds without the Play Services Ads SDK (see [Optional: AdMob](#optional-admob))

## Requirements

| Item | Version / note |
|------|----------------|
| **minSdk** | 24 (Android 7.0) |
| **targetSdk** | 36 |
| **compileSdk** | 36 |
| **Java** | 11 |
| **Android Gradle Plugin** | 9.1.0 |
| **Kotlin** | 2.2.10 |

Use a recent **Android Studio** version that supports the AGP and Compose setup declared in the Gradle files.

## Permissions

- **Camera** — required for scanning (the app explains when permission is missing).  
- **Internet** — declared for general connectivity (e.g. opening links); optional ad-related permissions are commented out while AdMob is off.

## Build and run

1. Clone the repository and open the project root in Android Studio.  
2. Let Gradle sync finish.  
3. Run the **app** configuration on a physical device or emulator **with a camera** for a realistic scan experience.

From the project root:

```bash
./gradlew assembleDebug
```

Debug APK output: `app/build/outputs/apk/debug/`.

Unit and instrumented tests (if you use them):

```bash
./gradlew test
./gradlew connectedAndroidTest
```

## Project layout

| Path | Role |
|------|------|
| `app/src/main/java/com/example/aurascan/` | Compose UI, navigation, camera + ML Kit pipeline, theme |
| `app/src/main/AndroidManifest.xml` | App id `com.example.aurascan`, permissions, launcher activity |
| `app/build.gradle.kts` | Module config, dependencies |
| `gradle/libs.versions.toml` | Version catalog for libraries and plugins |

Main flow: `MainActivity` → `AuraScanNavHost` → splash, home, scanner (`ScannerScreen`), and result (`ScanResultScreen`).

## Optional: AdMob

Advertising is intentionally **turned off** in the current tree. To enable it again, follow the steps documented in `app/src/main/java/com/example/aurascan/AdMobBanner.kt` (uncomment the Play Services Ads dependency, restore manifest entries and application initialization, and wire your real AdMob app and ad unit IDs in `app/src/main/res/values/strings.xml` instead of the Google test placeholders).

## License

No license file is included in this repository. Add one (for example MIT or Apache-2.0) if you plan to publish or share the project.
