package com.myapp.core.common.concurrency

import kotlinx.coroutines.Job

class JobTracker {

    private val jobs = mutableMapOf<String, Job>()

    fun track(name: String, job: Job) {
        jobs[name]?.cancel()
        jobs[name] = job
        job.invokeOnCompletion { jobs.remove(name) }
    }

    fun cancel(name: String) {
        jobs[name]?.cancel()
        jobs.remove(name)
    }

    fun cancelAll() {
        jobs.values.forEach { it.cancel() }
        jobs.clear()
    }

    fun isRunning(name: String): Boolean = jobs[name]?.isActive == true

    fun activeJobNames(): Set<String> = jobs.keys.toSet()
}
