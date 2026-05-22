package com.myapp.core.common.concurrency

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlin.time.Duration

/**
 * Returns a [Flow] that emits items from this flow only after [windowDuration] has elapsed
 * without another emission. Wraps Kotlin's built-in [debounce] operator.
 *
 * Useful for rate-limiting hot flows such as a search query input, where you want to
 * wait for the user to stop typing before triggering a network request.
 *
 * ## Example
 *
 * ```kotlin
 * searchQueryFlow
 *     .debounceFlow(300.milliseconds)
 *     .collect { query -> viewModel.search(query) }
 * ```
 *
 * @param windowDuration The silence window after which the latest emission is forwarded.
 */
fun <T> Flow<T>.debounceFlow(windowDuration: Duration): Flow<T> = debounce(windowDuration)

/**
 * A simple debounce helper that delays execution of a suspend block until [delayMs]
 * milliseconds have elapsed without a new [invoke] call.
 *
 * Useful for debouncing UI actions that are not backed by a [Flow], such as a text-change
 * listener that directly calls a ViewModel method.
 *
 * Any previously scheduled (but not yet executed) block is cancelled when [invoke] is
 * called again. Cancelled jobs are cleaned up automatically; call [cancel] explicitly only
 * if you need to abort the pending action before it fires (e.g. on view destruction).
 *
 * ## Example
 *
 * ```kotlin
 * // Inside a Fragment
 * private val searchDebounce = DebouncedAction(viewLifecycleOwner.lifecycleScope)
 *
 * binding.editSearch.doOnTextChanged { text, _, _, _ ->
 *     searchDebounce.invoke {
 *         viewModel.sendIntent(SearchIntent.OnQueryChanged(text?.toString().orEmpty()))
 *     }
 * }
 *
 * override fun onDestroyView() {
 *     super.onDestroyView()
 *     searchDebounce.cancel()
 * }
 * ```
 *
 * @param scope The [CoroutineScope] used to schedule the delayed block.
 * @param delayMs Milliseconds to wait before executing the block. Defaults to 300 ms.
 */
class DebouncedAction(
    private val scope: CoroutineScope,
    private val delayMs: Long = 300L,
) {
    private var job: Job? = null

    /** Schedules [block] to run after [delayMs], cancelling any previously scheduled block. */
    fun invoke(block: suspend () -> Unit) {
        job?.cancel()
        job = scope.launch {
            delay(delayMs)
            block()
        }
    }

    /** Cancels any pending block without executing it. */
    fun cancel() {
        job?.cancel()
        job = null
    }
}
