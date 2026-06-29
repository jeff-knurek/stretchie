package com.stretchie.presentation.routine

import com.stretchie.data.model.Pose
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RoutineStateTest {

    @Test
    fun `currentPose returns null when poses list is empty`() {
        val state = RoutineState(poses = emptyList())
        assertNull(state.currentPose)
    }

    @Test
    fun `currentPose returns correct pose based on currentPoseIndex`() {
        val poses = listOf(
            Pose(id = "pose1", name = "Pose 1", imageRes = 1, defaultDurationSeconds = 10),
            Pose(id = "pose2", name = "Pose 2", imageRes = 2, defaultDurationSeconds = 20)
        )
        
        val state1 = RoutineState(poses = poses, currentPoseIndex = 0)
        assertEquals("Pose 1", state1.currentPose?.name)

        val state2 = RoutineState(poses = poses, currentPoseIndex = 1)
        assertEquals("Pose 2", state2.currentPose?.name)
    }

    @Test
    fun `currentPose returns null when currentPoseIndex is out of bounds`() {
        val poses = listOf(
            Pose(id = "pose1", name = "Pose 1", imageRes = 1, defaultDurationSeconds = 10)
        )
        
        val state = RoutineState(poses = poses, currentPoseIndex = 5)
        assertNull(state.currentPose)
    }
}
