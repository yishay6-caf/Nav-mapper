package com.subnavar.app.ui.mapping

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnavar.app.ar.session.ARSessionManager
import com.subnavar.app.data.local.file.FileStorageManager
import com.subnavar.app.domain.model.Edge
import com.subnavar.app.domain.model.Floor
import com.subnavar.app.domain.model.Waypoint
import com.subnavar.app.domain.model.WaypointType
import com.subnavar.app.domain.repository.BuildingRepository
import com.subnavar.app.util.FileLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class MappingViewMode {
    FLOOR_PLAN,
    CAMERA,
    SPLIT
}

data class MappingUiState(
    val buildingId: Long = 0,
    val floor: Floor? = null,
    val waypoints: List<Waypoint> = emptyList(),
    val isTracking: Boolean = false,
    val isMappingActive: Boolean = false,
    val currentPositionX: Float = 0f,
    val currentPositionY: Float = 0f,
    val currentPositionZ: Float = 0f,
    val lastWaypointId: Long? = null,
    val showWaypointDialog: Boolean = false,
    val showFloorTransitionDialog: Boolean = false,
    val statusMessage: String = "",
    val waypointCount: Int = 0,
    val viewMode: MappingViewMode = MappingViewMode.FLOOR_PLAN,
    val isMarkingOnPlan: Boolean = false,
    val pendingPlanX: Float = 0f,
    val pendingPlanY: Float = 0f,
    val hasFloorPlan: Boolean = false
)

