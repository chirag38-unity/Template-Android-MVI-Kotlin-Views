package com.myapp.core.ui.click

import android.os.SystemClock
import android.view.View

/**
 * Sets a click listener on this view that ignores rapid consecutive taps within
 * [throttleMs] milliseconds.
 *
 * Prevents accidental double-clicks (e.g. a user tapping a button twice quickly) from
 * triggering duplicate actions such as opening the same screen twice or submitting a form
 * multiple times.
 *
 * ## Example
 *
 * ```kotlin
 * // Prevent double-navigation when the user taps a list item rapidly
 * binding.buttonSubmit.setThrottledClickListener {
 *     viewModel.sendIntent(SubmitIntent)
 * }
 *
 * // Custom throttle window
 * binding.buttonConfirm.setThrottledClickListener(throttleMs = 1_000L) {
 *     viewModel.sendIntent(ConfirmIntent)
 * }
 * ```
 *
 * @param throttleMs The minimum interval in milliseconds between accepted clicks.
 *   Defaults to 500 ms.
 * @param onClick Called with the clicked [View] when the throttle window has elapsed.
 */
fun View.setThrottledClickListener(
    throttleMs: Long = 500L,
    onClick: (View) -> Unit,
) {
    var lastClickTime = 0L
    setOnClickListener { view ->
        val now = SystemClock.uptimeMillis()
        if (now - lastClickTime >= throttleMs) {
            lastClickTime = now
            onClick(view)
        }
    }
}
