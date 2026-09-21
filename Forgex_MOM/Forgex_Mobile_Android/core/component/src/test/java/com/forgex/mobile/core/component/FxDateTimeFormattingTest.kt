package com.forgex.mobile.core.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class FxDateTimeFormattingTest {

    @Test
    fun parsesAndFormatsDateAndDateTimeValues() {
        assertEquals(LocalDate.of(2026, 9, 21), parseFxDate("2026-09-21"))
        assertEquals("2026-09-21", formatFxDate(LocalDate.of(2026, 9, 21)))
        assertEquals(
            LocalDateTime.of(2026, 9, 21, 14, 35),
            parseFxDateTime("2026-09-21 14:35")
        )
        assertEquals("2026-09-21 14:35", formatFxDateTime(LocalDateTime.of(2026, 9, 21, 14, 35)))
    }

    @Test
    fun invalidValuesReturnNullInsteadOfThrowing() {
        assertNull(parseFxDate("not-a-date"))
        assertNull(parseFxDateTime("2026-09-21"))
        assertNull(parseFxTime("25:99"))
    }

    @Test
    fun formatsRangeWithStableSeparator() {
        assertEquals(
            "2026-09-01 至 2026-09-21",
            formatFxDateRange(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 21))
        )
        assertEquals(LocalTime.of(9, 5), parseFxTime("09:05"))
    }
}
