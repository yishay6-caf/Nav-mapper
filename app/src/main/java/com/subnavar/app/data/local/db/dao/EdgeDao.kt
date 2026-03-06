package com.subnavar.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.subnavar.app.data.local.db.entity.EdgeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EdgeDao {
    @Query("SELECT * FROM edges WHERE fromWaypointId = :waypointId OR toWaypointId = :waypointId")
    fun getEdgesForWaypoint(waypointId: Long): Flow<List<EdgeEntity>>

    @Query("SELECT * FROM edges WHERE fromWaypointId = :waypointId OR toWaypointId = :waypointId")
    suspend fun getEdgesForWaypointSync(waypointId: Long): List<EdgeEntity>

    @Query("""
        SELECT * FROM edges WHERE 
        fromWaypointId IN (SELECT id FROM waypoints WHERE floorId IN (SELECT id FROM floors WHERE buildingId = :buildingId))
        OR toWaypointId IN (SELECT id FROM waypoints WHERE floorId IN (SELECT id FROM floors WHERE buildingId = :buildingId))
    """)
    suspend fun getEdgesByBuilding(buildingId: Long): List<EdgeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEdge(edge: EdgeEntity): Long

    @Delete
    suspend fun deleteEdge(edge: EdgeEntity)

    @Query("DELETE FROM edges WHERE id = :id")
    suspend fun deleteEdgeById(id: Long)

    @Query("DELETE FROM edges WHERE fromWaypointId = :waypointId OR toWaypointId = :waypointId")
    suspend fun deleteEdgesForWaypoint(waypointId: Long)

    @Query("""
        SELECT * FROM edges WHERE 
        (fromWaypointId = :waypointId1 AND toWaypointId = :waypointId2)
        OR (fromWaypointId = :waypointId2 AND toWaypointId = :waypointId1)
    """)
    suspend fun getEdgeBetween(waypointId1: Long, waypointId2: Long): EdgeEntity?
}
