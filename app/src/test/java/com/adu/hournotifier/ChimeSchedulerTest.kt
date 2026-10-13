package com.adu.hournotifier

import com.adu.hournotifier.data.ChimeSettings
import com.adu.hournotifier.scheduler.ChimeScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class ChimeSchedulerTest {

    @Test
    fun `calculateNextChimeTime returns null when master is disabled`() {
        val settings = ChimeSettings(isMasterEnabled = false, activeHours = (0..23).toSet())
        val nextTime = ChimeScheduler.calculateNextChimeTime(settings, System.currentTimeMillis())
        assertNull(nextTime)
    }

    @Test
    fun `calculateNextChimeTime returns null when active hours is empty`() {
        val settings = ChimeSettings(isMasterEnabled = true, activeHours = emptySet())
        val nextTime = ChimeScheduler.calculateNextChimeTime(settings, System.currentTimeMillis())
        assertNull(nextTime)
    }

    @Test
    fun `calculateNextChimeTime returns next top of hour when that hour is active`() {
        // Base time: 2026-03-30 10:15:30.000 in current time zone
        val zoneId = ZoneId.systemDefault()
        val nowZdt = ZonedDateTime.of(2026, 3, 30, 10, 15, 30, 0, zoneId)
        val nowMillis = nowZdt.toInstant().toEpochMilli()

        val settings = ChimeSettings(isMasterEnabled = true, activeHours = (0..23).toSet())
        val nextTimeMillis = ChimeScheduler.calculateNextChimeTime(settings, nowMillis)

        assertNotNull(nextTimeMillis)
        val expectedZdt = ZonedDateTime.of(2026, 3, 30, 11, 0, 0, 0, zoneId)
        assertEquals(expectedZdt.toInstant().toEpochMilli(), nextTimeMillis)
    }

    @Test
    fun `calculateNextChimeTime skips inactive hours and finds next active hour`() {
        // Base time: 2026-03-30 10:15:30.000
        val zoneId = ZoneId.systemDefault()
        val nowZdt = ZonedDateTime.of(2026, 3, 30, 10, 15, 30, 0, zoneId)
        val nowMillis = nowZdt.toInstant().toEpochMilli()

        // Hours 11, 12, 13 disabled. Hour 14 enabled.
        val activeHours = setOf(8, 9, 10, 14, 15, 16)
        val settings = ChimeSettings(isMasterEnabled = true, activeHours = activeHours)
        val nextTimeMillis = ChimeScheduler.calculateNextChimeTime(settings, nowMillis)

        assertNotNull(nextTimeMillis)
        val expectedZdt = ZonedDateTime.of(2026, 3, 30, 14, 0, 0, 0, zoneId)
        assertEquals(expectedZdt.toInstant().toEpochMilli(), nextTimeMillis)
    }

    @Test
    fun `calculateNextChimeTime wraps around to next day if no remaining active hours today`() {
        // Base time: 2026-03-30 22:30:00.000
        val zoneId = ZoneId.systemDefault()
        val nowZdt = ZonedDateTime.of(2026, 3, 30, 22, 30, 0, 0, zoneId)
        val nowMillis = nowZdt.toInstant().toEpochMilli()

        // Only morning hours 8 and 9 enabled
        val activeHours = setOf(8, 9)
        val settings = ChimeSettings(isMasterEnabled = true, activeHours = activeHours)
        val nextTimeMillis = ChimeScheduler.calculateNextChimeTime(settings, nowMillis)

        assertNotNull(nextTimeMillis)
        // Should trigger at 08:00 tomorrow (2026-03-31)
        val expectedZdt = ZonedDateTime.of(2026, 3, 31, 8, 0, 0, 0, zoneId)
        assertEquals(expectedZdt.toInstant().toEpochMilli(), nextTimeMillis)
    }

    @Test
    fun `calculateNextChimeTime skips quiet days`() {
        // 2026-03-30 is a Monday (DayOfWeek value = 1)
        val zoneId = ZoneId.systemDefault()
        val mondayZdt = ZonedDateTime.of(2026, 3, 30, 10, 15, 30, 0, zoneId)
        val nowMillis = mondayZdt.toInstant().toEpochMilli()

        // Monday (1) is in quietDays
        val settings = ChimeSettings(
            isMasterEnabled = true,
            activeHours = (0..23).toSet(),
            quietDays = setOf(1)
        )
        val nextTimeMillis = ChimeScheduler.calculateNextChimeTime(settings, nowMillis)

        assertNotNull(nextTimeMillis)
        // Should skip Monday and trigger at 00:00 on Tuesday 2026-03-31
        val expectedZdt = ZonedDateTime.of(2026, 3, 31, 0, 0, 0, 0, zoneId)
        assertEquals(expectedZdt.toInstant().toEpochMilli(), nextTimeMillis)
    }
}
