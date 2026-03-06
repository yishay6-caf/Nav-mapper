package com.subnavar.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "edges",
    foreignKeys = [
        ForeignKey(
            entity = WaypointEntity::class,
            parentColumns = ["id"],
            childColumns = ["fromWaypointId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WaypointEntity::class,
            parentColumns = ["id"],
            childColumns = ["toWaypointId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("fromWaypointId"), Index("toWaypointId")]
)
data class EdgeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fromWaypointId: Long,
    val toWaypointId: Long,
    val distance: Float = 0f,
    val isAccessible: Boolean = true
)
