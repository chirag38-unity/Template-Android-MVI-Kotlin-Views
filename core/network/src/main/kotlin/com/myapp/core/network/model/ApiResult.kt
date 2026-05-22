package com.myapp.core.network.model

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val error: NetworkError) : ApiResult<Nothing>()
}

data class NetworkError(
    val code: Int? = null,
    val message: String,
    val cause: Throwable? = null,
)
