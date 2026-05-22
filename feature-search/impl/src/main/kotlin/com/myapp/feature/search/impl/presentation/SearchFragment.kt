package com.myapp.feature.search.impl.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.myapp.core.common.lifecycle.collectWithLifecycle
import com.myapp.core.navigation.FragmentNavigator
import com.myapp.core.ui.base.BaseFragment
import com.myapp.core.ui.extensions.hide
import com.myapp.core.ui.extensions.show
import com.myapp.core.ui.insets.applySystemWindowInsetsPadding
import com.myapp.feature.player.details.api.PlayerDetailsFeatureEntry
import com.myapp.feature.search.impl.databinding.FragmentSearchBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : BaseFragment<FragmentSearchBinding>() {

    @Inject lateinit var playerDetailsEntry: PlayerDetailsFeatureEntry

    private val viewModel: SearchViewModel by viewModels()
    private val adapter: SearchPlayerAdapter by lazy {
        SearchPlayerAdapter { player ->
            viewModel.sendIntent(SearchIntent.OnPlayerClick(player))
        }
    }

    private var previousState: SearchState? = null

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentSearchBinding =
        FragmentSearchBinding.inflate(inflater, container, false)

    override fun setupUI() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SearchFragment.adapter
        }

        binding.editSearch.apply {
            // Apply top padding equal to the status-bar height using the core
            // WindowInsetsHelper utility.
            applySystemWindowInsetsPadding(applyTop = true)

            doOnTextChanged { text, _, _, _ ->
                viewModel.sendIntent(
                    SearchIntent.OnQueryChanged(text?.toString().orEmpty())
                )
            }
        }
    }

    override fun observeState() {
        // Use the core collectWithLifecycle utility to safely collect flows
        // within the fragment's view lifecycle (STARTED state by default).
        collectWithLifecycle(viewModel.state) { state -> render(state) }
        collectWithLifecycle(viewModel.effects) { effect -> handleEffect(effect) }
    }

    private fun render(state: SearchState) {
        val old = previousState
        if (old?.isLoading != state.isLoading) {
            if (state.isLoading) binding.progressBar.show() else binding.progressBar.hide()
        }
        if (old?.filteredPlayers != state.filteredPlayers) {
            adapter.submitList(state.filteredPlayers)
            if (!state.isLoading && state.filteredPlayers.isEmpty() && state.query.isNotBlank()) {
                binding.textEmpty.show()
            } else {
                binding.textEmpty.hide()
            }
        }
        if (old?.error != state.error && state.error != null) {
            binding.textEmpty.text = state.error
            binding.textEmpty.show()
        }
        previousState = state
    }

    private fun handleEffect(effect: SearchEffect) {
        when (effect) {
            is SearchEffect.NavigateToPlayerDetails -> {
                (parentFragment as? FragmentNavigator)
                    ?.push(playerDetailsEntry.createFragment(effect.playerId))
            }
            is SearchEffect.ShowError -> {
                Toast.makeText(requireContext(), effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
