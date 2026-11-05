# Gallery You 🖼️

<p align="center">
  <img src="app/src/main/res/drawable/ic_fg_you.xml" alt="App Icon" height="250"/>
</p>
<p align="center">
  <strong>A beautiful, feature-rich photo and video gallery for Android</strong><br>
  Built with Jetpack Compose and Material Design 3
</p>

<p align="center">
  <img src="assets/screenshot1.jpg" alt="Screenshot 1" width="200" style="border-radius:26px;"/>
  <img src="assets/screenshot2.jpg" alt="Screenshot 2" width="200" style="border-radius:26px;"/>
  <img src="assets/screenshot3.jpg" alt="Screenshot 3" width="200" style="border-radius:26px;"/>
  <img src="assets/screenshot4.jpg" alt="Screenshot 4" width="200" style="border-radius:26px;"/>
</p>

<p align="center">
    <a href="https://github.com/theovilardo/GalleryYou/releases/latest">
        <img src="https://img.shields.io/github/v/release/theovilardo/GalleryYou?include_prereleases&logo=github&style=for-the-badge&label=Latest%20Release" alt="Latest Release">
    </a>
    <a href="https://github.com/theovilardo/GalleryYou/releases">
        <img src="https://img.shields.io/github/downloads/theovilardo/GalleryYou/total?logo=github&style=for-the-badge" alt="Total Downloads">
    </a>
    <img src="https://img.shields.io/badge/Android-11%2B-green?style=for-the-badge&logo=android" alt="Android 11+">
    <img src="https://img.shields.io/badge/Kotlin-100%25-purple?style=for-the-badge&logo=kotlin" alt="Kotlin">
</p>

---

## ‼️ DISCLAIMER
- No fork of this project will receive support. If you use a fork, please ask the maintainer of that fork for support.

---

## ✨ Features

### 🎨 Modern UI/UX
- **Material You** - Dynamic color theming that seamlessly adapts to your wallpaper
- **Smooth Animations** - Fluid layout transitions, gesture controls, and micro-interactions
- **Customizable Interface** - Adjust grid column sizes, corner radius, and navigation bar styles
- **Dark/Light Theme** - Automatic theme switching matching your system settings
- **Adaptive Palette** - Dynamic color extraction from your photos and media covers

### 📸 Media Viewing & Playback
- **High-Performance Viewer** - Fast image viewing with gesture zoom and pan controls
- **Integrated Video Player** - Built-in video player supporting multiple formats and controls
- **Full EXIF Details** - Deep inspection of photo metadata (ISO, aperture, resolution, location, date)
- **High-Res Thumbnails** - Fast, responsive grid loading using Coil image caching

### 📂 Album & File Management
- **Smart Grouping** - Organize media by Albums, Dates, Folders, or Favorites
- **Multi-Selection Actions** - Batch share, delete, copy, move, and favorite items
- **Folder Filtering** - Exclude or include specific directories from your media index
- **Favorites System** - Quick access to your top photos and videos

### 🛡️ Privacy & Security
- **Onboarding Experience** - Smooth runtime permission management and privacy setup
- **Local Storage First** - Entirely offline media indexing without third-party server collection
- **App Icons Customization** - Switch between themed app icons (Default, Material You, Monochrome)

---

## 🛠️ Tech Stack

| Category | Technology |
|----------|------------|
| **Language** | [Kotlin](https://kotlinlang.org/) 100% |
| **UI Framework** | [Jetpack Compose](https://developer.android.com/jetpack/compose) |
| **Design System** | [Material Design 3](https://m3.material.io/) |
| **Architecture** | MVVM with StateFlow / SharedFlow |
| **Image Loading** | [Coil](https://coil-kt.github.io/coil/) |
| **Async Tasks** | Kotlin Coroutines & Flow |
| **Navigation** | Jetpack Compose Navigation |
| **Storage API** | MediaStore API & Scoped Storage |

---

## 📱 Requirements

- **Android 11** (API 30) or higher
- **4GB RAM** recommended for optimal gallery thumbnail rendering

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Ladybug | 2024.2.1 or newer
- Android SDK 30+
- JDK 17+

### Installation

1. **Clone the repository**
   ```sh
   git clone https://github.com/theovilardo/GalleryYou.git
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned `GalleryYou` directory

3. **Sync and Build**
   - Wait for Gradle to sync dependencies
   - Build the project (`Build → Make Project`)

4. **Run**
   - Connect an Android device or start an emulator
   - Click Run (▶️)

---

## ⬇️ Download

<p align="center">
  <a href="https://github.com/theovilardo/GalleryYou/releases/latest">
    <img src="https://raw.githubusercontent.com/Kunzisoft/Github-badge/main/get-it-on-github.png" alt="Get it on GitHub" height="60">
  </a>
</p>

---

## 📂 Project Structure

```
app/src/main/java/com/GalleryYou/Pixel/
├── About.kt              # App information, licenses, and credits
├── AppIcons.kt           # Dynamic app launcher icon switcher
├── Backdrop.kt           # Material backdrop & background components
├── MainActivity.kt       # Primary entry activity
├── MediaRepo.kt          # MediaStore fetcher and repository layer
├── Nav.kt                # Navigation setup and routes
├── Onboarding.kt         # First-launch onboarding and permission flows
├── Panels.kt             # Overlay sheets and bottom navigation panels
├── Screens.kt            # Primary app screens (Grid, Albums, Details)
├── Settings.kt           # User preferences and app settings
├── Theme.kt              # Material 3 colors, typography, and dynamic theming
├── Thumbs.kt             # Thumbnail rendering logic and image caching
└── Viewer.kt             # Full-screen photo viewer and video player
```

---

## 🤝 Contributing

Contributions are welcome! Feel free to open issues or submit Pull Requests.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under a Proprietary License - see the [LICENSE](LICENSE) file for details.

---

<p align="center">
  Made with ❤️ by <a href="https://github.com/theovilardo">theovilardo</a>
</p>