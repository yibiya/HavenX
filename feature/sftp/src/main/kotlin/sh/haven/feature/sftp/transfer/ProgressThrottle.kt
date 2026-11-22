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
 * The final value is always emitted: [flush] must be called when the transfer
 * ends so the UI never stops short of 100%.
 *
 * Not thread-safe on purpose — each transfer drives its monitor from a single
 * I/O coroutine.
 */
internal class ProgressThrottle(
    private val intervalMs: Long = DEFAULT_INTERVAL_MS,
    private val emit: (Long) -> Unit,
) {
    private var lastEmittedAt = 0L
    private var lastValue = 0L
    private var dirty = false

    /** Report a new absolute byte count; emits only when [intervalMs] has elapsed. */
    fun report(total: Long) {
        lastValue = total
        dirty = true
        val now = SystemClock.elapsedRealtime()
        if (now - lastEmittedAt >= intervalMs) {
            lastEmittedAt = now
            dirty = false
            emit(total)
        }
    }

    /** Emit the latest value if anything is still pending. */
    fun flush() {
        if (dirty) {
            dirty = false
            emit(lastValue)
        }
    }

    companion object {
        /**
         * 250 ms matches the cadence Android itself uses for progress
         * notifications and is well below human perception for a progress bar.
         */
        const val DEFAULT_INTERVAL_MS = 250L
    }
}
