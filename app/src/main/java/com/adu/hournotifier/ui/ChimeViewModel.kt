package com.adu.hournotifier.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.adu.hournotifier.audio.ChimeAudioPlayer
import com.adu.hournotifier.data.ChimePreferencesManager
import com.adu.hournotifier.data.ChimeSettings
import com.adu.hournotifier.scheduler.ChimeScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChimeUiState(
    val settings: ChimeSettings = ChimeSettings(),
    val hasExactAlarmPermission: Boolean = true,
    val nextChimeTimeMillis: Long? = null,
)

class ChimeViewModel(
    private val applicationContext: Context,
    private val preferencesManager: ChimePreferencesManager = ChimePreferencesManager(applicationContext),
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ChimeUiState(
            hasExactAlarmPermission = ChimeScheduler.canScheduleExactAlarms(applicationContext)
        )
    )
    val uiState: StateFlow<ChimeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesManager.chimeSettingsFlow.collect { settings ->
                val canSchedule = ChimeScheduler.canScheduleExactAlarms(applicationContext)
                val nextChimeMillis = if (settings.isMasterEnabled) {
                    ChimeScheduler.scheduleNextChime(applicationContext, settings)
                } else {
                    ChimeScheduler.cancelChime(applicationContext)
                    null
                }
                _uiState.update { currentState ->
                    currentState.copy(
                        settings = settings,
                        hasExactAlarmPermission = canSchedule,
                        nextChimeTimeMillis = nextChimeMillis
                    )
                }
            }
        }
    }

    fun refreshPermissionStatus() {
        val canSchedule = ChimeScheduler.canScheduleExactAlarms(applicationContext)
        _uiState.update { currentState ->
            currentState.copy(hasExactAlarmPermission = canSchedule)
        }
        if (canSchedule && _uiState.value.settings.isMasterEnabled) {
            val nextTime = ChimeScheduler.scheduleNextChime(applicationContext, _uiState.value.settings)
            _uiState.update { it.copy(nextChimeTimeMillis = nextTime) }
        }
    }

    fun setMasterEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setMasterEnabled(enabled)
        }
    }

    fun toggleHour(hour: Int) {
        viewModelScope.launch {
            val currentHours = _uiState.value.settings.activeHours
            val isEnabled = !currentHours.contains(hour)
            preferencesManager.toggleHour(hour, isEnabled)
        }
    }

    fun selectAllHours() {
        viewModelScope.launch {
            preferencesManager.selectAllHours()
        }
    }

    fun deselectAllHours() {
        viewModelScope.launch {
            preferencesManager.deselectAllHours()
        }
    }

    fun selectDaytimeHours() {
        viewModelScope.launch {
            preferencesManager.setActiveHours((8..21).toSet())
        }
    }

    fun selectNighttimeHours() {
        viewModelScope.launch {
            preferencesManager.setActiveHours(setOf(22, 23, 0, 1, 2, 3, 4, 5, 6, 7))
        }
    }

    fun setSound(soundUriString: String?, soundName: String) {
        viewModelScope.launch {
            preferencesManager.setSound(soundUriString, soundName)
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setVibrationEnabled(enabled)
        }
    }

    fun toggleQuietDay(dayOfWeek: Int) {
        viewModelScope.launch {
            val currentQuietDays = _uiState.value.settings.quietDays.toMutableSet()
            if (currentQuietDays.contains(dayOfWeek)) {
                currentQuietDays.remove(dayOfWeek)
            } else {
                currentQuietDays.add(dayOfWeek)
            }
            preferencesManager.setQuietDays(currentQuietDays)
        }
    }

    fun testChime() {
        val settings = _uiState.value.settings
        ChimeAudioPlayer.playChime(
            context = applicationContext,
            soundUriString = settings.soundUriString,
            isVibrationEnabled = settings.isVibrationEnabled
        )
    }

    companion object {
        fun Factory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ChimeViewModel(context.applicationContext) as T
            }
        }
    }
}
