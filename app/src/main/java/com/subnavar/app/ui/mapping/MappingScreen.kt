package com.subnavar.app.ui.mapping

import android.Manifest
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Elevator
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stairs
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.subnavar.app.domain.model.Waypoint
import com.subnavar.app.domain.model.WaypointType
import com.subnavar.app.util.LocaleManager
import com.subnavar.app.util.Strings
import com.subnavar.app.util.Strings.get
import java.io.File
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MappingScreen(
    viewModel: MappingViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lang = remember { LocaleManager.getLanguage(context) }
    var hasCameraPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "${Strings.mappingTitle.get(lang)}: ${uiState.floor?.name ?: ""}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "${uiState.waypointCount} ${Strings.waypointsPlacedCount.get(lang)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, Strings.back.get(lang))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    // View mode toggle buttons
                    IconButton(onClick = { viewModel.setViewMode(MappingViewMode.FLOOR_PLAN) }) {
                        Icon(
                            Icons.Default.Map,
                            Strings.floorPlanMode.get(lang),
                            tint = if (uiState.viewMode == MappingViewMode.FLOOR_PLAN)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    IconButton(onClick = { viewModel.setViewMode(MappingViewMode.CAMERA) }) {
                        Icon(
                            Icons.Default.CameraAlt,
                            Strings.cameraMode.get(lang),
                            tint = if (uiState.viewMode == MappingViewMode.CAMERA)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    IconButton(onClick = { viewModel.setViewMode(MappingViewMode.SPLIT) }) {
                        Icon(
                            Icons.Default.Splitscreen,
                            Strings.splitMode.get(lang),
                            tint = if (uiState.viewMode == MappingViewMode.SPLIT)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Start/Stop mapping
                    FilledTonalButton(
                        onClick = {
                            if (uiState.isMappingActive) viewModel.stopMapping()
                            else viewModel.startMapping()
                        }
                    ) {
                        Icon(
                            if (uiState.isMappingActive) Icons.Default.Pause
                            else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(if (uiState.isMappingActive) Strings.pause.get(lang) else Strings.start.get(lang))
                    }

                    // Place waypoint
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.showWaypointDialog() },
                        containerColor = if (uiState.isMarkingOnPlan)
                            Color(0xFF4CAF50)
                        else
                            MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(if (uiState.isMarkingOnPlan) Strings.placeHere.get(lang) else Strings.waypoint.get(lang))
                    }

                    // Take photo
                    FilledTonalButton(
                        onClick = { viewModel.capturePhoto() }
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(Strings.photo.get(lang))
                    }

                    // Record video
                    FilledTonalButton(
                        onClick = { viewModel.toggleRecording() }
                    ) {
                        Icon(
                            if (uiState.isRecording) Icons.Default.Stop
                            else Icons.Default.Videocam,
                            contentDescription = null,
                            tint = if (uiState.isRecording) Color.Red else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(if (uiState.isRecording) Strings.stop.get(lang) else Strings.record.get(lang))
                    }

                    // Floor transition
                    FilledTonalButton(
                        onClick = { viewModel.showFloorTransitionDialog() }
                    ) {
                        Icon(Icons.Default.Stairs, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(Strings.transitionButton.get(lang))
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState.viewMode) {
                MappingViewMode.FLOOR_PLAN -> {
                    // Floor plan with tap-to-mark
                    FloorPlanMappingView(
                        floor = uiState.floor,
                        waypoints = uiState.waypoints,
                        isMarkingOnPlan = uiState.isMarkingOnPlan,
                        pendingPlanX = uiState.pendingPlanX,
                        pendingPlanY = uiState.pendingPlanY,
                        statusMessage = uiState.statusMessage,
                        onTapOnPlan = { x, y -> viewModel.markLocationOnPlan(x, y) },
                        onClearMark = { viewModel.clearPlanMark() }
                    )
                }
                MappingViewMode.CAMERA -> {
                    // Full camera view with live feed
                    if (hasCameraPermission) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CameraPreviewView(
                                cameraRecordingManager = viewModel.cameraRecordingManager,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Recording controls overlay
                            CameraOverlayControls(
                                isRecording = uiState.isRecording,
                                recordingDurationMs = uiState.recordingDurationMs,
                                statusMessage = uiState.statusMessage,
                                onToggleRecording = { viewModel.toggleRecording() },
                                modifier = Modifier.align(Alignment.TopCenter)
                            )
                        }
                    } else {
                        CameraMappingView(
                            hasCameraPermission = false,
                            isTracking = uiState.isTracking,
                            statusMessage = uiState.statusMessage,
                            currentPositionX = uiState.currentPositionX,
                            currentPositionY = uiState.currentPositionY,
                            currentPositionZ = uiState.currentPositionZ
                        )
                    }
                }
                MappingViewMode.SPLIT -> {
                    // Split view: live camera on top, 3D floor plan on bottom
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Top: Live camera feed
                        Box(modifier = Modifier.weight(1f)) {
                            if (hasCameraPermission) {
                                CameraPreviewView(
                                    cameraRecordingManager = viewModel.cameraRecordingManager,
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Recording controls overlay
                                CameraOverlayControls(
                                    isRecording = uiState.isRecording,
                                    recordingDurationMs = uiState.recordingDurationMs,
                                    statusMessage = uiState.statusMessage,
                                    onToggleRecording = { viewModel.toggleRecording() },
                                    modifier = Modifier.align(Alignment.TopCenter)
                                )
                            } else {
                                CameraMappingView(
                                    hasCameraPermission = false,
                                    isTracking = uiState.isTracking,
                                    statusMessage = uiState.statusMessage,
                                    currentPositionX = uiState.currentPositionX,
                                    currentPositionY = uiState.currentPositionY,
                                    currentPositionZ = uiState.currentPositionZ
                                )
                            }
                        }
                        // Bottom: 3D floor plan view with tap-to-mark
                        Box(modifier = Modifier.weight(1f)) {
                            FloorPlan3DView(
                                floor = uiState.floor,
                                waypoints = uiState.waypoints,
                                isMarkingOnPlan = uiState.isMarkingOnPlan,
                                pendingPlanX = uiState.pendingPlanX,
                                pendingPlanY = uiState.pendingPlanY,
                                onTapOnPlan = { x, y -> viewModel.markLocationOnPlan(x, y) },
                                onWaypointRelocate = { id, x, y -> viewModel.relocateWaypoint(id, x, y) },
                                lang = lang
                            )
                        }
                    }
                }
            }
        }
    }

    // Place Waypoint Dialog (use plan coordinates if marked)
    if (uiState.showWaypointDialog) {
        PlaceWaypointDialog(
            onDismiss = { viewModel.hideWaypointDialog() },
            onConfirm = { label, type ->
                if (uiState.isMarkingOnPlan) {
                    viewModel.placeWaypointAtMark(
                        label = label,
                        type = type,
                        capturedBitmap = null
                    )
                } else {
                    viewModel.placeWaypoint(
                        label = label,
                        type = type,
                        planX = 0f,
                        planY = 0f,
                        capturedBitmap = null
                    )
                }
            },
            isMarkingOnPlan = uiState.isMarkingOnPlan,
            planX = uiState.pendingPlanX,
            planY = uiState.pendingPlanY
        )
    }

    // Floor Transition Dialog
    if (uiState.showFloorTransitionDialog) {
        FloorTransitionDialog(
            onDismiss = { viewModel.hideFloorTransitionDialog() },
            onConfirm = { label, type, connectedFloorId ->
                viewModel.placeFloorTransitionWaypoint(
                    label = label,
                    type = type,
                    connectedFloorId = connectedFloorId,
                    planX = if (uiState.isMarkingOnPlan) uiState.pendingPlanX else 0f,
                    planY = if (uiState.isMarkingOnPlan) uiState.pendingPlanY else 0f
                )
            }
        )
    }
}

@Composable
private fun FloorPlanMappingView(
    floor: com.subnavar.app.domain.model.Floor?,
    waypoints: List<Waypoint>,
    isMarkingOnPlan: Boolean,
    pendingPlanX: Float,
    pendingPlanY: Float,
    statusMessage: String,
    onTapOnPlan: (Float, Float) -> Unit,
    onClearMark: () -> Unit,
    lang: LocaleManager.AppLanguage = LocaleManager.AppLanguage.ENGLISH
) {
    val planPath = floor?.planImagePath
    if (planPath != null && File(planPath).exists()) {
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        var imageSize by remember { mutableStateOf(IntSize.Zero) }

        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.5f, 5f)
                            offset = Offset(
                                x = offset.x + pan.x,
                                y = offset.y + pan.y
                            )
                        }
                    }
                    .pointerInput(scale, offset) {
                        detectTapGestures { tapOffset ->
                            // Convert screen tap to plan coordinates
                            val planX = (tapOffset.x - offset.x) / scale
                            val planY = (tapOffset.y - offset.y) / scale
                            onTapOnPlan(planX, planY)
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .onSizeChanged { imageSize = it }
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(File(planPath)),
                        contentDescription = "Floor Plan",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    // Render existing waypoints
                    waypoints.forEach { waypoint ->
                        MappingWaypointMarker(
                            waypoint = waypoint,
                            modifier = Modifier.offset {
                                IntOffset(
                                    waypoint.planX.roundToInt(),
                                    waypoint.planY.roundToInt()
                                )
                            }
                        )
                    }

                    // Show pending mark location
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
                                .background(Color(0xFF4CAF50).copy(alpha = 0.8f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.MyLocation,
                                contentDescription = "Marked location",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Instructions overlay
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .background(
                        Color.Black.copy(alpha = 0.7f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isMarkingOnPlan) Strings.locationMarkedTapPlace.get(lang)
                    else Strings.tapOnFloorPlanToMark.get(lang),
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
                if (statusMessage.isNotBlank() && !isMarkingOnPlan) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = statusMessage,
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Clear mark button
            if (isMarkingOnPlan) {
                IconButton(
                    onClick = onClearMark,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            CircleShape
                        )
                ) {
                    Icon(Icons.Default.Close, Strings.clearMark.get(lang), tint = Color.White)
                }
            }
        }
    } else {
        // No floor plan imported
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Map,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = Strings.noFloorPlanImportedMapping.get(lang),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun CameraMappingView(
    hasCameraPermission: Boolean,
    isTracking: Boolean,
    statusMessage: String,
    currentPositionX: Float,
    currentPositionY: Float,
    currentPositionZ: Float,
    lang: LocaleManager.AppLanguage = LocaleManager.AppLanguage.ENGLISH
) {
    if (!hasCameraPermission) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(Strings.cameraPermissionRequired.get(lang))
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1A1A1A))
        ) {
            // Status overlay
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.FiberManualRecord,
                        contentDescription = null,
                        tint = if (isTracking) Color.Green else Color.Red,
                        modifier = Modifier.height(12.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isTracking) Strings.tracking.get(lang) else Strings.notTracking.get(lang),
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (statusMessage.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = statusMessage,
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Position info
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            ) {
                Text(
                    Strings.position.get(lang),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "X: %.2f  Y: %.2f  Z: %.2f".format(
                        currentPositionX,
                        currentPositionY,
                        currentPositionZ
                    ),
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun MappingWaypointMarker(
    waypoint: Waypoint,
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
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
        waypoint.label?.let { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(
                        Color.White.copy(alpha = 0.8f),
                        RoundedCornerShape(2.dp)
                    )
                    .padding(horizontal = 2.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceWaypointDialog(
    onDismiss: () -> Unit,
    onConfirm: (String?, WaypointType) -> Unit,
    isMarkingOnPlan: Boolean = false,
    planX: Float = 0f,
    planY: Float = 0f,
    lang: LocaleManager.AppLanguage = LocaleManager.AppLanguage.ENGLISH
) {
    var label by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(WaypointType.HALLWAY) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isMarkingOnPlan) Strings.placeWaypointOnPlan.get(lang) else Strings.placeWaypointTitle.get(lang)) },
        text = {
            Column {
                if (isMarkingOnPlan) {
                    Text(
                        text = "Location: (%.0f, %.0f)".format(planX, planY),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                }
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text(Strings.labelOptional.get(lang)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedType.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(Strings.typeLabel.get(lang)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        WaypointType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    selectedType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(label.ifBlank { null }, selectedType)
            }) { Text(Strings.placeButton.get(lang)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(Strings.cancelButton.get(lang)) }
        }
    )
}

@Composable
private fun CameraOverlayControls(
    isRecording: Boolean,
    recordingDurationMs: Long,
    statusMessage: String,
    onToggleRecording: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(12.dp)
            .background(
                Color.Black.copy(alpha = 0.6f),
                RoundedCornerShape(8.dp)
            )
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isRecording) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.FiberManualRecord,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(6.dp))
                val seconds = (recordingDurationMs / 1000) % 60
                val minutes = (recordingDurationMs / 1000) / 60
                Text(
                    text = "REC %02d:%02d".format(minutes, seconds),
                    color = Color.Red,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        if (statusMessage.isNotBlank()) {
            Text(
                text = statusMessage,
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun FloorTransitionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String?, WaypointType, Long) -> Unit,
    lang: LocaleManager.AppLanguage = LocaleManager.AppLanguage.ENGLISH
) {
    var label by remember { mutableStateOf("") }
    var isElevator by remember { mutableStateOf(false) }
    var connectedFloorId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.floorTransitionTitle.get(lang)) },
        text = {
            Column {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text(Strings.labelStairwell.get(lang)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Row {
                    FilledTonalButton(
                        onClick = { isElevator = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Stairs, null)
                        Spacer(Modifier.width(4.dp))
                        Text(Strings.stairsButton.get(lang))
                    }
                    Spacer(Modifier.width(8.dp))
                    FilledTonalButton(
                        onClick = { isElevator = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Elevator, null)
                        Spacer(Modifier.width(4.dp))
                        Text(Strings.elevatorButton.get(lang))
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = connectedFloorId,
                    onValueChange = { connectedFloorId = it },
                    label = { Text(Strings.connectedFloorIdLabel.get(lang)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val floorId = connectedFloorId.toLongOrNull() ?: 0L
                onConfirm(
                    label.ifBlank { null },
                    if (isElevator) WaypointType.ELEVATOR else WaypointType.STAIRWELL,
                    floorId
                )
            }) { Text(Strings.placeButton.get(lang)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(Strings.cancelButton.get(lang)) }
        }
    )
}
