# Development Notes

## Architecture Overview

### MVVM Pattern
The app follows the Model-View-ViewModel (MVVM) architecture pattern:
- **Model**: Room database entities (`AppConfig`)
- **View**: Jetpack Compose screens (`MainScreen`, `AddEditAppScreen`, `AppConfigScreen`)
- **ViewModel**: `AppViewModel` manages state and business logic

### Data Flow
```
User Action → Compose UI → ViewModel → Repository → Room Database
                    ↑           ↓
                    ←  StateFlow ←
```

### Key Technologies

#### Jetpack Compose
- Modern declarative UI framework
- Material 3 design components
- Composition-based architecture

#### Room Database
- Local data persistence
- Type-safe SQL queries
- Automatic schema generation

#### OkHttp
- HTTP client for downloads
- Streaming download support with no buffering delays
- Progress tracking capability

## Implementation Details

### Download Flow

1. **User Input**: Version name entered in `AppConfigScreen`
2. **URL Generation**: `{version}` replaced in pattern
3. **Download**: `DownloadManager` streams the APK with progress updates using OkHttp
4. **State Updates**: `DownloadState` flows from ViewModel to UI
5. **Installation**: `ApkInstaller` triggers Android's package installer

### Database Schema

```kotlin
@Entity(tableName = "app_configs")
data class AppConfig(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val urlPattern: String
)
```

### State Management

The app uses Kotlin Flows for reactive state management:
- `StateFlow<List<AppConfig>>` for app list
- `StateFlow<DownloadState>` for download progress
- `collectAsState()` in Compose for UI updates

## Future Enhancement Ideas

### Short Term
1. **Edit App**: Add ability to edit existing app configurations
2. **Validation**: Add URL validation before saving
3. **Error Retry**: Allow users to retry failed downloads
4. **Clear Cache**: Button to clear downloaded APK files

### Medium Term
1. **Version History**: Track previously downloaded versions
2. **Search/Filter**: Search through app list
3. **Sorting**: Sort apps by name or date added
4. **Export/Import**: Share configurations via JSON

### Long Term
1. **Multi-Placeholder**: Support `{version}`, `{build}`, `{flavor}` patterns
2. **QR Code**: Generate QR codes for sharing download links
3. **Push Notifications**: Notify when new versions are available
4. **Authentication**: Add login/password protection
5. **Cloud Sync**: Sync configurations across devices
6. **APK Verification**: Check APK signatures before installation

## Testing Strategy

### Unit Tests
- Repository tests for database operations
- ViewModel tests for business logic
- Utility tests for URL pattern replacement

### Integration Tests
- Database migration tests
- Download flow end-to-end tests

### UI Tests
- Compose UI tests using ComposeTestRule
- Navigation flow tests
- User interaction tests

## Performance Considerations

### Current Implementation
- APKs downloaded to cache directory (auto-cleaned by Android)
- Database operations run on background thread via coroutines
- UI updates only when state changes (Compose recomposition)

### Optimization Opportunities
1. **Download Resume**: Support resuming interrupted downloads
2. **Parallel Downloads**: Allow multiple concurrent downloads
3. **Compression**: Support downloading compressed APKs
4. **Caching**: Cache metadata about available versions

## Security Notes

### Current Security
- HTTPS support for encrypted downloads
- FileProvider for secure file sharing
- App permissions limited to Internet and Install Packages

### Security Enhancements to Consider
1. **APK Signature Verification**: Verify APK signature before installation
2. **Certificate Pinning**: Pin server certificates to prevent MITM attacks
3. **Encrypted Storage**: Encrypt sensitive configuration data
4. **Access Control**: Add PIN/biometric authentication

## Known Limitations

1. **Single Placeholder**: Only supports `{version}` placeholder
2. **No History**: Doesn't track downloaded versions
3. **No Validation**: Doesn't verify APK before installation
4. **Manual Version Entry**: User must know version names
5. **No Authentication**: No login or user management

## Code Style Guidelines

### Kotlin
- Use Kotlin idioms (data classes, sealed classes, etc.)
- Prefer immutability (val over var)
- Use coroutines for async operations
- Follow official Kotlin style guide

### Compose
- Keep Composables small and focused
- Extract reusable components
- Use remember and LaunchedEffect appropriately
- Follow Material Design guidelines

### Architecture
- Keep business logic in ViewModel
- Use Repository pattern for data access
- Separate concerns (UI, business logic, data)
- Use dependency injection where appropriate

## Build Configuration

### Gradle Versions
- AGP: 8.13.2
- Kotlin: 2.2.21
- Compose BOM: 2025.12.01
- Room: 2.8.4
- OkHttp: 5.3.2

### Build Variants
- **Debug**: Development build with debugging enabled
- **Release**: Production build with ProGuard disabled (can be enabled)

### Signing
Release builds require signing configuration. Add to `app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("path/to/keystore.jks")
            storePassword = "store_password"
            keyAlias = "key_alias"
            keyPassword = "key_password"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

## Debugging Tips

### Common Issues

**Downloads fail silently**
- Check Logcat for exceptions
- Verify URL is correct and accessible
- Ensure INTERNET permission is granted

**Installation doesn't start**
- Check REQUEST_INSTALL_PACKAGES permission
- Enable "Install Unknown Apps" in Android settings
- Verify FileProvider is configured correctly

**Database errors**
- Delete app data and reinstall
- Check schema version and migrations
- Verify DAO queries are correct

### Useful Logcat Filters
```
# All app logs
adb logcat -s Distro

# Download manager logs
adb logcat | grep DownloadManager

# Room database logs
adb logcat | grep RoomDatabase
```

## Maintenance

### Dependency Updates
Check for updates regularly:
```bash
./gradlew dependencyUpdates
```

### Database Migrations
When modifying database schema:
1. Update entity class
2. Increment version in `@Database` annotation
3. Add migration in `AppDatabase.kt`

Example migration:
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE app_configs ADD COLUMN description TEXT")
    }
}
```

## License and Credits

This project uses the following open source libraries:
- Jetpack Compose - Apache 2.0
- Room Database - Apache 2.0
- OkHttp - Apache 2.0
- Timber - Apache 2.0
- Kotlin Coroutines - Apache 2.0

All dependencies and their licenses are listed in the project's dependency tree.
