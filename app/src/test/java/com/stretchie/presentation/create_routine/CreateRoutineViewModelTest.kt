package com.stretchie.presentation.create_routine

import com.stretchie.data.model.Routine
import com.stretchie.data.model.RoutinePoseSettings
import com.stretchie.data.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class CreateRoutineViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var viewModel: CreateRoutineViewModel

    private val existingRoutine = Routine(
        id = "routine-1",
        name = "My Routine",
        poseSettings = mapOf(
            "pose1" to RoutinePoseSettings(included = true, duration = 20, intervalCount = 2)
        )
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        settingsRepository = mock {
            on { getRoutinesFlow() } doReturn flowOf(listOf(existingRoutine))
        }
        viewModel = CreateRoutineViewModel(settingsRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // loadRoutine

    @Test
    fun `loadRoutine populates state from matching routine`() = runTest {
        viewModel.loadRoutine("routine-1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("routine-1", state.routineId)
        assertEquals("My Routine", state.name)
        assertEquals(existingRoutine.poseSettings, state.poseSettings)
    }

    @Test
    fun `loadRoutine is noop when routine id does not match`() = runTest {
        viewModel.loadRoutine("unknown-id")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("", state.name)
        assertTrue(state.poseSettings.isEmpty())
    }

    @Test
    fun `loadRoutine only executes once when called multiple times`() = runTest {
        viewModel.loadRoutine("routine-1")
        testDispatcher.scheduler.advanceUntilIdle()
        val nameAfterFirst = viewModel.state.value.name

        viewModel.loadRoutine("unknown-id")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("My Routine", nameAfterFirst)
        assertEquals("My Routine", viewModel.state.value.name)
    }

    // updateName

    @Test
    fun `updateName updates state name`() {
        viewModel.updateName("Leg Day")
        assertEquals("Leg Day", viewModel.state.value.name)
    }

    @Test
    fun `updateName does not trim whitespace during editing`() {
        viewModel.updateName("  Leg Day  ")
        assertEquals("  Leg Day  ", viewModel.state.value.name)
    }

    // togglePoseIncluded

    @Test
    fun `togglePoseIncluded flips included from true to false for existing pose`() {
        viewModel = CreateRoutineViewModel(settingsRepository).apply {
            // start with pose1 included=true
        }
        val initial = mapOf("pose1" to RoutinePoseSettings(included = true, duration = 15))
        viewModel.updatePoseConfig("pose1", 15, 1)

        viewModel.togglePoseIncluded("pose1", defaultIncluded = true)
        assertFalse(viewModel.state.value.poseSettings["pose1"]!!.included)
    }

    @Test
    fun `togglePoseIncluded flips included from false to true for existing pose`() {
        viewModel.updatePoseConfig("pose1", 10, 1)
        // manually set it to excluded first
        viewModel.togglePoseIncluded("pose1", defaultIncluded = true)
        assertFalse(viewModel.state.value.poseSettings["pose1"]!!.included)

        viewModel.togglePoseIncluded("pose1", defaultIncluded = true)
        assertTrue(viewModel.state.value.poseSettings["pose1"]!!.included)
    }

    @Test
    fun `togglePoseIncluded uses defaultIncluded when pose not in settings`() {
        viewModel.togglePoseIncluded("new-pose", defaultIncluded = true)
        assertFalse(viewModel.state.value.poseSettings["new-pose"]!!.included)
    }

    @Test
    fun `togglePoseIncluded with defaultIncluded false toggles to true when pose not in settings`() {
        viewModel.togglePoseIncluded("new-pose", defaultIncluded = false)
        assertTrue(viewModel.state.value.poseSettings["new-pose"]!!.included)
    }

    @Test
    fun `togglePoseIncluded preserves duration and intervalCount when toggling`() {
        viewModel.updatePoseConfig("pose1", duration = 25, intervalCount = 3)
        viewModel.togglePoseIncluded("pose1", defaultIncluded = true)

        val settings = viewModel.state.value.poseSettings["pose1"]!!
        assertFalse(settings.included)
        assertEquals(25, settings.duration)
        assertEquals(3, settings.intervalCount)
    }

    // updatePoseConfig

    @Test
    fun `updatePoseConfig sets duration and intervalCount for new pose`() {
        viewModel.updatePoseConfig("pose2", duration = 30, intervalCount = 4)

        val settings = viewModel.state.value.poseSettings["pose2"]!!
        assertEquals(30, settings.duration)
        assertEquals(4, settings.intervalCount)
    }

    @Test
    fun `updatePoseConfig preserves included flag when updating existing pose`() {
        viewModel.togglePoseIncluded("pose1", defaultIncluded = true)
        assertFalse(viewModel.state.value.poseSettings["pose1"]!!.included)

        viewModel.updatePoseConfig("pose1", duration = 15, intervalCount = 2)

        val settings = viewModel.state.value.poseSettings["pose1"]!!
        assertFalse(settings.included)
        assertEquals(15, settings.duration)
        assertEquals(2, settings.intervalCount)
    }

    @Test
    fun `updatePoseConfig creates entry with default included when pose not in map`() {
        viewModel.updatePoseConfig("brand-new", duration = 10, intervalCount = 1)

        val settings = viewModel.state.value.poseSettings["brand-new"]!!
        assertTrue(settings.included)
        assertEquals(10, settings.duration)
        assertEquals(1, settings.intervalCount)
    }

    // saveRoutine

    @Test
    fun `saveRoutine calls repository with correct routine data`() = runTest {
        viewModel.updateName("Push Day")
        viewModel.updatePoseConfig("pose1", duration = 20, intervalCount = 2)

        viewModel.saveRoutine {}
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).saveRoutine(
            org.mockito.kotlin.argThat { routine ->
                routine.name == "Push Day" &&
                        routine.poseSettings["pose1"]?.duration == 20 &&
                        routine.poseSettings["pose1"]?.intervalCount == 2
            }
        )
    }

    @Test
    fun `saveRoutine trims whitespace from name`() = runTest {
        viewModel.updateName("  Pull Day  ")

        viewModel.saveRoutine {}
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).saveRoutine(
            org.mockito.kotlin.argThat<Routine> { routine -> routine.name == "Pull Day" }
        )
    }

    @Test
    fun `saveRoutine invokes onSaved callback after persisting`() = runTest {
        var callbackInvoked = false

        viewModel.saveRoutine { callbackInvoked = true }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(callbackInvoked)
    }
}
