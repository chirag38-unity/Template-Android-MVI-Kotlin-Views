package com.myapp.core.ui.mvi

import java.util.concurrent.atomic.AtomicBoolean

class ConsumeOnce<T>(private val value: T) {

    private val consumed = AtomicBoolean(false)

    fun consume(block: (T) -> Unit) {
        if (consumed.compareAndSet(false, true)) {
            block(value)
        }
    }

    fun peek(): T = value
}

typealias Event<T> = ConsumeOnce<T>
