package com.myapp.feature.search.api

import androidx.fragment.app.Fragment

/**
 * Public contract for the Search feature. Provides the entry-point Fragment.
 */
interface SearchFeatureEntry {
    fun createEntryFragment(): Fragment
}
