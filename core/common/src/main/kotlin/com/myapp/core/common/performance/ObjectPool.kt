package com.myapp.core.common.performance

class ObjectPool<T>(
    private val maxSize: Int = 10,
    private val factory: () -> T,
    private val reset: (T) -> Unit = {},
) {
    private val pool = ArrayDeque<T>(maxSize)

    fun acquire(): T = synchronized(pool) {
        pool.removeFirstOrNull() ?: factory()
    }

    fun release(obj: T) {
        synchronized(pool) {
            if (pool.size < maxSize) {
                reset(obj)
                pool.addLast(obj)
            }
        }
    }

    fun clear() {
        synchronized(pool) { pool.clear() }
    }

    val size: Int get() = synchronized(pool) { pool.size }
}
