package com.myapp.core.network.error

import com.myapp.core.common.error.DomainException

object ApiErrorParser {

    fun parse(statusCode: Int, body: String? = null): DomainException = when (statusCode) {
        401, 403 -> DomainException.AuthException(
            message = body ?: "Authentication failed (HTTP $statusCode)",
        )
        404 -> DomainException.NetworkException(
            message = body ?: "Resource not found (HTTP 404)",
        )
        in 400..499 -> DomainException.NetworkException(
            message = body ?: "Client error (HTTP $statusCode)",
        )
        in 500..599 -> DomainException.NetworkException(
            message = body ?: "Server error (HTTP $statusCode)",
        )
        else -> DomainException.UnknownException(
            message = body ?: "Unexpected HTTP status $statusCode",
        )
    }
}
