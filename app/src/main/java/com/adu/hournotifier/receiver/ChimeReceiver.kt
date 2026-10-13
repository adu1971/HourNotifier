package com.adu.hournotifier.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.adu.hournotifier.audio.ChimeAudioPlayer
import com.adu.hournotifier.data.ChimePreferencesManager
import com.adu.hournotifier.scheduler.ChimeScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

/**
 * BroadcastReceiver responsible for catching top-of-hour exact alarm triggers,
 * playing the chime sound/vibration, and re-scheduling the next exact alarm after boot or trigger.
 */
class ChimeReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ChimeReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        Log.d(TAG, "Received broadcast action: $action")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefsManager = ChimePreferencesManager(context.applicationContext)
                val settings = prefsManager.getChimeSettings()

                if (action == ChimeScheduler.ACTION_HOURLY_CHIME) {
                    if (settings.isMasterEnabled) {
                        val currentHour = ZonedDateTime.now().hour
                        val currentDayOfWeek = ZonedDateTime.now().dayOfWeek.value
                        val isQuietDay = settings.quietDays.contains(currentDayOfWeek)
                        val isActiveHour = settings.activeHours.contains(currentHour)

                        if (!isQuietDay && isActiveHour) {
                            Log.d(TAG, "Triggering chime for hour: $currentHour")
                            ChimeAudioPlayer.playChime(
                                context = context.applicationContext,
                                soundUriString = settings.soundUriString,
                                isVibrationEnabled = settings.isVibrationEnabled,
                            )
                        } else {
                            Log.d(TAG, "Skipping chime playback (quiet day: $isQuietDay, active hour: $isActiveHour)")
                        }
                    }
                }

                // Reschedule next chime on any trigger event (alarm fire, boot, app update, permission change)
                if (settings.isMasterEnabled) {
                    ChimeScheduler.scheduleNextChime(context.applicationContext, settings)
                } else {
                    ChimeScheduler.cancelChime(context.applicationContext)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing broadcast in ChimeReceiver", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
