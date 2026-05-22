package com.myapp.core.common.concurrency

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

suspend fun <A, B> parallelMap(
    a: suspend () -> A,
    b: suspend () -> B,
): Pair<A, B> = coroutineScope {
    val deferredA = async { a() }
    val deferredB = async { b() }
    Pair(deferredA.await(), deferredB.await())
}

suspend fun <T> List<suspend () -> T>.executeParallel(): List<T> = coroutineScope {
    map { task -> async { task() } }.map { it.await() }
}
