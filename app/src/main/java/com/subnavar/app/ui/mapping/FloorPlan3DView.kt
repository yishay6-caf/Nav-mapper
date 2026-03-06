package com.subnavar.app.ui.mapping

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.subnavar.app.domain.model.Floor
import com.subnavar.app.domain.model.Waypoint
import com.subnavar.app.domain.model.WaypointType
import com.subnavar.app.util.LocaleManager
import com.subnavar.app.util.Strings
import com.subnavar.app.util.Strings.get
import java.io.File
import kotlin.math.roundToInt

/**
 * 3D perspective floor plan view.
 * Uses graphicsLayer with rotationX for tilt and rotationY/Z for rotation,
 * giving the floor plan a 3D isometric/perspective appearance.
 * Supports pinch-to-zoom, pan, two-finger rotate, tap-to-place, and drag-to-relocate.
 */
@Composable
fun FloorPlan3DView(
    floor: Floor?,
    waypoints: List<Waypoint>,
    isMarkingOnPlan: Boolean,
    pendingPlanX: Float,
    pendingPlanY: Float,
    onTapOnPlan: ((Float, Float) -> Unit)? = null,
    onWaypointRelocate: ((Long, Float, Float) -> Unit)? = null,
    lang: LocaleManager.AppLanguage = LocaleManager.AppLanguage.ENGLISH,
    modifier: Modifier = Modifier
) {
    val planPath = floor?.planImagePath
    if (planPath != null && File(planPath).exists()) {
        var scale by remember { mutableFloatStateOf(0.8f) }
        var offsetX by remember { mutableFloatStateOf(0f) }
        var offsetY by remember { mutableFloatStateOf(0f) }
        var rotationX by remember { mutableFloatStateOf(45f) } // Tilt angle for 3D effect
        var rotationZ by remember { mutableFloatStateOf(0f) }  // Rotation around Z axis
        var containerSize by remember { mutableStateOf(IntSize.Zero) }

        // Drag-to-relocate state
        var draggingWaypointId by remember { mutableLongStateOf(-1L) }
        var dragOffsetX by remember { mutableFloatStateOf(0f) }
        var dragOffsetY by remember { mutableFloatStateOf(0f) }

        Box(
            modifier = modifier
                .fillMaxSize()
                .clip(RectangleShape) // Prevent zoom overflow into camera area
                .background(Color(0xFF1A1A2E))
                .onSizeChanged { containerSize = it }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, rotation ->
                        scale = (scale * zoom).coerceIn(0.3f, 10f)
                        offsetX += pan.x
                        offsetY += pan.y
                        rotationZ += rotation
                    }
                }
                .pointerInput(scale, offsetX, offsetY, containerSize) {
                    if (onTapOnPlan != null) {
                        detectTapGestures { tapOffset ->
                            // Convert screen tap to plan coordinates
                            // graphicsLayer centers content then applies scale+translation from center
                            val centerX = containerSize.width / 2f
                            val centerY = containerSize.height / 2f
                            // Tap relative to container center, undo translation, undo scale, restore to plan space
                            val planX = (tapOffset.x - centerX - offsetX) / scale + centerX
                            val planY = (tapOffset.y - centerY - offsetY) / scale + centerY
                            onTapOnPlan(planX, planY)
                        }
                    }
                }
        ) {
            // 3D transformed floor plan
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offsetX
                        translationY = offsetY
                        this.rotationX = rotationX
                        this.rotationZ = rotationZ
                        // Enable perspective for true 3D effect
                        cameraDistance = 12f * density
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(File(planPath)),
                    contentDescription = Strings.floorPlan3D.get(lang),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                // Render waypoints on the 3D plan (with drag-to-relocate support)
                waypoints.forEach { waypoint ->
                    val isDragging = draggingWaypointId == waypoint.id
                    val wpX = if (isDragging) dragOffsetX else waypoint.planX
                    val wpY = if (isDragging) dragOffsetY else waypoint.planY

                    Waypoint3DMarker(
                        waypoint = waypoint,
                        isDragging = isDragging,
                        onDragStart = {
                            draggingWaypointId = waypoint.id
                            dragOffsetX = waypoint.planX
                            dragOffsetY = waypoint.planY
                        },
                        onDrag = { dx, dy ->
                            dragOffsetX += dx / scale
                            dragOffsetY += dy / scale
                        },
                        onDragEnd = {
                            onWaypointRelocate?.invoke(
                                waypoint.id,
                                dragOffsetX,
                                dragOffsetY
                            )
                            draggingWaypointId = -1L
                        },
                        modifier = Modifier.offset {
                            IntOffset(
                                wpX.roundToInt(),
                                wpY.roundToInt()
                            )
                        }
                    )
                }

                // Show pending mark
                if (isMarkingOnPlan) {
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    pendingPlanX.roundToInt() - 12,
                                    pendingPlanY.roundToInt() - 12
                                )
                            }
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50).copy(alpha = 0.9f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = Strings.markedLocation.get(lang),
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // 3D view controls overlay
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = Strings.view3D.get(lang),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${Strings.tilt.get(lang)}: ${rotationX.roundToInt()}°",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = Strings.pinchToZoomDragToPan.get(lang),
                    color = Color.White.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            // Tilt control buttons
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(8.dp)
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "▲",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    if (event.changes.any { it.pressed }) {
                                        rotationX = (rotationX + 5f).coerceIn(0f, 80f)
                                    }
                                }
                            }
                        }
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "▼",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    if (event.changes.any { it.pressed }) {
                                        rotationX = (rotationX - 5f).coerceIn(0f, 80f)
                                    }
                                }
                            }
                        }
                )
            }
        }
    } else {
        // No floor plan
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF1A1A2E)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Map,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.White.copy(alpha = 0.3f)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = Strings.noFloorPlanAvailable.get(lang),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun Waypoint3DMarker(
    waypoint: Waypoint,
    isDragging: Boolean = false,
    onDragStart: () -> Unit = {},
    onDrag: (Float, Float) -> Unit = { _, _ -> },
    onDragEnd: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val color = when (waypoint.type) {
        WaypointType.STAIRWELL -> Color(0xFFFF9800)
        WaypointType.ELEVATOR -> Color(0xFF2196F3)
        WaypointType.ENTRANCE -> Color(0xFF4CAF50)
        WaypointType.POI -> Color(0xFFE91E63)
        WaypointType.ROOM -> Color(0xFF9C27B0)
        else -> Color(0xFF607D8B)
    }

    Column(
        modifier = modifier
            .pointerInput(waypoint.id) {
                detectDragGestures(
                    onDragStart = { onDragStart() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x, dragAmount.y)
                    },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Elevated marker with shadow effect for 3D - larger when dragging
        Box(
            modifier = Modifier
                .size(if (isDragging) 22.dp else 16.dp)
                .clip(CircleShape)
                .background(if (isDragging) color.copy(alpha = 1f) else color),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(if (isDragging) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
        waypoint.label?.let { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(
                        color.copy(alpha = 0.8f),
                        RoundedCornerShape(2.dp)
                    )
                    .padding(horizontal = 3.dp, vertical = 1.dp)
            )
        }
    }
}
