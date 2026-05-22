package com.myapp.feature.player.details.impl.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.myapp.core.common.concurrency.safeLaunch
import com.myapp.core.common.result.Result
import com.myapp.core.ui.base.BaseViewModel
import com.myapp.feature.feed.api.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel for [PlayerDetailsFragment].
 *
 * Reads [playerId] from [SavedStateHandle] (populated automatically from the fragment's
 * argument bundle), then loads the player from [PlayerRepository] lazily on first
 * subscription via [onStart].
 *
 * State is backed by [SavedStateHandle] so both configuration changes and process death
 * are handled without a redundant network/DB call after restoration.
 */
@HiltViewModel
class PlayerDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val playerRepository: PlayerRepository,
) : BaseViewModel<PlayerDetailsState, PlayerDetailsIntent, PlayerDetailsEffect>(PlayerDetailsState()) {

    companion object {
        private const val KEY_STATE = "KEY_PLAYER_DETAILS_STATE"
        private const val UNKNOWN_ERROR = "An unexpected error occurred"
        // Matches PlayerDetailsFragment.ARG_PLAYER_ID — the SavedStateHandle is seeded with
        // the Fragment's argument bundle, so this key must align with the Fragment's constant.
        internal const val ARG_PLAYER_ID = "arg_player_id"
    }

    /**
     * The player ID passed as a fragment argument.
     * Populated from [SavedStateHandle] which is seeded with the fragment's arguments.
     */
    private val playerId: String =
        checkNotNull(savedStateHandle[ARG_PLAYER_ID]) {
            "PlayerDetailsViewModel requires $ARG_PLAYER_ID in SavedStateHandle"
        }

    private val internalState = savedStateHandle.getStateFlow(KEY_STATE, PlayerDetailsState())

    override val state: StateFlow<PlayerDetailsState> = internalState
        .onStart { loadPlayer() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = PlayerDetailsState(),
        )

    override fun updateState(reducer: PlayerDetailsState.() -> PlayerDetailsState) {
        savedStateHandle[KEY_STATE] = internalState.value.reducer()
    }

    override fun handleIntent(intent: PlayerDetailsIntent) {
        when (intent) {
            PlayerDetailsIntent.Retry -> loadPlayer()
        }
    }

    /**
     * Loads the player from the repository unless a load is already in progress.
     *
     * Uses [safeLaunch] to guard against unexpected coroutine exceptions. Repository-level
     * errors are surfaced via [Result.Error] in the normal flow.
     */
    private fun loadPlayer() {
        if (internalState.value.isLoading) return
        safeLaunch(
            onError = { e ->
                val message = e.message ?: UNKNOWN_ERROR
                updateState { copy(isLoading = false, error = message) }
            },
        ) {
            updateState { copy(isLoading = true, error = null) }
            when (val result = playerRepository.getPlayerById(playerId)) {
                is Result.Success -> updateState {
                    copy(isLoading = false, player = result.data, error = null)
                }
                is Result.Error -> {
                    val message = result.message ?: result.exception.message ?: UNKNOWN_ERROR
                    updateState { copy(isLoading = false, error = message) }
                }
                Result.Loading -> updateState { copy(isLoading = true) }
            }
        }
    }
}
