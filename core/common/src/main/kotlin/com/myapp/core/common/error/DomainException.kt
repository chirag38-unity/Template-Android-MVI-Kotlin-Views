package com.myapp.core.common.error

/**
 * Sealed hierarchy of domain-level exceptions used to decouple feature modules from
 * framework-specific error types (e.g. Retrofit exceptions, SQLite exceptions).
 *
 * Map all raw errors to a [DomainException] subclass at repository boundaries using
 * [ExceptionMapper.map] so that ViewModels and use cases only deal with clean domain errors.
 *
 * ## Example mapping at a repository boundary
 *
 * ```kotlin
 * try {
 *     api.getPlayer(id)
 * } catch (e: IOException) {
 *     throw DomainException.NetworkException("No internet connection", cause = e)
 * } catch (e: HttpException) {
 *     throw DomainException.NetworkException("Server error ${e.code()}", cause = e)
 * }
 * ```
 *
 * ## Example handling in a ViewModel
 *
 * ```kotlin
 * safeLaunch(onError = { e ->
 *     val message = when (e) {
 *         is DomainException.NetworkException  -> "Check your internet connection"
 *         is DomainException.DatabaseException -> "Failed to read local data"
 *         is DomainException.AuthException     -> "Session expired, please log in again"
 *         else                                 -> e.message ?: "Unknown error"
 *     }
 *     updateState { copy(error = message) }
 * }) {
 *     loadData()
 * }
 * ```
 */
sealed class DomainException(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause) {

    /** Thrown when a network request fails (e.g. no connectivity, timeout, HTTP error). */
    class NetworkException(
        message: String? = null,
        cause: Throwable? = null,
    ) : DomainException(message, cause)

    /** Thrown when a local database operation fails. */
    class DatabaseException(
        message: String? = null,
        cause: Throwable? = null,
    ) : DomainException(message, cause)

    /**
     * Thrown when input validation fails.
     *
     * @param field The name of the field that failed validation.
     */
    class ValidationException(
        val field: String,
        message: String? = null,
        cause: Throwable? = null,
    ) : DomainException(message, cause)

    /** Thrown when the user is not authenticated or their session has expired. */
    class AuthException(
        message: String? = null,
        cause: Throwable? = null,
    ) : DomainException(message, cause)

    /** Fallback for errors that do not fit the other categories. */
    class UnknownException(
        message: String? = null,
        cause: Throwable? = null,
    ) : DomainException(message, cause)
}
