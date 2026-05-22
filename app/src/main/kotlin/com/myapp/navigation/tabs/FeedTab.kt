package com.myapp.navigation.tabs

import androidx.fragment.app.Fragment
import com.myapp.core.navigation.contracts.NavigationTab
import com.myapp.feature.feed.impl.presentation.FeedFragment
import com.myapp.R

/**
 * Feed tab definition. Registers the Feed feature as the first (default) tab
 * in the bottom navigation.
 */
object FeedTab : NavigationTab {
    override val id: String = "feed"
    override val menuItemId: Int = R.id.nav_feed
    override val startDestinationFactory: () -> Fragment = { FeedFragment() }
}
