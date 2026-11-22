package sh.haven.feature.sftp.transfer

import android.os.SystemClock

/**
 * Coalesces byte-counter callbacks into at most one state emission per [intervalMs].
 *
 * JSch invokes [com.jcraft.jsch.SftpProgressMonitor.count] once per transferred
 * packet (~32 KB). On a fast link that is hundreds of calls per second, and each
 * one previously rewrote the whole `_transfers` StateFlow, which in turn rebuilt
 * and re-posted the foreground notification. Emitting at a fixed low rate keeps
 * the progress bar responsive while removing the redundant work.
 *
 * [report] returns whatever the last real emission returned, so callers that use
 * the return value as a cancellation signal keep working: a `false` from [emit]
 * is surfaced on the call that produced it and on every skipped call after it,
 * until the next emission. At worst one extra packet is read before the transfer
 * stops.
 *
 * The final value is always emitted: [flush] must be called when the transfer
 * ends so the UI never stops short of 100%.
 *
 * Not thread-safe on purpose — each transfer drives its monitor from a single
 * I/O coroutine.
 */
internal class ProgressThrottle(
    private val intervalMs: Long = DEFAULT_INTERVAL_MS,
    private val emit: (Long) -> Boolean,
) {
    private var lastEmittedAt = 0L
    private var lastValue = 0L
    private var dirty = false
    private var lastResult = true

    /** Report a new absolute byte count; emits only when [intervalMs] has elapsed. */
    fun report(total: Long): Boolean {
        lastValue = total
        dirty = true
        val now = SystemClock.elapsedRealtime()
        if (now - lastEmittedAt >= intervalMs) {
            lastEmittedAt = now
            dirty = false
            lastResult = emit(total)
        }
        return lastResult
    }

    /** Emit the latest value if anything is still pending. */
    fun flush(): Boolean {
        if (dirty) {
            dirty = false
            lastResult = emit(lastValue)
        }
        return lastResult
    }

    companion object {
        /**
         * 250 ms matches the cadence Android itself uses for progress
         * notifications and is well below human perception for a progress bar.
         */
        const val DEFAULT_INTERVAL_MS = 250L
    }
}
