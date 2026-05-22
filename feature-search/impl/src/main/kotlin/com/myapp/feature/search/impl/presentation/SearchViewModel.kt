package com.myapp.feature.search.impl.presentation

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
 * ViewModel for the Search screen.
 *
 * Loads all players from [PlayerRepository] on first subscription, then filters them
 * in-memory based on the user's query (by name, nationality, or club name).
 *
 * ## Design decisions
 * - **In-memory filtering**: All players are loaded once and filtered client-side for
 *   instant, latency-free search. Suitable for the current data set size.
 * - **SavedStateHandle persistence**: Both the full player list and the active query are
 *   persisted so that process death or configuration changes restore the exact search state.
 * - **Lazy loading**: Data is fetched the first time a subscriber attaches to [state];
 *   if players are already present in [SavedStateHandle], the load is skipped.
 *
 * @param savedStateHandle Injected by Hilt; persists [SearchState] across process death.
 * @param playerRepository Repository that provides the full list of players.
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val playerRepository: PlayerRepository,
) : BaseViewModel<SearchState, SearchIntent, SearchEffect>(SearchState()) {

    companion object {
        private const val KEY_SEARCH_STATE = "KEY_SEARCH_STATE"
        private const val UNKNOWN_ERROR = "An unexpected error occurred"
    }

    private val internalState = savedStateHandle.getStateFlow(KEY_SEARCH_STATE, SearchState())

    override val state: StateFlow<SearchState> = internalState
        .onStart { loadAllPlayers() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = SearchState(),
        )

    override fun updateState(reducer: SearchState.() -> SearchState) {
        savedStateHandle[KEY_SEARCH_STATE] = internalState.value.reducer()
    }

    override fun handleIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.OnQueryChanged -> applyFilter(intent.query)
            is SearchIntent.OnPlayerClick -> sendEffect(SearchEffect.NavigateToPlayerDetails(intent.player.id))
        }
    }

    /**
     * Loads all players unless they are already cached in state or a load is in progress.
     *
     * Uses [safeLaunch] to catch unexpected exceptions outside of the normal [Result]
     * error handling path.
     */
    private fun loadAllPlayers() {
        if (internalState.value.isLoading || internalState.value.allPlayers.isNotEmpty()) return
        safeLaunch(
            onError = { e ->
                val message = e.message ?: UNKNOWN_ERROR
                updateState { copy(isLoading = false, error = message) }
                sendEffect(SearchEffect.ShowError(message))
            },
        ) {
            updateState { copy(isLoading = true, error = null) }
            playerRepository.getPlayers().collect { result ->
                when (result) {
                    is Result.Success -> {
                        val players = result.data
                        val query = internalState.value.query
                        updateState {
                            copy(
                                isLoading = false,
                                allPlayers = players,
                                filteredPlayers = players.filter(query),
                                error = null,
                            )
                        }
                    }
                    is Result.Error -> {
                        val message = result.message ?: result.exception.message ?: UNKNOWN_ERROR
                        updateState { copy(isLoading = false, error = message) }
                        sendEffect(SearchEffect.ShowError(message))
                    }
                    Result.Loading -> updateState { copy(isLoading = true) }
                }
            }
        }
    }

    private fun applyFilter(query: String) {
        updateState {
            copy(
                query = query,
                filteredPlayers = allPlayers.filter(query),
            )
        }
    }
}

/** Filters players whose name, nationality, or team name contains [query] (case-insensitive). */
private fun List<com.myapp.feature.feed.api.model.Player>.filter(query: String) =
    if (query.isBlank()) this
    else filter { player ->
        player.name.contains(query, ignoreCase = true) ||
            player.nationality.contains(query, ignoreCase = true) ||
            player.teamName.contains(query, ignoreCase = true)
    }
