package com.myapp.core.navigation.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.MenuRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.myapp.core.navigation.ActivityNavigator
import com.myapp.core.navigation.FragmentNavigator
import com.myapp.core.navigation.R
import com.myapp.core.navigation.contracts.NavigationTab
import com.myapp.core.navigation.contracts.NavigationTabs
import com.myapp.core.navigation.contracts.RootNavigator
import com.myapp.core.navigation.controller.ShowHideTabSwitcher
import com.myapp.core.navigation.databinding.FragmentNavigationHostBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Reusable navigation host fragment that wires together bottom navigation and per-tab
 * back-stacks without any knowledge of concrete tabs or feature fragments.
 *
 * ## Setup
 * 1. Provide tabs via Hilt (annotated with [@NavigationTabs][NavigationTabs]):
 *    ```kotlin
 *    @Provides @NavigationTabs
 *    fun provideTabs(): List<NavigationTab> = listOf(FeedTab, SearchTab)
 *    ```
 * 2. Create the fragment with the bottom-nav menu resource:
 *    ```kotlin
 *    NavigationHostFragment.newInstance(R.menu.bottom_nav_menu)
 *    ```
 *
 * ## Tab management
 * Uses a show/hide strategy (via [ShowHideTabSwitcher]) so each tab's ViewModel
 * instances, RecyclerView scroll positions, and child back-stacks are preserved
 * across switches.
 *
 * ## Navigation contracts
 * - [FragmentNavigator] — push/pop within the active tab's stack
 * - [ActivityNavigator] — launch a new Activity
 * - [RootNavigator] — switch between tabs
 *
 * ## Back-press behaviour
 * 1. Pop the current tab's stack.
 * 2. If empty and not on the default tab → navigate to the default tab.
 * 3. If on the default tab with an empty stack → exit the app.
 */
@AndroidEntryPoint
class NavigationHostFragment :
    Fragment(),
    RootNavigator,
    FragmentNavigator,
    ActivityNavigator {

    @Inject
    @NavigationTabs
    lateinit var tabs: List<@JvmSuppressWildcards NavigationTab>

    private var _binding: FragmentNavigationHostBinding? = null
    private val binding get() = _binding!!

    private val tabSwitcher: ShowHideTabSwitcher = ShowHideTabSwitcher()

    override var currentTabId: String? = null
        private set

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentNavigationHostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val defaultTabId = tabs.first().id

        currentTabId = savedInstanceState?.getString(KEY_CURRENT_TAB) ?: defaultTabId

        val menuResId = requireArguments().getInt(ARG_MENU_RES_ID)
        binding.bottomNavigation.inflateMenu(menuResId)

        setupInitialTab()
        setupBottomNavigation()
        setupBackPressHandler(defaultTabId)
    }

    private fun setupInitialTab() {
        val tabId = currentTabId ?: return
        val tab = tabs.firstOrNull { it.id == tabId } ?: return

        if (childFragmentManager.findFragmentByTag(tabId) == null) {
            val container = NavigationContainerFragment.newInstance()
            childFragmentManager.beginTransaction()
                .add(R.id.navigation_host_container, container, tabId)
                .commitNow()
            container.setRootFragment(tab.startDestinationFactory())
        }

        binding.bottomNavigation.selectedItemId = tab.menuItemId
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val tab = tabs.firstOrNull { it.menuItemId == item.itemId }
                ?: return@setOnItemSelectedListener false
            navigateTo(tab.id)
            true
        }
    }

    private fun setupBackPressHandler(defaultTabId: String) {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (pop()) return
                    if (currentTabId != defaultTabId) {
                        val defaultTab = tabs.firstOrNull { it.id == defaultTabId } ?: return
                        binding.bottomNavigation.selectedItemId = defaultTab.menuItemId
                        navigateTo(defaultTabId)
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            },
        )
    }

    // ── RootNavigator ──────────────────────────────────────────────────────────

    override fun navigateTo(tabId: String) {
        if (tabId == currentTabId) return

        val tab = tabs.firstOrNull { it.id == tabId } ?: return
        val isNew = childFragmentManager.findFragmentByTag(tabId) == null

        tabSwitcher.switch(
            fragmentManager = childFragmentManager,
            containerId = R.id.navigation_host_container,
            currentTag = currentTabId,
            targetTag = tabId,
            factory = { NavigationContainerFragment.newInstance() },
        )

        if (isNew) {
            (childFragmentManager.findFragmentByTag(tabId) as? NavigationContainerFragment)
                ?.setRootFragment(tab.startDestinationFactory())
        }

        currentTabId = tabId
    }

    // ── FragmentNavigator ──────────────────────────────────────────────────────

    override fun push(fragment: Fragment) {
        currentContainer()?.push(fragment)
    }

    override fun pop(): Boolean = currentContainer()?.pop() ?: false

    // ── ActivityNavigator ──────────────────────────────────────────────────────

    override fun launch(intent: Intent) {
        startActivity(intent)
    }

    // ── Internal helpers ───────────────────────────────────────────────────────

    private fun currentContainer(): NavigationContainerFragment? =
        childFragmentManager.findFragmentByTag(currentTabId) as? NavigationContainerFragment

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_CURRENT_TAB, currentTabId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val KEY_CURRENT_TAB = "current_tab"
        private const val ARG_MENU_RES_ID = "arg_menu_res_id"

        /**
         * Create a new [NavigationHostFragment] configured with the given bottom-nav menu.
         *
         * @param menuResId The menu resource ID (e.g. `R.menu.bottom_nav_menu`) whose items
         *   correspond to [NavigationTab.menuItemId] values in the injected tab list.
         */
        fun newInstance(@MenuRes menuResId: Int): NavigationHostFragment =
            NavigationHostFragment().apply {
                arguments = bundleOf(ARG_MENU_RES_ID to menuResId)
            }
    }
}
