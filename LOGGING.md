# Logging with Timber

This project uses **Timber** for logging, which provides better features than Android's default `Log` class.

## Why Timber?

- **Automatic tagging**: No need to manually specify tags - Timber automatically uses the calling class name
- **Tree-based logging**: Different logging behavior for debug vs. release builds
- **Better crash reporting integration**: Easy to integrate with Firebase Crashlytics, Sentry, etc.
- **Cleaner API**: Simpler method calls without redundant tag parameters
- **Production-ready**: Automatically strips debug/verbose logs in release builds

## Setup

Timber is initialized in `DistroApp.kt`:
- **Debug builds**: All logs (VERBOSE, DEBUG, INFO, WARN, ERROR) are printed
- **Release builds**: Only WARN and ERROR logs are printed (performance optimization)

## Usage Examples

### Basic Logging

```kotlin
import timber.log.Timber

// Debug log
Timber.d("User clicked download button")

// Info log
Timber.i("Download started for version %s", versionName)

// Warning log
Timber.w("Cache size exceeds limit: %d MB", cacheSizeMB)

// Error log
Timber.e("Failed to parse APK metadata")
```

### Logging with Exceptions

```kotlin
try {
    // risky operation
} catch (e: Exception) {
    // Timber automatically logs the full stack trace
    Timber.e(e, "Download failed: ${e.message}")
}
```

### String Formatting

```kotlin
// Using string templates (Kotlin style)
Timber.d("Processing ${apps.size} applications")

// Using format args (more efficient, especially for release builds)
Timber.d("Download progress: %.1f%%", progress * 100)
```

### Conditional Logging

```kotlin
// For expensive log operations, wrap in BuildConfig.DEBUG check
if (BuildConfig.DEBUG) {
    val detailedInfo = computeExpensiveDebugInfo()
    Timber.v("Detailed state: $detailedInfo")
}
```

## Log Levels

Use appropriate log levels:

- **`Timber.v()`** (VERBOSE): Extremely detailed information, rarely needed
- **`Timber.d()`** (DEBUG): Detailed information for debugging, stripped in release
- **`Timber.i()`** (INFO): Important informational messages
- **`Timber.w()`** (WARNING): Warning messages for potentially problematic situations
- **`Timber.e()`** (ERROR): Error messages, should always be investigated

## Best Practices

1. **Don't log sensitive data**: Never log passwords, API keys, or personal information
2. **Use appropriate log levels**: Don't use ERROR for warnings or DEBUG for production events
3. **Keep messages concise**: Logs should be readable and searchable
4. **Include context**: Add relevant variables to help debugging
5. **Log state changes**: Log important lifecycle events and state transitions
6. **Don't over-log**: Excessive logging impacts performance

## Examples in the Codebase

### DownloadManager.kt
```kotlin
Timber.d("Download started: ${response.code}")
Timber.d("Download completed: ${contentLength / (1024 * 1024)}MB in ${totalTime}ms")
Timber.e(e, "Download failed: ${e.message}")
```

### AppViewModel.kt
```kotlin
Timber.d("Checking installation status for ${app.packageName}")
Timber.e("Download error: ${state.message}")
```

### ApkInstaller.kt
```kotlin
Timber.d("Installing APK: ${file.name} (${file.length() / 1024}KB)")
Timber.i("Installation intent launched successfully")
Timber.e(e, "Failed to install APK: ${file.name}")
```

## Integration with Crash Reporting

To send logs to crash reporting services (Firebase Crashlytics, Sentry, etc.), modify the `ReleaseTree` in `DistroApp.kt`:

```kotlin
private class ReleaseTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
            return
        }
        
        // Send to Firebase Crashlytics
        FirebaseCrashlytics.getInstance().log(message)
        
        // If there's an exception, report it
        if (t != null) {
            FirebaseCrashlytics.getInstance().recordException(t)
        }
    }
}
```

## Testing

When writing unit tests, you can plant a test tree:

```kotlin
@Before
fun setup() {
    Timber.plant(object : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            println("[$tag] $message")
        }
    })
}

@After
fun teardown() {
    Timber.uprootAll()
}
```

## Migration from android.util.Log

Replace:
- `Log.d(TAG, message)` → `Timber.d(message)`
- `Log.e(TAG, message, throwable)` → `Timber.e(throwable, message)`
- `Log.i(TAG, message)` → `Timber.i(message)`
- `Log.w(TAG, message)` → `Timber.w(message)`
- `Log.v(TAG, message)` → `Timber.v(message)`

No need to define `TAG` constants anymore!
