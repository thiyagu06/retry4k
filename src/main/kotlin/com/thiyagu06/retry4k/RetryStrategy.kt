package com.thiyagu06.retry4k

class RetryStrategy<T> internal constructor(config: Builder<T>) {

    internal val retryableException: MutableSet<Class<out Throwable>> = config.retryableException
    internal val maxAttempt: Int = config.maxAttempt
    internal var waitStrategy: ((Int) -> Long) = config.waitStrategy
    internal val ignorableException: MutableSet<Class<out Throwable>> = config.ignorableException
    internal val retryOnResult: ((T) -> Boolean) = config.retryOnResult

   data class Builder<T> internal constructor(
         var retryableException: MutableSet<Class<out Throwable>> = mutableSetOf(),
         var waitStrategy: (Int) -> Long = { _ -> 1000 },
         var maxAttempt: Int = 0,
         var ignorableException: MutableSet<Class<out Throwable>> = mutableSetOf(),
         var retryOnResult: ((T) -> Boolean) = {false}
    ) {
        fun maxAttempt(maxAttempt: Int) = apply {
            this.maxAttempt = maxAttempt
        }

        internal fun waitDuration(waitTime: Long) = apply {
            this.waitStrategy = { waitTime }
        }

        internal fun retryOnException(vararg exception: Class<out Throwable>) = apply {
            this.retryableException.addAll(exception)
        }

        internal fun ignoreOnException(vararg exception: Class<out Throwable>)= apply {
            this.ignorableException.addAll(exception)
        }

       internal fun retryOnResult(retryOnResult: ((T) -> Boolean)) = apply {
           this.retryOnResult = retryOnResult
       }

        internal fun build(): RetryStrategy<T> = RetryStrategy(this)
    }
}
