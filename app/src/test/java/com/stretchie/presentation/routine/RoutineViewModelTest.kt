package com.stretchie.presentation.routine

import com.stretchie.UserSettings
import com.stretchie.data.model.Pose
import com.stretchie.data.model.Routine
import com.stretchie.data.model.RoutinePoseSettings
import com.stretchie.data.repository.PoseRepository
import com.stretchie.data.repository.SettingsRepository
import com.stretchie.presentation.util.AudioManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class RoutineViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var poseRepository: PoseRepository
    private lateinit var audioManager: AudioManager
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var viewModel: RoutineViewModel

    private val userSettingsFlow = MutableStateFlow(UserSettings.getDefaultInstance())
    private val routinesFlow = MutableStateFlow<List<Routine>>(emptyList())

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        val mockPoses = listOf(
            Pose(id = "pose1", name = "Pose 1", imageRes = 1, defaultDurationSeconds = 10),
            Pose(id = "pose2", name = "Pose 2", imageRes = 2, defaultDurationSeconds = 10)
        )

        poseRepository = mock { on { getAllPoses() } doReturn mockPoses }
        audioManager = mock()
        settingsRepository = mock {
            on { this.userSettingsFlow } doReturn userSettingsFlow
            on { this.getRoutinesFlow() } doReturn routinesFlow
        }
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(routineId: String = "") {
        viewModel = RoutineViewModel(routineId, poseRepository, audioManager, settingsRepository)
    }

    @Test
    fun `initialization loads poses correctly`() = runTest {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(2, state.poses.size)
        assertEquals(10, state.secondsRemaining)
        assertEquals(0, state.currentPoseIndex)
        assertFalse(state.isRunning)
    }

    @Test
    fun `togglePlayPause starts timer and decrements secondsRemaining`() = runTest {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.togglePlayPause()

        assertTrue(viewModel.state.value.isRunning)

        advanceTimeBy(1001)

        assertEquals(9, viewModel.state.value.secondsRemaining)
    }

    @Test
    fun `pauseRoutine stops timer from decrementing`() = runTest {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.togglePlayPause()
        advanceTimeBy(1001)
        assertEquals(9, viewModel.state.value.secondsRemaining)

        viewModel.togglePlayPause()
        assertFalse(viewModel.state.value.isRunning)

        advanceTimeBy(1001)
        assertEquals(9, viewModel.state.value.secondsRemaining)
    }

    @Test
    fun `timer automatically advances to next pose and plays sound when it hits zero`() = runTest {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.togglePlayPause()

        advanceTimeBy(10001)

        val state = viewModel.state.value
        assertEquals(1, state.currentPoseIndex)
        assertEquals("pose2", state.currentPose?.id)
        assertEquals(10, state.secondsRemaining)
        verify(audioManager).playPoseChangeSound()
    }

    @Test
    fun `routine completes after the last pose`() = runTest {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.togglePlayPause()

        advanceTimeBy(21501)

        val state = viewModel.state.value
        assertFalse(state.isCompleted)
        assertFalse(state.isRunning)
        assertEquals(10, state.secondsRemaining)
        assertEquals(0, state.currentPoseIndex)
        verify(audioManager).playIntervalSound()
    }

    @Test
    fun `timer plays interval sound and decrements interval count before advancing to next pose`() = runTest {
        val routine = Routine(
            id = "r1",
            name = "Test",
            poseSettings = mapOf(
                "pose1" to RoutinePoseSettings(included = true, duration = 10, intervalCount = 2)
            )
        )
        routinesFlow.value = listOf(routine)

        createViewModel(routineId = "r1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.togglePlayPause()

        advanceTimeBy(10001)

        var state = viewModel.state.value
        assertEquals(0, state.currentPoseIndex)
        assertEquals("pose1", state.currentPose?.id)
        assertEquals(2, state.currentInterval)
        assertEquals(10, state.secondsRemaining)
        verify(audioManager).playIntervalSound()

        advanceTimeBy(11501)

        state = viewModel.state.value
        assertEquals(1, state.currentPoseIndex)
        assertEquals("pose2", state.currentPose?.id)
        assertEquals(1, state.currentInterval)
        assertEquals(10, state.secondsRemaining)
        verify(audioManager).playPoseChangeSound()
    }

    @Test
    fun `routine excludes poses marked as not included`() = runTest {
        val routine = Routine(
            id = "r1",
            name = "Test",
            poseSettings = mapOf(
                "pose1" to RoutinePoseSettings(included = false)
            )
        )
        routinesFlow.value = listOf(routine)

        createViewModel(routineId = "r1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(1, state.poses.size)
        assertEquals("pose2", state.poses[0].id)
    }

    @Test
    fun `nextPose manually advances to next pose`() = runTest {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.nextPose()

        val state = viewModel.state.value
        assertEquals(1, state.currentPoseIndex)
        assertEquals("pose2", state.currentPose?.id)
        assertEquals(10, state.secondsRemaining)
    }

    @Test
    fun `previousPose manually goes back to previous pose`() = runTest {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.nextPose()
        assertEquals(1, viewModel.state.value.currentPoseIndex)

        viewModel.previousPose()

        val state = viewModel.state.value
        assertEquals(0, state.currentPoseIndex)
        assertEquals("pose1", state.currentPose?.id)
        assertEquals(10, state.secondsRemaining)
    }

    @Test
    fun `getTotalRemainingTime returns correct total at start`() = runTest {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val total = viewModel.getTotalRemainingTime()
        assertEquals(20, total)
    }

    @Test
    fun `getTotalRemainingTime updates after advancing to next pose`() = runTest {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.togglePlayPause()
        advanceTimeBy(10001)
        advanceTimeBy(1501)
        val total = viewModel.getTotalRemainingTime()
        assertEquals(10, total)
    }
}
