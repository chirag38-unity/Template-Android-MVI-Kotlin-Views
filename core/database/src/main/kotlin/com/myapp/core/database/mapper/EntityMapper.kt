package com.myapp.core.database.mapper

/**
 * Contract for converting between a database entity and a domain model.
 *
 * Implement this interface to keep entity↔domain mapping in one place, making the
 * mapper injectable and independently testable.
 *
 * Default implementations of [toDomainList] and [toEntityList] are provided for bulk
 * conversions using the single-item overloads.
 *
 * ## Implementation example
 *
 * ```kotlin
 * class UserEntityMapper @Inject constructor() : EntityMapper<UserEntity, User> {
 *
 *     override fun toDomain(entity: UserEntity) = User(
 *         id = entity.id,
 *         name = entity.name,
 *         email = entity.email,
 *     )
 *
 *     override fun toEntity(domain: User) = UserEntity(
 *         id = domain.id,
 *         name = domain.name,
 *         email = domain.email,
 *     )
 * }
 * ```
 *
 * ## Usage in a repository
 *
 * ```kotlin
 * class UserRepositoryImpl @Inject constructor(
 *     private val dao: UserDao,
 *     private val mapper: UserEntityMapper,
 * ) : UserRepository {
 *
 *     override fun getUsers(): Flow<List<User>> =
 *         dao.observeAll().map { mapper.toDomainList(it) }
 *
 *     override suspend fun saveUser(user: User) =
 *         dao.insert(mapper.toEntity(user))
 * }
 * ```
 *
 * @param Entity The Room database entity type.
 * @param Domain The clean-architecture domain model type.
 */
interface EntityMapper<Entity, Domain> {
    /** Converts a single [Entity] row into a [Domain] model. */
    fun toDomain(entity: Entity): Domain

    /** Converts a single [Domain] model into an [Entity] row for persistence. */
    fun toEntity(domain: Domain): Entity

    /** Converts a list of [Entity] rows into [Domain] models. */
    fun toDomainList(entities: List<Entity>): List<Domain> = entities.map(::toDomain)

    /** Converts a list of [Domain] models into [Entity] rows. */
    fun toEntityList(domains: List<Domain>): List<Entity> = domains.map(::toEntity)
}