@HiltViewModel
class MappingViewModel @Inject constructor(
    private val repository: BuildingRepository,
    private val arSessionManager: ARSessionManager,
    private val fileStorageManager: FileStorageManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(MappingUiState())
    val uiState: StateFlow<MappingUiState> = _uiState.asStateFlow()

    private val buildingId: Long = savedStateHandle.get<Long>("buildingId") ?: 0
    private val floorId: Long = savedStateHandle.get<Long>("floorId") ?: 0

    init {
        FileLogger.log("MAPPING_VM", "init: buildingId=$buildingId, floorId=$floorId")
        _uiState.value = _uiState.value.copy(buildingId = buildingId)
        loadFloorData()
    }

    private fun loadFloorData() {
        FileLogger.log("MAPPING_VM", "loadFloorData")
        viewModelScope.launch {
            val floor = repository.getFloorById(floorId)
            val count = repository.getWaypointCount(floorId)
            FileLogger.log("MAPPING_VM", "loadFloorData: floor=${floor?.name}, planPath=${floor?.planImagePath}, waypointCount=$count")
            _uiState.value = _uiState.value.copy(
                floor = floor,
                waypointCount = count,
                hasFloorPlan = floor?.planImagePath != null
            )
            repository.getWaypointsByFloor(floorId).collect { waypoints ->
                _uiState.value = _uiState.value.copy(
                    waypoints = waypoints,
                    waypointCount = waypoints.size
                )
            }
        }
    }

    fun startMapping() {
        FileLogger.log("MAPPING_VM", "startMapping")
        _uiState.value = _uiState.value.copy(
            isMappingActive = true,
            statusMessage = "Mapping active - walk and place waypoints"
        )
    }

    fun stopMapping() {
        FileLogger.log("MAPPING_VM", "stopMapping")
        _uiState.value = _uiState.value.copy(
            isMappingActive = false,
            statusMessage = "Mapping paused"
        )
    }

    fun updateTrackingState(isTracking: Boolean, x: Float, y: Float, z: Float) {
        _uiState.value = _uiState.value.copy(
            isTracking = isTracking,
            currentPositionX = x,
            currentPositionY = y,
            currentPositionZ = z
        )
    }

    fun showWaypointDialog() {
        FileLogger.log("MAPPING_VM", "showWaypointDialog")
        _uiState.value = _uiState.value.copy(showWaypointDialog = true)
    }

    fun hideWaypointDialog() {
        _uiState.value = _uiState.value.copy(showWaypointDialog = false)
    }

    fun placeWaypoint(
        label: String?,
        type: WaypointType,
        planX: Float,
        planY: Float,
        capturedBitmap: Bitmap?
    ) {
        FileLogger.log("MAPPING_VM", "placeWaypoint: label=$label, type=$type, planX=$planX, planY=$planY")
        val state = _uiState.value
        val trackingInfo = arSessionManager.getCurrentTrackingInfo()

        viewModelScope.launch {
            val waypoint = Waypoint(
                floorId = floorId,
                label = label,
                type = type,
                posX = if (trackingInfo.isTracking) trackingInfo.positionX else state.currentPositionX,
                posY = if (trackingInfo.isTracking) trackingInfo.positionY else state.currentPositionY,
                posZ = if (trackingInfo.isTracking) trackingInfo.positionZ else state.currentPositionZ,
                orientationW = trackingInfo.orientationW,
                orientationX = trackingInfo.orientationX,
                orientationY = trackingInfo.orientationY,
                orientationZ = trackingInfo.orientationZ,
                planX = planX,
                planY = planY
            )

            val waypointId = repository.insertWaypoint(waypoint)

            // Save captured photo if available
            if (capturedBitmap != null) {
                val photoPath = fileStorageManager.saveWaypointPhoto(
                    buildingId, floorId, waypointId, capturedBitmap, 0
                )
                fileStorageManager.saveThumbnail(buildingId, floorId, waypointId, capturedBitmap)
                val updatedWaypoint = waypoint.copy(
                    id = waypointId,
                    imageDirPath = fileStorageManager.getWaypointDir(buildingId, floorId, waypointId).absolutePath
                )
                repository.updateWaypoint(updatedWaypoint)
            }

            // Auto-connect to last waypoint
            val lastId = state.lastWaypointId
            if (lastId != null) {
                val lastWaypoint = repository.getWaypointById(lastId)
                if (lastWaypoint != null) {
                    val dx = waypoint.posX - lastWaypoint.posX
                    val dy = waypoint.posY - lastWaypoint.posY
                    val dz = waypoint.posZ - lastWaypoint.posZ
                    val distance = kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
                    repository.insertEdge(Edge(
                        fromWaypointId = lastId,
                        toWaypointId = waypointId,
                        distance = distance
                    ))
                }
            }

            _uiState.value = _uiState.value.copy(
                lastWaypointId = waypointId,
                showWaypointDialog = false,
                statusMessage = "Waypoint placed: ${label ?: "WP-${waypointId}"}"
            )
        }
    }

    fun placeFloorTransitionWaypoint(
        label: String?,
        type: WaypointType,
        connectedFloorId: Long,
        planX: Float,
        planY: Float
    ) {
        val state = _uiState.value
        val trackingInfo = arSessionManager.getCurrentTrackingInfo()

        viewModelScope.launch {
            val waypoint = Waypoint(
                floorId = floorId,
                label = label,
                type = type,
                posX = if (trackingInfo.isTracking) trackingInfo.positionX else state.currentPositionX,
                posY = if (trackingInfo.isTracking) trackingInfo.positionY else state.currentPositionY,
                posZ = if (trackingInfo.isTracking) trackingInfo.positionZ else state.currentPositionZ,
                orientationW = trackingInfo.orientationW,
                orientationX = trackingInfo.orientationX,
                orientationY = trackingInfo.orientationY,
                orientationZ = trackingInfo.orientationZ,
                planX = planX,
                planY = planY,
                isFloorTransition = true,
                connectedFloorId = connectedFloorId
            )

            val waypointId = repository.insertWaypoint(waypoint)

            // Auto-connect to last waypoint
            val lastId = state.lastWaypointId
            if (lastId != null) {
                val lastWaypoint = repository.getWaypointById(lastId)
                if (lastWaypoint != null) {
                    val dx = waypoint.posX - lastWaypoint.posX
                    val dy = waypoint.posY - lastWaypoint.posY
                    val dz = waypoint.posZ - lastWaypoint.posZ
                    val distance = kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
                    repository.insertEdge(Edge(
                        fromWaypointId = lastId,
                        toWaypointId = waypointId,
                        distance = distance
                    ))
                }
            }

            _uiState.value = _uiState.value.copy(
                lastWaypointId = waypointId,
                showFloorTransitionDialog = false,
                statusMessage = "Floor transition placed: ${label ?: "Transition"}"
            )
        }
    }

    fun showFloorTransitionDialog() {
        _uiState.value = _uiState.value.copy(showFloorTransitionDialog = true)
    }

    fun hideFloorTransitionDialog() {
        _uiState.value = _uiState.value.copy(showFloorTransitionDialog = false)
    }

    fun deleteWaypoint(waypointId: Long) {
        viewModelScope.launch {
            repository.deleteWaypoint(waypointId)
            if (_uiState.value.lastWaypointId == waypointId) {
                _uiState.value = _uiState.value.copy(lastWaypointId = null)
            }
        }
    }

    fun isARCoreSupported(): Boolean = arSessionManager.isARCoreSupported()

    fun setViewMode(mode: MappingViewMode) {
        FileLogger.log("MAPPING_VM", "setViewMode: $mode")
        _uiState.value = _uiState.value.copy(viewMode = mode)
    }

    fun markLocationOnPlan(planX: Float, planY: Float) {
        FileLogger.log("MAPPING_VM", "markLocationOnPlan: x=$planX, y=$planY")
        _uiState.value = _uiState.value.copy(
            isMarkingOnPlan = true,
            pendingPlanX = planX,
            pendingPlanY = planY,
            statusMessage = "Location marked on plan. Tap + Waypoint to place."
        )
    }

    fun clearPlanMark() {
        _uiState.value = _uiState.value.copy(
            isMarkingOnPlan = false,
            pendingPlanX = 0f,
            pendingPlanY = 0f
        )
    }

    fun placeWaypointAtMark(
        label: String?,
        type: WaypointType,
        capturedBitmap: Bitmap?
    ) {
        val state = _uiState.value
        placeWaypoint(
            label = label,
            type = type,
            planX = state.pendingPlanX,
            planY = state.pendingPlanY,
            capturedBitmap = capturedBitmap
        )
        _uiState.value = _uiState.value.copy(
            isMarkingOnPlan = false,
            pendingPlanX = 0f,
            pendingPlanY = 0f
        )
    }
}
