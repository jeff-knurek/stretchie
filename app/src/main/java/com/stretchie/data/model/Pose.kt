package com.stretchie.data.model

import androidx.annotation.DrawableRes

/**
 * Data class representing a stretching pose.
 * 
 * @property id Unique identifier for the pose.
 * @property name Human-readable name of the pose.
 * @property imageRes Android drawable resource ID for the pose image.
 * @property defaultDurationSeconds Default duration for the pose in seconds.
 * @property isSkippable Whether the user can skip this pose.
 */
data class Pose(
    val id: String,
    val name: String,
    @DrawableRes val imageRes: Int,
    val defaultDurationSeconds: Int = 30,
    val isSkippable: Boolean = true
)
