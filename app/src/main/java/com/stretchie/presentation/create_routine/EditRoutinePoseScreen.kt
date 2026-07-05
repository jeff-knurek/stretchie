package com.stretchie.presentation.create_routine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.stretchie.data.repository.PoseRepository
import com.stretchie.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRoutinePoseScreen(
    poseId: String,
    initialDuration: Int,
    initialIntervalCount: Int,
    poseRepository: PoseRepository,
    navController: NavController
) {
    val pose = remember(poseId) { poseRepository.getPoseById(poseId) }
    var duration by remember { mutableIntStateOf(if (initialDuration > 0) initialDuration else pose?.defaultDurationSeconds ?: 30) }
    var intervalCount by remember { mutableIntStateOf(if (initialIntervalCount > 0) initialIntervalCount else pose?.defaultIntervalCount ?: 1) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(colors = listOf(LightPeach, DarkPeach))),
        topBar = {
            TopAppBar(
                title = { Text("Edit ${pose?.name ?: "Pose"}", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
        containerColor = Color.Transparent
    ) { padding ->
        if (pose == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Pose not found", color = OnSurface)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Duration (Seconds)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { if (duration > 5) duration -= 5 },
                            colors = ButtonDefaults.buttonColors(containerColor = LightPeach)
                        ) { Text("-", color = OnSurface) }
                        Spacer(modifier = Modifier.width(24.dp))
                        Text(
                            "$duration",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = OrangePrimary
                        )
                        Spacer(modifier = Modifier.width(24.dp))
                        Button(
                            onClick = { duration += 5 },
                            colors = ButtonDefaults.buttonColors(containerColor = LightPeach)
                        ) { Text("+", color = OnSurface) }
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Interval Count",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { if (intervalCount > 1) intervalCount -= 1 },
                            colors = ButtonDefaults.buttonColors(containerColor = LightPeach)
                        ) { Text("-", color = OnSurface) }
                        Spacer(modifier = Modifier.width(24.dp))
                        Text(
                            "$intervalCount",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = OrangePrimary
                        )
                        Spacer(modifier = Modifier.width(24.dp))
                        Button(
                            onClick = { intervalCount += 1 },
                            colors = ButtonDefaults.buttonColors(containerColor = LightPeach)
                        ) { Text("+", color = OnSurface) }
                    }
                }
            }

            AsyncImage(
                model = pose.imageRes,
                contentDescription = pose.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFEAEAEA))
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    navController.previousBackStackEntry?.savedStateHandle?.apply {
                        set("result_pose_id", poseId)
                        set("result_duration", duration)
                        set("result_interval_count", intervalCount)
                    }
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                Text("Save Changes", color = OnPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
