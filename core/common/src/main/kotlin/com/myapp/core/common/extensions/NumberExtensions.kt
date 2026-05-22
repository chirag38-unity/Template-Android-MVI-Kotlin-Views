package com.myapp.core.common.extensions

import android.content.res.Resources
import android.util.TypedValue
import kotlin.math.roundToInt

/**
 * Converts this integer pixel value from dp (density-independent pixels) to the device's
 * actual pixel count.
 *
 * ## Example
 *
 * ```kotlin
 * view.setPadding(16.dp, 8.dp, 16.dp, 8.dp)
 * ```
 */
val Int.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).roundToInt()

/**
 * Converts this integer from dp to pixels as a [Float].
 * Use when fractional precision is required (e.g. custom drawing).
 */
val Int.dpFloat: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    )

/**
 * Converts this integer from sp (scale-independent pixels) to pixels as a [Float].
 * Primarily used for text size calculations.
 */
val Int.sp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    )

/**
 * Rounds this [Float] to [places] decimal places.
 *
 * ## Example
 *
 * ```kotlin
 * 3.14159f.roundTo(2) // → 3.14f
 * ```
 */
fun Float.roundTo(places: Int): Float {
    val factor = Math.pow(10.0, places.toDouble()).toFloat()
    return (this * factor).roundToInt() / factor
}

/**
 * Rounds this [Double] to [places] decimal places.
 *
 * ## Example
 *
 * ```kotlin
 * 3.14159.roundTo(2) // → 3.14
 * ```
 */
fun Double.roundTo(places: Int): Double {
    val factor = Math.pow(10.0, places.toDouble())
    return (this * factor).roundToInt() / factor
}

/**
 * Clamps this integer to the range [[min], [max]] (inclusive on both ends).
 *
 * ## Example
 *
 * ```kotlin
 * 150.clamp(0, 100) // → 100
 * (-5).clamp(0, 100) // → 0
 * 50.clamp(0, 100)  // → 50
 * ```
 */
fun Int.clamp(min: Int, max: Int): Int = maxOf(min, minOf(max, this))

/**
 * Converts this epoch-millisecond duration into a human-readable string.
 *
 * ## Example output
 *
 * ```
 * 90_000L.toReadableDuration()      // → "1m 30s"
 * 3_661_000L.toReadableDuration()   // → "1h 1m"
 * 90_000_000L.toReadableDuration()  // → "1d 1h"
 * ```
 */
fun Long.toReadableDuration(): String {
    val seconds = this / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    return when {
        days > 0 -> "${days}d ${hours % 24}h"
        hours > 0 -> "${hours}h ${minutes % 60}m"
        minutes > 0 -> "${minutes}m ${seconds % 60}s"
        else -> "${seconds}s"
    }
}

/**
 * Converts this integer to its ordinal string representation.
 *
 * ## Example
 *
 * ```kotlin
 * 1.toOrdinal()  // → "1st"
 * 2.toOrdinal()  // → "2nd"
 * 11.toOrdinal() // → "11th"
 * 21.toOrdinal() // → "21st"
 * ```
 */
fun Int.toOrdinal(): String {
    val suffix = when {
        this % 100 in 11..13 -> "th"
        this % 10 == 1 -> "st"
        this % 10 == 2 -> "nd"
        this % 10 == 3 -> "rd"
        else -> "th"
    }
    return "$this$suffix"
}
