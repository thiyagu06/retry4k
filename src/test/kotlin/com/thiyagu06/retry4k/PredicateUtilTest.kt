package com.thiyagu06.retry4k

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import java.io.FileNotFoundException
import java.io.IOException


internal class PredicateUtilTest {

    @Test
    fun `should create predicate chain for the given exceptions`() {
        val retryStrategy = RetryStrategy.Builder<String>()
            .retryOnException(IllegalArgumentException::class.java, NullPointerException::class.java)
            .ignoreOnException(ArrayIndexOutOfBoundsException::class.java).build()
        val predicateChain = retryStrategy.exceptionPredicate
        assertTrue(predicateChain(IllegalArgumentException()))
        assertTrue(predicateChain(NullPointerException()))
        assertFalse(predicateChain(IOException()))
        assertFalse(predicateChain(ArrayIndexOutOfBoundsException()))
    }

    @Test
    fun `empty exception chain should always return true for all exception instance`(){
        val retryStrategy = RetryStrategy.Builder<String>().build()
        val predicateChain = retryStrategy.exceptionPredicate
        assertTrue(predicateChain(IllegalArgumentException()))
        assertTrue(predicateChain(IOException()))
    }
}