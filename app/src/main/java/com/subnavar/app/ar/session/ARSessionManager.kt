package com.subnavar.app.ar.session

import android.app.Activity
import android.content.Context
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.UnavailableException
import com.subnavar.app.util.FileLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ARSessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var session: Session? = null
    private var isSessionResumed = false

    data class TrackingInfo(
        val isTracking: Boolean,
        val pose: Pose?,
        val positionX: Float,
        val positionY: Float,
        val positionZ: Float,
        val orientationW: Float,
        val orientationX: Float,
        val orientationY: Float,
        val orientationZ: Float
    ) {
        companion object {
            val NOT_TRACKING = TrackingInfo(
                isTracking = false,
                pose = null,
                positionX = 0f, positionY = 0f, positionZ = 0f,
                orientationW = 1f, orientationX = 0f, orientationY = 0f, orientationZ = 0f
            )
        }
    }

    fun isARCoreSupported(): Boolean {
        return try {
            val availability = ArCoreApk.getInstance().checkAvailability(context)
            FileLogger.log("AR_SESSION", "isARCoreSupported: availability=$availability, isSupported=${availability.isSupported}")
            availability.isSupported
        } catch (e: Exception) {
            FileLogger.logError("AR_SESSION", "isARCoreSupported check failed", e)
            false
        }
    }

    fun requestInstall(activity: Activity): Boolean {
        return try {
            val installStatus = ArCoreApk.getInstance().requestInstall(activity, true)
            installStatus == ArCoreApk.InstallStatus.INSTALLED
        } catch (e: UnavailableException) {
            false
        }
    }

    fun createSession(activity: Activity): Boolean {
        FileLogger.log("AR_SESSION", "createSession")
        return try {
            if (session == null) {
                session = Session(activity)
                val config = Config(session)
                config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                config.focusMode = Config.FocusMode.AUTO
                config.lightEstimationMode = Config.LightEstimationMode.AMBIENT_INTENSITY
                session?.configure(config)
            }
            true
        } catch (e: UnavailableException) {
            false
        }
    }

    fun resumeSession() {
        FileLogger.log("AR_SESSION", "resumeSession")
        session?.let {
            if (!isSessionResumed) {
                it.resume()
                isSessionResumed = true
            }
        }
    }

    fun pauseSession() {
        FileLogger.log("AR_SESSION", "pauseSession")
        session?.let {
            if (isSessionResumed) {
                it.pause()
                isSessionResumed = false
            }
        }
    }

    fun destroySession() {
        FileLogger.log("AR_SESSION", "destroySession")
        session?.close()
        session = null
        isSessionResumed = false
    }

    fun getSession(): Session? = session

    fun getCurrentTrackingInfo(): TrackingInfo {
        val s = session ?: return TrackingInfo.NOT_TRACKING
        return try {
            val frame: Frame = s.update()
            val camera = frame.camera
            if (camera.trackingState == TrackingState.TRACKING) {
                val pose = camera.pose
                val translation = pose.translation
                val rotation = pose.rotationQuaternion
                TrackingInfo(
                    isTracking = true,
                    pose = pose,
                    positionX = translation[0],
                    positionY = translation[1],
                    positionZ = translation[2],
                    orientationW = rotation[3],
                    orientationX = rotation[0],
                    orientationY = rotation[1],
                    orientationZ = rotation[2]
                )
            } else {
                TrackingInfo.NOT_TRACKING
            }
        } catch (e: Exception) {
            TrackingInfo.NOT_TRACKING
        }
    }

    fun getDistanceBetweenPoses(pose1: Pose, pose2: Pose): Float {
        val dx = pose1.tx() - pose2.tx()
        val dy = pose1.ty() - pose2.ty()
        val dz = pose1.tz() - pose2.tz()
        return kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
    }
}
