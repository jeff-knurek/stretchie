package com.stretchie.presentation.settings

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import coil3.compose.AsyncImage
import androidx.compose.ui.unit.dp
import com.stretchie.UserSettings
import com.stretchie.data.model.Pose
import com.stretchie.data.repository.PoseRepository
import com.stretchie.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEditPose: (String) -> Unit,
    poseRepository: PoseRepository
) {
    val settings by viewModel.userSettings.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Poses", "Other")
    
    val poses = remember { poseRepository.getAllPoses() }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(LightPeach, DarkPeach)
                )
            ),
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
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
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = OrangePrimary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) },
                        selectedContentColor = OrangePrimary,
                        unselectedContentColor = OnSurface.copy(alpha = 0.6f)
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> PosesTabContent(
                    poses = poses, 
                    settings = settings, 
                    onToggleSkip = { poseId, dur, intv, skip ->
                        viewModel.toggleSkipPose(poseId, dur, intv, skip)
                    },
                    onEditPose = onNavigateToEditPose
                )
                1 -> OtherTabContent(
                    settings = settings,
                    onPoseChangeSoundPicked = { viewModel.updatePoseChangeSound(it) },
                    onIntervalSoundPicked = { viewModel.updateIntervalSound(it) }
                )
            }
        }
    }
}

@Composable
fun PosesTabContent(
    poses: List<Pose>, 
    settings: UserSettings,
    onToggleSkip: (String, Int, Int, Boolean) -> Unit,
    onEditPose: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(poses) { pose ->
            val override = settings.poseOverridesMap[pose.id]
            val duration = if (override != null && override.duration > 0) override.duration else pose.defaultDurationSeconds
            val intervalCount = if (override != null && override.intervalCount > 0) override.intervalCount else 1
            val isSkipped = override?.getIsSkipped() ?: false

            PoseSettingsItem(
                pose = pose,
                duration = duration,
                intervalCount = intervalCount,
                isSkipped = isSkipped,
                onToggleSkip = { skipped ->
                    onToggleSkip(pose.id, duration, intervalCount, skipped)
                },
                onClick = { onEditPose(pose.id) }
            )
        }
    }
}

@Composable
fun PoseSettingsItem(
    pose: Pose,
    duration: Int,
    intervalCount: Int,
    isSkipped: Boolean,
    onToggleSkip: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = pose.imageRes,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFEAEAEA))
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = pose.name, fontWeight = FontWeight.Bold, color = OnSurface)
            Text(
                text = "${duration}s • $intervalCount interval" + if(intervalCount > 1) "s" else "",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurface.copy(alpha = 0.6f)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Skip", style = MaterialTheme.typography.bodyMedium, color = OnSurface)
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isSkipped,
                onCheckedChange = onToggleSkip,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = OnPrimary,
                    checkedTrackColor = OrangePrimary
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Rounded.ChevronRight, contentDescription = "Edit", tint = OnSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun OtherTabContent(
    settings: UserSettings,
    onPoseChangeSoundPicked: (String) -> Unit,
    onIntervalSoundPicked: (String) -> Unit
) {
    val context = LocalContext.current

    val poseChangeLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            onPoseChangeSoundPicked(uri?.toString() ?: "")
        }
    }

    val intervalSoundLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            onIntervalSoundPicked(uri?.toString() ?: "")
        }
    }

    fun getRingtoneName(uriString: String): String {
        if (uriString.isEmpty()) return "Silent"
        try {
            val uri = Uri.parse(uriString)
            val ringtone = RingtoneManager.getRingtone(context, uri)
            return ringtone?.getTitle(context) ?: "Unknown"
        } catch (e: Exception) {
            return "Default"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Sound Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnSurface)
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Surface)
                .clickable {
                    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                    }
                    poseChangeLauncher.launch(intent)
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Pose Change Sound", color = OnSurface)
            Text(
                text = if (settings.poseChangeSound.isNotEmpty()) getRingtoneName(settings.poseChangeSound) else "Default", 
                color = OnSurface.copy(alpha = 0.6f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Surface)
                .clickable {
                    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                    }
                    intervalSoundLauncher.launch(intent)
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Interval Sound", color = OnSurface)
            Text(
                text = if (settings.intervalSound.isNotEmpty()) getRingtoneName(settings.intervalSound) else "Default", 
                color = OnSurface.copy(alpha = 0.6f)
            )
        }
    }
}
