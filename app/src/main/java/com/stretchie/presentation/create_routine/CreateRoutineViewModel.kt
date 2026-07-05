package com.stretchie.presentation.create_routine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.stretchie.data.model.Routine
import com.stretchie.data.model.RoutinePoseSettings
import com.stretchie.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class CreateRoutineState(
    val routineId: String = UUID.randomUUID().toString(),
    val name: String = "",
    val poseSettings: Map<String, RoutinePoseSettings> = emptyMap()
)

class CreateRoutineViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CreateRoutineState())
    val state: StateFlow<CreateRoutineState> = _state.asStateFlow()

    fun loadRoutine(routineId: String) {
        viewModelScope.launch {
            val routines = settingsRepository.getRoutinesFlow().first()
            val routine = routines.find { it.id == routineId } ?: return@launch
            _state.update {
                it.copy(
                    routineId = routine.id,
                    name = routine.name,
                    poseSettings = routine.poseSettings
                )
            }
        }
    }

    fun updateName(name: String) {
        _state.update { it.copy(name = name) }
    }

    fun togglePoseIncluded(poseId: String, defaultIncluded: Boolean) {
        val current = _state.value.poseSettings[poseId]
        val newIncluded = !(current?.included ?: defaultIncluded)
        val updated = _state.value.poseSettings.toMutableMap()
        updated[poseId] = (current ?: RoutinePoseSettings()).copy(included = newIncluded)
        _state.update { it.copy(poseSettings = updated) }
    }

    fun updatePoseConfig(poseId: String, duration: Int, intervalCount: Int) {
        val current = _state.value.poseSettings[poseId]
        val updated = _state.value.poseSettings.toMutableMap()
        updated[poseId] = (current ?: RoutinePoseSettings()).copy(
            duration = duration,
            intervalCount = intervalCount
        )
        _state.update { it.copy(poseSettings = updated) }
    }

    fun saveRoutine(onSaved: () -> Unit) {
        viewModelScope.launch {
            val s = _state.value
            val routine = Routine(
                id = s.routineId,
                name = s.name.trim(),
                poseSettings = s.poseSettings
            )
            settingsRepository.saveRoutine(routine)
            onSaved()
        }
    }

    class Factory(private val settingsRepository: SettingsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CreateRoutineViewModel::class.java)) {
                return CreateRoutineViewModel(settingsRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
