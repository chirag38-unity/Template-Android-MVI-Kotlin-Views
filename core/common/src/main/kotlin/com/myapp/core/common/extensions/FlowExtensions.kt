package com.myapp.core.common.extensions

import com.myapp.core.common.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Wraps each emission of this [Flow] in a [Result.Success], and any thrown exception in
 * a [Result.Error], producing a [Flow] of [Result].
 *
 * This is a convenience extension for converting a raw-data flow into a result-aware flow
 * without needing try/catch at every collection site.
 *
 * ## Example
 *
 * ```kotlin
 * // In a repository
 * fun getUsers(): Flow<Result<List<User>>> =
 *     localDataSource.observeUsers().asResult()
 *
 * // In a ViewModel
 * repository.getUsers().collect { result ->
 *     when (result) {
 *         is Result.Success -> updateState { copy(users = result.data) }
 *         is Result.Error   -> updateState { copy(error = result.message) }
 *         Result.Loading    -> { /* not emitted by asResult() */ }
 *     }
 * }
 * ```
 *
 * @return A [Flow] that emits [Result.Success] for each upstream value and a single
 *   [Result.Error] if the upstream throws.
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> {
    return map<T, Result<T>> { Result.Success(it) }
        .catch { emit(Result.Error(it, it.message)) }
}
