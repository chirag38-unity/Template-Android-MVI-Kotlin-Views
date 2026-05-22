package com.myapp.core.ui.mvi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StateStore<S : UiState>(initial: S) {

    private val _state = MutableStateFlow(initial)
    val state: StateFlow<S> = _state.asStateFlow()

    fun update(reducer: S.() -> S) {
        _state.value = _state.value.reducer()
    }

    fun <R> withState(block: S.() -> R): R = _state.value.block()
}
