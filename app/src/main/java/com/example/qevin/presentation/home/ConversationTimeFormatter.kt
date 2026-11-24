package com.example.qevin.presentation.home

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

object ConversationTimeFormatter {

    private const val WEEKDAY_WINDOW_DAYS = 6L

    fun format(
        epochMillis: Long,
        now: Instant = Instant.now(),
        zone: ZoneId = ZoneId.systemDefault(),
        locale: Locale = Locale.getDefault()
    ): String {
        if (epochMillis <= 0L) return ""

        val moment = Instant.ofEpochMilli(epochMillis).atZone(zone)
        val date = moment.toLocalDate()
        val today = now.atZone(zone).toLocalDate()

        val daysAgo = ChronoUnit.DAYS.between(date, today)

        return when {
            // Future timestamps (clock skew) are treated as "now".
            daysAgo <= 0L -> DateTimeFormatter
                .ofLocalizedTime(FormatStyle.SHORT)
                .withLocale(locale)
                .format(moment)
            daysAgo == 1L -> "Yesterday"
            daysAgo <= WEEKDAY_WINDOW_DAYS -> DateTimeFormatter
                .ofPattern("EEE", locale)
                .format(moment)
            date.year == today.year -> DateTimeFormatter
                .ofPattern("d MMM", locale)
                .format(moment)
            else -> DateTimeFormatter
                .ofPattern("d MMM yyyy", locale)
                .format(moment)
        }
    }

    /** Badge text: raw count up to 99, then "99+". Empty when zero. */
    fun unreadBadge(count: Int): String = when {
        count <= 0 -> ""
        count > 99 -> "99+"
        else -> count.toString()
    }
}