package com.myapp.core.navigation.contracts

import androidx.fragment.app.Fragment

/**
 * Contract for a single bottom-navigation tab in the host navigation graph.
 *
 * App layer implements this for every tab it registers:
 * ```kotlin
 * object FeedTab : NavigationTab {
 *     override val id = "feed"
 *     override val menuItemId = R.id.nav_feed
 *     override val startDestinationFactory = { FeedFragment() }
 * }
 * ```
 *
 * Tabs are contributed to the navigation host via Hilt injection — see
 * [com.myapp.core.navigation.contracts.NavigationTabs].
 */
interface NavigationTab {
    /** Stable string identifier used for fragment tags and saved-state restoration. */
    val id: String

    /** ID of the corresponding [com.google.android.material.bottomnavigation.BottomNavigationView] menu item. */
    val menuItemId: Int

    /** Factory that produces the root (entry) [Fragment] for this tab. */
    val startDestinationFactory: () -> Fragment
}
