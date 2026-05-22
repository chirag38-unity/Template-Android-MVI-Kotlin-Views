package com.myapp.core.ui.insets

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Applies system window inset padding to this view so its content is not obscured by
 * the status bar, navigation bar, or other system UI elements.
 *
 * The original padding values set in XML are preserved and the inset amounts are **added**
 * on top, so the layout behaves correctly whether or not insets are present.
 *
 * Prefer this helper over manual [ViewCompat.setOnApplyWindowInsetsListener] calls to
 * keep inset handling consistent and concise across the app.
 *
 * ## Example — status bar only (top app bar / list)
 *
 * ```kotlin
 * binding.recyclerView.applySystemWindowInsetsPadding(applyTop = true)
 * ```
 *
 * ## Example — navigation bar only (bottom sheet / FAB)
 *
 * ```kotlin
 * binding.bottomSheet.applySystemWindowInsetsPadding(applyBottom = true)
 * ```
 *
 * ## Example — all edges (full-screen content)
 *
 * ```kotlin
 * binding.root.applySystemWindowInsetsPadding(
 *     applyTop    = true,
 *     applyBottom = true,
 *     applyLeft   = true,
 *     applyRight  = true,
 * )
 * ```
 *
 * @param applyTop    Whether to add the top system inset (status bar height) as top padding.
 * @param applyBottom Whether to add the bottom system inset (navigation bar height) as bottom padding.
 * @param applyLeft   Whether to add the left system inset as left padding.
 * @param applyRight  Whether to add the right system inset as right padding.
 */
fun View.applySystemWindowInsetsPadding(
    applyTop: Boolean = false,
    applyBottom: Boolean = false,
    applyLeft: Boolean = false,
    applyRight: Boolean = false,
) {
    val originalLeft = paddingLeft
    val originalTop = paddingTop
    val originalRight = paddingRight
    val originalBottom = paddingBottom
    applyInsets { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.setPadding(
            originalLeft + if (applyLeft) systemBars.left else 0,
            originalTop + if (applyTop) systemBars.top else 0,
            originalRight + if (applyRight) systemBars.right else 0,
            originalBottom + if (applyBottom) systemBars.bottom else 0,
        )
    }
}

/**
 * Applies system window inset margins to this view.
 *
 * Similar to [applySystemWindowInsetsPadding] but adjusts layout margins instead of
 * internal padding. Useful for floating views (FABs, overlays) where you want to shift
 * the view rather than add inner space.
 *
 * Original margin values are preserved and insets are added on top.
 *
 * ## Example — floating action button above the navigation bar
 *
 * ```kotlin
 * binding.fab.applySystemWindowInsetsMargin(applyBottom = true)
 * ```
 *
 * @param applyTop    Whether to add the top system inset as a top margin.
 * @param applyBottom Whether to add the bottom system inset as a bottom margin.
 * @param applyLeft   Whether to add the left system inset as a left margin.
 * @param applyRight  Whether to add the right system inset as a right margin.
 */
fun View.applySystemWindowInsetsMargin(
    applyTop: Boolean = false,
    applyBottom: Boolean = false,
    applyLeft: Boolean = false,
    applyRight: Boolean = false,
) {
    val layoutParams = layoutParams as? ViewGroup.MarginLayoutParams ?: return

    val originalLeft = layoutParams.leftMargin
    val originalTop = layoutParams.topMargin
    val originalRight = layoutParams.rightMargin
    val originalBottom = layoutParams.bottomMargin

    applyInsets { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

        val params = view.layoutParams as ViewGroup.MarginLayoutParams

        params.leftMargin = originalLeft + if (applyLeft) systemBars.left else 0
        params.topMargin = originalTop + if (applyTop) systemBars.top else 0
        params.rightMargin = originalRight + if (applyRight) systemBars.right else 0
        params.bottomMargin = originalBottom + if (applyBottom) systemBars.bottom else 0

        view.layoutParams = params
    }
}

private inline fun View.applyInsets(
    crossinline block: (
        view: View,
        insets: WindowInsetsCompat,
    ) -> Unit,
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        block(view, insets)
        insets
    }

    requestApplyInsets()
}
