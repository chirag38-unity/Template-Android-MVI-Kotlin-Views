package com.myapp.core.common.extensions

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

private val systemTimeZone = TimeZone.currentSystemDefault()

/**
 * Formats this epoch-millisecond timestamp as a short date string: `"DD Mon YYYY"`.
 *
 * ## Example
 *
 * ```kotlin
 * 1_716_307_200_000L.toFormattedDate() // → "21 May 2024"
 * ```
 */
fun Long.toFormattedDate(): String {
    val localDateTime = Instant
        .fromEpochMilliseconds(this)
        .toLocalDateTime(systemTimeZone)

    val day = localDateTime.day
        .toString()
        .padStart(2, '0')

    val month = localDateTime.month.name
        .lowercase()
        .replaceFirstChar { it.uppercase() }
        .take(3)

    val year = localDateTime.year

    return "$day $month $year"
}

/**
 * Returns a human-readable relative time string describing how long ago this
 * epoch-millisecond timestamp was.
 *
 * Falls back to [toFormattedDate] for timestamps older than 30 days.
 *
 * ## Example output
 *
 * ```
 * "just now"   (< 60 seconds ago)
 * "5m ago"     (< 1 hour ago)
 * "3h ago"     (< 1 day ago)
 * "2d ago"     (< 7 days ago)
 * "1w ago"     (< 30 days ago)
 * "21 May 2024" (30+ days ago)
 * ```
 */
fun Long.toRelativeTimeString(): String {
    val now = Clock.System.now().toEpochMilliseconds()
    val diff = now - this
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        days < 30 -> "${days / 7}w ago"
        else -> toFormattedDate()
    }
}

/**
 * Returns `true` if this epoch-millisecond timestamp falls on the current calendar day
 * in the device's local time zone.
 */
fun Long.isToday(): Boolean {
    val today = Clock.System.now()
        .toLocalDateTime(systemTimeZone)
        .date

    val date = Instant
        .fromEpochMilliseconds(this)
        .toLocalDateTime(systemTimeZone)
        .date

    return date == today
}

/**
 * Returns `true` if this epoch-millisecond timestamp falls on the same calendar day as
 * [other] in the device's local time zone.
 *
 * ## Example
 *
 * ```kotlin
 * val morningTs = ...
 * val eveningTs = ...
 * morningTs.isSameDay(eveningTs) // → true if same date
 * ```
 */
fun Long.isSameDay(other: Long): Boolean {
    val thisDate = Instant
        .fromEpochMilliseconds(this)
        .toLocalDateTime(systemTimeZone)
        .date

    val otherDate = Instant
        .fromEpochMilliseconds(other)
        .toLocalDateTime(systemTimeZone)
        .date

    return thisDate == otherDate
}
