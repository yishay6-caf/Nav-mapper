package com.subnavar.app.data.local.file

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.subnavar.app.util.FileLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val baseDir: File
        get() = File(context.filesDir, "buildings").also { it.mkdirs() }

    fun getBuildingDir(buildingId: Long): File =
        File(baseDir, buildingId.toString()).also { it.mkdirs() }

    fun getFloorDir(buildingId: Long, floorId: Long): File =
        File(getBuildingDir(buildingId), "floors/$floorId").also { it.mkdirs() }

    fun getWaypointDir(buildingId: Long, floorId: Long, waypointId: Long): File =
        File(getFloorDir(buildingId, floorId), "waypoints/$waypointId").also { it.mkdirs() }

    suspend fun importFloorPlan(buildingId: Long, floorId: Long, uri: Uri): String =
        withContext(Dispatchers.IO) {
            FileLogger.log("FILE_STORAGE", "importFloorPlan: buildingId=$buildingId, floorId=$floorId, uri=$uri")
            val floorDir = getFloorDir(buildingId, floorId)
            FileLogger.log("FILE_STORAGE", "importFloorPlan: floorDir=${floorDir.absolutePath}, exists=${floorDir.exists()}")
            val contentType = context.contentResolver.getType(uri) ?: ""
            FileLogger.log("FILE_STORAGE", "importFloorPlan: contentType=$contentType")
            val targetFile = if (contentType.contains("pdf")) {
                FileLogger.log("FILE_STORAGE", "importFloorPlan: converting PDF to image")
                convertPdfToImage(uri, floorDir)
            } else {
                FileLogger.log("FILE_STORAGE", "importFloorPlan: copying image file")
                copyImageFile(uri, floorDir)
            }
            FileLogger.log("FILE_STORAGE", "importFloorPlan: result=${targetFile.absolutePath}, size=${targetFile.length()}")
            targetFile.absolutePath
        }

    private fun convertPdfToImage(uri: Uri, floorDir: File): File {
        FileLogger.log("FILE_STORAGE", "convertPdfToImage: start")
        val pdfFile = File(floorDir, "plan_temp.pdf")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(pdfFile).use { output ->
                input.copyTo(output)
            }
        }

        val outputFile = File(floorDir, "plan.jpg")
        val descriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(descriptor)
        val page = renderer.openPage(0)

        val scale = 3
        val bitmap = Bitmap.createBitmap(
            page.width * scale,
            page.height * scale,
            Bitmap.Config.ARGB_8888
        )
        bitmap.eraseColor(android.graphics.Color.WHITE)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        renderer.close()
        descriptor.close()

        FileOutputStream(outputFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        bitmap.recycle()
        pdfFile.delete()

        return outputFile
    }

    private fun copyImageFile(uri: Uri, floorDir: File): File {
        FileLogger.log("FILE_STORAGE", "copyImageFile: start")
        val outputFile = File(floorDir, "plan.jpg")
        val inputStream = context.contentResolver.openInputStream(uri)
        FileLogger.log("FILE_STORAGE", "copyImageFile: inputStream=${inputStream != null}")
        inputStream?.use { input ->
            FileOutputStream(outputFile).use { output ->
                val bytes = input.copyTo(output)
                FileLogger.log("FILE_STORAGE", "copyImageFile: copied $bytes bytes")
            }
        }
        FileLogger.log("FILE_STORAGE", "copyImageFile: outputFile=${outputFile.absolutePath}, size=${outputFile.length()}")
        return outputFile
    }

    suspend fun saveWaypointPhoto(
        buildingId: Long,
        floorId: Long,
        waypointId: Long,
        bitmap: Bitmap,
        angle: Int
    ): String = withContext(Dispatchers.IO) {
        val dir = getWaypointDir(buildingId, floorId, waypointId)
        val file = File(dir, "photo_$angle.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        file.absolutePath
    }

    suspend fun saveThumbnail(
        buildingId: Long,
        floorId: Long,
        waypointId: Long,
        bitmap: Bitmap
    ): String = withContext(Dispatchers.IO) {
        val dir = getWaypointDir(buildingId, floorId, waypointId)
        val file = File(dir, "thumbnail.jpg")
        val scaled = Bitmap.createScaledBitmap(bitmap, 200, 150, true)
        FileOutputStream(file).use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, 70, out)
        }
        scaled.recycle()
        file.absolutePath
    }

    fun loadBitmap(path: String): Bitmap? {
        val file = File(path)
        return if (file.exists()) BitmapFactory.decodeFile(path) else null
    }

    suspend fun exportBuildingData(buildingId: Long, outputUri: Uri): Unit =
        withContext(Dispatchers.IO) {
            val buildingDir = getBuildingDir(buildingId)
            val outputStream = context.contentResolver.openOutputStream(outputUri)
                ?: return@withContext
            outputStream.use { os ->
                ZipOutputStream(os).use { zip ->
                    zipDirectory(buildingDir, buildingDir.name, zip)
                }
            }
        }

    suspend fun importBuildingData(inputUri: Uri, buildingId: Long): Unit =
        withContext(Dispatchers.IO) {
            val buildingDir = getBuildingDir(buildingId)
            val inputStream = context.contentResolver.openInputStream(inputUri)
                ?: return@withContext
            inputStream.use { iStream ->
                ZipInputStream(iStream).use { zip ->
                    var entry: ZipEntry? = zip.nextEntry
                    while (entry != null) {
                        val file = File(buildingDir, entry.name)
                        if (entry.isDirectory) {
                            file.mkdirs()
                        } else {
                            file.parentFile?.mkdirs()
                            FileOutputStream(file).use { out ->
                                zip.copyTo(out)
                            }
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            }
        }

    fun deleteBuildingData(buildingId: Long) {
        getBuildingDir(buildingId).deleteRecursively()
    }

    private fun zipDirectory(dir: File, baseName: String, zip: ZipOutputStream) {
        dir.listFiles()?.forEach { file ->
            val entryName = "$baseName/${file.name}"
            if (file.isDirectory) {
                zipDirectory(file, entryName, zip)
            } else {
                zip.putNextEntry(ZipEntry(entryName))
                file.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
    }
}
