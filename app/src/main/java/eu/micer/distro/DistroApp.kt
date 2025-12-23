package eu.micer.distro

import android.app.Application
import android.util.Log
import timber.log.Timber

class DistroApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber
        if (BuildConfig.DEBUG) {
            // Debug tree with automatic tagging based on calling class
            Timber.plant(Timber.DebugTree())
        } else {
            // Production tree - only log warnings and errors to avoid performance impact
            Timber.plant(ReleaseTree())
        }
    }
    
    /**
     * Custom Timber tree for release builds
     * Logs only warnings and errors, and could be extended to send logs to crash reporting
     */
    private class ReleaseTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
                return
            }
            
            // TODO: Send to crash reporting service (Firebase Crashlytics, Sentry, etc.)
            // For now, just log to system
            if (t != null) {
                Log.println(priority, tag ?: "DistroApp", "$message\n${Log.getStackTraceString(t)}")
            } else {
                Log.println(priority, tag ?: "DistroApp", message)
            }
        }
    }
}
