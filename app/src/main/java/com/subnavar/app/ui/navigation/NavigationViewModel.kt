package com.subnavar.app.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnavar.app.domain.model.Building
import com.subnavar.app.domain.model.Edge
import com.subnavar.app.domain.model.Floor
import com.subnavar.app.domain.model.Waypoint
import com.subnavar.app.domain.repository.BuildingRepository
import com.subnavar.app.nav.PathFinder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NavigationUiState(
    val buildings: List<Building> = emptyList(),
    val selectedBuilding: Building? = null,
    val floors: List<Floor> = emptyList(),
    val allWaypoints: List<Waypoint> = emptyList(),
    val startWaypoint: Waypoint? = null,
    val endWaypoint: Waypoint? = null,
    val pathResult: PathFinder.PathResult? = null,
    val isSelectingStart: Boolean = true,
    val isCalculating: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val repository: BuildingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NavigationUiState())
    val uiState: StateFlow<NavigationUiState> = _uiState.asStateFlow()

    private val pathFinder = PathFinder()

    init {
        loadBuildings()
    }

    private fun loadBuildings() {
        viewModelScope.launch {
            repository.getAllBuildings().collect { buildings ->
                _uiState.value = _uiState.value.copy(buildings = buildings)
                if (buildings.size == 1 && _uiState.value.selectedBuilding == null) {
                    selectBuilding(buildings.first())
                }
            }
        }
    }

    fun selectBuilding(building: Building) {
        _uiState.value = _uiState.value.copy(selectedBuilding = building)
        viewModelScope.launch {
            val floors = repository.getFloorsByBuilding(building.id).first()
            val waypoints = repository.getWaypointsByBuilding(building.id)
            _uiState.value = _uiState.value.copy(
                floors = floors,
                allWaypoints = waypoints
            )
        }
    }

    fun selectStartWaypoint(waypoint: Waypoint) {
        _uiState.value = _uiState.value.copy(
            startWaypoint = waypoint,
            isSelectingStart = false,
            pathResult = null
        )
    }

    fun selectEndWaypoint(waypoint: Waypoint) {
        _uiState.value = _uiState.value.copy(
            endWaypoint = waypoint,
            pathResult = null
        )
        calculatePath()
    }

    fun swapStartEnd() {
        val state = _uiState.value
        _uiState.value = state.copy(
            startWaypoint = state.endWaypoint,
            endWaypoint = state.startWaypoint,
            pathResult = null
        )
        if (_uiState.value.startWaypoint != null && _uiState.value.endWaypoint != null) {
            calculatePath()
        }
    }

    fun resetNavigation() {
        _uiState.value = _uiState.value.copy(
            startWaypoint = null,
            endWaypoint = null,
            pathResult = null,
            isSelectingStart = true,
            errorMessage = null
        )
    }

    private fun calculatePath() {
        val state = _uiState.value
        val start = state.startWaypoint ?: return
        val end = state.endWaypoint ?: return
        val building = state.selectedBuilding ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCalculating = true)
            try {
                val allWaypoints = repository.getWaypointsByBuilding(building.id)
                val allEdges = repository.getEdgesByBuilding(building.id)
                val result = pathFinder.findPath(start, end, allWaypoints, allEdges)
                _uiState.value = _uiState.value.copy(
                    pathResult = result,
                    isCalculating = false,
                    errorMessage = if (result == null) "No path found between these waypoints" else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCalculating = false,
                    errorMessage = "Error calculating path: ${e.message}"
                )
            }
        }
    }

    fun toggleSelectingMode() {
        _uiState.value = _uiState.value.copy(
            isSelectingStart = !_uiState.value.isSelectingStart
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
