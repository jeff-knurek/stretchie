package com.stretchie.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.stretchie.presentation.create_routine.CreateRoutineScreen
import com.stretchie.presentation.create_routine.CreateRoutineViewModel
import com.stretchie.presentation.create_routine.EditRoutinePoseScreen
import com.stretchie.presentation.home.RoutinesHomeScreen
import com.stretchie.presentation.home.RoutinesHomeViewModel
import com.stretchie.presentation.routine.ActiveRoutineScreen
import com.stretchie.presentation.routine.RoutineViewModel
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
    val allPoses = remember { poseRepository.getAllPoses() }

    NavHost(navController = navController, startDestination = "routines_home") {
        composable("routines_home") {
            val viewModel: RoutinesHomeViewModel = viewModel(
                factory = RoutinesHomeViewModel.Factory(settingsRepository)
            )
            RoutinesHomeScreen(
                viewModel = viewModel,
                allPoses = allPoses,
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToCreateRoutine = { navController.navigate("create_routine") },
                onNavigateToEditRoutine = { routineId -> navController.navigate("edit_routine/$routineId") },
                onNavigateToActiveRoutine = { routineId -> navController.navigate("active_routine/$routineId") }
            )
        }

        composable("settings") {
            val viewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(settingsRepository)
            )
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("create_routine") { backStackEntry ->
            val viewModel: CreateRoutineViewModel = viewModel(
                factory = CreateRoutineViewModel.Factory(settingsRepository)
            )
            CreateRoutineScreen(
                viewModel = viewModel,
                navBackStackEntry = backStackEntry,
                allPoses = allPoses,
                isEditing = false,
                navController = navController,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "edit_routine/{routineId}",
            arguments = listOf(navArgument("routineId") { type = NavType.StringType })
        ) { backStackEntry ->
            val routineId = backStackEntry.arguments?.getString("routineId") ?: return@composable
            val viewModel: CreateRoutineViewModel = viewModel(
                factory = CreateRoutineViewModel.Factory(settingsRepository)
            )
            LaunchedEffect(routineId) {
                viewModel.loadRoutine(routineId)
            }
            CreateRoutineScreen(
                viewModel = viewModel,
                navBackStackEntry = backStackEntry,
                allPoses = allPoses,
                isEditing = true,
                navController = navController,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "edit_routine_pose/{poseId}/{duration}/{intervalCount}",
            arguments = listOf(
                navArgument("poseId") { type = NavType.StringType },
                navArgument("duration") { type = NavType.IntType },
                navArgument("intervalCount") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val poseId = backStackEntry.arguments?.getString("poseId") ?: return@composable
            val duration = backStackEntry.arguments?.getInt("duration") ?: 0
            val intervalCount = backStackEntry.arguments?.getInt("intervalCount") ?: 1
            EditRoutinePoseScreen(
                poseId = poseId,
                initialDuration = duration,
                initialIntervalCount = intervalCount,
                poseRepository = poseRepository,
                navController = navController
            )
        }

        composable(
            route = "active_routine/{routineId}",
            arguments = listOf(navArgument("routineId") { type = NavType.StringType })
        ) { backStackEntry ->
            val routineId = backStackEntry.arguments?.getString("routineId") ?: return@composable
            val viewModel: RoutineViewModel = viewModel(
                factory = RoutineViewModel.Factory(routineId, poseRepository, audioManager, settingsRepository)
            )
            ActiveRoutineScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
