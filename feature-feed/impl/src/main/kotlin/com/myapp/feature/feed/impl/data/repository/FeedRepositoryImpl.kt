package com.myapp.feature.feed.impl.data.repository

import com.myapp.core.common.result.Result
import com.myapp.core.database.dao.PlayerDao
import com.myapp.feature.feed.api.PlayerRepository
import com.myapp.feature.feed.api.model.Player
import com.myapp.feature.feed.impl.data.FakePlayersDataSource
import com.myapp.feature.feed.impl.data.mapper.PlayerEntityMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Offline-first implementation of [PlayerRepository].
 *
 * Uses [FakePlayersDataSource] as a mock backend (to be replaced by a real API) and
 * [PlayerDao] (Room) as the local cache. Entity↔domain mapping is delegated to
 * [PlayerEntityMapper] which implements the `core:database` [com.myapp.core.database.mapper.EntityMapper]
 * interface.
 *
 * ## Data flow for [getPlayers]
 * ```
 * 1. Emit Result.Loading
 * 2. Emit cached players immediately (offline-first)
 * 3. Fetch fresh data from the mock source (simulated network call)
 * 4. Clear & repopulate the local cache with fresh data
 * 5. Emit fresh players
 * ```
 *
 * @param playerDao Room DAO for reading and writing cached player rows.
 * @param mapper Converts between [com.myapp.core.database.entity.PlayerEntity] and [Player].
 */
class FeedRepositoryImpl @Inject constructor(
    private val playerDao: PlayerDao,
    private val mapper: PlayerEntityMapper,
) : PlayerRepository {

    override fun getPlayers(): Flow<Result<List<Player>>> = flow {
        emit(Result.Loading)

        // 1. Emit cached data immediately (offline-first)
        val cached = playerDao.getAllPlayers()
        if (cached.isNotEmpty()) {
            emit(Result.Success(mapper.toDomainList(cached)))
        }

        // 2. "Fetch" from the mock source (simulates a network call)
        val fresh = FakePlayersDataSource.players

        // 3. Refresh the cache
        playerDao.deleteAll()
        playerDao.insertAll(mapper.toEntityList(fresh))

        // 4. Emit the fresh list
        emit(Result.Success(fresh))
    }

    override suspend fun getPlayerById(id: String): Result<Player> {
        val entity = playerDao.getPlayerById(id)
        if (entity != null) {
            return Result.Success(mapper.toDomain(entity))
        }
        // Fall back to mock source if not in cache
        val player = FakePlayersDataSource.players.firstOrNull { it.id == id }
        return if (player != null) {
            playerDao.insertAll(listOf(mapper.toEntity(player)))
            Result.Success(player)
        } else {
            Result.Error(NoSuchElementException("Player $id not found"), "Player not found")
        }
    }
}
