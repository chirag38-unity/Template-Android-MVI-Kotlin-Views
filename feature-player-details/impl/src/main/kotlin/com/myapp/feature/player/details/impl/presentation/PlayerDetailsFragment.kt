package com.myapp.feature.player.details.impl.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.myapp.core.common.lifecycle.collectWithLifecycle
import com.myapp.core.navigation.FragmentNavigator
import com.myapp.core.ui.base.BaseFragment
import com.myapp.core.ui.insets.applySystemWindowInsetsPadding
import com.myapp.feature.feed.api.model.Player
import com.myapp.feature.player.details.impl.databinding.FragmentPlayerDetailsBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Reusable destination that displays full details for a player.
 *
 * Can be pushed from any feature tab's back-stack **or** hosted inside
 * [PlayerDetailsActivity] as a full-screen destination.
 *
 * Receives only a lightweight [ARG_PLAYER_ID]; actual player data is loaded by
 * [PlayerDetailsViewModel] via the repository, which cleanly supports process death
 * recovery without stale Parcelable state.
 *
 * ## Back navigation
 * The toolbar's up-button delegates to the Activity's [OnBackPressedDispatcher]:
 * - When hosted in a tab stack → [com.myapp.root.RootFragment]'s callback pops the stack.
 * - When hosted in [PlayerDetailsActivity] → the Activity finishes.
 */
@AndroidEntryPoint
class PlayerDetailsFragment : BaseFragment<FragmentPlayerDetailsBinding>() {

    private val viewModel: PlayerDetailsViewModel by viewModels()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentPlayerDetailsBinding =
        FragmentPlayerDetailsBinding.inflate(inflater, container, false)

    override fun setupUI() {
        // Apply top padding equal to the status-bar height using the core
        // WindowInsetsHelper utility, so the toolbar is not hidden behind the system bars.
        binding.toolbar.applySystemWindowInsetsPadding(applyTop = true)

        binding.toolbar.setNavigationOnClickListener {
            (parentFragment as? FragmentNavigator)
                ?.takeIf { it.pop() }
                ?: requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.textPlayerName.setOnClickListener {
            (parentFragment as? FragmentNavigator)
                ?.push(
                    PlayerDetailsFragment.newInstance(
                        arguments?.getString(ARG_PLAYER_ID) ?: error("Missing player id")
                    )
                )
        }
    }

    override fun observeState() {
        // Use the core collectWithLifecycle utility to safely collect the state flow
        // within the fragment's view lifecycle (STARTED state by default).
        collectWithLifecycle(viewModel.state) { state -> render(state) }
    }

    private fun render(state: PlayerDetailsState) {
        val player = state.player ?: return
        bindPlayer(player)
    }

    private fun bindPlayer(player: Player) {
        with(binding) {
            toolbar.title = player.name
            textPlayerName.text = player.name
            textPosition.text = player.position
            textTeamValue.text = player.teamName
            textNationalityValue.text = player.nationality
            textGoalsValue.text = player.goals.toString()
            textAssistsValue.text = player.assists.toString()
            textRatingValue.text = String.format("%.1f", player.rating)
            textDescription.text = player.description
            textAvatarInitial.text = player.name.first().uppercase()
        }
    }

    companion object {
        /** Fragment argument key for the player's stable identifier. */
        const val ARG_PLAYER_ID = "arg_player_id"

        /**
         * Create a new [PlayerDetailsFragment] pre-configured with [playerId].
         *
         * Only [PlayerDetailsEntryImpl] and [PlayerDetailsActivity] should call this method
         * directly — all other callers should go through [PlayerDetailsFeatureEntry].
         */
        fun newInstance(playerId: String): PlayerDetailsFragment =
            PlayerDetailsFragment().apply {
                arguments = bundleOf(ARG_PLAYER_ID to playerId)
            }
    }
}

