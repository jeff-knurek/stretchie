package com.stretchie.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stretchie.UserSettings
import com.stretchie.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val userSettings: StateFlow<UserSettings> = settingsRepository.userSettingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings.getDefaultInstance()
        )

    fun toggleSkipPose(poseId: String, currentDuration: Int, currentInterval: Int, isSkipped: Boolean) {
        viewModelScope.launch {
            settingsRepository.updatePoseOverride(poseId, isSkipped, currentDuration, currentInterval)
        }
    }
    
    fun updatePoseChangeSound(uri: String) {
        viewModelScope.launch {
            settingsRepository.updatePoseChangeSound(uri)
        }
    }
    
    fun updateCompletionSound(uri: String) {
        viewModelScope.launch {
            settingsRepository.updateCompletionSound(uri)
        }
    }

    class Factory(
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                return SettingsViewModel(settingsRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
