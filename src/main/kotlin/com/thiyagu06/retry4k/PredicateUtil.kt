package com.thiyagu06.retry4k

fun <T> convertExceptionsToPredicate(config: RetryOptions.Builder<T>): ExceptionPredicate {

    val defaultPredicate: ExceptionPredicate = { true }
    val retryOnExceptionPredicate = createPredicate(config.retryableException) ?: defaultPredicate
    val ignorableExceptionPredicate = createPredicate(config.ignorableException)?.negate() ?: defaultPredicate
    return retryOnExceptionPredicate.and(ignorableExceptionPredicate)
}

private fun createPredicate(predicates: MutableSet<Class<out Throwable>>): ((Throwable) -> Boolean)? {
    return predicates
        .map { convertToPredicate(it) }
        .takeIf { it.isNotEmpty() }
        ?.reduce { p1, p2 -> p1.or(p2) }
}

fun convertToPredicate(ex: Class<out Throwable>) = { e: Throwable -> ex.isInstance(e) }

fun <T> ((T) -> Boolean).and(arg: (T) -> Boolean): (T) -> Boolean = { this(it) && arg(it) }
fun <T> ((T) -> Boolean).or(arg: (T) -> Boolean): (T) -> Boolean = { this(it) || arg(it) }
fun <T> ((T) -> Boolean).negate(): (T) -> Boolean = { !this(it) }
typealias ExceptionPredicate = (Throwable) -> Boolean
