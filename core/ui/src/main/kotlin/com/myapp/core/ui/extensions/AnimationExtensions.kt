package com.myapp.core.ui.extensions

import android.view.View

fun View.fadeIn(duration: Long = 300) {
    alpha = 0f
    visibility = View.VISIBLE
    animate()
        .alpha(1f)
        .setDuration(duration)
        .start()
}

fun View.fadeOut(duration: Long = 300, gone: Boolean = true) {
    animate()
        .alpha(0f)
        .setDuration(duration)
        .withEndAction {
            visibility = if (gone) View.GONE else View.INVISIBLE
        }
        .start()
}

fun View.slideInFromBottom(duration: Long = 300) {
    translationY = height.toFloat()
    visibility = View.VISIBLE
    animate()
        .translationY(0f)
        .setDuration(duration)
        .start()
}

fun View.crossFade(show: View, hide: View, duration: Long = 300) {
    show.fadeIn(duration)
    hide.fadeOut(duration)
}
