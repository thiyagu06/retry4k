# retry4k
Retry4k is resilience and fault tolerance library written in kotlin.

[![Kotlin version badge](https://img.shields.io/badge/kotlin-1.3-blue.svg)](https://kotlinlang.org/docs/reference/whatsnew13.html) 
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![CircleCI](https://circleci.com/gh/thiyagu06/reactive-sqs-consumer.svg?style=svg)](https://circleci.com/gh/thiyagu06/reactive-sqs-consumer)
[![codecov](https://codecov.io/gh/thiyagu06/retry4k/branch/master/graph/badge.svg)](https://codecov.io/gh/thiyagu06/retry4k)

## Usage

```kotlin
//retry maximum of 3 times if nullpointer exception occurred
 RetryOptions.Builder<String>().maxAttempt(3).retryOnException(NullPointerException::class.java).build()
```

```kotlin 
//retry maximum of 3 times if action returns returns result with string retryme and wait for 1000ms before every retry

 val retryOnResult: (String) -> Boolean = {result -> result == "retryme"}

 RetryOptions.Builder<String>().maxAttempt(3).retryOnResult(retryOnResult).waitDuration(1000).build()

```

```kotlin 
//Don't retry if the exception thrown by action is IllegalArgumentException. 

 val retryOnResult: (String) -> Boolean = {result -> result == "retryme"}

 RetryOptions.Builder<String>().waitDuration(1000).maxAttempt(5).retryOnException(NullPointerException::class.java).retryOnResult(retryOnResult).ignoreOnException(IllegalArgumentException::class.java).build()
```


```kotlin 
//Do some action before every retry attempt and after completion of either successfull or failure execution of action. 

 val beforeRetry:(Throwable?, String?, Int) -> Unit = { throwable,result, cuurentAttempt -> println("attempting for $cuurentAttempt time)}
 
 val onCompleted:(Throwable?, String?, Int) -> Unit = { throwable,result, cuurentAttempt -> println("completed after $cuurentAttempt times)}
 
  val retryStrategy = RetryOptions.Builder<String>().maxAttempt(5).retryOnException(NullPointerException::class.java)
                 .doOnBeforeRetry(beforeRetry).doOnCompleted(onCompleted).build()
```

## Execution

```kotlin
    val retryOption = RetryOptions.Builder<String>().maxAttempt(3).retryOnException(NullPointerException::class.java).build()
    val retryExecutor = RetryExecutor(retryOption)
    retryExecutor.execute { business logic }
```
TODO:

- [ ] allow option to define custom wait strategy
- [ ] implement multiple backOff strategy (exponential, Jitter etc)
- [ ] publish as maven artifact
- [ ] pluggable metric publisher (dropwizard, micrometer)
- [ ] DSL based retryOptions
