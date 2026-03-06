package com.subnavar.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.subnavar.app.data.local.db.dao.BuildingDao
import com.subnavar.app.data.local.db.dao.EdgeDao
import com.subnavar.app.data.local.db.dao.FloorDao
import com.subnavar.app.data.local.db.dao.WaypointDao
import com.subnavar.app.data.local.db.entity.BuildingEntity
import com.subnavar.app.data.local.db.entity.EdgeEntity
import com.subnavar.app.data.local.db.entity.FloorEntity
import com.subnavar.app.data.local.db.entity.WaypointEntity

@Database(
    entities = [
        BuildingEntity::class,
        FloorEntity::class,
        WaypointEntity::class,
        EdgeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SubNavDatabase : RoomDatabase() {
    abstract fun buildingDao(): BuildingDao
    abstract fun floorDao(): FloorDao
    abstract fun waypointDao(): WaypointDao
    abstract fun edgeDao(): EdgeDao
}
