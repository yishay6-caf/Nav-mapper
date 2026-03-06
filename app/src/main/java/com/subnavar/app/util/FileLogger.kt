package com.subnavar.app.util

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileLogger {

    private var logFile: File? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    fun init(context: Context) {
        val logsDir = File(context.getExternalFilesDir(null), "logs")
        logsDir.mkdirs()
        logFile = File(logsDir, "lot25_debug.log")
        log("INIT", "Logger initialized. Log file: ${logFile?.absolutePath}")
        log("INIT", "Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
        log("INIT", "Android: ${android.os.Build.VERSION.RELEASE} (SDK ${android.os.Build.VERSION.SDK_INT})")
    }

    fun log(tag: String, message: String) {
        val timestamp = dateFormat.format(Date())
        val line = "$timestamp [$tag] $message"
        android.util.Log.d("Lot25Log", line)
        try {
            logFile?.let { file ->
                FileWriter(file, true).use { writer ->
                    writer.appendLine(line)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("Lot25Log", "Failed to write log: ${e.message}")
        }
    }

    fun logError(tag: String, message: String, throwable: Throwable) {
        log(tag, "$message: ${throwable.javaClass.simpleName} - ${throwable.message}")
        try {
            logFile?.let { file ->
                FileWriter(file, true).use { writer ->
                    val pw = PrintWriter(writer)
                    throwable.printStackTrace(pw)
                    pw.flush()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("Lot25Log", "Failed to write error log: ${e.message}")
        }
    }

    fun getLogFilePath(): String {
        return logFile?.absolutePath ?: "Logger not initialized"
    }
}
