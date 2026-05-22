package com.myapp.feature.feed.impl.presentation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.myapp.core.common.result.Result
import com.myapp.feature.feed.api.model.Player
import com.myapp.feature.feed.impl.domain.GetPlayersUseCase
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getPlayersUseCase: GetPlayersUseCase
    private lateinit var viewModel: FeedViewModel

    private val samplePlayers = listOf(
        Player(id = "1", name = "Erling Haaland", nationality = "Norwegian",
            teamName = "Manchester City", photoUrl = "", position = "Striker"),
        Player(id = "2", name = "Kylian Mbappé", nationality = "French",
            teamName = "Real Madrid", photoUrl = "", position = "Forward"),
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getPlayersUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()): FeedViewModel =
        FeedViewModel(savedStateHandle, getPlayersUseCase)

    @Test
    fun `when first subscriber attaches, players are loaded successfully`() = runTest {
        every { getPlayersUseCase() } returns flowOf(Result.Success(samplePlayers))
        viewModel = createViewModel()

        viewModel.state.test {
            // Initial state before onStart fires
            val initial = awaitItem()
            assertFalse(initial.isLoading)
            assertTrue(initial.players.isEmpty())

            // Advance so loadPlayers() coroutine (triggered by onStart) runs
            testDispatcher.scheduler.advanceUntilIdle()

            val loading = awaitItem()
            assertTrue(loading.isLoading)

            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(samplePlayers, loaded.players)
            assertNull(loaded.error)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when load fails, error state is set`() = runTest {
        val errorMsg = "Network error"
        every { getPlayersUseCase() } returns flowOf(Result.Error(Exception(errorMsg), errorMsg))
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem() // initial

            testDispatcher.scheduler.advanceUntilIdle()

            val loading = awaitItem()
            assertTrue(loading.isLoading)

            val errorState = awaitItem()
            assertFalse(errorState.isLoading)
            assertEquals(errorMsg, errorState.error)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when player clicked, NavigateToPlayerDetails effect is emitted`() = runTest {
        every { getPlayersUseCase() } returns flowOf(Result.Success(samplePlayers))
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem()
            testDispatcher.scheduler.advanceUntilIdle()
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.effects.test {
            viewModel.sendIntent(FeedIntent.OnPlayerClick(samplePlayers[0]))
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is FeedEffect.NavigateToPlayerDetails)
            assertEquals(samplePlayers[0].id, (effect as FeedEffect.NavigateToPlayerDetails).playerId)
        }
    }

    @Test
    fun `RetryLoad intent triggers a new load cycle`() = runTest {
        every { getPlayersUseCase() } returns flowOf(Result.Success(samplePlayers))
        viewModel = createViewModel()

        viewModel.state.test {
            awaitItem() // initial
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() // loading
            awaitItem() // loaded

            viewModel.sendIntent(FeedIntent.RetryLoad)
            testDispatcher.scheduler.advanceUntilIdle()

            val retryLoading = awaitItem()
            assertTrue(retryLoading.isLoading)

            val retryLoaded = awaitItem()
            assertFalse(retryLoaded.isLoading)
            assertEquals(samplePlayers, retryLoaded.players)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
