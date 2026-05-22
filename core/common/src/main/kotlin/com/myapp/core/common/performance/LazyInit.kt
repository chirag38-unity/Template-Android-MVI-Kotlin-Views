package com.myapp.core.common.performance

import java.util.concurrent.atomic.AtomicReference

class AtomicLazy<T>(private val initializer: () -> T) {

    private val ref = AtomicReference<Any?>(UNINITIALIZED)

    @Suppress("UNCHECKED_CAST")
    val value: T
        get() {
            val current = ref.get()
            if (current !== UNINITIALIZED) return current as T
            val newValue = initializer()
            return if (ref.compareAndSet(UNINITIALIZED, newValue)) newValue else ref.get() as T
        }

    val isInitialized: Boolean get() = ref.get() !== UNINITIALIZED

    private companion object {
        val UNINITIALIZED = Any()
    }
}

fun <T> atomicLazy(init: () -> T): AtomicLazy<T> = AtomicLazy(init)
