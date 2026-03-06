package com.subnavar.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.subnavar.app.data.local.db.entity.WaypointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WaypointDao {
    @Query("SELECT * FROM waypoints WHERE floorId = :floorId ORDER BY createdAt ASC")
    fun getWaypointsByFloor(floorId: Long): Flow<List<WaypointEntity>>

    @Query("SELECT * FROM waypoints WHERE id = :id")
    suspend fun getWaypointById(id: Long): WaypointEntity?

    @Query("SELECT * FROM waypoints WHERE floorId = :floorId")
    suspend fun getWaypointsByFloorSync(floorId: Long): List<WaypointEntity>

    @Query("SELECT * FROM waypoints WHERE floorId IN (SELECT id FROM floors WHERE buildingId = :buildingId)")
    suspend fun getWaypointsByBuilding(buildingId: Long): List<WaypointEntity>

    @Query("SELECT * FROM waypoints WHERE isFloorTransition = 1 AND floorId = :floorId")
    suspend fun getFloorTransitionWaypoints(floorId: Long): List<WaypointEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaypoint(waypoint: WaypointEntity): Long

    @Update
    suspend fun updateWaypoint(waypoint: WaypointEntity)

    @Delete
    suspend fun deleteWaypoint(waypoint: WaypointEntity)

    @Query("DELETE FROM waypoints WHERE id = :id")
    suspend fun deleteWaypointById(id: Long)

    @Query("SELECT COUNT(*) FROM waypoints WHERE floorId = :floorId")
    suspend fun getWaypointCount(floorId: Long): Int
}
