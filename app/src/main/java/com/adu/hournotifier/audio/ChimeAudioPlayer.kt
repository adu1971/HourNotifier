package com.adu.hournotifier.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.VibrationEffect
import android.os.VibratorManager
import android.util.Log
import androidx.core.net.toUri

/**
 * Handles audio playback for hourly chimes using system RingtoneManager/MediaPlayer
 * with AudioAttributes.USAGE_NOTIFICATION and vibration effects, without creating status bar notifications.
 */
object ChimeAudioPlayer {

    private const val TAG = "ChimeAudioPlayer"

    fun playChime(context: Context, soundUriString: String?, isVibrationEnabled: Boolean) {
        playChimeSound(context, soundUriString)
        if (isVibrationEnabled) {
            vibrate(context)
        }
    }

    fun playChimeSound(context: Context, soundUriString: String?) {
        try {
            val soundUri = if (!soundUriString.isNullOrEmpty()) {
                soundUriString.toUri()
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            } ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val ringtone = RingtoneManager.getRingtone(context, soundUri)
            if (ringtone != null) {
                ringtone.audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                ringtone.play()
            } else {
                MediaPlayer().apply {
                    setDataSource(context, soundUri)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    prepare()
                    start()
                    setOnCompletionListener { mp -> mp.release() }
                    setOnErrorListener { mp, _, _ ->
                        mp.release()
                        true
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing chime sound", e)
            try {
                val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val ringtone = RingtoneManager.getRingtone(context, defaultUri)
                ringtone?.let {
                    it.audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                    it.play()
                }
            } catch (fallbackEx: Exception) {
                Log.e(TAG, "Fallback chime sound also failed", fallbackEx)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun vibrate(context: Context) {
        try {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator

            if (vibrator.hasVibrator()) {
                val timings = longArrayOf(0, 150, 100, 150)
                val amplitudes = intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrating device", e)
        }
    }
}
