package com.myapp.core.database.result

sealed class DatabaseResult<out T> {
    data class Success<T>(val data: T) : DatabaseResult<T>()
    data object Empty : DatabaseResult<Nothing>()
    data class Error(val exception: Throwable) : DatabaseResult<Nothing>()
}

suspend fun <T> dbCall(block: suspend () -> T): DatabaseResult<T> = try {
    val result = block()
    if (result == null) DatabaseResult.Empty else DatabaseResult.Success(result)
} catch (e: Exception) {
    DatabaseResult.Error(e)
}
