package com.myapp.core.database.sync

import com.myapp.core.database.result.DatabaseResult
import javax.inject.Inject

class LocalRemoteSyncHelper @Inject constructor() {

    suspend fun <R, L> sync(
        fetchRemote: suspend () -> R,
        saveLocal: suspend (R) -> Unit,
        loadLocal: suspend () -> L,
    ): DatabaseResult<L> = try {
        val remoteData = fetchRemote()
        saveLocal(remoteData)
        DatabaseResult.Success(loadLocal())
    } catch (e: Exception) {
        DatabaseResult.Error(e)
    }
}
