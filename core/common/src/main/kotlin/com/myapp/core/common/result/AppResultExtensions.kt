package com.myapp.core.common.result

inline fun <T> AppResult<T>.onSuccess(action: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) action(data)
    return this
}

inline fun <T> AppResult<T>.onError(action: (Throwable, String?) -> Unit): AppResult<T> {
    if (this is AppResult.Error) action(exception, message)
    return this
}

inline fun <T> AppResult<T>.onEmpty(action: () -> Unit): AppResult<T> {
    if (this is AppResult.Empty) action()
    return this
}

fun <T> AppResult<T>.getOrNull(): T? = if (this is AppResult.Success) data else null

fun <T> AppResult<T>.getOrDefault(default: T): T = if (this is AppResult.Success) data else default

inline fun <T, R> AppResult<T>.mapSuccess(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Error -> this
    is AppResult.Empty -> AppResult.Empty
}

/**
 * Converts [AppResult] to the existing [Result] type.
 *
 * **Lossy:** [AppResult.Empty] → [Result.Error] with [NoSuchElementException].
 * Use [onEmpty] to handle the empty state distinctly before calling this function.
 */
fun <T> AppResult<T>.toResult(): Result<T> = when (this) {
    is AppResult.Success -> Result.Success(data)
    is AppResult.Error -> Result.Error(exception, message)
    is AppResult.Empty -> Result.Error(NoSuchElementException("Empty result"), "Empty result")
}

/**
 * Converts the existing [Result] to [AppResult].
 *
 * **Lossy:** [Result.Loading] → [AppResult.Empty] (in-flight/loading state is dropped).
 * Check for [Result.Loading] before calling this function if that state is meaningful.
 */
fun <T> Result<T>.toAppResult(): AppResult<T> = when (this) {
    is Result.Success -> AppResult.Success(data)
    is Result.Error -> AppResult.Error(exception, message)
    is Result.Loading -> AppResult.Empty
}
