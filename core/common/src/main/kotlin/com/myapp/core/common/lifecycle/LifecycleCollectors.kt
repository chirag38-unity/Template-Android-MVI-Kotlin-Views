package com.myapp.core.common.lifecycle

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Collects [flow] within the Fragment's **view** lifecycle, automatically stopping when the
 * lifecycle drops below [state] and restarting when it rises back to [state].
 *
 * This is a safe alternative to `lifecycleScope.launch { flow.collect { } }` which would
 * continue collecting even when the fragment's view is destroyed (e.g. when navigating to
 * a new destination), causing unnecessary work and potential crashes.
 *
 * Defaults to [Lifecycle.State.STARTED] which is the recommended state for UI-bound flows:
 * - Collection pauses when the app goes to the background (STOPPED).
 * - Collection resumes when the app returns to the foreground (STARTED).
 *
 * ## Example
 *
 * ```kotlin
 * // Collect a state flow and render it
 * collectWithLifecycle(viewModel.state) { state -> render(state) }
 *
 * // Collect a one-shot effects channel
 * collectWithLifecycle(viewModel.effects) { effect -> handleEffect(effect) }
 *
 * // Collect only when RESUMED (e.g. animations / camera)
 * collectWithLifecycle(viewModel.state, Lifecycle.State.RESUMED) { render(it) }
 * ```
 *
 * @param flow The [Flow] to collect.
 * @param state The minimum [Lifecycle.State] required for collection. Defaults to STARTED.
 * @param collector Called for each emitted value.
 */
fun <T> Fragment.collectWithLifecycle(
    flow: Flow<T>,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    collector: suspend (T) -> Unit,
) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(state) {
            flow.collect { collector(it) }
        }
    }
}

/**
 * Collects [flow] within the Activity's lifecycle, automatically stopping when the
 * lifecycle drops below [state] and restarting when it returns.
 *
 * Prefer the [Fragment] overload when collecting inside a Fragment — use this overload
 * only when collection is owned by an Activity directly.
 *
 * ## Example
 *
 * ```kotlin
 * // In an AppCompatActivity
 * collectWithLifecycle(viewModel.uiState) { state -> render(state) }
 * ```
 *
 * @param flow The [Flow] to collect.
 * @param state The minimum [Lifecycle.State] required for collection. Defaults to STARTED.
 * @param collector Called for each emitted value.
 */
fun <T> AppCompatActivity.collectWithLifecycle(
    flow: Flow<T>,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    collector: suspend (T) -> Unit,
) {
    lifecycleScope.launch {
        repeatOnLifecycle(state) {
            flow.collect { collector(it) }
        }
    }
}
