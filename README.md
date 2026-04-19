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

<p align="center">
  <img src="app/src/main/res/drawable/aurascan_splash_logo.jpg" width="128" height="128" alt="AuraScan Logo">
</p>

## ✨ Features

- **🚀 Instant Scanning**: High-performance scanning using [ML Kit](https://developers.google.com/ml-kit/vision/barcode-scanning) and [CameraX](https://developer.android.com/training/camerax).
- **📱 Smart Actions**: AuraScan doesn't just scan; it understands.
  - **🌐 Web**: Open links directly in your browser.
  - **📶 Wi-Fi**: Connect to networks instantly from QR codes.
  - **💸 Payments**: Support for UPI apps for quick payments.
  - **👤 Contacts**: Save VCards or dial/SMS phone numbers.
  - **📅 Calendar**: Add events to your calendar from QR codes.
  - **📍 Maps**: Open locations in Google Maps or your preferred map app.
  - **💬 Social**: Quick links to WhatsApp and Instagram.
  - **🔵 Bluetooth**: Pair devices via Bluetooth QR codes.
- **🎨 Material 3 Design**: Beautiful, modern UI with support for dynamic colors.
- **🌗 Theme Support**: Choose between Light, Dark, or System Default modes.
- **🔦 Torch Toggle**: Built-in flashlight support for scanning in dark environments.
- **📋 Clipboard Integration**: Quickly copy scanned content with a single tap.

## 📸 Screenshots

*(Add your app screenshots here to make the README even more attractive!)*

| Home Screen | Scanner | Scan Result | Settings |
| :---: | :---: | :---: | :---: |
| ![Home](app/src/main/res/mipmap-xxxhdpi/ic_launcher.png) | ![Scanner](app/src/main/res/mipmap-xxxhdpi/ic_launcher.png) | ![Result](app/src/main/res/mipmap-xxxhdpi/ic_launcher.png) | ![Settings](app/src/main/res/mipmap-xxxhdpi/ic_launcher.png) |

## 🛠 Tech Stack

- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (100% Kotlin)
- **Architecture**: MVVM with Clean Architecture principles.
- **Scanning**: Google ML Kit Barcode Scanning.
- **Camera**: Jetpack CameraX.
- **Navigation**: Jetpack Compose Navigation.
- **Design**: Material 3.

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug or newer.
- Android SDK 24 (Android 7.0) or higher.

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/AuraScan.git
   ```
2. Open the project in Android Studio.
3. Sync Gradle and run the app on your device.

## 📦 Project Structure

- `app/src/main/java/com/example/aurascan/`: Main source code.
- `ui/`: Theme and common UI components.
- `MainActivity.kt`: Entry point and Navigation Host.
- `ScannerScreen.kt`: Camera implementation.
- `ScanResultScreen.kt`: Result handling and smart actions.

## 🤝 Contributing

Contributions are welcome! If you have any ideas, suggestions, or bug reports, please open an issue or submit a pull request.

## 📄 License

AuraScan is available under the MIT License. See the [LICENSE](LICENSE) file for more info.

---

<p align="center">Made with ❤️ for the Android Community</p>
