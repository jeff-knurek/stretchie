package com.stretchie.presentation.routine

import android.view.WindowManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.stretchie.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveRoutineScreen(viewModel: RoutineViewModel, onNavigateBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val animatedScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val timerScale = if (state.isTransitioning) animatedScale else 1.0f

    DisposableEffect(state.isRunning) {
        val window = (context as? android.app.Activity)?.window
        if (state.isRunning) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.routineName.ifEmpty { "Routine" },
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
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(colors = listOf(LightPeach, DarkPeach)))
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (state.currentPose != null) {
                AsyncImage(
                    model = state.currentPose!!.imageRes,
                    contentDescription = state.currentPose!!.name,
                    modifier = Modifier
                        .size(300.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFEAEAEA))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFEAEAEA)),
                    contentAlignment = Alignment.Center
                ) { Text("No Poses", color = OnSurface) }
            }

            Spacer(modifier = Modifier.height(24.dp))

            state.currentPose?.let { pose ->
                Text(
                    text = pose.name,
                    style = MaterialTheme.typography.headlineLarge,
                    color = OnSurface
                )
                Text(
                    text = "Pose ${state.currentPoseIndex + 1} of ${state.poses.size}" +
                        if (state.currentIntervalCount > 1) " • Interval ${state.currentInterval} of ${state.currentIntervalCount}" else ""
                )
                Text(
                    text = formatTotalTime(viewModel.getTotalRemainingTime()),
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnSurface.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.weight(0.6f))

            Text(
                text = formatTime(state.secondsRemaining),
                modifier = Modifier.graphicsLayer(scaleX = timerScale, scaleY = timerScale),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp, color = OrangePrimary)
            )

            Spacer(modifier = Modifier.weight(0.4f))

            if (state.isRunning) {
                ActiveControls(
                    onPause = { viewModel.togglePlayPause() },
                    onNext = { viewModel.nextPose() },
                    onPrevious = { viewModel.previousPose() }
                )
            } else {
                IdleControls(
                    onPlay = { viewModel.togglePlayPause() },
                    onPrevious = { viewModel.previousPose() },
                    onNext = { viewModel.nextPose() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
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
fun IdleControls(onPlay: () -> Unit, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", tint = OnSurface)
        }
        Spacer(modifier = Modifier.width(24.dp))
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
        Spacer(modifier = Modifier.width(24.dp))
        IconButton(onClick = onNext) {
            Icon(Icons.Rounded.SkipNext, contentDescription = "Next", tint = OnSurface)
        }
    }
}

private fun formatTime(seconds: Int): String {
    return when {
        seconds >= 60 -> String.format("%d:%02d", seconds / 60, seconds % 60)
        seconds >= 10 -> String.format("%02d", seconds)
        else -> String.format("%d", seconds)
    }
}

private fun formatTotalTime(seconds: Int): String {
    return String.format("%d:%02d", seconds / 60, seconds % 60)
}
