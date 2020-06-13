package com.thiyagu06.retry4k

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
class RetryExecutorTest {

    private interface NoOpAction {
        fun noOp(): String
    }

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @Test
    internal fun `it should not retry any exception occurred during action if exception is not specified for retry`() {

        val retryStrategy =
            RetryStrategy.Builder<String>().maxAttempt(3).retryOnException(NullPointerException::class.java).build()

        val retryExecutor = RetryExecutor(retryStrategy)

        val dummyAction = mockk<NoOpAction>()

        every { dummyAction.noOp() } throws RuntimeException()

        assertThatExceptionOfType(RuntimeException::class.java).isThrownBy { retryExecutor.execute { dummyAction.noOp() } }

        verify(exactly = 1) { dummyAction.noOp() }
    }

    @Test
    internal fun `it should retry once if an exception occurred during action and it is specified for retry`() {

        val retryStrategy =
            RetryStrategy.Builder<String>().maxAttempt(1).retryOnException(RuntimeException::class.java).build()

        val retryExecutor = RetryExecutor(retryStrategy)

        val dummyAction = mockk<NoOpAction>()

        every { dummyAction.noOp() } throws RuntimeException()

        assertThatExceptionOfType(ExceededRetryAttemptException::class.java).isThrownBy { retryExecutor.execute { dummyAction.noOp() } }

        verify(exactly = 2) { dummyAction.noOp() }
    }

    @Test
    internal fun `it should retry once if action return a string "retryme" and it is specified for retry`() {

        val retryOnResult: (String) -> Boolean = {result -> result == "retryme"}

        val retryStrategy =
            RetryStrategy.Builder<String>().maxAttempt(1).retryOnException(RuntimeException::class.java).retryOnResult(retryOnResult).build()

        val retryExecutor = RetryExecutor(retryStrategy)

        val dummyAction = mockk<NoOpAction>()

        every { dummyAction.noOp() } returns "retryme"

        assertThatExceptionOfType(ExceededRetryAttemptException::class.java).isThrownBy { retryExecutor.execute { dummyAction.noOp() } }

        verify(exactly = 2) { dummyAction.noOp() }
    }

    @Test
    internal fun `should not retry if no exception thrown`() {

        val retryOnResult: (String) -> Boolean = {result -> result == "retryme"}

        val retryStrategy =
            RetryStrategy.Builder<String>().maxAttempt(1).retryOnException(RuntimeException::class.java).retryOnResult(retryOnResult).build()

        val retryExecutor = RetryExecutor(retryStrategy)

        val dummyAction = mockk<NoOpAction>()

        every { dummyAction.noOp() } returns "success"

        retryExecutor.execute { dummyAction.noOp() }

        verify(exactly = 1) { dummyAction.noOp() }
    }

    @Test
    internal fun `should not retry if exception thrown should be ignored`() {

        val retryOnResult: (String) -> Boolean = {result -> result == "retryme"}

        val retryStrategy =
            RetryStrategy.Builder<String>().waitDuration(1000).maxAttempt(5).retryOnException(NullPointerException::class.java).retryOnResult(retryOnResult).ignoreOnException(IllegalArgumentException::class.java).build()

        val retryExecutor = RetryExecutor(retryStrategy)

        val dummyAction = mockk<NoOpAction>()

        every { dummyAction.noOp() } throws IllegalArgumentException()

        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy { retryExecutor.execute { dummyAction.noOp() } }

        verify(exactly = 1) { dummyAction.noOp() }
    }
}