package com.stretchie.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stretchie.data.model.Pose
import com.stretchie.data.model.Routine
import com.stretchie.ui.theme.*

@Composable
fun RoutinesHomeScreen(
    viewModel: RoutinesHomeViewModel,
    allPoses: List<Pose>,
    onNavigateToSettings: () -> Unit,
    onNavigateToCreateRoutine: () -> Unit,
    onNavigateToEditRoutine: (String) -> Unit,
    onNavigateToActiveRoutine: (String) -> Unit
) {
    val routines by viewModel.routines.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(colors = listOf(LightPeach, DarkPeach)))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 55.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Stretchie",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = OrangePrimary
                        )
                        Text(
                            text = "Your stretching routines",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurface.copy(alpha = 0.6f)
                        )
                    }
                    Button(
                        onClick = onNavigateToSettings,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Surface)
                    ) {
                        Icon(Icons.Rounded.Settings, contentDescription = null, tint = OnSurface)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Settings", color = OnSurface, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Routines",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    TextButton(onClick = onNavigateToCreateRoutine) {
                        Icon(Icons.Rounded.Add, contentDescription = null, tint = OrangePrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New", color = OrangePrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (routines.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No routines yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = OnSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "Tap New to create your first routine",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            } else {
                items(routines, key = { it.id }) { routine ->
                    RoutineCard(
                        routine = routine,
                        allPoses = allPoses,
                        onPlay = { onNavigateToActiveRoutine(routine.id) },
                        onEdit = { onNavigateToEditRoutine(routine.id) },
                        onDelete = { viewModel.deleteRoutine(routine.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onNavigateToCreateRoutine,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, tint = OnPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create New Routine", color = OnPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun RoutineCard(
    routine: Routine,
    allPoses: List<Pose>,
    onPlay: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val includedPoses = allPoses.filter { pose ->
        val ps = routine.poseSettings[pose.id]
        ps == null || ps.included
    }
    val totalSeconds = includedPoses.sumOf { pose ->
        val ps = routine.poseSettings[pose.id]
        val dur = if (ps != null && ps.duration > 0) ps.duration else pose.defaultDurationSeconds
        val intv = if (ps != null && ps.intervalCount > 0) ps.intervalCount else pose.defaultIntervalCount
        dur * intv
    }
    val totalMinutes = totalSeconds / 60
    val metaText = "${includedPoses.size} poses  •  ~$totalMinutes min"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFE0B2)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.SelfImprovement,
                contentDescription = null,
                tint = OrangePrimary,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = routine.name,
                fontWeight = FontWeight.Bold,
                color = OnSurface,
                fontSize = 16.sp
            )
            Text(
                text = metaText,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurface.copy(alpha = 0.6f)
            )
        }

        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "More", tint = OnSurface.copy(alpha = 0.6f))
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = { showMenu = false; onEdit() },
                    leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = { showMenu = false; onDelete() },
                    leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null) }
                )
            }
        }

        IconButton(
            onClick = onPlay,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(OrangePrimary)
        ) {
            Icon(Icons.Rounded.PlayArrow, contentDescription = "Play", tint = OnPrimary)
        }
    }
}
