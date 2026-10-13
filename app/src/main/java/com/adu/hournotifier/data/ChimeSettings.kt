package com.adu.hournotifier.data

/**
 * Data model representing user settings for hourly chimes.
 *
 * @property isMasterEnabled Master toggle state (true = hourly chimes active, false = disabled)
 * @property activeHours Set of active hours (0..23) when chimes should trigger
 * @property soundUriString String representation of selected sound URI, null for system default notification
 * @property soundName Human readable name of the selected sound
 * @property isVibrationEnabled True if vibration is enabled on chime
 * @property quietDays Set of days of week (1 = Monday .. 7 = Sunday) on which chimes are silenced
 */
data class ChimeSettings(
    val isMasterEnabled: Boolean = true,
    val activeHours: Set<Int> = (0..23).toSet(),
    val soundUriString: String? = null,
    val soundName: String = "Default Notification Sound",
    val isVibrationEnabled: Boolean = true,
    val quietDays: Set<Int> = emptySet()
)
