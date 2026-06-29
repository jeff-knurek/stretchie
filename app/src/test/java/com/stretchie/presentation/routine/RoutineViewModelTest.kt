package com.stretchie.presentation.routine

import com.stretchie.UserSettings
import com.stretchie.data.model.Pose
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

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        val mockPoses =
                listOf(
                        Pose(
                                id = "pose1",
                                name = "Pose 1",
                                imageRes = 1,
                                defaultDurationSeconds = 10
                        ),
                        Pose(
                                id = "pose2",
                                name = "Pose 2",
                                imageRes = 2,
                                defaultDurationSeconds = 10
                        )
                )

        poseRepository = mock { on { getAllPoses() } doReturn mockPoses }
        audioManager = mock()
        settingsRepository = mock { on { this.userSettingsFlow } doReturn userSettingsFlow }
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        viewModel = RoutineViewModel(poseRepository, audioManager, settingsRepository)
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

        // Start routine
        viewModel.togglePlayPause()

        assertTrue(viewModel.state.value.isRunning)

        // Advance time by 1 second
        advanceTimeBy(1001)

        // Timer should decrement
        assertEquals(9, viewModel.state.value.secondsRemaining)
    }

    @Test
    fun `pauseRoutine stops timer from decrementing`() = runTest {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.togglePlayPause() // Start
        advanceTimeBy(1001)
        assertEquals(9, viewModel.state.value.secondsRemaining)

        viewModel.togglePlayPause() // Pause
        assertFalse(viewModel.state.value.isRunning)

        advanceTimeBy(1001) // Should not decrement
        assertEquals(9, viewModel.state.value.secondsRemaining)
    }

    @Test
    fun `timer automatically advances to next pose and plays sound when it hits zero`() = runTest {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.togglePlayPause() // Start

        // Advance 10 seconds (duration of first pose)
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

        viewModel.togglePlayPause() // Start

        // First pose 10s + 2s transition + Second pose 10s = 22s total
        advanceTimeBy(21501)

        val state = viewModel.state.value
        assertFalse(state.isCompleted)
        assertFalse(state.isRunning)
        assertEquals(10, state.secondsRemaining)
        assertEquals(0, state.currentPoseIndex)
        verify(audioManager).playIntervalSound()
    }

    @Test
    fun `timer plays interval sound and decrements interval count before advancing to next pose`() =
            runTest {
                // Set pose1 to have 2 intervals of 10s each
                val poseSettings =
                        com.stretchie.PoseSettings.newBuilder()
                                .setIntervalCount(2)
                                .setDuration(10)
                                .build()
                val mockSettings =
                        UserSettings.newBuilder().putPoseOverrides("pose1", poseSettings).build()
                userSettingsFlow.value = mockSettings

                createViewModel()
                testDispatcher.scheduler.advanceUntilIdle()

                viewModel.togglePlayPause() // Start

                // First interval is 10s. Advance by 10s.
                advanceTimeBy(10001)

                // It should still be on pose1, but now on interval 2.
                var state = viewModel.state.value
                assertEquals(0, state.currentPoseIndex)
                assertEquals("pose1", state.currentPose?.id)
                assertEquals(2, state.currentInterval)
                assertEquals(10, state.secondsRemaining)
                verify(audioManager).playIntervalSound()

                // Advance transition delay (1.5s) + second interval (10s) = 11.5s
                advanceTimeBy(11501)

                state = viewModel.state.value
                assertEquals(1, state.currentPoseIndex)
                assertEquals("pose2", state.currentPose?.id)
                assertEquals(1, state.currentInterval)
                assertEquals(10, state.secondsRemaining)
                verify(audioManager).playPoseChangeSound()
            }

    @Test
    fun `skip logic ignores skipped poses when loading`() = runTest {
        // Skip pose1
        val poseSettings = com.stretchie.PoseSettings.newBuilder().setIsSkipped(true).build()
        val mockSettings = UserSettings.newBuilder().putPoseOverrides("pose1", poseSettings).build()
        userSettingsFlow.value = mockSettings

        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        // Should only have pose2 now
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

        viewModel.nextPose() // Go to pose2
        assertEquals(1, viewModel.state.value.currentPoseIndex)

        viewModel.previousPose() // Go back to pose1

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
        // Two poses, each 10 seconds, interval count default 1
        assertEquals(20, total)
    }

    @Test
    fun `getTotalRemainingTime updates after advancing to next pose`() = runTest {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.togglePlayPause()
        // advance first pose duration
        advanceTimeBy(10001)
        // allow transition delay to process
        advanceTimeBy(1501)
        val total = viewModel.getTotalRemainingTime()
        // now only second pose remains 10 seconds
        assertEquals(10, total)
    }

}
