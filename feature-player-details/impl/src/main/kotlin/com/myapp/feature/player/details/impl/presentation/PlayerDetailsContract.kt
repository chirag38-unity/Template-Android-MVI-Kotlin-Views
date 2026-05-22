package com.myapp.feature.player.details.impl.presentation

import android.os.Parcelable
import com.myapp.feature.feed.api.model.Player
import kotlinx.parcelize.Parcelize

/**
 * UI state for the player-details screen.
 *
 * Stored in [androidx.lifecycle.SavedStateHandle] via [PlayerDetailsViewModel] so that
 * the loaded [player] survives process death and configuration changes without needing
 * to reload from the repository.
 */
@Parcelize
data class PlayerDetailsState(
    val player: Player? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
) : Parcelable

/** User-initiated intents for the player-details screen. */
sealed class PlayerDetailsIntent {
    /** Retry a failed player load. */
    data object Retry : PlayerDetailsIntent()
}

/** One-shot side effects emitted by [PlayerDetailsViewModel]. */
sealed class PlayerDetailsEffect
