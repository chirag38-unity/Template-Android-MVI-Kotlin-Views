package com.myapp.core.domain.usecase

import com.myapp.core.common.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Base class for a use case that takes a parameter and returns a [Flow] of results.
 *
 * Subclasses implement [execute] which returns raw domain data; [invoke] automatically
 * wraps each emission in [Result.Success] and any thrown exception in [Result.Error].
 *
 * ## Implementation example
 *
 * ```kotlin
 * class GetUserUseCase @Inject constructor(
 *     private val repository: UserRepository,
 * ) : UseCase<String, User>() {
 *
 *     override fun execute(parameters: String): Flow<User> =
 *         repository.observeUser(parameters)
 * }
 * ```
 *
 * ## Usage in a ViewModel
 *
 * ```kotlin
 * getUserUseCase(userId).collect { result ->
 *     when (result) {
 *         is Result.Success -> updateState { copy(user = result.data) }
 *         is Result.Error   -> updateState { copy(error = result.message) }
 *         Result.Loading    -> { /* not emitted by UseCase */ }
 *     }
 * }
 * ```
 *
 * @param P Type of the input parameter.
 * @param R Type of the domain result.
 */
abstract class UseCase<in P, out R> {
    operator fun invoke(parameters: P): Flow<Result<R>> {
        return execute(parameters)
            .map<R, Result<R>> { Result.Success(it) }
            .catch { emit(Result.Error(it, it.message)) }
    }

    protected abstract fun execute(parameters: P): Flow<R>
}

/**
 * Base class for a use case that takes no parameter and returns a [Flow] of results.
 *
 * Subclasses implement [execute] which returns raw domain data; [invoke] automatically
 * wraps each emission in [Result.Success] and any thrown exception in [Result.Error].
 *
 * **Note**: [execute] must return a plain `Flow<R>` (not `Flow<Result<R>>`). If your
 * repository already returns a `Flow<Result<R>>` containing [Result.Loading] states,
 * delegate directly rather than extending this class.
 *
 * ## Implementation example
 *
 * ```kotlin
 * class GetAllPlayersUseCase @Inject constructor(
 *     private val repository: PlayerRepository,
 * ) : NoParamUseCase<List<Player>>() {
 *
 *     override fun execute(): Flow<List<Player>> = repository.observePlayers()
 * }
 * ```
 *
 * @param R Type of the domain result.
 */
abstract class NoParamUseCase<out R> {
    operator fun invoke(): Flow<Result<R>> {
        return execute()
            .map<R, Result<R>> { Result.Success(it) }
            .catch { emit(Result.Error(it, it.message)) }
    }

    protected abstract fun execute(): Flow<R>
}

/**
 * Base class for a use case that takes a parameter and executes a **single** suspend call
 * (non-streaming), returning a [Result] directly.
 *
 * Subclasses implement [execute] which may throw; [invoke] wraps success in
 * [Result.Success] and any exception in [Result.Error].
 *
 * ## Implementation example
 *
 * ```kotlin
 * class LoginUseCase @Inject constructor(
 *     private val authRepository: AuthRepository,
 * ) : SuspendUseCase<LoginParams, User>() {
 *
 *     override suspend fun execute(parameters: LoginParams): User =
 *         authRepository.login(parameters.email, parameters.password)
 * }
 * ```
 *
 * ## Usage
 *
 * ```kotlin
 * val result: Result<User> = loginUseCase(LoginParams(email, password))
 * when (result) {
 *     is Result.Success -> navigateToHome()
 *     is Result.Error   -> showError(result.message)
 *     Result.Loading    -> { /* not emitted by SuspendUseCase */ }
 * }
 * ```
 *
 * @param P Type of the input parameter.
 * @param R Type of the domain result.
 */
abstract class SuspendUseCase<in P, out R> {
    suspend operator fun invoke(parameters: P): Result<R> {
        return try {
            Result.Success(execute(parameters))
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    protected abstract suspend fun execute(parameters: P): R
}
