package com.thiyagu06.retry4k

import java.lang.RuntimeException

class RetryExecutor<T>(private val retryStrategy: RetryStrategy<T>) {

     fun execute(block:   () -> T): T {
        var retryCount = 0
        var lastThrowable: Throwable?
        while (true) {
            try {
                val result = block()
                if (!shouldRetryOnResult(result)) return result
                if (isRetryAttemptExceeded(retryCount)) throw ExceededRetryAttemptException()
            } catch (e: Exception) {
                 lastThrowable = e
                if(isRetryAttemptExceeded(retryCount)) throw  ExceededRetryAttemptException()
                if(!shouldRetryOnException(e) || shouldIgnoreOnException(e)) throw lastThrowable
            }
            retryCount++
            Thread.sleep(retryStrategy.waitStrategy(retryCount))
        }
    }

    private fun shouldRetryOnResult(result: T): Boolean = retryStrategy.retryOnResult(result)

    private fun isRetryAttemptExceeded(attemptsDone: Int): Boolean = attemptsDone >= retryStrategy.maxAttempt

    private fun shouldRetryOnException(exception: Exception): Boolean = retryStrategy.retryableException.any { it.isInstance(exception) }

    private fun shouldIgnoreOnException(exception: Exception): Boolean = retryStrategy.ignorableException.any { it.isInstance(exception) }
}

class ExceededRetryAttemptException : RuntimeException()