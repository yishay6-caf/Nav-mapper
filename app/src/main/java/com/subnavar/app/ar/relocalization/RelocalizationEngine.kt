package com.subnavar.app.ar.relocalization

import android.graphics.Bitmap
import com.subnavar.app.domain.model.Waypoint
import com.subnavar.app.domain.repository.BuildingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Re-localization engine that uses ORB feature matching to identify
 * the user's current position by comparing the camera feed against
 * stored waypoint images.
 *
 * Flow:
 * 1. User opens AR navigation mode
 * 2. Camera frames are captured periodically
 * 3. Features are extracted from each frame
 * 4. Features are matched against stored waypoint features
 * 5. Best matching waypoint determines user's approximate position
 * 6. AR overlay is rendered based on this position
 */
@Singleton
class RelocalizationEngine @Inject constructor(
    private val orbFeatureManager: ORBFeatureManager,
    private val repository: BuildingRepository
) {
    data class LocalizationResult(
        val waypoint: Waypoint,
        val confidence: Float,
        val matchCount: Int
    )

    private var cachedFeatures: Map<Long, List<ORBFeatureManager.FeaturePoint>> = emptyMap()
    private var cachedBuildingId: Long = -1
    private var cachedFloorId: Long = -1

    /**
     * Pre-load all waypoint features for a specific floor.
     * Call this when the user selects a floor for navigation.
     */
    suspend fun loadFloorFeatures(buildingId: Long, floorId: Long) = withContext(Dispatchers.IO) {
        if (buildingId == cachedBuildingId && floorId == cachedFloorId) return@withContext

        val waypoints = repository.getWaypointsByFloorSync(floorId)
        val featureMap = mutableMapOf<Long, List<ORBFeatureManager.FeaturePoint>>()

        for (waypoint in waypoints) {
            val featurePath = waypoint.featureFilePath
            if (featurePath != null) {
                val features = orbFeatureManager.loadFeatures(featurePath)
                if (features.isNotEmpty()) {
                    featureMap[waypoint.id] = features
                }
            }
        }

        cachedFeatures = featureMap
        cachedBuildingId = buildingId
        cachedFloorId = floorId
    }

    /**
     * Attempt to localize using the current camera frame.
     * Returns the best matching waypoint or null if no match found.
     */
    suspend fun localize(cameraFrame: Bitmap): LocalizationResult? {
        if (cachedFeatures.isEmpty()) return null

        val currentFeatures = orbFeatureManager.extractFeatures(cameraFrame)
        if (currentFeatures.isEmpty()) return null

        val matchResult = orbFeatureManager.findBestMatch(currentFeatures, cachedFeatures)
            ?: return null

        val waypoint = repository.getWaypointById(matchResult.waypointId)
            ?: return null

        return LocalizationResult(
            waypoint = waypoint,
            confidence = matchResult.confidence,
            matchCount = matchResult.matchCount
        )
    }

    /**
     * Extract and save features for a waypoint photo during mapping.
     */
    suspend fun extractAndSaveFeatures(
        bitmap: Bitmap,
        outputPath: String
    ): Int {
        val features = orbFeatureManager.extractFeatures(bitmap)
        if (features.isNotEmpty()) {
            orbFeatureManager.saveFeatures(features, outputPath)
        }
        return features.size
    }

    /**
     * Clear cached features to free memory.
     */
    fun clearCache() {
        cachedFeatures = emptyMap()
        cachedBuildingId = -1
        cachedFloorId = -1
    }
}
