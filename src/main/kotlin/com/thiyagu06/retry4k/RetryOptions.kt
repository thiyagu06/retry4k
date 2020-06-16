package com.thiyagu06.retry4k

private const val DEFAULT_WAIT_TIME_SECONDS = 1000L
class RetryOptions<T> private constructor(config: Builder<T>) {

    internal val maxAttempt: Int = config.maxAttempt
    internal var waitStrategy: ((Int) -> Long) = config.waitStrategy
    internal val retryOnResult: ((T) -> Boolean) = config.retryOnResult
    internal val exceptionPredicate: ExceptionPredicate = convertExceptionsToPredicate(config)
    internal val beforeRetry: ((Throwable?, T?, Int) -> Unit)? = config.beforeRetry
    internal val onCompleted: ((Throwable?, T?, Int) -> Unit)? = config.onCompleted

    class Builder<T> internal constructor(
         var retryableException: MutableSet<Class<out Throwable>> = mutableSetOf(),
         var waitStrategy: (Int) -> Long = { _ -> DEFAULT_WAIT_TIME_SECONDS },
         var maxAttempt: Int = 0,
         var ignorableException: MutableSet<Class<out Throwable>> = mutableSetOf(),
         var retryOnResult: ((T) -> Boolean) = {false},
         var beforeRetry: ((Throwable?,T?, Int) -> Unit)? = null,
         var onCompleted: ((Throwable?,T?, Int) -> Unit)? = null
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

        internal fun doOnCompleted(block: (Throwable?, T?, Int) -> Unit) = apply {
            this.onCompleted = block
        }

        internal fun doOnBeforeRetry(block: (Throwable?,T?, Int) -> Unit) = apply {
            this.beforeRetry = block
        }

        internal fun build(): RetryOptions<T> = RetryOptions(this)
    }
}
