package com.myapp.core.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.launch

/**
 * Base Fragment implementation that provides:
 *
 * - Automatic [ViewBinding] handling
 * - Safe binding lifecycle management
 * - Common initialization hooks
 * - Lifecycle-aware coroutine collection helpers
 *
 * ## Lifecycle Flow
 *
 * The internal lifecycle execution order is:
 *
 * 1. [onInit]
 * 2. [setupUI]
 * 3. [populateUI]
 * 4. [observeState]
 *
 * ## Binding Lifecycle
 *
 * The binding is:
 *
 * - Created in [onCreateView]
 * - Accessible only between [onCreateView] and [onDestroyView]
 * - Cleared automatically in [onDestroyView]
 *
 * Accessing [binding] outside the valid lifecycle scope will throw an exception.
 *
 * ## Usage Example
 *
 * ```kotlin
 * @AndroidEntryPoint
 * class HomeFragment : BaseFragment<FragmentHomeBinding>() {
 *
 *     override fun inflateBinding(
 *         inflater: LayoutInflater,
 *         container: ViewGroup?
 *     ): FragmentHomeBinding {
 *         return FragmentHomeBinding.inflate(inflater, container, false)
 *     }
 *
 *     override fun setupUI() {
 *         binding.recyclerView.adapter = adapter
 *     }
 *
 *     override fun observeState() {
 *         launchWhenStarted {
 *             viewModel.state.collect { state ->
 *                 render(state)
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @param VB Type of [ViewBinding] associated with this Fragment.
 */
abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    /**
     * Backing property for [binding].
     *
     * This is cleared in [onDestroyView] to avoid memory leaks.
     */
    private var _binding: VB? = null

    /**
     * Non-null access to the current [ViewBinding].
     *
     * Can only be accessed between [onCreateView] and [onDestroyView].
     *
     * @throws IllegalStateException if accessed outside the valid view lifecycle.
     */
    protected val binding: VB
        get() = _binding
            ?: error("Binding accessed outside of onCreateView/onDestroyView lifecycle.")

    /**
     * Inflate the Fragment's [ViewBinding].
     *
     * Called internally from [onCreateView].
     *
     * @param inflater LayoutInflater used to inflate the binding.
     * @param container Optional parent ViewGroup.
     *
     * @return Inflated [ViewBinding] instance.
     */
    abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    /**
     * Fragment initialization callback.
     *
     * Invoked during [onCreate].
     *
     * Recommended for:
     * - Reading arguments
     * - Initializing non-UI dependencies
     * - Creating adapters
     * - One-time setup logic
     *
     * Avoid accessing [binding] here since the view is not created yet.
     */
    protected open fun onInit() {}

    /**
     * UI setup callback.
     *
     * Invoked immediately after binding inflation inside [onCreateView].
     *
     * Recommended for:
     * - Setting adapters
     * - Configuring listeners
     * - Applying window insets
     * - Setting up RecyclerViews
     */
    protected open fun setupUI() {}

    /**
     * Initial UI population callback.
     *
     * Invoked from [onViewCreated].
     *
     * Recommended for:
     * - Rendering static data
     * - Restoring UI state
     * - Setting initial values
     */
    protected open fun populateUI() {}

    /**
     * State observation callback.
     *
     * Invoked from [onViewCreated].
     *
     * Recommended for:
     * - Collecting Flows
     * - Observing LiveData
     * - Handling UI effects/events
     */
    protected open fun observeState() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onInit()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = inflateBinding(inflater, container)
        setupUI()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateUI()
        observeState()
    }

    /**
     * Launch a lifecycle-aware coroutine scoped to the Fragment's view lifecycle.
     *
     * The provided [block] is automatically:
     *
     * - Started when the lifecycle reaches [Lifecycle.State.STARTED]
     * - Cancelled when the lifecycle falls below STARTED
     * - Restarted when the lifecycle returns to STARTED
     *
     * This is ideal for collecting Flows safely without leaking the Fragment view.
     *
     * ## Example
     *
     * ```kotlin
     * launchWhenStarted {
     *     viewModel.state.collect(::render)
     * }
     * ```
     *
     * @param block Suspend lambda executed within the STARTED lifecycle state.
     */
    protected fun launchWhenStarted(block: suspend () -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                block()
            }
        }
    }

    /**
     * Clears the current binding reference to avoid memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}