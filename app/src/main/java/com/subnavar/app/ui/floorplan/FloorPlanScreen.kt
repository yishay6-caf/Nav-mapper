package com.subnavar.app.ui.floorplan

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.subnavar.app.domain.model.Building
import com.subnavar.app.domain.model.Floor
import com.subnavar.app.domain.model.Waypoint
import com.subnavar.app.ui.common.EmptyStateMessage
import com.subnavar.app.ui.common.LoadingIndicator
import java.io.File
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloorPlanScreen(
    viewModel: FloorPlanViewModel = hiltViewModel(),
    onNavigateToMapping: (Long, Long) -> Unit = { _, _ -> },
    onNavigateToStreetView: (Long) -> Unit = { }
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Move launcher registration to top level so it has stable lifecycle
    val floorPlanPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
                // Permission may already be granted or not persistable
            }
            viewModel.importFloorPlan(it)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            uiState.selectedFloor != null -> uiState.selectedFloor!!.name
                            uiState.selectedBuilding != null -> uiState.selectedBuilding!!.name
                            else -> "Lot25 Map"
                        }
                    )
                },
                navigationIcon = {
                    if (uiState.selectedBuilding != null) {
                        IconButton(onClick = {
                            if (uiState.selectedFloor != null) viewModel.goBackToFloors()
                            else viewModel.goBackToBuildings()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    if (uiState.selectedFloor != null) {
                        IconButton(onClick = { viewModel.deleteFloor() }) {
                            Icon(Icons.Default.Delete, "Delete Floor")
                        }
                    } else if (uiState.selectedBuilding != null) {
                        IconButton(onClick = { viewModel.deleteBuilding() }) {
                            Icon(Icons.Default.Delete, "Delete Building")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            when {
                uiState.selectedFloor != null -> {
                    Column(horizontalAlignment = Alignment.End) {
                        if (uiState.selectedFloor?.planImagePath == null) {
                            ExtendedFloatingActionButton(
                                onClick = {
                                    floorPlanPicker.launch(
                                        arrayOf(
                                            "image/*",
                                            "application/pdf"
                                        )
                                    )
                                },
                                icon = { Icon(Icons.Default.Upload, "Import") },
                                text = { Text("Import Plan") }
                            )
                        } else {
                            FloatingActionButton(
                                onClick = {
                                    val building = uiState.selectedBuilding ?: return@FloatingActionButton
                                    val floor = uiState.selectedFloor ?: return@FloatingActionButton
                                    onNavigateToMapping(building.id, floor.id)
                                }
                            ) {
                                Icon(Icons.Default.LocationOn, "Map")
                            }
                        }
                    }
                }
                uiState.selectedBuilding != null -> {
                    FloatingActionButton(onClick = { viewModel.showAddFloorDialog() }) {
                        Icon(Icons.Default.Add, "Add Floor")
                    }
                }
                else -> {
                    FloatingActionButton(onClick = { viewModel.showAddBuildingDialog() }) {
                        Icon(Icons.Default.Add, "Add Building")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when {
                uiState.isLoading -> LoadingIndicator()
                uiState.selectedFloor != null -> FloorDetailView(
                    floor = uiState.selectedFloor!!,
                    waypoints = uiState.waypoints,
                    onWaypointClick = { onNavigateToStreetView(it.id) }
                )
                uiState.selectedBuilding != null -> FloorListView(
                    floors = uiState.floors,
                    onFloorClick = { viewModel.selectFloor(it) }
                )
                else -> BuildingListView(
                    buildings = uiState.buildings,
                    onBuildingClick = { viewModel.selectBuilding(it) }
                )
            }
        }
    }

    // Add Building Dialog
    if (uiState.showAddBuildingDialog) {
        AddBuildingDialog(
            onDismiss = { viewModel.hideAddBuildingDialog() },
            onConfirm = { name, desc -> viewModel.addBuilding(name, desc) }
        )
    }

    // Add Floor Dialog
    if (uiState.showAddFloorDialog) {
        AddFloorDialog(
            onDismiss = { viewModel.hideAddFloorDialog() },
            onConfirm = { name, level -> viewModel.addFloor(name, level) }
        )
    }
}

@Composable
private fun BuildingListView(
    buildings: List<Building>,
    onBuildingClick: (Building) -> Unit
) {
    if (buildings.isEmpty()) {
        EmptyStateMessage(
            icon = Icons.Default.Apartment,
            message = "No buildings yet.\nTap + to add one."
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(buildings) { building ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onBuildingClick(building) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Apartment,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = building.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            building.description?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FloorListView(
    floors: List<Floor>,
    onFloorClick: (Floor) -> Unit
) {
    if (floors.isEmpty()) {
        EmptyStateMessage(
            icon = Icons.Default.Layers,
            message = "No floors yet.\nTap + to add a floor."
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(floors) { floor ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFloorClick(floor) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "B${floor.level}",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = floor.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (floor.planImagePath != null) "Floor plan imported" else "No floor plan",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (floor.planImagePath != null)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FloorDetailView(
    floor: Floor,
    waypoints: List<Waypoint>,
    onWaypointClick: (Waypoint) -> Unit
) {
    if (floor.planImagePath != null && File(floor.planImagePath).exists()) {
        InteractiveFloorPlan(
            planImagePath = floor.planImagePath,
            waypoints = waypoints,
            onWaypointClick = onWaypointClick
        )
    } else {
        EmptyStateMessage(
            icon = Icons.Default.Map,
            message = "No floor plan imported yet.\nTap the upload button to import one."
        )
    }
}

@Composable
fun InteractiveFloorPlan(
    planImagePath: String,
    waypoints: List<Waypoint>,
    onWaypointClick: (Waypoint) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
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
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        ) {
            Image(
                painter = rememberAsyncImagePainter(File(planImagePath)),
                contentDescription = "Floor Plan",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // Render waypoints on top of floor plan
            waypoints.forEach { waypoint ->
                WaypointMarker(
                    waypoint = waypoint,
                    onClick = { onWaypointClick(waypoint) },
                    modifier = Modifier.offset {
                        IntOffset(
                            waypoint.planX.roundToInt(),
                            waypoint.planY.roundToInt()
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun WaypointMarker(
    waypoint: Waypoint,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = when (waypoint.type) {
        com.subnavar.app.domain.model.WaypointType.STAIRWELL -> Color(0xFFFF9800)
        com.subnavar.app.domain.model.WaypointType.ELEVATOR -> Color(0xFF2196F3)
        com.subnavar.app.domain.model.WaypointType.ENTRANCE -> Color(0xFF4CAF50)
        com.subnavar.app.domain.model.WaypointType.POI -> Color(0xFFE91E63)
        else -> Color(0xFF607D8B)
    }

    Box(
        modifier = modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(color)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Composable
private fun AddBuildingDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Building") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Building Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, description.ifBlank { null }) },
                enabled = name.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddFloorDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Floor") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Floor Name (e.g., B1, B2)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = level,
                    onValueChange = { level = it },
                    label = { Text("Level Number (e.g., -1, -2)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val levelInt = level.toIntOrNull() ?: 0
                    onConfirm(name, levelInt)
                },
                enabled = name.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
