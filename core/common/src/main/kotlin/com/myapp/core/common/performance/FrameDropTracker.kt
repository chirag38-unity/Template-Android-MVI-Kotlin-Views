package com.myapp.core.common.performance

import android.view.Choreographer
import java.util.concurrent.atomic.AtomicInteger

class FrameDropTracker : Choreographer.FrameCallback {

    private val _droppedFrames = AtomicInteger(0)
    val droppedFrames: Int get() = _droppedFrames.get()

    private var lastFrameTimeNs = 0L
    private var running = false

    override fun doFrame(frameTimeNanos: Long) {
        if (!running) return
        if (lastFrameTimeNs != 0L) {
            val frameMs = (frameTimeNanos - lastFrameTimeNs) / 1_000_000L
            if (frameMs > JANK_THRESHOLD_MS) {
                val dropped = ((frameMs / TARGET_FRAME_MS) - 1).toInt()
                if (dropped > 0) _droppedFrames.addAndGet(dropped)
            }
        }
        lastFrameTimeNs = frameTimeNanos
        Choreographer.getInstance().postFrameCallback(this)
    }

    fun start() {
        if (running) return
        running = true
        lastFrameTimeNs = 0L
        Choreographer.getInstance().postFrameCallback(this)
    }

    fun stop() {
        running = false
        Choreographer.getInstance().removeFrameCallback(this)
    }

    fun reset() {
        _droppedFrames.set(0)
        lastFrameTimeNs = 0L
    }

    companion object {
        private const val TARGET_FRAME_MS = 16L
        private const val JANK_THRESHOLD_MS = 20L
    }
}
