package com.example.qevin.presentation.calls

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

object CallTimeFormatter {

    private const val WEEKDAY_WINDOW_DAYS = 6L

    /** "Today, 4:15 PM", "Yesterday, 8:30 PM", "Mon, 9:05 AM", "3 Nov, 7:10 PM". */
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

        val time = DateTimeFormatter
            .ofLocalizedTime(FormatStyle.SHORT)
            .withLocale(locale)
            .format(moment)

        val day = when {
            // Future timestamps (clock skew) are treated as "today".
            daysAgo <= 0L -> "Today"
            daysAgo == 1L -> "Yesterday"
            daysAgo <= WEEKDAY_WINDOW_DAYS ->
                DateTimeFormatter.ofPattern("EEE", locale).format(moment)
            date.year == today.year ->
                DateTimeFormatter.ofPattern("d MMM", locale).format(moment)
            else ->
                DateTimeFormatter.ofPattern("d MMM yyyy", locale).format(moment)
        }

        return "$day, $time"
    }

    /** "40 s", "4 min", "1 h 5 min". Null for zero or negative durations. */
    fun duration(millis: Long): String? {
        val totalSeconds = millis / 1000
        if (totalSeconds <= 0L) return null

        val minutes = totalSeconds / 60
        return when {
            totalSeconds < 60L -> "$totalSeconds s"
            minutes < 60L -> "$minutes min"
            else -> {
                val hours = minutes / 60
                val rest = minutes % 60
                if (rest == 0L) "$hours h" else "$hours h $rest min"
            }
        }
    }
}