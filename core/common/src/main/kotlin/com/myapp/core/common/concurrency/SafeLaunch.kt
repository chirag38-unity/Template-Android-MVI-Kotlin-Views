package com.myapp.core.common.concurrency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Launches a coroutine in [viewModelScope] with structured error handling.
 *
 * Unlike a raw `viewModelScope.launch { }`, this extension:
 * - Catches any [Throwable] thrown inside [block] (except [CancellationException], which
 *   is always re-thrown to maintain structured concurrency).
 * - Forwards unexpected errors to [onError] so the ViewModel can update UI state without
 *   crashing the app.
 *
 * ## Usage
 *
 * ```kotlin
 * // Basic usage — swallows unexpected errors silently
 * safeLaunch {
 *     updateState { copy(isLoading = true) }
 *     val data = repository.fetchData()
 *     updateState { copy(isLoading = false, data = data) }
 * }
 *
 * // With error handling — surface the error in UI state
 * safeLaunch(
 *     onError = { e ->
 *         updateState { copy(isLoading = false, error = e.message) }
 *     }
 * ) {
 *     updateState { copy(isLoading = true) }
 *     val data = repository.fetchData()
 *     updateState { copy(isLoading = false, data = data) }
 * }
 *
 * // On a background dispatcher (e.g. IO-heavy work)
 * safeLaunch(dispatcher = Dispatchers.IO) {
 *     processHeavyData()
 * }
 * ```
 *
 * @param dispatcher The [CoroutineDispatcher] to run [block] on. Defaults to [Dispatchers.Main].
 * @param onError Called with the caught [Throwable] when an exception escapes [block].
 *   Defaults to a no-op. **Note**: this callback runs on [dispatcher].
 * @param block The suspend work to execute inside [viewModelScope].
 * @return The [Job] backing the launched coroutine.
 */
fun ViewModel.safeLaunch(
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
    onError: (Throwable) -> Unit = {},
    block: suspend () -> Unit,
): Job = this.viewModelScope.launch(dispatcher) {
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        onError(e)
    }
}
