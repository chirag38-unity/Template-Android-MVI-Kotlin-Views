package com.myapp.core.common.dispatchers

import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

interface CoroutineScopeProvider {
    val appScope: CoroutineScope
}

class DefaultCoroutineScopeProvider @Inject constructor(
    @ApplicationScope override val appScope: CoroutineScope,
) : CoroutineScopeProvider
