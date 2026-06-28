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
                        id = "back_stretch",
                        name = "Back Stretch",
                        imageRes = R.drawable.back_stretch,
                        defaultDurationSeconds = 30
                ),
                Pose(
                        id = "tricep",
                        name = "Tricep",
                        imageRes = R.drawable.tricep,
                        defaultDurationSeconds = 30
                ),
                Pose(
                        id = "shoulder_opener",
                        name = "Shoulder Opener",
                        imageRes = R.drawable.shoulder_opener,
                        defaultDurationSeconds = 30
                ),
                Pose(
                        id = "standing_quad",
                        name = "Standing Quad",
                        imageRes = R.drawable.standing_quad,
                        defaultDurationSeconds = 30
                ),
                Pose(
                        id = "calf_raises",
                        name = "Calf Raises",
                        imageRes = R.drawable.calf_raises,
                        defaultDurationSeconds = 30
                ),
                Pose(
                        id = "calf_stretch_toes_on_wall",
                        name = "Calf Stretch Toes On Wall",
                        imageRes = R.drawable.calf_stretch_toes_on_wall,
                        defaultDurationSeconds = 30
                ),
                Pose(
                        id = "calf_stretch",
                        name = "Calf Stretch",
                        imageRes = R.drawable.calf_stretch,
                        defaultDurationSeconds = 30
                ),
                Pose(
                        id = "forward_fold",
                        name = "Forward Fold",
                        imageRes = R.drawable.forward_fold,
                        defaultDurationSeconds = 30
                ),
                Pose(
                        id = "side_lunge",
                        name = "Side Lunge",
                        imageRes = R.drawable.side_lunge,
                        defaultDurationSeconds = 30
                ),
                Pose(
                        id = "lunge",
                        name = "Lunge",
                        imageRes = R.drawable.lunge,
                        defaultDurationSeconds = 30
                ),
                Pose(
                        id = "lunge_with_twist",
                        name = "Lunge With Twist",
                        imageRes = R.drawable.lunge_with_twist,
                        defaultDurationSeconds = 30
                ),
                Pose(
                        id = "cat_cow",
                        name = "Cat Cow",
                        imageRes = R.drawable.cat_cow,
                        defaultDurationSeconds = 30
                ),
                Pose(
                        id = "hero",
                        name = "Hero",
                        imageRes = R.drawable.hero,
                        defaultDurationSeconds = 30
                ),
                Pose(
                        id = "pigeon",
                        name = "Pigeon",
                        imageRes = R.drawable.pigeon,
                        defaultDurationSeconds = 30
                ),
                Pose(
                        id = "butterfly",
                        name = "Butterfly",
                        imageRes = R.drawable.butterfly,
                        defaultDurationSeconds = 30
                ),
                Pose(
                        id = "crunch",
                        name = "Crunch",
                        imageRes = R.drawable.crunch,
                        defaultDurationSeconds = 30
                ),
                Pose(
                        id = "figure_four",
                        name = "Figure Four",
                        imageRes = R.drawable.figure_four,
                        defaultDurationSeconds = 30
                ),
                Pose(
                        id = "ankle_rotation",
                        name = "Ankle Rotation",
                        imageRes = R.drawable.ankle_rotation,
                        defaultDurationSeconds = 30
                ),
                Pose(
                        id = "it_band",
                        name = "It Band",
                        imageRes = R.drawable.it_band,
                        defaultDurationSeconds = 30
                )
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
