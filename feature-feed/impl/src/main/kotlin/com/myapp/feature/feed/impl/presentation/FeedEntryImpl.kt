package com.myapp.feature.feed.impl.presentation

import androidx.fragment.app.Fragment
import com.myapp.feature.feed.api.FeedFeatureEntry
import javax.inject.Inject

class FeedEntryImpl @Inject constructor() : FeedFeatureEntry {
    override fun createEntryFragment(): Fragment = FeedFragment()
}
