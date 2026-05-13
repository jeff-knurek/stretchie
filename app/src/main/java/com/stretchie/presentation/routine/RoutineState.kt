package com.stretchie.presentation.routine

import com.stretchie.data.model.Pose

data class RoutineState(
        val isRunning: Boolean = false,
        val currentPoseIndex: Int = 0,
        val poses: List<Pose> = emptyList(),
        val secondsRemaining: Int = 0,
        val isCompleted: Boolean = false
) {
    val currentPose: Pose?
        get() = poses.getOrNull(currentPoseIndex)
}
