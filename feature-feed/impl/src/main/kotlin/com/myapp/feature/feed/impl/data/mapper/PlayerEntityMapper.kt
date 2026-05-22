package com.myapp.feature.feed.impl.data.mapper

import com.myapp.core.database.entity.PlayerEntity
import com.myapp.core.database.mapper.EntityMapper
import com.myapp.feature.feed.api.model.Player
import javax.inject.Inject

/**
 * Maps between [PlayerEntity] (Room database layer) and [Player] (domain layer).
 *
 * Implements the [EntityMapper] interface from `core:database`, keeping the mapping
 * logic in one place and making it injectable for easier testing.
 *
 * ## Example
 *
 * ```kotlin
 * // Single entity → domain
 * val player: Player = mapper.toDomain(playerEntity)
 *
 * // Single domain → entity
 * val entity: PlayerEntity = mapper.toEntity(player)
 *
 * // Bulk conversions (inherited from EntityMapper)
 * val players: List<Player> = mapper.toDomainList(entities)
 * val entities: List<PlayerEntity> = mapper.toEntityList(players)
 * ```
 */
class PlayerEntityMapper @Inject constructor() : EntityMapper<PlayerEntity, Player> {

    /**
     * Converts a [PlayerEntity] database row into a [Player] domain model.
     */
    override fun toDomain(entity: PlayerEntity) = Player(
        id = entity.id,
        name = entity.name,
        nationality = entity.nationality,
        teamName = entity.teamName,
        photoUrl = entity.photoUrl,
        position = entity.position,
        goals = entity.goals,
        assists = entity.assists,
        rating = entity.rating,
        description = entity.description,
    )

    /**
     * Converts a [Player] domain model into a [PlayerEntity] for Room persistence.
     */
    override fun toEntity(domain: Player) = PlayerEntity(
        id = domain.id,
        name = domain.name,
        nationality = domain.nationality,
        teamName = domain.teamName,
        photoUrl = domain.photoUrl,
        position = domain.position,
        goals = domain.goals,
        assists = domain.assists,
        rating = domain.rating,
        description = domain.description,
    )
}
