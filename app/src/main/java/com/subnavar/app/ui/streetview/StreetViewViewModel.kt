package com.subnavar.app.ui.streetview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnavar.app.domain.model.Edge
import com.subnavar.app.domain.model.Waypoint
import com.subnavar.app.domain.repository.BuildingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class StreetViewUiState(
    val currentWaypoint: Waypoint? = null,
    val connectedWaypoints: List<Waypoint> = emptyList(),
    val photoPath: String? = null,
    val allPhotoPaths: List<String> = emptyList(),
    val currentPhotoIndex: Int = 0,
    val isLoading: Boolean = true,
    val floorName: String = ""
)

@HiltViewModel
class StreetViewViewModel @Inject constructor(
    private val repository: BuildingRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(StreetViewUiState())
    val uiState: StateFlow<StreetViewUiState> = _uiState.asStateFlow()

    private val waypointId: Long = savedStateHandle.get<Long>("waypointId") ?: 0

    init {
        loadWaypoint(waypointId)
    }

    private fun loadWaypoint(id: Long) {
        viewModelScope.launch {
            val waypoint = repository.getWaypointById(id)
            if (waypoint != null) {
                val floor = repository.getFloorById(waypoint.floorId)
                val edges = repository.getEdgesForWaypoint(waypoint.id)
                val connected = loadConnectedWaypoints(waypoint.id, edges)
                val photos = loadWaypointPhotos(waypoint)

                _uiState.value = StreetViewUiState(
                    currentWaypoint = waypoint,
                    connectedWaypoints = connected,
                    photoPath = photos.firstOrNull(),
                    allPhotoPaths = photos,
                    currentPhotoIndex = 0,
                    isLoading = false,
                    floorName = floor?.name ?: ""
                )
            }
        }
    }

    private suspend fun loadConnectedWaypoints(waypointId: Long, edges: List<Edge>): List<Waypoint> {
        val connectedIds = edges.map { edge ->
            if (edge.fromWaypointId == waypointId) edge.toWaypointId else edge.fromWaypointId
        }.distinct()
        return connectedIds.mapNotNull { repository.getWaypointById(it) }
    }

    private fun loadWaypointPhotos(waypoint: Waypoint): List<String> {
        val dir = waypoint.imageDirPath ?: return emptyList()
        val dirFile = File(dir)
        if (!dirFile.exists()) return emptyList()
        return dirFile.listFiles()
            ?.filter { it.extension in listOf("jpg", "jpeg", "png") && !it.name.contains("thumbnail") }
            ?.sortedBy { it.name }
            ?.map { it.absolutePath }
            ?: emptyList()
    }

    fun navigateToWaypoint(waypoint: Waypoint) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadWaypoint(waypoint.id)
    }

    fun nextPhoto() {
        val state = _uiState.value
        if (state.allPhotoPaths.isNotEmpty()) {
            val nextIndex = (state.currentPhotoIndex + 1) % state.allPhotoPaths.size
            _uiState.value = state.copy(
                currentPhotoIndex = nextIndex,
                photoPath = state.allPhotoPaths[nextIndex]
            )
        }
    }

    fun previousPhoto() {
        val state = _uiState.value
        if (state.allPhotoPaths.isNotEmpty()) {
            val prevIndex = if (state.currentPhotoIndex > 0)
                state.currentPhotoIndex - 1
            else
                state.allPhotoPaths.size - 1
            _uiState.value = state.copy(
                currentPhotoIndex = prevIndex,
                photoPath = state.allPhotoPaths[prevIndex]
            )
        }
    }
}
