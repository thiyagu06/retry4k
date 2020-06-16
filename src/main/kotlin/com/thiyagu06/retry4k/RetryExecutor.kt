package com.thiyagu06.retry4k

import java.lang.RuntimeException

class RetryExecutor<T>(private val retryOptions: RetryOptions<T>) {

    fun execute(block: () -> T): T {
        var currentAttempt = 0
        var currentStatus: AttemptStatus<T>? = null
        try {
            while (true) {
                currentStatus = tryExecute(block)
                if (!currentStatus.shouldRetry) {
                    return currentStatus.result?.let { currentStatus.result } ?: throw currentStatus.exception!!
                }
                handleFailedTry(currentStatus, currentAttempt)
                currentAttempt++
                Thread.sleep(retryOptions.waitStrategy(currentAttempt))
            }
        } finally {
            retryOptions.onCompleted?.let { it(currentStatus!!.exception, currentStatus.result, currentAttempt) }
        }
    }

    private fun handleFailedTry(currentStatus: AttemptStatus<T>, currentAttempt: Int) {
        if (currentAttempt >= retryOptions.maxAttempt) {
            throw RetryExhaustedException("Retry failed after attempted for $currentAttempt")
        }
        retryOptions.beforeRetry?.let { it(currentStatus.exception, currentStatus.result, currentAttempt) }
    }

    private fun tryExecute(block: () -> T): AttemptStatus<T> {
        return try {
            val result = block()
            AttemptStatus(result, null, shouldRetryOnResult(result))
        } catch (e: Exception) {
            AttemptStatus(null, e, shouldRetryOnException(e))
        }
    }

    private fun shouldRetryOnResult(result: T): Boolean = retryOptions.retryOnResult(result)

    private fun shouldRetryOnException(e:Throwable) : Boolean = retryOptions.exceptionPredicate(e)
}

class RetryExhaustedException(message:String) : RuntimeException(message)
