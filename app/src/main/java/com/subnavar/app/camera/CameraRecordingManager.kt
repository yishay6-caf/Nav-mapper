package com.subnavar.app.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.subnavar.app.util.FileLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraRecordingManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var imageCapture: ImageCapture? = null
    private var activeRecording: Recording? = null
    private var preview: Preview? = null

    data class RecordingState(
        val isRecording: Boolean = false,
        val isPaused: Boolean = false,
        val currentFilePath: String? = null,
        val durationMs: Long = 0,
        val segmentIndex: Int = 0
    )

    private var recordingState = RecordingState()

    fun getRecordingState(): RecordingState = recordingState

    /**
     * Bind camera preview and video capture to the lifecycle.
     * Returns the Preview use case for binding to a PreviewView.
     */
    fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        FileLogger.log("CAMERA_REC", "bindCamera requested")
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val provider = cameraProviderFuture.get()
                cameraProvider = provider

                // Preview
                preview = Preview.Builder()
                    .build()
                    .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                // Video capture with compressed quality
                val qualitySelector = QualitySelector.from(
                    Quality.HD,
                    FallbackStrategy.higherQualityOrLowerThan(Quality.SD)
                )
                val recorder = Recorder.Builder()
                    .setQualitySelector(qualitySelector)
                    .build()
                videoCapture = VideoCapture.withOutput(recorder)

                // ImageCapture for photos
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                // Select back camera
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // Unbind any existing use cases and bind new ones
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    videoCapture,
                    imageCapture
                )
                FileLogger.log("CAMERA_REC", "Camera bound successfully with preview + video + imageCapture")
            } catch (e: Exception) {
                FileLogger.logError("CAMERA_REC", "Failed to bind camera", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Start recording video to the mapping directory.
     */
    fun startRecording(buildingId: Long, floorId: Long) {
        FileLogger.log("CAMERA_REC", "startRecording: building=$buildingId, floor=$floorId")
        val capture = videoCapture ?: run {
            FileLogger.log("CAMERA_REC", "startRecording: videoCapture is null, cannot record")
            return
        }

        val mappingDir = getMappingVideoDir(buildingId, floorId)
        val segmentIndex = recordingState.segmentIndex
        val videoFile = File(mappingDir, "segment_${segmentIndex}_${System.currentTimeMillis()}.mp4")

        val outputOptions = FileOutputOptions.Builder(videoFile).build()

        val pendingRecording = capture.output
            .prepareRecording(context, outputOptions)

        // Apply audio if permission available (we won't require it)
        activeRecording = pendingRecording.start(ContextCompat.getMainExecutor(context)) { event ->
            when (event) {
                is VideoRecordEvent.Start -> {
                    FileLogger.log("CAMERA_REC", "Recording started: ${videoFile.absolutePath}")
                    recordingState = recordingState.copy(
                        isRecording = true,
                        isPaused = false,
                        currentFilePath = videoFile.absolutePath
                    )
                }
                is VideoRecordEvent.Finalize -> {
                    if (event.hasError()) {
                        FileLogger.log("CAMERA_REC", "Recording error: ${event.error}, cause: ${event.cause?.message}")
                    } else {
                        FileLogger.log("CAMERA_REC", "Recording saved: ${videoFile.absolutePath}, size=${videoFile.length()}")
                    }
                    recordingState = recordingState.copy(
                        isRecording = false,
                        isPaused = false,
                        segmentIndex = segmentIndex + 1
                    )
                }
                is VideoRecordEvent.Status -> {
                    val stats = event.recordingStats
                    recordingState = recordingState.copy(
                        durationMs = stats.recordedDurationNanos / 1_000_000
                    )
                }
                is VideoRecordEvent.Pause -> {
                    FileLogger.log("CAMERA_REC", "Recording paused")
                    recordingState = recordingState.copy(isPaused = true)
                }
                is VideoRecordEvent.Resume -> {
                    FileLogger.log("CAMERA_REC", "Recording resumed")
                    recordingState = recordingState.copy(isPaused = false)
                }
            }
        }
    }

    fun pauseRecording() {
        activeRecording?.pause()
    }

    fun resumeRecording() {
        activeRecording?.resume()
    }

    fun stopRecording() {
        FileLogger.log("CAMERA_REC", "stopRecording")
        activeRecording?.stop()
        activeRecording = null
    }

    /**
     * Bind camera with Preview + ImageAnalysis for navigation mode.
     * Captures frames and delivers them as Bitmaps via the onFrame callback.
     */
    fun bindCameraForAnalysis(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onFrame: (Bitmap) -> Unit
    ) {
        FileLogger.log("CAMERA_REC", "bindCameraForAnalysis requested")
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val provider = cameraProviderFuture.get()
                cameraProvider = provider

                // Preview
                preview = Preview.Builder()
                    .build()
                    .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                // ImageAnalysis for frame capture
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                    val bitmap = imageProxyToBitmap(imageProxy)
                    if (bitmap != null) {
                        onFrame(bitmap)
                    }
                    imageProxy.close()
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
                FileLogger.log("CAMERA_REC", "Camera bound for analysis with preview + imageAnalysis")
            } catch (e: Exception) {
                FileLogger.logError("CAMERA_REC", "Failed to bind camera for analysis", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
        return try {
            val planes = image.planes
            val yBuffer = planes[0].buffer
            val uBuffer = planes[1].buffer
            val vBuffer = planes[2].buffer

            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()

            val nv21 = ByteArray(ySize + uSize + vSize)
            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)

            val yuvImage = android.graphics.YuvImage(
                nv21,
                android.graphics.ImageFormat.NV21,
                image.width,
                image.height,
                null
            )
            val out = java.io.ByteArrayOutputStream()
            yuvImage.compressToJpeg(
                android.graphics.Rect(0, 0, image.width, image.height),
                80,
                out
            )
            val imageBytes = out.toByteArray()
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

            // Apply rotation if needed
            val rotationDegrees = image.imageInfo.rotationDegrees
            if (rotationDegrees != 0 && bitmap != null) {
                val matrix = Matrix()
                matrix.postRotate(rotationDegrees.toFloat())
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }
        } catch (e: Exception) {
            FileLogger.logError("CAMERA_REC", "imageProxyToBitmap failed", e)
            null
        }
    }

    /**
     * Capture a single photo from the camera.
     * Uses ImageCapture use case bound alongside preview and video.
     */
    fun capturePhoto(buildingId: Long, floorId: Long, onPhotoCaptured: (File?) -> Unit) {
        FileLogger.log("CAMERA_REC", "capturePhoto: building=$buildingId, floor=$floorId")
        val capture = imageCapture ?: run {
            FileLogger.log("CAMERA_REC", "capturePhoto: imageCapture is null, cannot capture")
            onPhotoCaptured(null)
            return
        }

        val photoDir = getPhotoDir(buildingId, floorId)
        val photoFile = File(photoDir, "photo_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    FileLogger.log("CAMERA_REC", "Photo saved: ${photoFile.absolutePath}, size=${photoFile.length()}")
                    onPhotoCaptured(photoFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    FileLogger.logError("CAMERA_REC", "Photo capture failed", exception)
                    onPhotoCaptured(null)
                }
            }
        )
    }

    fun getPhotoDir(buildingId: Long, floorId: Long): File {
        val dir = File(context.filesDir, "buildings/$buildingId/floors/$floorId/photos")
        dir.mkdirs()
        return dir
    }

    fun unbindCamera() {
        FileLogger.log("CAMERA_REC", "unbindCamera")
        stopRecording()
        cameraProvider?.unbindAll()
        cameraProvider = null
        preview = null
        videoCapture = null
        imageCapture = null
    }

    fun getMappingVideoDir(buildingId: Long, floorId: Long): File {
        val dir = File(context.filesDir, "buildings/$buildingId/floors/$floorId/mapping_video")
        dir.mkdirs()
        return dir
    }

    fun getRecordedSegments(buildingId: Long, floorId: Long): List<File> {
        val dir = getMappingVideoDir(buildingId, floorId)
        return dir.listFiles()
            ?.filter { it.extension == "mp4" }
            ?.sortedBy { it.name }
            ?: emptyList()
    }

    fun getTotalRecordedSize(buildingId: Long, floorId: Long): Long {
        return getRecordedSegments(buildingId, floorId).sumOf { it.length() }
    }
}
