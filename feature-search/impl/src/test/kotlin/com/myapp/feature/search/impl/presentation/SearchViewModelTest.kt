package com.myapp.feature.search.impl.presentation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.myapp.core.common.result.Result
import com.myapp.feature.feed.api.PlayerRepository
import com.myapp.feature.feed.api.model.Player
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var playerRepository: PlayerRepository
    private lateinit var viewModel: SearchViewModel

    private val allPlayers = listOf(
        Player(id = "1", name = "Erling Haaland", nationality = "Norwegian",
            teamName = "Manchester City", photoUrl = "", position = "Striker"),
        Player(id = "2", name = "Kylian Mbappé", nationality = "French",
            teamName = "Real Madrid", photoUrl = "", position = "Forward"),
        Player(id = "3", name = "Bukayo Saka", nationality = "English",
            teamName = "Arsenal", photoUrl = "", position = "Right Winger"),
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        playerRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()): SearchViewModel =
        SearchViewModel(savedStateHandle, playerRepository)

    @Test
    fun `on first subscription, all players are loaded`() = runTest {
        every { playerRepository.getPlayers() } returns flowOf(Result.Success(allPlayers))
        viewModel = createViewModel()

        viewModel.state.test {
            val initial = awaitItem()
            assertFalse(initial.isLoading)
            assertTrue(initial.allPlayers.isEmpty())

            testDispatcher.scheduler.advanceUntilIdle()

            val loading = awaitItem()
            assertTrue(loading.isLoading)

            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(allPlayers, loaded.allPlayers)
            assertEquals(allPlayers, loaded.filteredPlayers)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `query filters by player name`() = runTest {
        every { playerRepository.getPlayers() } returns flowOf(Result.Success(allPlayers))
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem() // initial
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() // loading
            awaitItem() // loaded (all players)

            viewModel.sendIntent(SearchIntent.OnQueryChanged("haaland"))
            testDispatcher.scheduler.advanceUntilIdle()

            val filtered = awaitItem()
            assertEquals(1, filtered.filteredPlayers.size)
            assertEquals("Erling Haaland", filtered.filteredPlayers[0].name)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `query filters by nationality`() = runTest {
        every { playerRepository.getPlayers() } returns flowOf(Result.Success(allPlayers))
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem()
            awaitItem()

            viewModel.sendIntent(SearchIntent.OnQueryChanged("english"))
            testDispatcher.scheduler.advanceUntilIdle()

            val filtered = awaitItem()
            assertEquals(1, filtered.filteredPlayers.size)
            assertEquals("Bukayo Saka", filtered.filteredPlayers[0].name)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `empty query returns all players`() = runTest {
        every { playerRepository.getPlayers() } returns flowOf(Result.Success(allPlayers))
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem()
            awaitItem()

            // Apply a filter, then clear it
            viewModel.sendIntent(SearchIntent.OnQueryChanged("Arsenal"))
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() // filtered state

            viewModel.sendIntent(SearchIntent.OnQueryChanged(""))
            testDispatcher.scheduler.advanceUntilIdle()

            val cleared = awaitItem()
            assertEquals(allPlayers.size, cleared.filteredPlayers.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnPlayerClick sends NavigateToPlayerDetails effect`() = runTest {
        every { playerRepository.getPlayers() } returns flowOf(Result.Success(allPlayers))
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()
            testDispatcher.scheduler.advanceUntilIdle()
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.effects.test {
            viewModel.sendIntent(SearchIntent.OnPlayerClick(allPlayers[0]))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is SearchEffect.NavigateToPlayerDetails)
            assertEquals(allPlayers[0].id, (effect as SearchEffect.NavigateToPlayerDetails).playerId)
        }
    }
}
