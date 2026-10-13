package com.adu.hournotifier.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Deselect
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adu.hournotifier.scheduler.ChimeScheduler
import com.adu.hournotifier.ui.theme.HourNotifierTheme
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChimeScreen(
    viewModel: ChimeViewModel,
    onLaunchSoundPicker: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.NotificationsActive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Hour Notifier",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (uiState.settings.isMasterEnabled) "Hourly Chimes Enabled" else "Hourly Chimes Disabled",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth >= 600.dp

            if (isWideScreen) {
                // Wide tablet / landscape layout
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MasterChimeCard(
                            isMasterEnabled = uiState.settings.isMasterEnabled,
                            nextChimeMillis = uiState.nextChimeTimeMillis,
                            onMasterToggle = { viewModel.setMasterEnabled(it) }
                        )

                        if (!uiState.hasExactAlarmPermission) {
                            ExactAlarmPermissionCard(
                                onGrantPermission = { ChimeScheduler.openExactAlarmSettings(context) }
                            )
                        }

                        ActiveHoursSection(
                            activeHours = uiState.settings.activeHours,
                            onToggleHour = { viewModel.toggleHour(it) },
                            onSelectAll = { viewModel.selectAllHours() },
                            onDeselectAll = { viewModel.deselectAllHours() },
                            onSelectDaytime = { viewModel.selectDaytimeHours() },
                            onSelectNighttime = { viewModel.selectNighttimeHours() },
                            columns = 6
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SoundSettingsCard(
                            soundName = uiState.settings.soundName,
                            isVibrationEnabled = uiState.settings.isVibrationEnabled,
                            onLaunchSoundPicker = onLaunchSoundPicker,
                            onTestChime = { viewModel.testChime() },
                            onVibrationToggle = { viewModel.setVibrationEnabled(it) }
                        )

                        QuietDaysCard(
                            quietDays = uiState.settings.quietDays,
                            onToggleQuietDay = { viewModel.toggleQuietDay(it) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            } else {
                // Phone layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MasterChimeCard(
                        isMasterEnabled = uiState.settings.isMasterEnabled,
                        nextChimeMillis = uiState.nextChimeTimeMillis,
                        onMasterToggle = { viewModel.setMasterEnabled(it) }
                    )

                    if (!uiState.hasExactAlarmPermission) {
                        ExactAlarmPermissionCard(
                            onGrantPermission = { ChimeScheduler.openExactAlarmSettings(context) }
                        )
                    }

                    ActiveHoursSection(
                        activeHours = uiState.settings.activeHours,
                        onToggleHour = { viewModel.toggleHour(it) },
                        onSelectAll = { viewModel.selectAllHours() },
                        onDeselectAll = { viewModel.deselectAllHours() },
                        onSelectDaytime = { viewModel.selectDaytimeHours() },
                        onSelectNighttime = { viewModel.selectNighttimeHours() },
                        columns = 4
                    )

                    SoundSettingsCard(
                        soundName = uiState.settings.soundName,
                        isVibrationEnabled = uiState.settings.isVibrationEnabled,
                        onLaunchSoundPicker = onLaunchSoundPicker,
                        onTestChime = { viewModel.testChime() },
                        onVibrationToggle = { viewModel.setVibrationEnabled(it) }
                    )

                    QuietDaysCard(
                        quietDays = uiState.settings.quietDays,
                        onToggleQuietDay = { viewModel.toggleQuietDay(it) }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun MasterChimeCard(
    isMasterEnabled: Boolean,
    nextChimeMillis: Long?,
    onMasterToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isMasterEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isMasterEnabled) Icons.Rounded.NotificationsActive else Icons.Rounded.AccessTime,
                        contentDescription = null,
                        tint = if (isMasterEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Master Chime",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isMasterEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isMasterEnabled) formatNextChimeTime(nextChimeMillis) else "Background chime service paused",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isMasterEnabled) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = isMasterEnabled,
                onCheckedChange = onMasterToggle,
                modifier = Modifier.semantics { contentDescription = "Master Hourly Chime Switch" }
            )
        }
    }
}

@Composable
fun ExactAlarmPermissionCard(
    onGrantPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Exact Alarm Permission Required",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Text(
                text = "Exact alarm permission is required to trigger hourly chimes precisely at the top of each hour. Please allow this permission in system settings.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Button(
                onClick = onGrantPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Grant Permission")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActiveHoursSection(
    activeHours: Set<Int>,
    onToggleHour: (Int) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onSelectDaytime: () -> Unit,
    onSelectNighttime: () -> Unit,
    columns: Int,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Chime Hours",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "${activeHours.size} / 24 active",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = "Quick selection",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                AssistChip(
                    onClick = onSelectAll,
                    label = { Text("Select All") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.SelectAll,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
                AssistChip(
                    onClick = onDeselectAll,
                    label = { Text("Deselect All") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Deselect,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
                AssistChip(
                    onClick = onSelectDaytime,
                    label = { Text("Daytime (8-22)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.WbSunny,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
                AssistChip(
                    onClick = onSelectNighttime,
                    label = { Text("Nighttime (22-8)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.NightsStay,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            val rows = (24 + columns - 1) / columns
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (r in 0 until rows) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (c in 0 until columns) {
                            val hour = r * columns + c
                            if (hour < 24) {
                                val isSelected = activeHours.contains(hour)
                                HourGridCell(
                                    hour = hour,
                                    isSelected = isSelected,
                                    onClick = { onToggleHour(hour) },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HourGridCell(
    hour: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
        ) {
            Text(
                text = formatHourLabel(hour),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = formatHourSubLabel(hour),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun SoundSettingsCard(
    soundName: String,
    isVibrationEnabled: Boolean,
    onLaunchSoundPicker: () -> Unit,
    onTestChime: () -> Unit,
    onVibrationToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Sound & Feedback",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Chime Sound",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = soundName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    OutlinedButton(onClick = onLaunchSoundPicker) {
                        Text("Change")
                    }
                }
            }

            FilledTonalButton(
                onClick = onTestChime,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Test Chime Preview",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Vibration,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Vibrate on Chime",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Vibrate when hourly chime plays",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isVibrationEnabled,
                    onCheckedChange = onVibrationToggle
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuietDaysCard(
    quietDays: Set<Int>,
    onToggleQuietDay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val daysOfWeek = listOf(
        1 to "Mon",
        2 to "Tue",
        3 to "Wed",
        4 to "Thu",
        5 to "Fri",
        6 to "Sat",
        7 to "Sun"
    )

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column {
                Text(
                    text = "Quiet Days",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Select days when chimes should be silenced completely",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                daysOfWeek.forEach { (dayInt, dayLabel) ->
                    val isQuiet = quietDays.contains(dayInt)
                    FilterChip(
                        selected = isQuiet,
                        onClick = { onToggleQuietDay(dayInt) },
                        label = { Text(dayLabel) },
                        leadingIcon = if (isQuiet) {
                            {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    )
                }
            }
        }
    }
}

private fun formatNextChimeTime(millis: Long?): String {
    if (millis == null) return "No chimes scheduled"
    val now = ZonedDateTime.now()
    val chimeTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())

    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val timeStr = chimeTime.format(timeFormatter)

    return when {
        chimeTime.toLocalDate() == now.toLocalDate() -> "Next chime today at $timeStr"
        chimeTime.toLocalDate() == now.toLocalDate().plusDays(1) -> "Next chime tomorrow at $timeStr"
        else -> {
            val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d 'at' HH:mm")
            "Next chime on ${chimeTime.format(dateFormatter)}"
        }
    }
}

private fun formatHourLabel(hour: Int): String {
    return String.format(Locale.getDefault(), "%02d:00", hour)
}

private fun formatHourSubLabel(hour: Int): String {
    return when (hour) {
        0 -> "12 AM"
        in 1..11 -> "$hour AM"
        12 -> "12 PM"
        else -> "${hour - 12} PM"
    }
}

@Preview(showBackground = true, device = "id:pixel_8")
@Composable
fun ActiveHoursSectionPreview() {
    HourNotifierTheme {
        ActiveHoursSection(
            activeHours = (8..21).toSet(),
            onToggleHour = {},
            onSelectAll = {},
            onDeselectAll = {},
            onSelectDaytime = {},
            onSelectNighttime = {},
            columns = 4
        )
    }
}
