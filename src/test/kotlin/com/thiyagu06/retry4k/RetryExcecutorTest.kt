package com.thiyagu06.retry4k

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test

class RetryExecutorTest {

    private interface DummyAction {
        fun noop(): String
    }

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @Test
    internal fun `it should not retry any exception occurred during action if exception is not specified for retry`() {

        val retryStrategy =
            RetryStrategy.Builder<String>().maxAttempt(3).retryOnException(NullPointerException::class.java).build()

        val retryExecutor = RetryExecutor<String>(retryStrategy)

        val dummyAction = mockk<DummyAction>()

        every { dummyAction.noop() } throws RuntimeException()

        assertThatExceptionOfType(RuntimeException::class.java).isThrownBy { retryExecutor.execute { dummyAction.noop() } }

        verify(exactly = 1) { dummyAction.noop() }
    }

    @Test
    internal fun `it should retry once if an exception occurred during action and it is specified for retry`() {

        val retryStrategy =
            RetryStrategy.Builder<String>().maxAttempt(1).retryOnException(RuntimeException::class.java).build()

        val retryExecutor = RetryExecutor(retryStrategy)

        val dummyAction = mockk<DummyAction>()

        every { dummyAction.noop() } throws RuntimeException()

        assertThatExceptionOfType(ExceededRetryAttemptException::class.java).isThrownBy { retryExecutor.execute { dummyAction.noop() } }

        verify(exactly = 2) { dummyAction.noop() }
    }

    @Test
    internal fun `it should retry once if action return a string "retryme" and it is specified for retry`() {

        val retryOnResult: (String) -> Boolean = {result -> result == "retryme"}

        val retryStrategy =
            RetryStrategy.Builder<String>().maxAttempt(1).retryOnException(RuntimeException::class.java).retryOnResult(retryOnResult).build()

        val retryExecutor = RetryExecutor(retryStrategy)

        val dummyAction = mockk<DummyAction>()

        every { dummyAction.noop() } returns "retryme"

        assertThatExceptionOfType(ExceededRetryAttemptException::class.java).isThrownBy { retryExecutor.execute { dummyAction.noop() } }

        verify(exactly = 2) { dummyAction.noop() }
    }

    @Test
    internal fun `should not retry if no exception thrown`() {

        val retryOnResult: (String) -> Boolean = {result -> result == "retryme"}

        val retryStrategy =
            RetryStrategy.Builder<String>().maxAttempt(1).retryOnException(RuntimeException::class.java).retryOnResult(retryOnResult).build()

        val retryExecutor = RetryExecutor(retryStrategy)

        val dummyAction = mockk<DummyAction>()

        every { dummyAction.noop() } returns "success"

        retryExecutor.execute { dummyAction.noop() }

        verify(exactly = 1) { dummyAction.noop() }
    }

    @Test
    internal fun `should not retry if exception thrown should be ignored`() {

        val retryOnResult: (String) -> Boolean = {result -> result == "retryme"}

        val retryStrategy =
            RetryStrategy.Builder<String>().maxAttempt(5).retryOnException(NullPointerException::class.java).retryOnResult(retryOnResult).ignoreOnException(IllegalArgumentException::class.java).build()

        val retryExecutor = RetryExecutor(retryStrategy)

        val dummyAction = mockk<DummyAction>()

        every { dummyAction.noop() } throws IllegalArgumentException()

        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy { retryExecutor.execute { dummyAction.noop() } }

        verify(exactly = 1) { dummyAction.noop() }
    }
}