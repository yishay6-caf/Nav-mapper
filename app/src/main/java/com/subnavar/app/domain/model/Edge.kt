package com.subnavar.app.domain.model

data class Edge(
    val id: Long = 0,
    val fromWaypointId: Long,
    val toWaypointId: Long,
    val distance: Float = 0f,
    val isAccessible: Boolean = true
)
