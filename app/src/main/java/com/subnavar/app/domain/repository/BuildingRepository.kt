package com.subnavar.app.domain.repository

import android.net.Uri
import com.subnavar.app.domain.model.Building
import com.subnavar.app.domain.model.Edge
import com.subnavar.app.domain.model.Floor
import com.subnavar.app.domain.model.Waypoint
import kotlinx.coroutines.flow.Flow

interface BuildingRepository {
    fun getAllBuildings(): Flow<List<Building>>
    suspend fun getBuildingById(id: Long): Building?
    suspend fun insertBuilding(building: Building): Long
    suspend fun updateBuilding(building: Building)
    suspend fun deleteBuilding(id: Long)

    fun getFloorsByBuilding(buildingId: Long): Flow<List<Floor>>
    suspend fun getFloorById(id: Long): Floor?
    suspend fun insertFloor(floor: Floor): Long
    suspend fun updateFloor(floor: Floor)
    suspend fun deleteFloor(id: Long)

    fun getWaypointsByFloor(floorId: Long): Flow<List<Waypoint>>
    suspend fun getWaypointById(id: Long): Waypoint?
    suspend fun getWaypointsByFloorSync(floorId: Long): List<Waypoint>
    suspend fun getWaypointsByBuilding(buildingId: Long): List<Waypoint>
    suspend fun insertWaypoint(waypoint: Waypoint): Long
    suspend fun updateWaypoint(waypoint: Waypoint)
    suspend fun deleteWaypoint(id: Long)
    suspend fun getWaypointCount(floorId: Long): Int

    suspend fun getEdgesForWaypoint(waypointId: Long): List<Edge>
    suspend fun getEdgesByBuilding(buildingId: Long): List<Edge>
    suspend fun insertEdge(edge: Edge): Long
    suspend fun deleteEdge(id: Long)
    suspend fun getEdgeBetween(waypointId1: Long, waypointId2: Long): Edge?

    suspend fun importFloorPlan(buildingId: Long, floorId: Long, uri: Uri): String
    suspend fun exportBuildingData(buildingId: Long, outputUri: Uri)
    suspend fun importBuildingData(inputUri: Uri, buildingId: Long)
}
