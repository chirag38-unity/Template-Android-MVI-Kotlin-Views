package com.myapp.core.common.result

/**
 * A discriminated union for representing the outcome of an async operation that can emit
 * multiple states over time (including an intermediate [Loading] state).
 *
 * Use [Result] in repositories and use cases that return [kotlinx.coroutines.flow.Flow].
 * For single-shot suspend operations, prefer [AppResult][com.myapp.core.common.result.AppResult]
 * which replaces [Loading] with [AppResult.Empty].
 *
 * ## Example
 *
 * ```kotlin
 * // Emitting from a repository
 * fun getPlayers(): Flow<Result<List<Player>>> = flow {
 *     emit(Result.Loading)
 *     val cached = dao.getAll()
 *     if (cached.isNotEmpty()) emit(Result.Success(cached.toDomain()))
 *     val fresh = api.fetchPlayers()
 *     emit(Result.Success(fresh))
 * }
 *
 * // Consuming in a ViewModel
 * repository.getPlayers().collect { result ->
 *     when (result) {
 *         is Result.Success -> updateState { copy(players = result.data, isLoading = false) }
 *         is Result.Error   -> updateState { copy(error = result.message, isLoading = false) }
 *         Result.Loading    -> updateState { copy(isLoading = true) }
 *     }
 * }
 * ```
 *
 * ## Chaining with extension functions
 *
 * ```kotlin
 * result
 *     .onSuccess { data -> render(data) }
 *     .onError { _, message -> showError(message) }
 *     .onLoading { showSpinner() }
 * ```
 */
sealed class Result<out T> {
    /** The operation completed successfully and produced [data]. */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * The operation failed with [exception].
     *
     * [message] is an optional human-readable description suitable for display in the UI.
     * Prefer using [message] over [exception].message directly to allow callers to supply
     * localised error strings.
     */
    data class Error(val exception: Throwable, val message: String? = null) : Result<Nothing>()

    /** The operation is in progress. */
    data object Loading : Result<Nothing>()
}

/**
 * Executes [action] if this result is [Result.Success], then returns `this` for chaining.
 */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

/**
 * Executes [action] if this result is [Result.Error], then returns `this` for chaining.
 *
 * @param action Receives the [Throwable] and the optional human-readable message.
 */
inline fun <T> Result<T>.onError(action: (Throwable, String?) -> Unit): Result<T> {
    if (this is Result.Error) action(exception, message)
    return this
}

/**
 * Executes [action] if this result is [Result.Loading], then returns `this` for chaining.
 */
inline fun <T> Result<T>.onLoading(action: () -> Unit): Result<T> {
    if (this is Result.Loading) action()
    return this
}
