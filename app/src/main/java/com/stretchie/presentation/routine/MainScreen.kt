package com.stretchie.presentation.routine

import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stretchie.ui.theme.*

@Composable
fun MainScreen(viewModel: RoutineViewModel, onNavigateToSettings: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    DisposableEffect(state.isRunning) {
        val window = (context as? android.app.Activity)?.window
        if (state.isRunning) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    Column(
            modifier =
                    Modifier.fillMaxSize()
                            .background(
                                    Brush.linearGradient(colors = listOf(LightPeach, DarkPeach))
                            )
                            .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        if (state.currentPose != null) {
            Image(
                    painter = painterResource(id = state.currentPose!!.imageRes),
                    contentDescription = state.currentPose!!.name,
                    modifier =
                            Modifier.size(300.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Surface)
            )
        } else {
            Box(
                    modifier =
                            Modifier.size(300.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Surface),
                    contentAlignment = Alignment.Center
            ) { Text("No Poses", color = OnSurface) }
        }

        Spacer(modifier = Modifier.height(32.dp))

        state.currentPose?.let { pose ->
            Text(
                    text = pose.name,
                    style = MaterialTheme.typography.headlineLarge,
                    color = OnSurface
            )
            Text(
                    text = "Pose ${state.currentPoseIndex + 1} of ${state.poses.size}" +
                            if (state.currentIntervalCount > 1) " • Interval ${state.currentInterval} of ${state.currentIntervalCount}" else "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnSurface.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
                text = formatTime(state.secondsRemaining),
                style =
                        MaterialTheme.typography.displayLarge.copy(
                                fontSize = 80.sp,
                                color = OrangePrimary
                        )
        )

        Spacer(modifier = Modifier.weight(1f))

        if (state.isRunning) {
            ActiveControls(
                    onPause = { viewModel.togglePlayPause() },
                    onNext = { viewModel.nextPose() },
                    onPrevious = { viewModel.previousPose() }
            )
        } else {
            IdleControls(
                    onPlay = { viewModel.togglePlayPause() },
                    onSettings = onNavigateToSettings
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ActiveControls(onPause: () -> Unit, onNext: () -> Unit, onPrevious: () -> Unit) {
    Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", tint = OnSurface)
        }

        Spacer(modifier = Modifier.width(24.dp))

        Button(
                onClick = onPause,
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                    imageVector = Icons.Rounded.Pause,
                    contentDescription = "Pause",
                    tint = OnPrimary,
                    modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(24.dp))

        IconButton(onClick = onNext) {
            Icon(Icons.Rounded.SkipNext, contentDescription = "Next", tint = OnSurface)
        }
    }
}

@Composable
fun IdleControls(onPlay: () -> Unit, onSettings: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Button(
                onClick = onPlay,
                modifier = Modifier.size(140.dp, 48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
        ) {
            Icon(Icons.Rounded.PlayArrow, contentDescription = "Play", tint = OnPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Play", color = OnPrimary, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Button(
                onClick = onSettings,
                modifier = Modifier.size(140.dp, 48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Surface)
        ) {
            Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = OnSurface)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Settings", color = OnSurface, fontWeight = FontWeight.Bold)
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
