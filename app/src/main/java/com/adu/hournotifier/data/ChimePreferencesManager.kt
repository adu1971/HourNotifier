package com.adu.hournotifier.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "chime_preferences")

class ChimePreferencesManager(private val context: Context) {

    companion object {
        val KEY_MASTER_ENABLED = booleanPreferencesKey("master_enabled")
        val KEY_ACTIVE_HOURS = stringSetPreferencesKey("active_hours")
        val KEY_SOUND_URI = stringPreferencesKey("sound_uri")
        val KEY_SOUND_NAME = stringPreferencesKey("sound_name")
        val KEY_VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val KEY_QUIET_DAYS = stringSetPreferencesKey("quiet_days")

        val DEFAULT_ACTIVE_HOURS: Set<Int> = (0..23).toSet()
    }

    val chimeSettingsFlow: Flow<ChimeSettings> = context.dataStore.data.map { prefs ->
        val masterEnabled = prefs[KEY_MASTER_ENABLED] ?: true
        val activeHoursSet = prefs[KEY_ACTIVE_HOURS]?.mapNotNull { it.toIntOrNull() }?.toSet()
            ?: DEFAULT_ACTIVE_HOURS
        val soundUri = prefs[KEY_SOUND_URI]
        val soundName = prefs[KEY_SOUND_NAME] ?: "Default Notification Sound"
        val vibrationEnabled = prefs[KEY_VIBRATION_ENABLED] ?: true
        val quietDays = prefs[KEY_QUIET_DAYS]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()

        ChimeSettings(
            isMasterEnabled = masterEnabled,
            activeHours = activeHoursSet,
            soundUriString = soundUri,
            soundName = soundName,
            isVibrationEnabled = vibrationEnabled,
            quietDays = quietDays,
        )
    }

    suspend fun getChimeSettings(): ChimeSettings = chimeSettingsFlow.first()

    suspend fun setMasterEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_MASTER_ENABLED] = enabled
        }
    }

    suspend fun setActiveHours(activeHours: Set<Int>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACTIVE_HOURS] = activeHours.map { it.toString() }.toSet()
        }
    }

    suspend fun toggleHour(hour: Int, enabled: Boolean) {
        val currentSettings = getChimeSettings()
        val updated = currentSettings.activeHours.toMutableSet()
        if (enabled) {
            updated.add(hour)
        } else {
            updated.remove(hour)
        }
        setActiveHours(updated)
    }

    suspend fun selectAllHours() {
        setActiveHours((0..23).toSet())
    }

    suspend fun deselectAllHours() {
        setActiveHours(emptySet())
    }

    suspend fun setSound(soundUriString: String?, soundName: String) {
        context.dataStore.edit { prefs ->
            if (soundUriString != null) {
                prefs[KEY_SOUND_URI] = soundUriString
            } else {
                prefs.remove(KEY_SOUND_URI)
            }
            prefs[KEY_SOUND_NAME] = soundName
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun setQuietDays(quietDays: Set<Int>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_QUIET_DAYS] = quietDays.map { it.toString() }.toSet()
        }
    }
}
