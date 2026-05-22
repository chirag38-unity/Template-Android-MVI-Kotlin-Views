package com.myapp.feature.feed.api

import androidx.fragment.app.Fragment

/** Public entry-point of the feed feature, consumed by :app. */
interface FeedFeatureEntry {
    fun createEntryFragment(): Fragment
}
