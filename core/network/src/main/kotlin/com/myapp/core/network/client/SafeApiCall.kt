package com.myapp.core.network.client

import com.myapp.core.network.model.ApiResult
import com.myapp.core.network.model.NetworkError
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import java.io.IOException

suspend fun <T> safeApiCall(apiCall: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(apiCall())
    } catch (e: ClientRequestException) {
        ApiResult.Error(
            NetworkError(
                code = e.response.status.value,
                message = e.message,
                cause = e,
            )
        )
    } catch (e: ServerResponseException) {
        ApiResult.Error(
            NetworkError(
                code = e.response.status.value,
                message = e.message,
                cause = e,
            )
        )
    } catch (e: IOException) {
        ApiResult.Error(NetworkError(message = "Network connection error", cause = e))
    } catch (e: Exception) {
        ApiResult.Error(NetworkError(message = e.message ?: "Unknown error", cause = e))
    }
}
