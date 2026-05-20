package com.theupnextapp.network

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger

/**
 * A global rate limiter for TVMaze API calls.
 * TVMaze public API restricts clients to 20 calls per 10 seconds.
 * Using a 600ms delay between calls ensures we only make ~16 calls per 10 seconds,
 * well within the safe limits, avoiding HTTP 429 Too Many Requests errors.
 */
object TvMazeRateLimiter {
    private val mutex = Mutex()
    private var lastRequestTime = 0L
    private const val DELAY_MS = 600L
    
    // Tracks the number of high-priority requests waiting for the lock
    private val highPriorityCount = AtomicInteger(0)

    /**
     * Acquires the rate limiter lock.
     * @param isHighPriority If true, this request will bypass low-priority requests in the queue.
     */
    suspend fun <T> acquire(isHighPriority: Boolean = false, block: suspend () -> T): T {
        if (isHighPriority) {
            highPriorityCount.incrementAndGet()
        }

        try {
            while (true) {
                // If we are low priority and there are high priority requests waiting, yield and spin.
                if (!isHighPriority && highPriorityCount.get() > 0) {
                    delay(50)
                    continue
                }

                // Try to acquire the lock immediately without suspending
                if (mutex.tryLock()) {
                    try {
                        val now = System.currentTimeMillis()
                        val timeSinceLastRequest = now - lastRequestTime
                        if (timeSinceLastRequest < DELAY_MS) {
                            delay(DELAY_MS - timeSinceLastRequest)
                        }
                        lastRequestTime = System.currentTimeMillis()
                        break // We got the lock and delayed, now we can execute block()
                    } finally {
                        mutex.unlock()
                    }
                } else {
                    // Lock is held by someone else. Spin wait.
                    delay(50)
                }
            }
        } finally {
            if (isHighPriority) {
                highPriorityCount.decrementAndGet()
            }
        }

        // Execute network request outside the lock to prevent HTTP blocking
        return block()
    }
}
