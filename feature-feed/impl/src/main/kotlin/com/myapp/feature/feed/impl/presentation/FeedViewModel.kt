package com.myapp.feature.feed.impl.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.myapp.core.common.concurrency.safeLaunch
import com.myapp.core.common.result.Result
import com.myapp.core.ui.base.BaseViewModel
import com.myapp.feature.feed.impl.domain.GetPlayersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel for the Feed screen.
 *
 * Responsibilities:
 * - Loads the player list via [GetPlayersUseCase] on first subscription (lazy loading).
 * - Persists state in [SavedStateHandle] for process-death and configuration-change recovery.
 * - Handles the [FeedIntent.RetryLoad] intent to trigger a fresh data load.
 * - Emits [FeedEffect.NavigateToPlayerDetails] when a player is tapped.
 *
 * ## State flow
 * ```
 * FeedFragment subscribes to state
 *       │
 *       └── onStart triggers loadPlayers()
 *               │
 *               ├── updateState(isLoading = true)
 *               │
 *               └── getPlayersUseCase().collect { result ->
 *                       Result.Success  → updateState(players = ...)
 *                       Result.Error    → updateState(error = ...) + ShowError effect
 *                       Result.Loading  → updateState(isLoading = true)
 *                   }
 * ```
 *
 * @param savedStateHandle Injected by Hilt; used to persist [FeedState] across process death.
 * @param getPlayersUseCase Use case that fetches the player list from the repository.
 */
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getPlayersUseCase: GetPlayersUseCase,
) : BaseViewModel<FeedState, FeedIntent, FeedEffect>(FeedState()) {

    companion object {
        private const val KEY_FEED_STATE = "KEY_FEED_STATE"
        private const val UNKNOWN_ERROR = "An unexpected error occurred"
    }

    private val internalState = savedStateHandle.getStateFlow(KEY_FEED_STATE, FeedState())

    override val state: StateFlow<FeedState> = internalState
        .onStart { loadPlayers() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = FeedState(),
        )

    override fun updateState(reducer: FeedState.() -> FeedState) {
        savedStateHandle[KEY_FEED_STATE] = internalState.value.reducer()
    }

    override fun handleIntent(intent: FeedIntent) {
        when (intent) {
            FeedIntent.RetryLoad -> loadPlayers()
            is FeedIntent.OnPlayerClick -> sendEffect(FeedEffect.NavigateToPlayerDetails(intent.player.id))
        }
    }

    /**
     * Triggers a data load unless one is already in progress.
     *
     * Uses [safeLaunch] (from `core:common`) to handle unexpected coroutine exceptions
     * without crashing the app. Domain-level errors from the repository are handled
     * via [Result.Error] inside the collect block.
     */
    private fun loadPlayers() {
        if (internalState.value.isLoading) return
        safeLaunch(
            onError = { e ->
                val message = e.message ?: UNKNOWN_ERROR
                updateState { copy(isLoading = false, error = message) }
                sendEffect(FeedEffect.ShowError(message))
            },
        ) {
            updateState { copy(isLoading = true, error = null) }
            getPlayersUseCase().collect { result ->
                when (result) {
                    is Result.Success -> updateState {
                        copy(isLoading = false, players = result.data, error = null)
                    }
                    is Result.Error -> {
                        val message = result.message
                            ?: result.exception.message
                            ?: UNKNOWN_ERROR
                        updateState { copy(isLoading = false, error = message) }
                        sendEffect(FeedEffect.ShowError(message))
                    }
                    Result.Loading -> updateState { copy(isLoading = true) }
                }
            }
        }
    }
}
