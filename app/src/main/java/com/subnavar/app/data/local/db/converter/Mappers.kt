package com.subnavar.app.data.local.db.converter

import com.subnavar.app.data.local.db.entity.BuildingEntity
import com.subnavar.app.data.local.db.entity.EdgeEntity
import com.subnavar.app.data.local.db.entity.FloorEntity
import com.subnavar.app.data.local.db.entity.WaypointEntity
import com.subnavar.app.domain.model.Building
import com.subnavar.app.domain.model.Edge
import com.subnavar.app.domain.model.Floor
import com.subnavar.app.domain.model.Waypoint
import com.subnavar.app.domain.model.WaypointType

fun BuildingEntity.toDomain() = Building(
    id = id,
    name = name,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Building.toEntity() = BuildingEntity(
    id = id,
    name = name,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun FloorEntity.toDomain() = Floor(
    id = id,
    buildingId = buildingId,
    name = name,
    level = level,
    planImagePath = planImagePath,
    originX = originX,
    originY = originY,
    originZ = originZ,
    scale = scale,
    rotation = rotation
)

fun Floor.toEntity() = FloorEntity(
    id = id,
    buildingId = buildingId,
    name = name,
    level = level,
    planImagePath = planImagePath,
    originX = originX,
    originY = originY,
    originZ = originZ,
    scale = scale,
    rotation = rotation
)

fun WaypointEntity.toDomain() = Waypoint(
    id = id,
    floorId = floorId,
    label = label,
    type = try { WaypointType.valueOf(type) } catch (e: Exception) { WaypointType.HALLWAY },
    posX = posX,
    posY = posY,
    posZ = posZ,
    orientationW = orientationW,
    orientationX = orientationX,
    orientationY = orientationY,
    orientationZ = orientationZ,
    planX = planX,
    planY = planY,
    imageDirPath = imageDirPath,
    featureFilePath = featureFilePath,
    isFloorTransition = isFloorTransition,
    connectedFloorId = connectedFloorId,
    createdAt = createdAt
)

fun Waypoint.toEntity() = WaypointEntity(
    id = id,
    floorId = floorId,
    label = label,
    type = type.name,
    posX = posX,
    posY = posY,
    posZ = posZ,
    orientationW = orientationW,
    orientationX = orientationX,
    orientationY = orientationY,
    orientationZ = orientationZ,
    planX = planX,
    planY = planY,
    imageDirPath = imageDirPath,
    featureFilePath = featureFilePath,
    isFloorTransition = isFloorTransition,
    connectedFloorId = connectedFloorId,
    createdAt = createdAt
)

fun EdgeEntity.toDomain() = Edge(
    id = id,
    fromWaypointId = fromWaypointId,
    toWaypointId = toWaypointId,
    distance = distance,
    isAccessible = isAccessible
)

fun Edge.toEntity() = EdgeEntity(
    id = id,
    fromWaypointId = fromWaypointId,
    toWaypointId = toWaypointId,
    distance = distance,
    isAccessible = isAccessible
)
