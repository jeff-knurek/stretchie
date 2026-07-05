package com.stretchie.data.model

data class RoutinePoseSettings(
    val included: Boolean = true,
    val duration: Int = 0,
    val intervalCount: Int = 0
)

data class Routine(
    val id: String,
    val name: String,
    val poseSettings: Map<String, RoutinePoseSettings> = emptyMap()
)
