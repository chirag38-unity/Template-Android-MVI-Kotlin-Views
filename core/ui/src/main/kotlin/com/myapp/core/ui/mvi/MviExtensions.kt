package com.myapp.core.ui.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Maps a [StateFlow] of [UiState] to a [StateFlow] of [Boolean] using the given [transform].
 * Useful for deriving sub-states like `isLoading` from the full state without observing the
 * entire state object.
 */
fun <S : UiState> StateFlow<S>.mapState(
    scope: CoroutineScope,
    transform: S.() -> Boolean,
): StateFlow<Boolean> = map { it.transform() }
    .stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = value.transform(),
    )
