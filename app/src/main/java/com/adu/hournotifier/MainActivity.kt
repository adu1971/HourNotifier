package com.adu.hournotifier

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adu.hournotifier.ui.ChimeScreen
import com.adu.hournotifier.ui.ChimeViewModel
import com.adu.hournotifier.ui.theme.HourNotifierTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HourNotifierTheme {
                val viewModel: ChimeViewModel = viewModel(
                    factory = ChimeViewModel.Factory(applicationContext)
                )
                val uiState by viewModel.uiState.collectAsState()

                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            viewModel.refreshPermissionStatus()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                val soundPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == RESULT_OK) {
                        val data = result.data
                        val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                        }
                        val uriString = uri?.toString()
                        val soundName = if (uri != null) {
                            try {
                                val ringtone = RingtoneManager.getRingtone(applicationContext, uri)
                                ringtone?.getTitle(applicationContext) ?: "Custom Sound"
                            } catch (_: Exception) {
                                "Custom Sound"
                            }
                        } else {
                            "Default Notification Sound"
                        }
                        viewModel.setSound(uriString, soundName)
                    }
                }

                ChimeScreen(
                    viewModel = viewModel,
                    onLaunchSoundPicker = {
                        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Chime Sound")
                            val currentUri = uiState.settings.soundUriString
                            if (!currentUri.isNullOrEmpty()) {
                                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentUri.toUri())
                            } else {
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                            }
                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                        }
                        soundPickerLauncher.launch(intent)
                    }
                )
            }
        }
    }
}
