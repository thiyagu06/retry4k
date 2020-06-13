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
                if(!retryStrategy.exceptionPredicate(e)) throw lastThrowable
            }
            retryCount++
            Thread.sleep(retryStrategy.waitStrategy(retryCount))
        }
    }

    private fun shouldRetryOnResult(result: T): Boolean = retryStrategy.retryOnResult(result)

    private fun isRetryAttemptExceeded(attemptsDone: Int): Boolean = attemptsDone >= retryStrategy.maxAttempt
}

class ExceededRetryAttemptException : RuntimeException()
