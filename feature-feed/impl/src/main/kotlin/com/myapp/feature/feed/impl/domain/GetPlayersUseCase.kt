package com.myapp.feature.feed.impl.domain

import com.myapp.core.common.result.Result
import com.myapp.feature.feed.api.PlayerRepository
import com.myapp.feature.feed.api.model.Player
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case that exposes the player list from [PlayerRepository].
 *
 * ## Design note — why not extend [com.myapp.core.domain.usecase.NoParamUseCase]?
 *
 * [com.myapp.core.domain.usecase.NoParamUseCase] expects `execute()` to return a plain
 * `Flow<R>` and wraps each item in [Result.Success] automatically. However,
 * [PlayerRepository.getPlayers] already returns a `Flow<Result<List<Player>>>` that
 * includes the [Result.Loading] state for the offline-first pattern. Wrapping that flow
 * again would produce `Result<Result<List<Player>>>`, which is incorrect.
 *
 * For this reason, this use case acts as a thin delegation layer: it forwards the repository
 * flow unchanged, keeping the Loading/Success/Error states intact. Use this pattern whenever
 * the underlying data source already manages result states.
 *
 * ## Usage in a ViewModel
 *
 * ```kotlin
 * getPlayersUseCase().collect { result ->
 *     when (result) {
 *         is Result.Success -> updateState { copy(players = result.data, isLoading = false) }
 *         is Result.Error   -> updateState { copy(error = result.message, isLoading = false) }
 *         Result.Loading    -> updateState { copy(isLoading = true) }
 *     }
 * }
 * ```
 */
class GetPlayersUseCase @Inject constructor(
    private val repository: PlayerRepository,
) {
    operator fun invoke(): Flow<Result<List<Player>>> = repository.getPlayers()
}
