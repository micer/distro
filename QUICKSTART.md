# Quick Start Guide

## Installation

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17 or later
- Android device or emulator running Android 9.0 (API 28) or higher

### Setup

1. **Clone and Open**
   ```bash
   git clone <repo-url>
   cd Distro
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the Distro folder

3. **Sync Dependencies**
   - Android Studio will automatically prompt to sync Gradle
   - Click "Sync Now" or run: `./gradlew build`

4. **Run the App**
   - Connect an Android device or start an emulator
   - Click the "Run" button (or press Shift+F10)

## First Use

### Adding Your First App

1. Launch the app - you'll see an empty state
2. Tap the blue **+** button in the bottom right
3. Fill in the configuration:
   - **App Name**: Optional - leave empty to auto-fill from APK, or enter a custom name
   - **URL Pattern**: Enter your APK URL with `{version}` placeholder
   
   Example:
   ```
   https://cdn.example.com/apps/myapp-{version}.apk
   ```
4. Tap **Save**

### Downloading Your First APK

1. Tap on the app you just added to open its configuration screen
2. Enter the version name (e.g., `v1.2.3` or `1.0.0-beta1`)
3. Check the preview URL to ensure it looks correct
4. Tap **Download & Install**
5. Watch the progress bar fill up
6. When prompted, allow the installation

## Testing the App

### Setting Up Your Own Server

To test with your own APK files:

1. Upload your APK to a web server (Apache, Nginx, AWS S3, etc.)
2. Ensure the APK is publicly accessible via HTTPS
3. Create a naming convention with a version identifier
4. Configure the app with your URL pattern

Example with different naming schemes:
```
# Version at the end
https://cdn.example.com/apps/myapp-{version}.apk

# Version in path
https://cdn.example.com/apps/{version}/app.apk

# Build number
https://builds.mycompany.com/android/{version}/release.apk
```

## Development

### Project Structure Overview

```
Distro/
├── app/
│   ├── src/main/
│   │   ├── java/eu/micer/distro/
│   │   │   ├── data/           # Database and models
│   │   │   ├── ui/             # Compose UI screens
│   │   │   ├── utils/          # Helper utilities
│   │   │   ├── viewmodel/      # ViewModels
│   │   │   └── MainActivity.kt
│   │   ├── res/                # Resources (layouts, strings, etc.)
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml      # Dependency versions
├── build.gradle.kts
└── settings.gradle.kts
```

### Key Files to Understand

1. **MainActivity.kt**: Navigation setup and app entry point
2. **AppViewModel.kt**: Business logic, state management, and bulk operations
3. **MainScreen.kt**: Home screen showing app list with installation status
4. **AppConfigScreen.kt**: App management screen (add, edit, delete, import)
5. **AddEditAppScreen.kt**: Add/Edit individual app configuration
6. **DownloadManager.kt**: OkHttp-based download implementation with metadata parsing
7. **ApkInstaller.kt**: Android APK installation handling

### Building Different Variants

```bash
# Debug build (with debugging enabled)
./gradlew assembleDebug

# Release build (optimized, no debugging)
./gradlew assembleRelease

# Install debug on connected device
./gradlew installDebug

# Clean build
./gradlew clean build
```

### Common Development Tasks

**Adding a new dependency:**
1. Open `gradle/libs.versions.toml`
2. Add version in `[versions]`
3. Add library in `[libraries]`
4. Reference in `app/build.gradle.kts`

**Modifying the UI:**
- Compose screens are in `app/src/main/java/eu/micer/distro/ui/`
- Theme colors in `ui/theme/Theme.kt`
- Material 3 design system used throughout

**Database changes:**
1. Modify entity in `data/AppConfig.kt`
2. Increment database version in `AppDatabase.kt`
3. Add migration if needed

## Troubleshooting

### Build Issues

**Issue**: "SDK location not found"
- **Solution**: Create `local.properties` with `sdk.dir=/path/to/android/sdk`

**Issue**: Gradle sync fails
- **Solution**: Run `./gradlew clean` and sync again

### Runtime Issues

**Issue**: App crashes on launch
- **Solution**: Check Logcat for stack traces, ensure minimum Android version is met

**Issue**: Installation fails
- **Solution**: Enable "Install Unknown Apps" permission for the Distro app in Android settings

**Issue**: Download fails with 404
- **Solution**: Check the URL in the preview, verify the version name is correct

## Next Steps

- Read the full [README.md](README.md) for detailed documentation
- Check [CONFIGURATION_GUIDE.md](CONFIGURATION_GUIDE.md) for URL pattern examples
- Explore the code to understand the implementation
- Customize the UI theme to match your brand

## Need Help?

- Check Android Studio's Build Output for error details
- Use Logcat to debug runtime issues
- Review the source code comments for implementation details
