package com.myapp.core.network.tracing

import com.myapp.core.common.logging.Logger

interface RequestTracer {
    fun onRequest(url: String, method: String)
    fun onResponse(url: String, statusCode: Int, durationMs: Long)
    fun onError(url: String, error: Throwable)
}

class LoggingRequestTracer : RequestTracer {

    override fun onRequest(url: String, method: String) {
        Logger.logD({"--> $method $url"})
    }

    override fun onResponse(url: String, statusCode: Int, durationMs: Long) {
        Logger.logD({"<-- $statusCode $url (${durationMs}ms)"})
    }

    override fun onError(url: String, error: Throwable) {
        Logger.logE({"<-- ERROR $url"},{null},{error})
    }
}
