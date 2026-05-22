package com.myapp.navigation.tabs

import androidx.fragment.app.Fragment
import com.myapp.core.navigation.contracts.NavigationTab
import com.myapp.feature.search.impl.presentation.SearchFragment
import com.myapp.R

/**
 * Search tab definition. Registers the Search feature as a bottom navigation tab.
 */
object SearchTab : NavigationTab {
    override val id: String = "search"
    override val menuItemId: Int = R.id.nav_search
    override val startDestinationFactory: () -> Fragment = { SearchFragment() }
}
