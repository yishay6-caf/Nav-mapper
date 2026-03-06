package com.subnavar.app

import android.app.Application
import com.subnavar.app.util.FileLogger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SubNavApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FileLogger.init(this)
        FileLogger.log("APP", "Application onCreate")

        // Global uncaught exception handler
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            FileLogger.logError("CRASH", "Uncaught exception on thread ${thread.name}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
