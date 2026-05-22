package com.myapp.feature.search.impl.presentation

import androidx.fragment.app.Fragment
import com.myapp.feature.search.api.SearchFeatureEntry
import javax.inject.Inject

class SearchEntryImpl @Inject constructor() : SearchFeatureEntry {
    override fun createEntryFragment(): Fragment = SearchFragment()
}
