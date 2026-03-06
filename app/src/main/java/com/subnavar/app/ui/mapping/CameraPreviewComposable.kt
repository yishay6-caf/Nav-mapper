package com.subnavar.app.ui.mapping

import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.subnavar.app.camera.CameraRecordingManager
import com.subnavar.app.util.FileLogger

/**
 * A Composable that displays the live CameraX camera preview.
 * When the composable enters composition, it binds the camera;
 * when it leaves, it unbinds.
 */
@Composable
fun CameraPreviewView(
    cameraRecordingManager: CameraRecordingManager,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(lifecycleOwner) {
        FileLogger.log("CAMERA_PREVIEW", "Binding camera to lifecycle")
        cameraRecordingManager.bindCamera(lifecycleOwner, previewView)
        onDispose {
            FileLogger.log("CAMERA_PREVIEW", "Unbinding camera from lifecycle")
            cameraRecordingManager.unbindCamera()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}
