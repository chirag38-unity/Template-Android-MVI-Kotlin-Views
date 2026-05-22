package com.myapp.feature.search.impl.presentation

import android.os.Parcelable
import com.myapp.feature.feed.api.model.Player
import kotlinx.parcelize.Parcelize

/**
 * Immutable UI state for the Search screen.
 *
 * Persisted in [androidx.lifecycle.SavedStateHandle] via [SearchViewModel] so it survives
 * both configuration changes and process death. Must remain [Parcelable] for this reason.
 *
 * @property query           The current search query string.
 * @property allPlayers      The full dataset loaded from the repository (loaded once).
 * @property filteredPlayers Subset of [allPlayers] that match [query].
 * @property isLoading       `true` while the initial data load is in progress.
 * @property error           Non-null human-readable error message when the last load failed.
 */
@Parcelize
data class SearchState(
    val query: String = "",
    val allPlayers: List<Player> = emptyList(),
    val filteredPlayers: List<Player> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
) : Parcelable

/** User-originated actions for the Search screen. Processed by [SearchViewModel.handleIntent]. */
sealed class SearchIntent {
    /** The search query text changed. */
    data class OnQueryChanged(val query: String) : SearchIntent()

    /** The user tapped a player row. */
    data class OnPlayerClick(val player: Player) : SearchIntent()
}

/** One-shot side effects emitted by [SearchViewModel]. */
sealed class SearchEffect {
    /** Navigate to the player-details screen using the player's stable [playerId]. */
    data class NavigateToPlayerDetails(val playerId: String) : SearchEffect()

    /** Show a brief error message (e.g. a Toast). */
    data class ShowError(val message: String) : SearchEffect()
}
