package com.thiyagu06.retry4k

data class AttemptStatus<T>(val result: T?, val exception: Throwable?, val shouldRetry: Boolean = false)
