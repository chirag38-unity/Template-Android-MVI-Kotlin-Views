package com.myapp.feature.player.details.impl.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.myapp.core.navigation.ui.NavigationContainerFragment
import com.myapp.feature.player.details.impl.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * Full-screen Activity that hosts [PlayerDetailsFragment].
 *
 * Launched by features that want to open player details **outside** the tab navigation
 * hierarchy (e.g. [com.myapp.feature.feed.impl.presentation.FeedFragment]).
 *
 * The Activity reads [EXTRA_PLAYER_ID] from its launch [Intent] and delegates all UI
 * rendering to [PlayerDetailsFragment], which loads player data from the repository via
 * [PlayerDetailsViewModel]. The Activity itself remains a thin host with no business logic.
 *
 * ## Fragment lifecycle
 * The fragment is created only once in [onCreate] (when [savedInstanceState] is `null`).
 * On configuration changes the fragment manager re-attaches the existing fragment automatically.
 *
 * ## Back navigation
 * [PlayerDetailsFragment]'s toolbar up-button calls
 * `requireActivity().onBackPressedDispatcher.onBackPressed()`, which finishes this Activity.
 * The system Back gesture also finishes the Activity normally.
 *
 * @see PlayerDetailsFeatureEntry.createActivityIntent
 */
@AndroidEntryPoint
class PlayerDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player_details)

        if (savedInstanceState == null) {

            val playerId = intent.getStringExtra(EXTRA_PLAYER_ID)
                ?: error("Missing player id")

            val container = NavigationContainerFragment.newInstance()

            supportFragmentManager.beginTransaction()
                .replace(R.id.container, container)
                .commitNow()

            container.setRootFragment(
                PlayerDetailsFragment.newInstance(playerId)
            )
        }
    }

    companion object {
        private const val EXTRA_PLAYER_ID = "extra_player_id"

        /**
         * Build a launch [Intent] for [PlayerDetailsActivity].
         *
         * Only [PlayerDetailsEntryImpl] should call this method directly — all callers in
         * feature modules should go through [PlayerDetailsFeatureEntry.createActivityIntent].
         *
         * @param context Android [Context] used to construct the [Intent].
         * @param playerId Stable identifier of the player to display.
         */
        fun intent(context: Context, playerId: String): Intent =
            Intent(context, PlayerDetailsActivity::class.java).apply {
                putExtra(EXTRA_PLAYER_ID, playerId)
            }
    }
}
