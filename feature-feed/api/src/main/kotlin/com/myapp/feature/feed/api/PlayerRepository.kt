package com.myapp.feature.feed.api

import com.myapp.core.common.result.Result
import com.myapp.feature.feed.api.model.Player
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for player data. Exposed in :api so other features
 * (e.g. feature-search) can depend on it without an impl→impl coupling.
 */
interface PlayerRepository {
    fun getPlayers(): Flow<Result<List<Player>>>
    suspend fun getPlayerById(id: String): Result<Player>
}