package com.subnavar.app.domain.model

data class Floor(
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
