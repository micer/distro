package eu.micer.distro

import android.app.Application
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class DistroApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase Crashlytics
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCustomKey("build_type", BuildConfig.BUILD_TYPE)
        crashlytics.setCustomKey("version_name", BuildConfig.VERSION_NAME)
        crashlytics.setCustomKey("version_code", BuildConfig.VERSION_CODE)

        // Initialize Timber
        if (BuildConfig.DEBUG) {
            // Debug tree with automatic tagging based on calling class
            Timber.plant(Timber.DebugTree())
        } else {
            // Production tree - sends logs to Crashlytics
            Timber.plant(CrashlyticsTree())
        }
    }

    /**
     * Timber tree that sends warnings, errors, and exceptions to Firebase Crashlytics.
     * Also logs to system log as a fallback.
     */
    private class CrashlyticsTree : Timber.Tree() {
        private val crashlytics = FirebaseCrashlytics.getInstance()

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            // Skip verbose, debug, and info logs in production
            if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
                return
            }

            // Set custom key for the tag to help with filtering
            tag?.let { crashlytics.setCustomKey("log_tag", it) }

            // Send log to Crashlytics
            crashlytics.log("[${getPriorityLabel(priority)}] ${tag?.let { "[$it] " } ?: ""}$message")

            // Record exception if present
            t?.let { throwable ->
                crashlytics.recordException(throwable)
            }

            // Also log to system log as fallback
            if (t != null) {
                Log.println(priority, tag ?: "DistroApp", "$message\n${Log.getStackTraceString(t)}")
            } else {
                Log.println(priority, tag ?: "DistroApp", message)
            }
        }

        private fun getPriorityLabel(priority: Int): String {
            return when (priority) {
                Log.VERBOSE -> "V"
                Log.DEBUG -> "D"
                Log.INFO -> "I"
                Log.WARN -> "W"
                Log.ERROR -> "E"
                Log.ASSERT -> "A"
                else -> "?"
            }
        }
    }
}
