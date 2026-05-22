package com.myapp.core.cache.impl.serializers

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import com.myapp.core.cache.api.Expirable
import kotlinx.serialization.InternalSerializationApi

/**
 * Serializable on-disk representation of a cache entry.
 *
 * The [data] field holds the serialized payload as a [JsonElement] so that the outer DTO can be
 * decoded without knowing the concrete type T at the time of deserialization.  The typed value is
 * decoded in a second step using the [KSerializer] carried by the [CacheKey].
 */
@InternalSerializationApi
@Serializable
internal data class CachedValueDto(
    val data: JsonElement,
    val createdAt: Long,
    override val expiresAt: Long? = null,
    val version: Int = 1,
) : Expirable
