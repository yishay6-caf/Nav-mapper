package com.subnavar.app.data.repository

import android.net.Uri
import com.subnavar.app.data.local.db.dao.BuildingDao
import com.subnavar.app.data.local.db.dao.EdgeDao
import com.subnavar.app.data.local.db.dao.FloorDao
import com.subnavar.app.data.local.db.dao.WaypointDao
import com.subnavar.app.data.local.db.converter.toDomain
import com.subnavar.app.data.local.db.converter.toEntity
import com.subnavar.app.data.local.file.FileStorageManager
import com.subnavar.app.domain.model.Building
import com.subnavar.app.domain.model.Edge
import com.subnavar.app.domain.model.Floor
import com.subnavar.app.domain.model.Waypoint
import com.subnavar.app.domain.repository.BuildingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuildingRepositoryImpl @Inject constructor(
    private val buildingDao: BuildingDao,
    private val floorDao: FloorDao,
    private val waypointDao: WaypointDao,
    private val edgeDao: EdgeDao,
    private val fileStorageManager: FileStorageManager
) : BuildingRepository {

    override fun getAllBuildings(): Flow<List<Building>> =
        buildingDao.getAllBuildings().map { list -> list.map { it.toDomain() } }

    override suspend fun getBuildingById(id: Long): Building? =
        buildingDao.getBuildingById(id)?.toDomain()

    override suspend fun insertBuilding(building: Building): Long =
        buildingDao.insertBuilding(building.toEntity())

    override suspend fun updateBuilding(building: Building) =
        buildingDao.updateBuilding(building.toEntity())

    override suspend fun deleteBuilding(id: Long) {
        fileStorageManager.deleteBuildingData(id)
        buildingDao.deleteBuildingById(id)
    }

    override fun getFloorsByBuilding(buildingId: Long): Flow<List<Floor>> =
        floorDao.getFloorsByBuilding(buildingId).map { list -> list.map { it.toDomain() } }

    override suspend fun getFloorById(id: Long): Floor? =
        floorDao.getFloorById(id)?.toDomain()

    override suspend fun insertFloor(floor: Floor): Long =
        floorDao.insertFloor(floor.toEntity())

    override suspend fun updateFloor(floor: Floor) =
        floorDao.updateFloor(floor.toEntity())

    override suspend fun deleteFloor(id: Long) =
        floorDao.deleteFloorById(id)

    override fun getWaypointsByFloor(floorId: Long): Flow<List<Waypoint>> =
        waypointDao.getWaypointsByFloor(floorId).map { list -> list.map { it.toDomain() } }

    override suspend fun getWaypointById(id: Long): Waypoint? =
        waypointDao.getWaypointById(id)?.toDomain()

    override suspend fun getWaypointsByFloorSync(floorId: Long): List<Waypoint> =
        waypointDao.getWaypointsByFloorSync(floorId).map { it.toDomain() }

    override suspend fun getWaypointsByBuilding(buildingId: Long): List<Waypoint> =
        waypointDao.getWaypointsByBuilding(buildingId).map { it.toDomain() }

    override suspend fun insertWaypoint(waypoint: Waypoint): Long =
        waypointDao.insertWaypoint(waypoint.toEntity())

    override suspend fun updateWaypoint(waypoint: Waypoint) =
        waypointDao.updateWaypoint(waypoint.toEntity())

    override suspend fun deleteWaypoint(id: Long) =
        waypointDao.deleteWaypointById(id)

    override suspend fun getWaypointCount(floorId: Long): Int =
        waypointDao.getWaypointCount(floorId)

    override suspend fun getEdgesForWaypoint(waypointId: Long): List<Edge> =
        edgeDao.getEdgesForWaypointSync(waypointId).map { it.toDomain() }

    override suspend fun getEdgesByBuilding(buildingId: Long): List<Edge> =
        edgeDao.getEdgesByBuilding(buildingId).map { it.toDomain() }

    override suspend fun insertEdge(edge: Edge): Long =
        edgeDao.insertEdge(edge.toEntity())

    override suspend fun deleteEdge(id: Long) =
        edgeDao.deleteEdgeById(id)

    override suspend fun getEdgeBetween(waypointId1: Long, waypointId2: Long): Edge? =
        edgeDao.getEdgeBetween(waypointId1, waypointId2)?.toDomain()

    override suspend fun importFloorPlan(buildingId: Long, floorId: Long, uri: Uri): String =
        fileStorageManager.importFloorPlan(buildingId, floorId, uri)

    override suspend fun exportBuildingData(buildingId: Long, outputUri: Uri) =
        fileStorageManager.exportBuildingData(buildingId, outputUri)

    override suspend fun importBuildingData(inputUri: Uri, buildingId: Long) =
        fileStorageManager.importBuildingData(inputUri, buildingId)
}
