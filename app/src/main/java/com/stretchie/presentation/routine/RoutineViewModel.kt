package com.stretchie.presentation.routine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stretchie.UserSettings
import com.stretchie.data.repository.PoseRepository
import com.stretchie.data.repository.SettingsRepository
import com.stretchie.presentation.util.AudioManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RoutineViewModel(
        private val poseRepository: PoseRepository,
        private val audioManager: AudioManager,
        private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RoutineState())
    val state: StateFlow<RoutineState> = _state.asStateFlow()

    private var currentSettings: UserSettings? = null
    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            settingsRepository.userSettingsFlow.collect { settings ->
                currentSettings = settings
                // Reload poses if we are idle and settings change (like skip toggle)
                if (!_state.value.isRunning && !_state.value.isCompleted) {
                    loadPoses()
                }
            }
        }
    }

    private fun loadPoses() {
        val allPoses = poseRepository.getAllPoses()
        val activePoses =
                allPoses.filter { pose ->
                    val override = currentSettings?.poseOverridesMap?.get(pose.id)
                    !(override?.getIsSkipped() ?: false)
                }
        if (activePoses.isNotEmpty()) {
            val firstPose = activePoses[0]
            val duration =
                    currentSettings?.poseOverridesMap?.get(firstPose.id)?.duration?.takeIf {
                        it > 0
                    }
                            ?: firstPose.defaultDurationSeconds
            _state.update {
                it.copy(poses = activePoses, currentPoseIndex = 0, secondsRemaining = duration)
            }
        }
    }

    fun togglePlayPause() {
        if (_state.value.isRunning) {
            pauseRoutine()
        } else {
            startRoutine()
        }
    }

    private fun startRoutine() {
        if (_state.value.isCompleted || _state.value.poses.isEmpty()) return

        _state.update { it.copy(isRunning = true) }
        timerJob?.cancel()
        timerJob =
                viewModelScope.launch {
                    while (_state.value.isRunning && !_state.value.isCompleted) {
                        delay(1000L)
                        tick()
                    }
                }
    }

    private fun pauseRoutine() {
        _state.update { it.copy(isRunning = false) }
        timerJob?.cancel()
    }

    private fun tick() {
        val currentState = _state.value
        if (currentState.secondsRemaining > 1) {
            _state.update { it.copy(secondsRemaining = it.secondsRemaining - 1) }
        } else {
            audioManager.playPoseChangeSound()
            nextPose()
        }
    }

    fun nextPose() {
        val currentState = _state.value
        if (currentState.currentPoseIndex < currentState.poses.size - 1) {
            val nextIndex = currentState.currentPoseIndex + 1
            val nextPose = currentState.poses[nextIndex]
            val duration =
                    currentSettings?.poseOverridesMap?.get(nextPose.id)?.duration?.takeIf { it > 0 }
                            ?: nextPose.defaultDurationSeconds

            _state.update { it.copy(currentPoseIndex = nextIndex, secondsRemaining = duration) }
        } else {
            audioManager.playCompletionSound()
            _state.update { it.copy(isRunning = false, isCompleted = true, secondsRemaining = 0) }
            timerJob?.cancel()
        }
    }

    fun previousPose() {
        val currentState = _state.value
        if (currentState.currentPoseIndex > 0) {
            val prevIndex = currentState.currentPoseIndex - 1
            val prevPose = currentState.poses[prevIndex]
            val duration =
                    currentSettings?.poseOverridesMap?.get(prevPose.id)?.duration?.takeIf { it > 0 }
                            ?: prevPose.defaultDurationSeconds

            _state.update { it.copy(currentPoseIndex = prevIndex, secondsRemaining = duration) }
        }
    }

    fun restart() {
        val activePoses = _state.value.poses
        if (activePoses.isNotEmpty()) {
            val firstPose = activePoses[0]
            val duration =
                    currentSettings?.poseOverridesMap?.get(firstPose.id)?.duration?.takeIf {
                        it > 0
                    }
                            ?: firstPose.defaultDurationSeconds
            _state.update {
                it.copy(
                        isRunning = false,
                        isCompleted = false,
                        currentPoseIndex = 0,
                        secondsRemaining = duration
                )
            }
        }
        timerJob?.cancel()
    }

    class Factory(
            private val poseRepository: PoseRepository,
            private val audioManager: AudioManager,
            private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RoutineViewModel::class.java)) {
                return RoutineViewModel(poseRepository, audioManager, settingsRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
