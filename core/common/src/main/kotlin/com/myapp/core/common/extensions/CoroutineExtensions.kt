package com.myapp.core.common.extensions

import com.myapp.core.common.dispatchers.AppDispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout as kotlinWithTimeout

/**
 * Runs [block] with a timeout of [timeMillis] milliseconds, returning `null` if the
 * timeout is exceeded instead of throwing [TimeoutCancellationException].
 *
 * ## Example
 *
 * ```kotlin
 * val result: Data? = withTimeout(5_000L) { api.fetchData() }
 * if (result == null) {
 *     showError("Request timed out")
 * }
 * ```
 *
 * @param timeMillis Maximum duration in milliseconds before cancellation.
 * @param block The suspend work to execute.
 * @return The result of [block], or `null` if [timeMillis] elapsed first.
 */
suspend fun <T> withTimeout(timeMillis: Long, block: suspend () -> T): T? = try {
    kotlinWithTimeout(timeMillis) { block() }
} catch (e: TimeoutCancellationException) {
    null
}

/**
 * Launches a coroutine in this scope with structured error handling.
 *
 * Like [CoroutineScope.launch] but catches any non-cancellation [Throwable] and forwards
 * it to [onError]. [CancellationException] is always re-thrown to preserve structured
 * concurrency.
 *
 * Prefer [com.myapp.core.common.concurrency.safeLaunch] inside a ViewModel, which
 * automatically uses `viewModelScope`.
 *
 * ## Example
 *
 * ```kotlin
 * viewModelScope.safeLaunch(onError = { e -> showError(e.message) }) {
 *     val data = repository.fetchData()
 *     updateUi(data)
 * }
 * ```
 *
 * @param onError Called with the caught [Throwable] when an exception escapes [block].
 * @param block The suspend work to execute.
 */
fun CoroutineScope.safeLaunch(
    onError: (Throwable) -> Unit = {},
    block: suspend CoroutineScope.() -> Unit,
): Job = launch {
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        onError(e)
    }
}

/**
 * Launches a coroutine on [AppDispatchers.io] using this scope.
 *
 * Convenience wrapper to avoid importing both [CoroutineScope.launch] and
 * [kotlinx.coroutines.Dispatchers.IO] at every call site.
 *
 * ## Example
 *
 * ```kotlin
 * scope.launchIO(dispatchers) {
 *     database.insert(entity)
 * }
 * ```
 */
fun CoroutineScope.launchIO(
    appDispatchers: AppDispatchers,
    block: suspend CoroutineScope.() -> Unit,
): Job = launch(appDispatchers.io) { block() }

/**
 * Launches a coroutine on [AppDispatchers.main] using this scope.
 *
 * Convenience wrapper for posting UI updates from a background scope.
 *
 * ## Example
 *
 * ```kotlin
 * scope.launchMain(dispatchers) {
 *     binding.textView.text = result
 * }
 * ```
 */
fun CoroutineScope.launchMain(
    appDispatchers: AppDispatchers,
    block: suspend CoroutineScope.() -> Unit,
): Job = launch(appDispatchers.main) { block() }
