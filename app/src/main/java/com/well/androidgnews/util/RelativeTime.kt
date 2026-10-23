package com.well.androidgnews.util

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("M/d HH:mm")

fun formatRelativeTime(
    instant: Instant,
    now: Instant = Instant.now(),
    zone: ZoneId = ZoneId.systemDefault(),
): String {
    val elapsed = Duration.between(instant, now)
    return when {
        elapsed.isNegative || elapsed.toMinutes() < 1 -> "剛剛"
        elapsed.toHours() < 1 -> "${elapsed.toMinutes()} 分鐘前"
        elapsed.toDays() < 1 -> "${elapsed.toHours()} 小時前"
        else -> dateFormatter.format(instant.atZone(zone))
    }
}
