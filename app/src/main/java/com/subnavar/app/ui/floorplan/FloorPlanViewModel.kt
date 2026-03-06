package com.subnavar.app.ui.floorplan

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subnavar.app.domain.model.Building
import com.subnavar.app.domain.model.Floor
import com.subnavar.app.domain.model.Waypoint
import com.subnavar.app.domain.repository.BuildingRepository
import com.subnavar.app.util.FileLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FloorPlanUiState(
    val buildings: List<Building> = emptyList(),
    val selectedBuilding: Building? = null,
    val floors: List<Floor> = emptyList(),
    val selectedFloor: Floor? = null,
    val waypoints: List<Waypoint> = emptyList(),
    val isLoading: Boolean = false,
    val showAddBuildingDialog: Boolean = false,
    val showAddFloorDialog: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class FloorPlanViewModel @Inject constructor(
    private val repository: BuildingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FloorPlanUiState())
    val uiState: StateFlow<FloorPlanUiState> = _uiState.asStateFlow()

    init {
        FileLogger.log("FLOORPLAN_VM", "init")
        loadBuildings()
    }

    private fun loadBuildings() {
        FileLogger.log("FLOORPLAN_VM", "loadBuildings")
        viewModelScope.launch {
            repository.getAllBuildings().collect { buildings ->
                FileLogger.log("FLOORPLAN_VM", "loadBuildings: got ${buildings.size} buildings")
                _uiState.value = _uiState.value.copy(buildings = buildings)
            }
        }
    }

    fun selectBuilding(building: Building) {
        FileLogger.log("FLOORPLAN_VM", "selectBuilding: ${building.name} (id=${building.id})")
        _uiState.value = _uiState.value.copy(
            selectedBuilding = building,
            selectedFloor = null,
            waypoints = emptyList()
        )
        loadFloors(building.id)
    }

    private fun loadFloors(buildingId: Long) {
        viewModelScope.launch {
            repository.getFloorsByBuilding(buildingId).collect { floors ->
                _uiState.value = _uiState.value.copy(floors = floors)
            }
        }
    }

    fun selectFloor(floor: Floor) {
        FileLogger.log("FLOORPLAN_VM", "selectFloor: ${floor.name} (id=${floor.id}, planPath=${floor.planImagePath})")
        _uiState.value = _uiState.value.copy(selectedFloor = floor)
        loadWaypoints(floor.id)
    }

    private fun loadWaypoints(floorId: Long) {
        viewModelScope.launch {
            repository.getWaypointsByFloor(floorId).collect { waypoints ->
                _uiState.value = _uiState.value.copy(waypoints = waypoints)
            }
        }
    }

    fun showAddBuildingDialog() {
        _uiState.value = _uiState.value.copy(showAddBuildingDialog = true)
    }

    fun hideAddBuildingDialog() {
        _uiState.value = _uiState.value.copy(showAddBuildingDialog = false)
    }

    fun addBuilding(name: String, description: String?) {
        FileLogger.log("FLOORPLAN_VM", "addBuilding: name=$name")
        viewModelScope.launch {
            val building = Building(name = name, description = description)
            val id = repository.insertBuilding(building)
            _uiState.value = _uiState.value.copy(showAddBuildingDialog = false)
            val inserted = repository.getBuildingById(id)
            if (inserted != null) {
                selectBuilding(inserted)
            }
        }
    }

    fun showAddFloorDialog() {
        _uiState.value = _uiState.value.copy(showAddFloorDialog = true)
    }

    fun hideAddFloorDialog() {
        _uiState.value = _uiState.value.copy(showAddFloorDialog = false)
    }

    fun addFloor(name: String, level: Int) {
        FileLogger.log("FLOORPLAN_VM", "addFloor: name=$name, level=$level")
        val building = _uiState.value.selectedBuilding ?: return
        viewModelScope.launch {
            val floor = Floor(
                buildingId = building.id,
                name = name,
                level = level
            )
            val id = repository.insertFloor(floor)
            _uiState.value = _uiState.value.copy(showAddFloorDialog = false)
            val inserted = repository.getFloorById(id)
            if (inserted != null) {
                selectFloor(inserted)
            }
        }
    }

    fun importFloorPlan(uri: Uri) {
        FileLogger.log("FLOORPLAN_VM", "importFloorPlan called with uri=$uri")
        val building = _uiState.value.selectedBuilding
        if (building == null) {
            FileLogger.log("FLOORPLAN_VM", "importFloorPlan: selectedBuilding is null, aborting")
            return
        }
        val floor = _uiState.value.selectedFloor
        if (floor == null) {
            FileLogger.log("FLOORPLAN_VM", "importFloorPlan: selectedFloor is null, aborting")
            return
        }
        FileLogger.log("FLOORPLAN_VM", "importFloorPlan: building=${building.id}, floor=${floor.id}")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                FileLogger.log("FLOORPLAN_VM", "importFloorPlan: calling repository.importFloorPlan")
                val path = repository.importFloorPlan(building.id, floor.id, uri)
                FileLogger.log("FLOORPLAN_VM", "importFloorPlan: got path=$path")
                val updated = floor.copy(planImagePath = path)
                repository.updateFloor(updated)
                FileLogger.log("FLOORPLAN_VM", "importFloorPlan: floor updated successfully")
                _uiState.value = _uiState.value.copy(
                    selectedFloor = updated,
                    isLoading = false
                )
            } catch (e: Exception) {
                FileLogger.logError("FLOORPLAN_VM", "importFloorPlan FAILED", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to import floor plan: ${e.message}"
                )
            }
        }
    }

    fun deleteBuilding() {
        FileLogger.log("FLOORPLAN_VM", "deleteBuilding")
        val building = _uiState.value.selectedBuilding ?: return
        viewModelScope.launch {
            repository.deleteBuilding(building.id)
            _uiState.value = _uiState.value.copy(
                selectedBuilding = null,
                selectedFloor = null,
                floors = emptyList(),
                waypoints = emptyList(),
                showDeleteConfirm = false
            )
        }
    }

    fun deleteFloor() {
        FileLogger.log("FLOORPLAN_VM", "deleteFloor")
        val floor = _uiState.value.selectedFloor ?: return
        viewModelScope.launch {
            repository.deleteFloor(floor.id)
            _uiState.value = _uiState.value.copy(
                selectedFloor = null,
                waypoints = emptyList()
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun goBackToBuildings() {
        FileLogger.log("FLOORPLAN_VM", "goBackToBuildings")
        _uiState.value = _uiState.value.copy(
            selectedBuilding = null,
            selectedFloor = null,
            floors = emptyList(),
            waypoints = emptyList()
        )
    }

    fun goBackToFloors() {
        FileLogger.log("FLOORPLAN_VM", "goBackToFloors")
        _uiState.value = _uiState.value.copy(
            selectedFloor = null,
            waypoints = emptyList()
        )
    }
}
