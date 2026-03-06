package com.subnavar.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "floors",
    foreignKeys = [
        ForeignKey(
            entity = BuildingEntity::class,
            parentColumns = ["id"],
            childColumns = ["buildingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("buildingId")]
)
data class FloorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val buildingId: Long,
    val name: String,
    val level: Int,
    val planImagePath: String? = null,
    val originX: Float = 0f,
    val originY: Float = 0f,
    val originZ: Float = 0f,
    val scale: Float = 1f,
    val rotation: Float = 0f
)
