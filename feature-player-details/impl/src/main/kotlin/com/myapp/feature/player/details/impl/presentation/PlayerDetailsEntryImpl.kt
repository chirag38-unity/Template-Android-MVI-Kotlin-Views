package com.myapp.feature.player.details.impl.presentation

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.myapp.feature.player.details.api.PlayerDetailsFeatureEntry
import javax.inject.Inject

/**
 * Concrete implementation of [PlayerDetailsFeatureEntry].
 *
 * This class is the **sole owner** of:
 * - Fragment creation ([PlayerDetailsFragment.newInstance])
 * - Activity intent construction ([PlayerDetailsActivity.intent])
 * - Argument key definitions ([PlayerDetailsFragment.ARG_PLAYER_ID])
 *
 * All feature modules that navigate to player details inject [PlayerDetailsFeatureEntry]
 * and call these factory methods — they never reference [PlayerDetailsFragment] or
 * [PlayerDetailsActivity] directly.
 */
class PlayerDetailsEntryImpl @Inject constructor() : PlayerDetailsFeatureEntry {

    override fun createFragment(playerId: String): Fragment =
        PlayerDetailsFragment.newInstance(playerId)

    override fun createActivityIntent(context: Context, playerId: String): Intent =
        PlayerDetailsActivity.intent(context, playerId)
}

