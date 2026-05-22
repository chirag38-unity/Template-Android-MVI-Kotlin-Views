package com.myapp.core.ui.extensions

import android.view.View
import androidx.core.view.isVisible

/**
 * Makes the view visible (`VISIBLE`).
 *
 * Prefer this over setting [View.visibility] directly for brevity and readability.
 *
 * ## Example
 *
 * ```kotlin
 * binding.progressBar.show()
 * ```
 */
fun View.show() {
    isVisible = true
}

/** Makes the view invisible (still occupies layout space). Use [gone] to remove from layout. */
fun View.invisible() {
    visibility = View.INVISIBLE
}

/**
 * Hides the view and removes it from the layout flow (`GONE`).
 *
 * The view no longer occupies any space. Use [invisible] if you need the view to
 * keep its space while hidden.
 *
 * ## Example
 *
 * ```kotlin
 * binding.errorLayout.hide()
 * ```
 */
fun View.hide() {
    visibility = View.GONE
}

/** Removes the view from the layout flow (`GONE`). Alias for [hide]. */
fun View.gone() {
    visibility = View.GONE
}
