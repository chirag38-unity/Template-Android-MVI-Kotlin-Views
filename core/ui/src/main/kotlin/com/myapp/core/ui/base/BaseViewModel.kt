package com.myapp.core.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel for the MVI (Model–View–Intent) pattern.
 *
 * Provides:
 * - A [StateFlow] of [State] for the UI to observe.
 * - A [Channel]-backed `effects` flow for one-shot side effects (navigation, toasts, etc.).
 * - An intent queue backed by a [MutableSharedFlow] that serialises all [Intent] processing.
 *
 * ## Subclassing
 *
 * Override [handleIntent] to react to user actions and call [updateState] / [sendEffect]
 * to produce outputs. Optionally override [state] and [updateState] if you need
 * [androidx.lifecycle.SavedStateHandle]-backed persistence for process-death recovery.
 *
 * ## Example
 *
 * ```kotlin
 * @HiltViewModel
 * class FeedViewModel @Inject constructor(
 *     private val savedStateHandle: SavedStateHandle,
 *     private val useCase: GetPlayersUseCase,
 * ) : BaseViewModel<FeedState, FeedIntent, FeedEffect>(FeedState()) {
 *
 *     // Override to persist state via SavedStateHandle
 *     private val internalState = savedStateHandle.getStateFlow(KEY, FeedState())
 *
 *     override val state: StateFlow<FeedState> = internalState
 *         .onStart { load() }
 *         .stateIn(viewModelScope, WhileSubscribed(5_000), FeedState())
 *
 *     override fun updateState(reducer: FeedState.() -> FeedState) {
 *         savedStateHandle[KEY] = internalState.value.reducer()
 *     }
 *
 *     override fun handleIntent(intent: FeedIntent) {
 *         when (intent) {
 *             FeedIntent.Retry        -> load()
 *             is FeedIntent.ItemClick -> sendEffect(FeedEffect.Navigate(intent.id))
 *         }
 *     }
 * }
 * ```
 *
 * @param State  The immutable UI state type. Must be a data class and, if it needs to
 *   survive process death, must also implement [android.os.Parcelable].
 * @param Intent The sealed class of user-originated actions (button clicks, swipe events).
 * @param Effect The sealed class of one-shot side effects (navigation, toasts).
 * @param initialState The default state emitted before any updates are applied.
 */
abstract class BaseViewModel<State, Intent, Effect>(initialState: State) : ViewModel() {

    protected val _stateFlow = MutableStateFlow(initialState)

    /**
     * Exposes the current UI state. Subclasses may override this to provide a custom
     * [StateFlow] implementation (e.g. backed by [SavedStateHandle][androidx.lifecycle.SavedStateHandle]
     * with [stateIn][kotlinx.coroutines.flow.stateIn] for process-death recovery).
     */
    open val state: StateFlow<State> = _stateFlow.asStateFlow()

    private val _effects = Channel<Effect>(Channel.BUFFERED)

    /**
     * One-shot side effects emitted by the ViewModel.
     *
     * Collected in the Fragment via `collectWithLifecycle(viewModel.effects) { handleEffect(it) }`.
     * Effects are buffered so they are not lost if the Fragment is momentarily inactive.
     */
    val effects = _effects.receiveAsFlow()

    private val intentFlow = MutableSharedFlow<Intent>()

    init {
        viewModelScope.launch {
            intentFlow.collect { intent ->
                handleIntent(intent)
            }
        }
    }

    /**
     * Enqueues [intent] for processing by [handleIntent].
     *
     * Safe to call from any thread; the intent is emitted on a shared flow and processed
     * serially on [viewModelScope].
     */
    fun sendIntent(intent: Intent) {
        viewModelScope.launch {
            intentFlow.emit(intent)
        }
    }

    /**
     * Reacts to a user [intent] by calling [updateState] or [sendEffect] as appropriate.
     *
     * Invoked serially for each intent; do **not** launch long-running work directly here —
     * use [com.myapp.core.common.concurrency.safeLaunch] inside the handler instead.
     */
    protected abstract fun handleIntent(intent: Intent)

    /**
     * Applies [reducer] to the current state to produce a new state.
     *
     * Subclasses may override to persist state via [SavedStateHandle][androidx.lifecycle.SavedStateHandle].
     *
     * ## Example
     *
     * ```kotlin
     * updateState { copy(isLoading = true, error = null) }
     * ```
     */
    protected open fun updateState(reducer: State.() -> State) {
        _stateFlow.value = _stateFlow.value.reducer()
    }

    /**
     * Emits [effect] as a one-shot side effect.
     *
     * The effect is buffered and delivered to the next active collector.
     */
    protected fun sendEffect(effect: Effect) {
        viewModelScope.launch {
            _effects.send(effect)
        }
    }
}
