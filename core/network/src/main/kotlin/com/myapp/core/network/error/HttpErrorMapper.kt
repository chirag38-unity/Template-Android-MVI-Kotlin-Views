package com.myapp.core.network.error

import com.myapp.core.common.error.DomainException
import com.myapp.core.network.model.NetworkError

object HttpErrorMapper {

    fun map(networkError: NetworkError): DomainException {
        val code = networkError.code
        return when {
            code != null -> ApiErrorParser.parse(code, networkError.message)
            networkError.cause != null -> DomainException.NetworkException(
                message = networkError.message,
                cause = networkError.cause,
            )
            else -> DomainException.UnknownException(message = networkError.message)
        }
    }
}
