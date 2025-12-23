# Distro - APK Distribution App

A modern Android application for distributing APK files to internal testers. Built with Kotlin and Jetpack Compose.

## Features

- **Automatic APK Metadata Extraction**: App name, package name, and version info extracted automatically from downloaded APKs
- **Installation Status Tracking**: See which apps are installed and their versions at a glance
- **Flexible URL Patterns**: Configure APK download URLs with version placeholders
- **Smart App Management**: Optional app names - let the APK provide its own name
- **Easy Version Management**: Simply enter a version name to download and install
- **Modern UI**: Built with Jetpack Compose and Material 3
- **Local Storage**: All configurations stored locally using Room Database
- **Download Progress**: Real-time progress tracking during APK downloads
- **Auto-Installation**: Seamless APK installation flow using Android PackageInstaller

## Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Database**: Room
- **HTTP Client**: OkHttp
- **Architecture**: MVVM with Repository pattern
- **Min SDK**: 28 (Android 9.0 Pie)
- **Target SDK**: 35 (Android 15)

## Project Structure

```
app/src/main/java/eu/micer/distro/
├── MainActivity.kt              # Main entry point with navigation
├── data/
│   ├── AppConfig.kt            # Room entity for app configurations (includes APK metadata)
│   ├── AppDao.kt               # Database access object
│   ├── AppDatabase.kt          # Room database with migrations
│   └── AppRepository.kt        # Data repository
├── ui/
│   ├── MainScreen.kt           # App list screen with installation status
│   ├── AddEditAppScreen.kt     # Add/Edit app configuration
│   ├── DownloadScreen.kt       # Download and install screen
│   └── theme/                  # Material 3 theme files
├── viewmodel/
│   └── AppViewModel.kt         # Business logic and state management
└── utils/
    ├── DownloadManager.kt      # APK download handling with metadata parsing
    ├── ApkInstaller.kt         # APK installation handling
    ├── ApkParser.kt            # APK metadata extraction
    └── InstalledAppChecker.kt  # Installed app version checking
```

## How to Use

### 1. Add an App Configuration

On first launch, the app will be empty. Tap the **+** button to add a new app configuration.

**Example Configuration:**
- **App Name**: Leave empty (or enter a placeholder) - will be auto-filled from APK
- **URL Pattern**: `https://cdn.example.com/apps/myapp-{version}.apk`

The `{version}` placeholder will be replaced with the actual version you enter later.

**New!** The app name is now optional. When you download an APK for the first time, the app will automatically extract the official app name, package name, and version information from the APK file.

### 2. Download and Install

1. Tap on an app from the list
2. Enter the version name (e.g., `v1.2.3` or `1.0.0-beta1`)
3. Review the final URL preview
4. Tap **Download & Install**
5. Wait for the download to complete
6. Follow Android's installation prompts

### 3. View Installation Status

The main screen now displays comprehensive information for each app:
- **Installation status**: ✓ Installed or ○ Not installed
- **Current installed version**: Shows the version currently on your device
- **Last downloaded version**: Tracks the version you most recently downloaded
- **Package name**: Displays the application ID (e.g., `com.example.app`)

### 4. Manage Apps

- **Delete**: Tap the trash icon on any app card
- **Edit**: Long-press on an app to edit (coming soon)

## Building the Project

```bash
# Clone the repository
git clone <repo-url>
cd Distro

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test

# Install on connected device
./gradlew installDebug
```

## Permissions

The app requires the following permissions:

- **INTERNET**: To download APK files from remote servers
- **REQUEST_INSTALL_PACKAGES**: To install downloaded APK files

## Configuration

The app stores all configurations locally in a Room database. No backend or external configuration is required.

## Notes

- APK files are downloaded to the app's cache directory and cleaned up automatically
- APK metadata (package name, version, app name) is extracted automatically after download
- Installation status is checked in real-time from the device's PackageManager
- The app supports dynamic theming on Android 12+ devices
- Installation requires the user to enable "Install from Unknown Sources" for this app

## Recent Updates

See [NEW_FEATURES.md](NEW_FEATURES.md) for detailed information about the latest APK management features, including:
- Automatic metadata extraction from APKs
- Installation status tracking
- Optional app names with auto-fill
- Enhanced main screen display with version information

For technical details, see [TECHNICAL_CHANGES.md](TECHNICAL_CHANGES.md).

## Future Enhancements

Potential features for future versions:

- Edit existing app configurations
- Version history tracking
- QR code generation for easy sharing
- Multiple placeholder support (e.g., `{version}`, `{build}`, `{environment}`)
- APK signature verification
- Push notifications for new versions
- User authentication

## License

[Add your license here]

## Support

For issues or questions, please contact [your contact info]
