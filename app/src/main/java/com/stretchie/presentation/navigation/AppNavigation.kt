package com.stretchie.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.stretchie.data.repository.PoseRepository
import com.stretchie.data.repository.SettingsRepository
import com.stretchie.presentation.routine.MainScreen
import com.stretchie.presentation.routine.RoutineViewModel
import com.stretchie.presentation.settings.EditPoseScreen
import com.stretchie.presentation.settings.SettingsScreen
import com.stretchie.presentation.settings.SettingsViewModel
import com.stretchie.presentation.util.AudioManager

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository(context) }
    val audioManager = remember { AudioManager(context, settingsRepository) }
    val poseRepository = remember { PoseRepository() }

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            val viewModel: RoutineViewModel = viewModel(
                factory = RoutineViewModel.Factory(poseRepository, audioManager, settingsRepository)
            )
            MainScreen(
                viewModel = viewModel,
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("settings") {
            val viewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(settingsRepository)
            )
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditPose = { poseId -> navController.navigate("edit_pose/$poseId") },
                poseRepository = poseRepository
            )
        }
        composable(
            route = "edit_pose/{poseId}",
            arguments = listOf(navArgument("poseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val poseId = backStackEntry.arguments?.getString("poseId") ?: return@composable
            val viewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(settingsRepository)
            )
            EditPoseScreen(
                poseId = poseId,
                viewModel = viewModel,
                poseRepository = poseRepository,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
