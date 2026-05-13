package com.stretchie.data.repository

import com.stretchie.R
import com.stretchie.data.model.Pose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Repository providing access to the available stretching poses. Currently using hardcoded data as
 * per the implementation plan.
 */
class PoseRepository {

    /** Returns a list of all available poses. */
    fun getAllPoses(): List<Pose> {
        return listOf(
                Pose(
                        id = "leg_extension",
                        name = "Leg Extension",
                        imageRes = R.drawable.leg_extension,
                        defaultDurationSeconds = 30
                ),
                Pose(
                        id = "quad-stretch",
                        name = "Quad Stretch",
                        imageRes = R.drawable.quad_stretch,
                        defaultDurationSeconds = 30
                )
                // Add more poses here as assets are added
                )
    }

    /** Returns a flow of all available poses. */
    fun getAllPosesFlow(): Flow<List<Pose>> {
        return flowOf(getAllPoses())
    }

    /** Returns a specific pose by ID. */
    fun getPoseById(id: String): Pose? {
        return getAllPoses().find { it.id == id }
    }
}
