package com.subnavar.app.ui.mapping

import android.Manifest
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Elevator
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stairs
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.subnavar.app.domain.model.WaypointType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MappingScreen(
    viewModel: MappingViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
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
                            "Mapping: ${uiState.floor?.name ?: ""}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "${uiState.waypointCount} waypoints placed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
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
                        Text(if (uiState.isMappingActive) "Pause" else "Start")
                    }

                    // Place waypoint
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.showWaypointDialog() },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Waypoint")
                    }

                    // Floor transition
                    FilledTonalButton(
                        onClick = { viewModel.showFloorTransitionDialog() }
                    ) {
                        Icon(Icons.Default.Stairs, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Transition")
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
            if (!hasCameraPermission) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Camera permission is required for AR mapping")
                }
            } else {
                // Camera preview placeholder - on a real device this would be the ARCore camera feed
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
                                tint = if (uiState.isTracking) Color.Green else Color.Red,
                                modifier = Modifier.height(12.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (uiState.isTracking) "Tracking" else "Not Tracking",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (uiState.statusMessage.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = uiState.statusMessage,
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
                            "Position",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "X: %.2f  Y: %.2f  Z: %.2f".format(
                                uiState.currentPositionX,
                                uiState.currentPositionY,
                                uiState.currentPositionZ
                            ),
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }

    // Place Waypoint Dialog
    if (uiState.showWaypointDialog) {
        PlaceWaypointDialog(
            onDismiss = { viewModel.hideWaypointDialog() },
            onConfirm = { label, type ->
                viewModel.placeWaypoint(
                    label = label,
                    type = type,
                    planX = 0f,
                    planY = 0f,
                    capturedBitmap = null
                )
            }
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
                    planX = 0f,
                    planY = 0f
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceWaypointDialog(
    onDismiss: () -> Unit,
    onConfirm: (String?, WaypointType) -> Unit
) {
    var label by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(WaypointType.HALLWAY) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Place Waypoint") },
        text = {
            Column {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label (optional)") },
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
                        label = { Text("Type") },
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
            }) { Text("Place") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun FloorTransitionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String?, WaypointType, Long) -> Unit
) {
    var label by remember { mutableStateOf("") }
    var isElevator by remember { mutableStateOf(false) }
    var connectedFloorId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Floor Transition") },
        text = {
            Column {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label (e.g., Stairwell A)") },
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
                        Text("Stairs")
                    }
                    Spacer(Modifier.width(8.dp))
                    FilledTonalButton(
                        onClick = { isElevator = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Elevator, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Elevator")
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = connectedFloorId,
                    onValueChange = { connectedFloorId = it },
                    label = { Text("Connected Floor ID") },
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
            }) { Text("Place") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
