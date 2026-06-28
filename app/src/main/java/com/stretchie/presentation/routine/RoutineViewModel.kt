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

    private fun getDurationForPose(pose: com.stretchie.data.model.Pose): Int {
        val override = currentSettings?.poseOverridesMap?.get(pose.id)
        return if (override != null && override.duration > 0) override.duration
        else pose.defaultDurationSeconds
    }

    private fun getIntervalCountForPose(pose: com.stretchie.data.model.Pose): Int {
        val override = currentSettings?.poseOverridesMap?.get(pose.id)
        return if (override != null && override.intervalCount > 0) override.intervalCount else pose.defaultIntervalCount
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
            val duration = getDurationForPose(firstPose)
            val intervalCount = getIntervalCountForPose(firstPose)
            _state.update {
                it.copy(
                        poses = activePoses,
                        currentPoseIndex = 0,
                        currentInterval = 1,
                        currentIntervalCount = intervalCount,
                        secondsRemaining = duration
                )
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
        _state.update { it.copy(isRunning = false, isTransitioning = false) }
        timerJob?.cancel()
    }

    private suspend fun playTransitionDelay() {
        _state.update { it.copy(isTransitioning = true) }
        delay(1500L)
        _state.update { it.copy(isTransitioning = false) }
    }

    private suspend fun tick() {
        val currentState = _state.value
        if (currentState.secondsRemaining > 1) {
            _state.update { it.copy(secondsRemaining = it.secondsRemaining - 1) }
        } else {
            val currentPose = currentState.currentPose
            if (currentPose != null) {
                val intervalCount = getIntervalCountForPose(currentPose)
                if (currentState.currentInterval < intervalCount) {
                    // next interval, same pose
                    audioManager.playIntervalSound()
                    _state.update {
                        it.copy(
                                currentInterval = it.currentInterval + 1,
                                secondsRemaining = getDurationForPose(currentPose)
                        )
                    }
                    playTransitionDelay()
                } else {
                    if (currentState.currentPoseIndex < currentState.poses.size - 1) {
                        // next pose
                        audioManager.playPoseChangeSound()
                        val nextIndex = currentState.currentPoseIndex + 1
                        val nextPose = currentState.poses[nextIndex]
                        val nextIntervalCount = getIntervalCountForPose(nextPose)
                        _state.update {
                            it.copy(
                                    currentPoseIndex = nextIndex,
                                    currentInterval = 1,
                                    currentIntervalCount = nextIntervalCount,
                                    secondsRemaining = getDurationForPose(nextPose)
                            )
                        }
                        playTransitionDelay()
                    } else {
                        // last pose, end of routine - reset to first pose for next run
                        audioManager.playIntervalSound()
                        // Reset state to start of routine without starting automatically
                        val firstPose = _state.value.poses.firstOrNull()
                        if (firstPose != null) {
                            val duration = getDurationForPose(firstPose)
                            val intervalCount = getIntervalCountForPose(firstPose)
                            _state.update {
                                it.copy(
                                    isRunning = false,
                                    isCompleted = false,
                                    currentPoseIndex = 0,
                                    currentInterval = 1,
                                    currentIntervalCount = intervalCount,
                                    secondsRemaining = duration
                                )
                            }
                        }
                        timerJob?.cancel()
                    }
                }
            }
        }
    }

    fun nextPose() {
        val currentState = _state.value
        if (currentState.currentPoseIndex < currentState.poses.size - 1) {
            val nextIndex = currentState.currentPoseIndex + 1
            val nextPose = currentState.poses[nextIndex]
            val duration = getDurationForPose(nextPose)
            val nextIntervalCount = getIntervalCountForPose(nextPose)

            _state.update {
                it.copy(
                        currentPoseIndex = nextIndex,
                        currentInterval = 1,
                        currentIntervalCount = nextIntervalCount,
                        secondsRemaining = duration
                )
            }
        } else {
            audioManager.playIntervalSound()
            _state.update { it.copy(isRunning = false, isCompleted = true, secondsRemaining = 0) }
            timerJob?.cancel()
        }
    }

    fun previousPose() {
        val currentState = _state.value
        if (currentState.currentPoseIndex > 0) {
            val prevIndex = currentState.currentPoseIndex - 1
            val prevPose = currentState.poses[prevIndex]
            val duration = getDurationForPose(prevPose)
            val prevIntervalCount = getIntervalCountForPose(prevPose)

            _state.update {
                it.copy(
                        currentPoseIndex = prevIndex,
                        currentInterval = 1,
                        currentIntervalCount = prevIntervalCount,
                        secondsRemaining = duration
                )
            }
        }
    }

    fun getTotalRemainingTime(): Int {
        val currentState = _state.value
        // If no poses, return 0
        if (currentState.poses.isEmpty()) return 0
        var total = currentState.secondsRemaining
        // Remaining intervals in current pose
        val remainingIntervalsInCurrent = currentState.currentIntervalCount - currentState.currentInterval
        val currentPose = currentState.currentPose
        if (currentPose != null) {
            val durationPerInterval = getDurationForPose(currentPose)
            total += remainingIntervalsInCurrent * durationPerInterval
        }
        // Add full durations for all future poses
        for (i in (currentState.currentPoseIndex + 1) until currentState.poses.size) {
            val pose = currentState.poses[i]
            val duration = getDurationForPose(pose)
            val intervalCount = getIntervalCountForPose(pose)
            total += duration * intervalCount
        }
        return total
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
