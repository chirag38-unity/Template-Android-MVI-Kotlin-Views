package com.myapp.core.common.extensions

/**
 * Returns the element at index 1. Throws [IndexOutOfBoundsException] if the list has
 * fewer than 2 elements.
 *
 * @throws IndexOutOfBoundsException if `size < 2`.
 */
fun <T> List<T>.second(): T = this[1]

/**
 * Returns the element at index 1, or `null` if the list has fewer than 2 elements.
 */
fun <T> List<T>.secondOrNull(): T? = if (size >= 2) this[1] else null

/**
 * Returns a new list with the elements at positions [i] and [j] swapped.
 *
 * ## Example
 *
 * ```kotlin
 * listOf("a", "b", "c").swap(0, 2) // → ["c", "b", "a"]
 * ```
 */
fun <T> List<T>.swap(i: Int, j: Int): List<T> {
    val mutable = toMutableList()
    val temp = mutable[i]
    mutable[i] = mutable[j]
    mutable[j] = temp
    return mutable
}

/**
 * Returns the index of the first element matching [predicate], or `null` if none matches.
 *
 * Unlike [indexOfFirst], this does not return -1 on failure, making null-checks idiomatic.
 *
 * ## Example
 *
 * ```kotlin
 * val index: Int? = players.indexOfFirstOrNull { it.id == selectedId }
 * ```
 */
fun <T> Iterable<T>.indexOfFirstOrNull(predicate: (T) -> Boolean): Int? {
    forEachIndexed { index, element ->
        if (predicate(element)) return index
    }
    return null
}

/**
 * Returns the value for [key], or throws [NoSuchElementException] if the key is absent.
 *
 * Use when the key is expected to always be present and its absence indicates a bug.
 */
fun <K, V> Map<K, V>.getOrThrow(key: K): V =
    get(key) ?: throw NoSuchElementException("Key $key not found in map")

/**
 * Splits a list into sub-lists, starting a new sub-list each time [predicate] returns `true`
 * (the triggering element is included as the first item of the new sub-list).
 *
 * ## Example
 *
 * ```kotlin
 * val groups = listOf(1, 2, 10, 3, 4, 10, 5).chunkedBy { it == 10 }
 * // → [[1, 2], [10, 3, 4], [10, 5]]
 * ```
 */
fun <T> List<T>.chunkedBy(predicate: (T) -> Boolean): List<List<T>> {
    val result = mutableListOf<List<T>>()
    var current = mutableListOf<T>()
    for (item in this) {
        if (predicate(item) && current.isNotEmpty()) {
            result.add(current)
            current = mutableListOf()
        }
        current.add(item)
    }
    if (current.isNotEmpty()) result.add(current)
    return result
}

/**
 * Filters out pairs whose value is `null`, returning a list of non-null pairs.
 *
 * ## Example
 *
 * ```kotlin
 * listOf("a" to 1, "b" to null, "c" to 3).filterNotNullValues()
 * // → [("a", 1), ("c", 3)]
 * ```
 */
fun <A, B> List<Pair<A, B?>>.filterNotNullValues(): List<Pair<A, B>> =
    mapNotNull { (key, value) -> value?.let { key to it } }
