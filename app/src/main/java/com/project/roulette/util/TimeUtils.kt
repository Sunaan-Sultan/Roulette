package com.project.roulette.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Utility for formatting timestamps to relative strings.
 */
object TimeUtils {
    fun getRelativeTime(instant: Instant): String {
        val now = Clock.System.now()
        val duration = now - instant
        
        val seconds = duration.inWholeSeconds
        val minutes = duration.inWholeMinutes
        val hours = duration.inWholeHours
        val days = duration.inWholeDays

        return when {
            seconds < 60 -> "Just now"
            minutes == 1L -> "1 minute ago"
            minutes < 60 -> "$minutes minutes ago"
            hours == 1L -> "1 hour ago"
            hours < 24 -> "$hours hours ago"
            days == 1L -> "Yesterday"
            days < 7 -> "$days days ago"
            else -> {
                val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                "${dateTime.dayOfMonth}/${dateTime.monthNumber}/${dateTime.year}"
            }
        }
    }
}
