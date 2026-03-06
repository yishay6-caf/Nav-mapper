package com.subnavar.app.domain.model

data class Waypoint(
    val id: Long = 0,
    val floorId: Long,
    val label: String? = null,
    val type: WaypointType = WaypointType.HALLWAY,
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

enum class WaypointType {
    HALLWAY,
    ROOM,
    STAIRWELL,
    ELEVATOR,
    ENTRANCE,
    POI
}
