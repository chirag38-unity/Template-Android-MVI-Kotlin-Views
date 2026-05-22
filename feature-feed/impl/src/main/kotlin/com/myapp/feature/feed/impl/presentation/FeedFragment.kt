package com.myapp.feature.feed.impl.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.myapp.core.common.lifecycle.collectWithLifecycle
import com.myapp.core.navigation.ActivityNavigator
import com.myapp.core.ui.base.BaseFragment
import com.myapp.core.ui.extensions.hide
import com.myapp.core.ui.extensions.show
import com.myapp.core.ui.insets.applySystemWindowInsetsPadding
import com.myapp.feature.feed.impl.databinding.FragmentFeedBinding
import com.myapp.feature.player.details.api.PlayerDetailsFeatureEntry
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : BaseFragment<FragmentFeedBinding>() {

    @Inject lateinit var playerDetailsEntry: PlayerDetailsFeatureEntry

    private val viewModel: FeedViewModel by viewModels()
    private val playerAdapter: PlayerAdapter by lazy {
        PlayerAdapter { player ->
            viewModel.sendIntent(FeedIntent.OnPlayerClick(player))
        }
    }

    private var previousState: FeedState? = null

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentFeedBinding =
        FragmentFeedBinding.inflate(inflater, container, false)

    override fun setupUI() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@FeedFragment.playerAdapter
            // Apply top padding equal to the status-bar height so content is not
            // hidden behind the system bars. Uses the core WindowInsetsHelper utility.
            applySystemWindowInsetsPadding(applyTop = true)
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.sendIntent(FeedIntent.RetryLoad)
        }
        binding.buttonRetry.setOnClickListener {
            viewModel.sendIntent(FeedIntent.RetryLoad)
        }
    }

    override fun populateUI() {
        super.populateUI()
    }

    override fun observeState() {
        // Use the core collectWithLifecycle utility to safely collect flows
        // within the fragment's view lifecycle (STARTED state by default).
        collectWithLifecycle(viewModel.state) { state -> render(state) }
        collectWithLifecycle(viewModel.effects) { effect -> handleEffect(effect) }
    }

    // ── Selective rendering ────────────────────────────────────────────────────

    private fun render(state: FeedState) {
        val old = previousState
        if (old?.isLoading != state.isLoading || (old?.players?.isEmpty() != state.players.isEmpty())) {
            renderLoading(state)
        }
        if (old?.players != state.players) {
            playerAdapter.submitList(state.players)
        }
        if (old?.error != state.error) {
            renderError(state)
        }
        previousState = state
    }

    private fun renderLoading(state: FeedState) {
        binding.swipeRefreshLayout.isRefreshing = state.isLoading && state.players.isNotEmpty()
        if (state.isLoading && state.players.isEmpty()) {
            binding.progressBar.show()
        } else {
            binding.progressBar.hide()
        }
    }

    private fun renderError(state: FeedState) {
        if (state.error != null && state.players.isEmpty()) {
            binding.layoutError.show()
            binding.textError.text = state.error
        } else {
            binding.layoutError.hide()
        }
    }

    // ── Effect handling ────────────────────────────────────────────────────────

    private fun handleEffect(effect: FeedEffect) {
        when (effect) {
            is FeedEffect.NavigateToPlayerDetails -> {
                (activity as? ActivityNavigator)
                    ?.launch(
                        playerDetailsEntry.createActivityIntent(
                            requireContext(),
                            effect.playerId,
                        )
                    )
            }
            is FeedEffect.ShowError -> {
                Toast.makeText(requireContext(), effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
