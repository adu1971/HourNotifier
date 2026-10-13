package com.adu.hournotifier.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.core.net.toUri
import com.adu.hournotifier.data.ChimeSettings
import com.adu.hournotifier.receiver.ChimeReceiver
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

/**
 * Helper object for calculating next top-of-hour chime times and scheduling exact alarms.
 */
object ChimeScheduler {

    private const val TAG = "ChimeScheduler"
    const val ACTION_HOURLY_CHIME = "com.adu.hournotifier.ACTION_HOURLY_CHIME"
    const val REQUEST_CODE_CHIME = 1001

    fun canScheduleExactAlarms(context: Context): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.canScheduleExactAlarms()
    }

    fun openExactAlarmSettings(context: Context) {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = "package:${context.packageName}".toUri()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun scheduleNextChime(
        context: Context,
        settings: ChimeSettings,
        nowMillis: Long = System.currentTimeMillis(),
    ): Long? {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ChimeReceiver::class.java).apply {
            action = ACTION_HOURLY_CHIME
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_CHIME,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = calculateNextChimeTime(settings, nowMillis)

        if (triggerAtMillis == null) {
            Log.d(TAG, "No active hours scheduled or master disabled; canceling existing alarms")
            alarmManager.cancel(pendingIntent)
            return null
        }

        Log.d(TAG, "Scheduling next chime at millis: $triggerAtMillis (${ZonedDateTime.ofInstant(Instant.ofEpochMilli(triggerAtMillis), ZoneId.systemDefault())})")

        try {
            if (!alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException scheduling exact alarm, falling back to setAndAllowWhileIdle", e)
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }

        return triggerAtMillis
    }

    fun cancelChime(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ChimeReceiver::class.java).apply {
            action = ACTION_HOURLY_CHIME
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_CHIME,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Chime alarm canceled")
    }

    /**
     * Calculates the exact top-of-hour timestamp (in UTC epoch millis) for the next active chime hour.
     */
    fun calculateNextChimeTime(
        settings: ChimeSettings,
        nowMillis: Long = System.currentTimeMillis(),
    ): Long? {
        if (!settings.isMasterEnabled || settings.activeHours.isEmpty()) {
            return null
        }

        val nowZdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(nowMillis), ZoneId.systemDefault())
        val nextHourZdt = nowZdt.truncatedTo(ChronoUnit.HOURS).plusHours(1)

        // Check up to 168 hours (1 week) to find the next active hour & day
        for (i in 0 until 168) {
            val candidate = nextHourZdt.plusHours(i.toLong())
            val dayOfWeek = candidate.dayOfWeek.value // 1 (Mon) .. 7 (Sun)
            if (!settings.quietDays.contains(dayOfWeek) && settings.activeHours.contains(candidate.hour)) {
                return candidate.toInstant().toEpochMilli()
            }
        }

        return null
    }
}
