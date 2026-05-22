package com.myapp.navigation.di

import com.myapp.core.navigation.contracts.NavigationTab
import com.myapp.core.navigation.contracts.NavigationTabs
import com.myapp.navigation.tabs.FeedTab
import com.myapp.navigation.tabs.SearchTab
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Provides the ordered list of [NavigationTab]s that [com.myapp.core.navigation.ui.NavigationHostFragment]
 * uses to build the bottom navigation and create per-tab containers.
 *
 * This is the **only** navigation-related configuration the app layer needs to provide.
 * All orchestration (tab switching, back-stack management, back-press handling) lives in
 * `:core:navigation`.
 *
 * To add a new tab, register it here and add a corresponding item to `bottom_nav_menu.xml`.
 */
@Module
@InstallIn(SingletonComponent::class)
object NavigationModule {

    @Provides
    @NavigationTabs
    fun provideTabs(): List<NavigationTab> = listOf(
        FeedTab,
        SearchTab,
    )
}
