package com.stretchie.presentation.util

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import com.stretchie.data.repository.SettingsRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

class AudioManager(
    private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    fun playPoseChangeSound() {
        try {
            val settings = runBlocking { settingsRepository.userSettingsFlow.firstOrNull() }
            val uriString = settings?.poseChangeSound
            val uri = if (!uriString.isNullOrEmpty()) {
                Uri.parse(uriString)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
            val ringtone = RingtoneManager.getRingtone(context, uri)
            ringtone.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playIntervalSound() {
        try {
            val settings = runBlocking { settingsRepository.userSettingsFlow.firstOrNull() }
            val uriString = settings?.intervalSound
            val uri = if (!uriString.isNullOrEmpty()) {
                Uri.parse(uriString)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
            val ringtone = RingtoneManager.getRingtone(context, uri)
            ringtone.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
