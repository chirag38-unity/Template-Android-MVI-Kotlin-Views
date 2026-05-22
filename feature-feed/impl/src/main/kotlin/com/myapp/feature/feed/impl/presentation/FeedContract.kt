package com.myapp.feature.feed.impl.presentation

import android.os.Parcelable
import com.myapp.feature.feed.api.model.Player
import kotlinx.parcelize.Parcelize

/**
 * Immutable UI state for the Feed screen.
 *
 * Persisted in [androidx.lifecycle.SavedStateHandle] via [FeedViewModel] so it survives
 * both configuration changes and process death. Must remain [Parcelable] for this reason.
 *
 * @property isLoading `true` while a data load is in progress.
 * @property players   The current list of players to display.
 * @property error     Non-null human-readable error message when the last load failed.
 */
@Parcelize
data class FeedState(
    val isLoading: Boolean = false,
    val players: List<Player> = emptyList(),
    val error: String? = null,
) : Parcelable

/** User-originated actions for the Feed screen. Processed by [FeedViewModel.handleIntent]. */
sealed class FeedIntent {
    /** The user requested a data reload (pull-to-refresh or retry button). */
    data object RetryLoad : FeedIntent()

    /** The user tapped a player row. */
    data class OnPlayerClick(val player: Player) : FeedIntent()
}

/** One-shot side effects emitted by [FeedViewModel]. */
sealed class FeedEffect {
    /** Navigate to the player-details screen using the player's stable [playerId]. */
    data class NavigateToPlayerDetails(val playerId: String) : FeedEffect()

    /** Show a brief error message (e.g. a Toast). */
    data class ShowError(val message: String) : FeedEffect()
}
