package com.stretchie.data.repository
 
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.stretchie.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException

/**
 * Repository for managing user settings using Proto DataStore.
 */
class SettingsRepository(private val context: Context) {

    private val Context.userSettingsStore: DataStore<UserSettings> by dataStore(
        fileName = "user_settings.pb",
        serializer = UserSettingsSerializer
    )

    /**
     * Flow of user settings.
     */
    val userSettingsFlow: Flow<UserSettings> = context.userSettingsStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(UserSettings.getDefaultInstance())
            } else {
                throw exception
            }
        }

    /**
     * Updates the interval count setting.
     */
    suspend fun updateIntervalCount(count: Int) {
        context.userSettingsStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setIntervalCount(count)
                .build()
        }
    }

    /**
     * Updates the interval duration setting.
     */
    suspend fun updateIntervalDuration(durationSeconds: Int) {
        context.userSettingsStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setIntervalDuration(durationSeconds)
                .build()
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
            currentSettings.toBuilder()
                .setPoseChangeSound(uriString)
                .build()
        }
    }

    suspend fun updateIntervalSound(uriString: String) {
        context.userSettingsStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setIntervalSound(uriString)
                .build()
        }
    }
}
