package com.myapp.core.navigation.contracts

import javax.inject.Qualifier

/**
 * Hilt qualifier for the ordered list of [NavigationTab]s provided by the app layer.
 *
 * App provides the list via a Hilt module:
 * ```kotlin
 * @Module
 * @InstallIn(SingletonComponent::class)
 * object NavigationModule {
 *     @Provides
 *     @NavigationTabs
 *     fun provideTabs(): List<NavigationTab> = listOf(FeedTab, SearchTab)
 * }
 * ```
 *
 * [com.myapp.core.navigation.ui.NavigationHostFragment] injects this list to drive
 * tab creation and bottom navigation setup, with no knowledge of concrete tab types.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NavigationTabs
