package com.stretchie.presentation.routine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stretchie.data.model.Pose
import com.stretchie.data.model.Routine
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
    private val routineId: String,
    private val poseRepository: PoseRepository,
    private val audioManager: AudioManager,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RoutineState())
    val state: StateFlow<RoutineState> = _state.asStateFlow()

    private var currentRoutine: Routine? = null
    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            settingsRepository.getRoutinesFlow().collect { routines ->
                val routine = routines.find { it.id == routineId }
                currentRoutine = routine
                if (!_state.value.isRunning && !_state.value.isCompleted) {
                    loadPoses(routine)
                }
            }
        }
    }

    private fun getDurationForPose(pose: Pose): Int {
        val ps = currentRoutine?.poseSettings?.get(pose.id)
        return if (ps != null && ps.duration > 0) ps.duration else pose.defaultDurationSeconds
    }

    private fun getIntervalCountForPose(pose: Pose): Int {
        val ps = currentRoutine?.poseSettings?.get(pose.id)
        return if (ps != null && ps.intervalCount > 0) ps.intervalCount else pose.defaultIntervalCount
    }

    private fun loadPoses(routine: Routine?) {
        val allPoses = poseRepository.getAllPoses()
        val activePoses = if (routine != null) {
            allPoses.filter { pose ->
                val ps = routine.poseSettings[pose.id]
                ps == null || ps.included
            }
        } else {
            allPoses
        }
        val name = routine?.name ?: ""
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
                    secondsRemaining = duration,
                    routineName = name
                )
            }
        } else {
            _state.update { it.copy(poses = emptyList(), routineName = name) }
        }
    }

    fun togglePlayPause() {
        if (_state.value.isRunning) pauseRoutine() else startRoutine()
    }

    private fun startRoutine() {
        if (_state.value.isCompleted || _state.value.poses.isEmpty()) return
        _state.update { it.copy(isRunning = true) }
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
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
                        audioManager.playIntervalSound()
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
        if (currentState.poses.isEmpty()) return 0
        var total = currentState.secondsRemaining
        val remainingIntervalsInCurrent = currentState.currentIntervalCount - currentState.currentInterval
        val currentPose = currentState.currentPose
        if (currentPose != null) {
            val durationPerInterval = getDurationForPose(currentPose)
            total += remainingIntervalsInCurrent * durationPerInterval
        }
        for (i in (currentState.currentPoseIndex + 1) until currentState.poses.size) {
            val pose = currentState.poses[i]
            val duration = getDurationForPose(pose)
            val intervalCount = getIntervalCountForPose(pose)
            total += duration * intervalCount
        }
        return total
    }

    class Factory(
        private val routineId: String,
        private val poseRepository: PoseRepository,
        private val audioManager: AudioManager,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RoutineViewModel::class.java)) {
                return RoutineViewModel(routineId, poseRepository, audioManager, settingsRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
