package com.myapp.core.common.error

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ExceptionMapper {

    fun map(throwable: Throwable): DomainException = when (throwable) {
        is DomainException -> throwable
        is SocketTimeoutException -> DomainException.NetworkException(
            message = "Request timed out",
            cause = throwable,
        )
        is UnknownHostException -> DomainException.NetworkException(
            message = "No internet connection",
            cause = throwable,
        )
        is IOException -> DomainException.NetworkException(
            message = throwable.message,
            cause = throwable,
        )
        is SecurityException -> DomainException.AuthException(
            message = throwable.message,
            cause = throwable,
        )
        else -> DomainException.UnknownException(
            message = throwable.message ?: "An unknown error occurred",
            cause = throwable,
        )
    }
}
