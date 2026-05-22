package com.myapp.core.common.result

/** Distinct from [Result] to avoid name conflicts. Has [Empty] state instead of [Loading]. */
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val exception: Throwable, val message: String? = null) : AppResult<Nothing>
    data object Empty : AppResult<Nothing>
}
