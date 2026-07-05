package com.stretchie.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.stretchie.RoutineConfig
import com.stretchie.RoutinePoseConfig
import com.stretchie.UserSettings
import com.stretchie.data.model.Routine
import com.stretchie.data.model.RoutinePoseSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class SettingsRepository(private val context: Context) {

    private val Context.userSettingsStore: DataStore<UserSettings> by dataStore(
        fileName = "user_settings.pb",
        serializer = UserSettingsSerializer
    )

    val userSettingsFlow: Flow<UserSettings> = context.userSettingsStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(UserSettings.getDefaultInstance())
            } else {
                throw exception
            }
        }

    fun getRoutinesFlow(): Flow<List<Routine>> = userSettingsFlow.map { settings ->
        settings.routinesMap.values.map { config ->
            Routine(
                id = config.id,
                name = config.name,
                poseSettings = config.poseConfigsMap.mapValues { (_, pc) ->
                    RoutinePoseSettings(
                        included = pc.included,
                        duration = pc.duration,
                        intervalCount = pc.intervalCount
                    )
                }
            )
        }
    }

    suspend fun saveRoutine(routine: Routine) {
        context.userSettingsStore.updateData { currentSettings ->
            val poseConfigsBuilder = routine.poseSettings.entries.fold(
                RoutineConfig.newBuilder()
                    .setId(routine.id)
                    .setName(routine.name)
            ) { builder, (poseId, ps) ->
                builder.putPoseConfigs(
                    poseId,
                    RoutinePoseConfig.newBuilder()
                        .setIncluded(ps.included)
                        .setDuration(ps.duration)
                        .setIntervalCount(ps.intervalCount)
                        .build()
                )
                builder
            }
            currentSettings.toBuilder()
                .putRoutines(routine.id, poseConfigsBuilder.build())
                .build()
        }
    }

    suspend fun deleteRoutine(routineId: String) {
        context.userSettingsStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .removeRoutines(routineId)
                .build()
        }
    }

    suspend fun updateIntervalCount(count: Int) {
        context.userSettingsStore.updateData { currentSettings ->
            currentSettings.toBuilder().setIntervalCount(count).build()
        }
    }

    suspend fun updateIntervalDuration(durationSeconds: Int) {
        context.userSettingsStore.updateData { currentSettings ->
            currentSettings.toBuilder().setIntervalDuration(durationSeconds).build()
        }
    }

    suspend fun updatePoseOverride(poseId: String, isSkipped: Boolean, duration: Int, intervalCount: Int) {
        context.userSettingsStore.updateData { currentSettings ->
            val builder = currentSettings.toBuilder()
            val poseSettings = com.stretchie.PoseSettings.newBuilder()
                .setIsSkipped(isSkipped)
                .setDuration(duration)
                .setIntervalCount(intervalCount)
                .build()
            builder.putPoseOverrides(poseId, poseSettings)
            builder.build()
        }
    }

    suspend fun updatePoseChangeSound(uriString: String) {
        context.userSettingsStore.updateData { currentSettings ->
            currentSettings.toBuilder().setPoseChangeSound(uriString).build()
        }
    }

    suspend fun updateIntervalSound(uriString: String) {
        context.userSettingsStore.updateData { currentSettings ->
            currentSettings.toBuilder().setIntervalSound(uriString).build()
        }
    }
}
