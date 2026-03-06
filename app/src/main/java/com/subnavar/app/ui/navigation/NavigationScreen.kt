package com.subnavar.app.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.FlagCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stairs
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.subnavar.app.domain.model.Waypoint
import com.subnavar.app.nav.PathFinder
import com.subnavar.app.ui.common.EmptyStateMessage
import com.subnavar.app.ui.common.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationScreen(
    viewModel: NavigationViewModel = hiltViewModel(),
    onNavigateToStreetView: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
                title = { Text("Navigate") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    if (uiState.startWaypoint != null || uiState.endWaypoint != null) {
                        IconButton(onClick = { viewModel.resetNavigation() }) {
                            Icon(Icons.Default.Close, "Reset")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Start/End selection panel
            NavigationPanel(
                startWaypoint = uiState.startWaypoint,
                endWaypoint = uiState.endWaypoint,
                isSelectingStart = uiState.isSelectingStart,
                onToggleMode = { viewModel.toggleSelectingMode() },
                onSwap = { viewModel.swapStartEnd() }
            )

            // Path result
            if (uiState.pathResult != null) {
                PathResultView(
                    result = uiState.pathResult!!,
                    onWaypointClick = { onNavigateToStreetView(it.id) }
                )
            } else if (uiState.isCalculating) {
                LoadingIndicator()
            } else if (uiState.isSelectingStart || uiState.endWaypoint == null) {
                val selectingLabel = if (uiState.isSelectingStart)
                    "Choose how to set your starting point:"
                else
                    "Choose how to set your destination:"

                Text(
                    text = selectingLabel,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                StartPointModeSelector(
                    selectedMode = uiState.startPointMode,
                    onModeSelected = { viewModel.setStartPointMode(it) }
                )

                Spacer(Modifier.height(8.dp))

                when (uiState.startPointMode) {
                    StartPointMode.SEARCH -> SearchModeContent(
                        searchQuery = uiState.searchQuery,
                        onQueryChange = { viewModel.updateSearchQuery(it) },
                        filteredWaypoints = uiState.filteredWaypoints,
                        startWaypointId = uiState.startWaypoint?.id,
                        endWaypointId = uiState.endWaypoint?.id,
                        onWaypointSelected = { wp ->
                            if (uiState.isSelectingStart) viewModel.selectStartWaypoint(wp)
                            else viewModel.selectEndWaypoint(wp)
                        }
                    )
                    StartPointMode.CAMERA_AR -> CameraArModeContent(
                        isLocating = uiState.isArLocating,
                        message = uiState.arLocateMessage,
                        onStartLocating = { viewModel.startArLocating() },
                        onStopLocating = { viewModel.stopArLocating() }
                    )
                    StartPointMode.ROOM_NUMBER -> RoomNumberModeContent(
                        roomQuery = uiState.roomNumberQuery,
                        onQueryChange = { viewModel.updateRoomNumberQuery(it) },
                        filteredWaypoints = uiState.filteredWaypoints,
                        startWaypointId = uiState.startWaypoint?.id,
                        endWaypointId = uiState.endWaypoint?.id,
                        onWaypointSelected = { wp ->
                            if (uiState.isSelectingStart) viewModel.selectStartWaypoint(wp)
                            else viewModel.selectEndWaypoint(wp)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StartPointModeSelector(
    selectedMode: StartPointMode,
    onModeSelected: (StartPointMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ModeTab(
            icon = Icons.Default.Search,
            label = "Search",
            isSelected = selectedMode == StartPointMode.SEARCH,
            onClick = { onModeSelected(StartPointMode.SEARCH) },
            modifier = Modifier.weight(1f)
        )
        ModeTab(
            icon = Icons.Default.CameraAlt,
            label = "Camera",
            isSelected = selectedMode == StartPointMode.CAMERA_AR,
            onClick = { onModeSelected(StartPointMode.CAMERA_AR) },
            modifier = Modifier.weight(1f)
        )
        ModeTab(
            icon = Icons.Default.MeetingRoom,
            label = "Room #",
            isSelected = selectedMode == StartPointMode.ROOM_NUMBER,
            onClick = { onModeSelected(StartPointMode.ROOM_NUMBER) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ModeTab(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isSelected) {
        FilledTonalButton(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Icon(icon, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun SearchModeContent(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    filteredWaypoints: List<Waypoint>,
    startWaypointId: Long?,
    endWaypointId: Long?,
    onWaypointSelected: (Waypoint) -> Unit
) {
    Column {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Search waypoints by name or type...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(8.dp))
        if (filteredWaypoints.isNotEmpty()) {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredWaypoints) { waypoint ->
                    WaypointSelectionCard(
                        waypoint = waypoint,
                        isStart = waypoint.id == startWaypointId,
                        isEnd = waypoint.id == endWaypointId,
                        onClick = { onWaypointSelected(waypoint) }
                    )
                }
            }
        } else {
            EmptyStateMessage(
                icon = Icons.Default.Navigation,
                message = if (searchQuery.isNotEmpty())
                    "No waypoints match your search."
                else
                    "No waypoints mapped yet.\nMap a building first to navigate."
            )
        }
    }
}

@Composable
private fun CameraArModeContent(
    isLocating: Boolean,
    message: String,
    onStartLocating: () -> Unit,
    onStopLocating: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "AR Location Detection",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (isLocating) message
                    else "Use your camera to detect your current location by matching visual features against mapped waypoints.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(20.dp))
                if (isLocating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(onClick = onStopLocating) {
                        Text("Cancel")
                    }
                } else {
                    Button(
                        onClick = onStartLocating,
                        modifier = Modifier.fillMaxWidth(0.7f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Start Camera Scan")
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Requires mapped waypoints with photos",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun RoomNumberModeContent(
    roomQuery: String,
    onQueryChange: (String) -> Unit,
    filteredWaypoints: List<Waypoint>,
    startWaypointId: Long?,
    endWaypointId: Long?,
    onWaypointSelected: (Waypoint) -> Unit
) {
    Column {
        OutlinedTextField(
            value = roomQuery,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Enter room number (e.g., 101, B2-05)...") },
            leadingIcon = { Icon(Icons.Default.DoorFront, null) },
            trailingIcon = {
                if (roomQuery.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, "Clear")
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(8.dp))
        if (filteredWaypoints.isNotEmpty()) {
            Text(
                text = "${filteredWaypoints.size} rooms found",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(4.dp))
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredWaypoints) { waypoint ->
                    WaypointSelectionCard(
                        waypoint = waypoint,
                        isStart = waypoint.id == startWaypointId,
                        isEnd = waypoint.id == endWaypointId,
                        onClick = { onWaypointSelected(waypoint) }
                    )
                }
            }
        } else {
            EmptyStateMessage(
                icon = Icons.Default.MeetingRoom,
                message = if (roomQuery.isNotEmpty())
                    "No rooms match your search."
                else
                    "No rooms mapped yet.\nMap rooms first, then search by number."
            )
        }
    }
}

@Composable
private fun NavigationPanel(
    startWaypoint: Waypoint?,
    endWaypoint: Waypoint?,
    isSelectingStart: Boolean,
    onToggleMode: () -> Unit,
    onSwap: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Start
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelectingStart) MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent
                        )
                        .clickable { onToggleMode() }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = startWaypoint?.label ?: "Select start point",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (startWaypoint != null)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(4.dp))

                // End
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (!isSelectingStart) MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent
                        )
                        .clickable { onToggleMode() }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.FlagCircle,
                        contentDescription = null,
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = endWaypoint?.label ?: "Select destination",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (endWaypoint != null)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Swap button
            IconButton(
                onClick = onSwap,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Icon(Icons.Default.SwapVert, "Swap")
            }
        }
    }
}

@Composable
private fun PathResultView(
    result: PathFinder.PathResult,
    onWaypointClick: (Waypoint) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Summary
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Directions, null, tint = MaterialTheme.colorScheme.primary)
                    Text("%.0f m".format(result.totalDistance), fontWeight = FontWeight.Bold)
                    Text("Distance", style = MaterialTheme.typography.labelSmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                    Text("${result.waypoints.size}", fontWeight = FontWeight.Bold)
                    Text("Waypoints", style = MaterialTheme.typography.labelSmall)
                }
                if (result.floorTransitions > 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Stairs, null, tint = MaterialTheme.colorScheme.tertiary)
                        Text("${result.floorTransitions}", fontWeight = FontWeight.Bold)
                        Text("Floor changes", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Step-by-step path
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(result.waypoints) { waypoint ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onWaypointClick(waypoint) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (waypoint.isFloorTransition)
                            MaterialTheme.colorScheme.tertiaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = waypoint.label ?: "Waypoint ${waypoint.id}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = waypoint.type.name.lowercase()
                                    .replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WaypointSelectionCard(
    waypoint: Waypoint,
    isStart: Boolean,
    isEnd: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isStart -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                isEnd -> Color(0xFFF44336).copy(alpha = 0.15f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when {
                    isStart -> Icons.Default.MyLocation
                    isEnd -> Icons.Default.FlagCircle
                    else -> Icons.Default.LocationOn
                },
                contentDescription = null,
                tint = when {
                    isStart -> Color(0xFF4CAF50)
                    isEnd -> Color(0xFFF44336)
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                },
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = waypoint.label ?: "Waypoint ${waypoint.id}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = waypoint.type.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}
