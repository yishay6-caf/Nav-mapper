package com.subnavar.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "waypoints",
    foreignKeys = [
        ForeignKey(
            entity = FloorEntity::class,
            parentColumns = ["id"],
            childColumns = ["floorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("floorId")]
)
data class WaypointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val floorId: Long,
    val label: String? = null,
    val type: String = "HALLWAY",
    val posX: Float = 0f,
    val posY: Float = 0f,
    val posZ: Float = 0f,
    val orientationW: Float = 1f,
    val orientationX: Float = 0f,
    val orientationY: Float = 0f,
    val orientationZ: Float = 0f,
    val planX: Float = 0f,
    val planY: Float = 0f,
    val imageDirPath: String? = null,
    val featureFilePath: String? = null,
    val isFloorTransition: Boolean = false,
    val connectedFloorId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
