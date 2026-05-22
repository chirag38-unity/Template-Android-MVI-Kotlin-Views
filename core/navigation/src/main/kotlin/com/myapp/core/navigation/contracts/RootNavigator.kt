package com.myapp.core.navigation.contracts

/**
 * Contract for root-level tab navigation.
 *
 * Implemented by [com.myapp.core.navigation.ui.NavigationHostFragment].
 * Feature modules obtain this by casting their host Activity or Fragment to [RootNavigator].
 *
 * Tab identifiers are plain strings matching [NavigationTab.id], so this interface has no
 * compile-time dependency on any concrete tab enum or class.
 *
 * For within-tab fragment navigation see [com.myapp.core.navigation.FragmentNavigator].
 * For launching new Activities see [com.myapp.core.navigation.ActivityNavigator].
 */
interface RootNavigator {
    /** The [NavigationTab.id] of the currently visible tab, or `null` before first render. */
    val currentTabId: String?

    /** Switch to the tab identified by [tabId], creating its container if needed. */
    fun navigateTo(tabId: String)
}
