package com.stretchie.presentation.create_routine

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.stretchie.data.model.Pose
import com.stretchie.data.model.RoutinePoseSettings
import com.stretchie.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoutineScreen(
    viewModel: CreateRoutineViewModel,
    navBackStackEntry: NavBackStackEntry,
    allPoses: List<Pose>,
    isEditing: Boolean,
    navController: NavController,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    val resultPoseId by navBackStackEntry.savedStateHandle
        .getStateFlow<String?>("result_pose_id", null).collectAsState()

    LaunchedEffect(resultPoseId) {
        val poseId = resultPoseId ?: return@LaunchedEffect
        val dur = navBackStackEntry.savedStateHandle.get<Int>("result_duration") ?: return@LaunchedEffect
        val intv = navBackStackEntry.savedStateHandle.get<Int>("result_interval_count") ?: return@LaunchedEffect
        viewModel.updatePoseConfig(poseId, dur, intv)
        navBackStackEntry.savedStateHandle.remove<String>("result_pose_id")
    }

    val includedCount = allPoses.count { pose ->
        val ps = state.poseSettings[pose.id]
        ps == null || ps.included
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(colors = listOf(LightPeach, DarkPeach))),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) state.name.ifEmpty { "Edit Routine" } else "New Routine",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = OnSurface,
                    navigationIconContentColor = OnSurface
                )
            )
        },
        containerColor = Color.Transparent,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = { viewModel.saveRoutine(onNavigateBack) },
                    enabled = state.name.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("Save Routine", color = OnPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Routine Name",
                    style = MaterialTheme.typography.labelLarge,
                    color = OnSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { viewModel.updateName(it) },
                    placeholder = { Text("e.g. Morning Stretch") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = OnSurface.copy(alpha = 0.3f),
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface
                    )
                )
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Poses",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    Badge(containerColor = OrangePrimary) {
                        Text("$includedCount", color = OnPrimary)
                    }
                }
            }

            items(allPoses, key = { it.id }) { pose ->
                val ps = state.poseSettings[pose.id]
                val included = ps?.included ?: true
                val duration = if (ps != null && ps.duration > 0) ps.duration else pose.defaultDurationSeconds
                val intervalCount = if (ps != null && ps.intervalCount > 0) ps.intervalCount else pose.defaultIntervalCount

                RoutinePoseRow(
                    pose = pose,
                    included = included,
                    duration = duration,
                    intervalCount = intervalCount,
                    onToggle = { viewModel.togglePoseIncluded(pose.id, true) },
                    onEdit = {
                        navController.navigate("edit_routine_pose/${pose.id}/$duration/$intervalCount")
                    }
                )
            }
        }
    }
}

@Composable
private fun RoutinePoseRow(
    pose: Pose,
    included: Boolean,
    duration: Int,
    intervalCount: Int,
    onToggle: () -> Unit,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .clickable(onClick = onEdit)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = pose.imageRes,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFEAEAEA))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = pose.name, fontWeight = FontWeight.Bold, color = OnSurface)
            Text(
                text = "${duration}s • $intervalCount interval" + if (intervalCount > 1) "s" else "",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurface.copy(alpha = 0.6f)
            )
        }

        Switch(
            checked = included,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = OnPrimary,
                checkedTrackColor = OrangePrimary
            )
        )
    }
}
