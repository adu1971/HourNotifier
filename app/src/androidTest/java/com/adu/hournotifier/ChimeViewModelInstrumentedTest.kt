package com.adu.hournotifier

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adu.hournotifier.data.ChimePreferencesManager
import com.adu.hournotifier.ui.ChimeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ChimeViewModelInstrumentedTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var preferencesManager: ChimePreferencesManager

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        preferencesManager = ChimePreferencesManager(context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun viewModelInitialStateLoadsFromPreferences() = runTest {
        val viewModel = ChimeViewModel(context, preferencesManager)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.settings.isMasterEnabled)
        assertEquals((0..23).toSet(), state.settings.activeHours)
    }

    @Test
    fun setMasterEnabledUpdatesPreferencesAndState() = runTest {
        val viewModel = ChimeViewModel(context, preferencesManager)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setMasterEnabled(false)
        testDispatcher.scheduler.advanceUntilIdle()

        val settings = preferencesManager.chimeSettingsFlow.first()
        assertFalse(settings.isMasterEnabled)
        assertFalse(viewModel.uiState.value.settings.isMasterEnabled)
    }

    @Test
    fun selectDaytimeHoursSetsActiveHoursTo8Through21() = runTest {
        val viewModel = ChimeViewModel(context, preferencesManager)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectDaytimeHours()
        testDispatcher.scheduler.advanceUntilIdle()

        val expected = (8..21).toSet()
        assertEquals(expected, viewModel.uiState.value.settings.activeHours)
    }

    @Test
    fun selectNighttimeHoursSetsActiveHoursTo22Through7() = runTest {
        val viewModel = ChimeViewModel(context, preferencesManager)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectNighttimeHours()
        testDispatcher.scheduler.advanceUntilIdle()

        val expected = setOf(22, 23, 0, 1, 2, 3, 4, 5, 6, 7)
        assertEquals(expected, viewModel.uiState.value.settings.activeHours)
    }

    @Test
    fun deselectAllHoursAndSelectAllHoursWorkCorrectly() = runTest {
        val viewModel = ChimeViewModel(context, preferencesManager)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.deselectAllHours()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.settings.activeHours.isEmpty())

        viewModel.selectAllHours()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals((0..23).toSet(), viewModel.uiState.value.settings.activeHours)
    }

    @Test
    fun setSoundUpdatesSoundUriAndSoundName() = runTest {
        val viewModel = ChimeViewModel(context, preferencesManager)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setSound("content://media/internal/audio/media/12", "Chime Bell")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("content://media/internal/audio/media/12", state.settings.soundUriString)
        assertEquals("Chime Bell", state.settings.soundName)
    }

    @Test
    fun toggleQuietDayTogglesQuietDayInSet() = runTest {
        val viewModel = ChimeViewModel(context, preferencesManager)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleQuietDay(1) // Monday
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.settings.quietDays.contains(1))

        viewModel.toggleQuietDay(1)
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.settings.quietDays.contains(1))
    }
}
