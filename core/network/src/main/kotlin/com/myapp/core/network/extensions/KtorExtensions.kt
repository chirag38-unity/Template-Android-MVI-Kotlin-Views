package com.myapp.core.network.extensions

import com.myapp.core.network.model.ApiResult
import com.myapp.core.network.model.NetworkError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders

suspend inline fun <reified T> HttpClient.getOrNull(
    url: String,
    block: HttpRequestBuilder.() -> Unit = {},
): T? = try {
    get(url, block).body()
} catch (e: Exception) {
    null
}

suspend inline fun <reified T> HttpClient.safeGet(url: String): ApiResult<T> = try {
    ApiResult.Success(get(url).body())
} catch (e: Exception) {
    ApiResult.Error(NetworkError(message = e.message ?: "Unknown error", cause = e))
}

fun HttpRequestBuilder.addAuthHeader(token: String) {
    header(HttpHeaders.Authorization, "Bearer $token")
}

fun HttpRequestBuilder.addETag(etag: String) {
    header(HttpHeaders.IfNoneMatch, etag)
}
