package com.example.qevin.presentation.home

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale

class ConversationTimeFormatterTest {

    private val zone = ZoneId.of("UTC")
    private val locale = Locale.US

    // Wednesday, 18 Nov 2026, 15:00 UTC
    private val now: Instant =
        ZonedDateTime.of(2026, 11, 18, 15, 0, 0, 0, zone).toInstant()

    private fun at(y: Int, m: Int, d: Int, h: Int = 9, min: Int = 32): Long =
        ZonedDateTime.of(y, m, d, h, min, 0, 0, zone).toInstant().toEpochMilli()

    private fun fmt(millis: Long) =
        ConversationTimeFormatter.format(millis, now, zone, locale)

    @Test fun zeroIsEmpty() = assertEquals("", fmt(0L))

    @Test fun todayShowsTime() {
        val text = fmt(at(2026, 11, 18, 9, 32))
        // Locale data can use a narrow no-break space before AM/PM.
        assertEquals("9:32 AM", text.replace('\u202F', ' '))
    }

    @Test fun yesterday() = assertEquals("Yesterday", fmt(at(2026, 11, 17)))

    @Test fun withinWeekShowsWeekday() = assertEquals("Fri", fmt(at(2026, 11, 13)))

    @Test fun olderThisYearShowsDayMonth() = assertEquals("2 Nov", fmt(at(2026, 11, 2)))

    @Test fun previousYearShowsYear() = assertEquals("12 Dec 2025", fmt(at(2025, 12, 12)))

    @Test fun futureTimestampTreatedAsToday() {
        val text = fmt(at(2026, 11, 19, 10, 0))
        assertEquals("10:00 AM", text.replace('\u202F', ' '))
    }

    @Test fun badgeEmptyForZero() = assertEquals("", ConversationTimeFormatter.unreadBadge(0))
    @Test fun badgeShowsCount() = assertEquals("7", ConversationTimeFormatter.unreadBadge(7))
    @Test fun badgeCapsAt99Plus() = assertEquals("99+", ConversationTimeFormatter.unreadBadge(150))
    @Test fun badgeNinetyNineStaysExact() = assertEquals("99", ConversationTimeFormatter.unreadBadge(99))
}