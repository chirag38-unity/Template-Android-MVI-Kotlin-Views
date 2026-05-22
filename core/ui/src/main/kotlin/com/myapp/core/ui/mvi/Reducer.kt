package com.myapp.core.ui.mvi

fun interface Reducer<S : UiState, E : UiEvent> {
    fun reduce(state: S, event: E): S
}
