package com.subnavar.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.subnavar.app.data.local.db.entity.FloorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FloorDao {
    @Query("SELECT * FROM floors WHERE buildingId = :buildingId ORDER BY level ASC")
    fun getFloorsByBuilding(buildingId: Long): Flow<List<FloorEntity>>

    @Query("SELECT * FROM floors WHERE id = :id")
    suspend fun getFloorById(id: Long): FloorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFloor(floor: FloorEntity): Long

    @Update
    suspend fun updateFloor(floor: FloorEntity)

    @Delete
    suspend fun deleteFloor(floor: FloorEntity)

    @Query("DELETE FROM floors WHERE id = :id")
    suspend fun deleteFloorById(id: Long)

    @Query("SELECT * FROM floors WHERE buildingId = :buildingId")
    suspend fun getFloorsByBuildingSync(buildingId: Long): List<FloorEntity>
}
