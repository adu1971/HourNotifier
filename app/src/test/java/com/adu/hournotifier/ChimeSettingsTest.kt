package com.adu.hournotifier

import com.adu.hournotifier.data.ChimeSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChimeSettingsTest {

    @Test
    fun `default ChimeSettings has master enabled and all 24 hours active`() {
        val settings = ChimeSettings()
        assertTrue(settings.isMasterEnabled)
        assertEquals((0..23).toSet(), settings.activeHours)
        assertEquals("Default Notification Sound", settings.soundName)
        assertTrue(settings.isVibrationEnabled)
        assertTrue(settings.quietDays.isEmpty())
    }

    @Test
    fun `daytime selection helper set calculation`() {
        val daytimeHours = (8..21).toSet()
        assertEquals(14, daytimeHours.size)
        assertTrue(daytimeHours.contains(8))
        assertTrue(daytimeHours.contains(21))
        assertFalse(daytimeHours.contains(7))
        assertFalse(daytimeHours.contains(22))
    }

    @Test
    fun `nighttime selection helper set calculation`() {
        val nighttimeHours = setOf(22, 23, 0, 1, 2, 3, 4, 5, 6, 7)
        assertEquals(10, nighttimeHours.size)
        assertTrue(nighttimeHours.contains(22))
        assertTrue(nighttimeHours.contains(7))
        assertFalse(nighttimeHours.contains(8))
        assertFalse(nighttimeHours.contains(21))
    }
}
