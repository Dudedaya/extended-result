package xyz.dudedayaworks.result

import org.junit.Test

class ExResultTest {

    @Test
    fun `Success values data is accessible`() {
        when (val result = getSuccessIntResult()) {
            is ExResult.Failure -> {
                /* no-op */
            }

            is ExResult.Success -> assert(result.data == 42) { "Invalid value" }
        }
    }

    @Test
    fun `Failure values error is accessible`() {
        when (val result = getFailureIntResult()) {
            is ExResult.Failure -> assert(result.error == ResultError.Error2) { "Invalid error" }
            is ExResult.Success -> {
                /* no-op */
            }
        }
    }

    @Test
    fun `Should smart cast Success results`() {
        val result = getSuccessIntResult()
        if (result.isSuccess()) {
            assert(result.data == 42) { "Wrong data" }
        }
    }

    @Test
    fun `Should smart cast Failure results`() {
        val result = getFailureIntResult()
        if (result.isFailure()) {
            assert(result.error == ResultError.Error2) { "Wrong error" }
        }
    }

    @Test
    fun `Should smart cast else results after isSuccess check`() {
        val result = getFailureIntResult()
        if (result.isSuccess()) {
            assert(false) { "Invalid result" }
        } else {
            assert(result.error == ResultError.Error2) { "Wrong error" }
        }
    }

    @Test
    fun `Should smart cast else results after isFailure check`() {
        val result = getSuccessIntResult()
        if (result.isFailure()) {
            assert(false) { "Invalid result" }
        } else {
            assert(result.data == 42) { "Wrong data" }
        }
    }

    @Test
    fun `onSuccess is getting called once for Success results`() {
        var onSuccessCalled = 0
        var onFailureCalled = 0
        getSuccessIntResult()
            .onSuccess {
                onSuccessCalled++
            }.onFailure {
                onFailureCalled++
            }
        assert(onSuccessCalled == 1) { "onSuccess wasn't called once" }
        assert(onFailureCalled == 0) { "onFailure was called" }
    }

    @Test
    fun `onFailure is getting called once for Failure results`() {
        var onSuccessCalled = 0
        var onFailureCalled = 0
        getFailureIntResult()
            .onSuccess {
                onSuccessCalled++
            }.onFailure {
                onFailureCalled++
            }
        assert(onSuccessCalled == 0) { "onSuccess was called" }
        assert(onFailureCalled == 1) { "onFailure wasn't called once" }
    }

    @Test
    fun `Should pass caused exception`() {
        val cause = Exception("42")
        var onFailureCalled = false
        getUnknownExceptionIntResult(cause).onFailure {
            onFailureCalled = true
            when (it) {
                ResultError.Error1 -> assert(false) { "wrong error" }
                ResultError.Error2 -> assert(false) { "wrong error" }
                ResultError.Error3 -> assert(false) { "wrong error" }
                is ResultError.UnknownError -> assert(it.cause == cause) { "Wrong exception" }
            }
        }
        assert(onFailureCalled) { "onFailure wasn't called" }
    }

    @Test
    fun `Should return default value when passed on Failure`() {
        val defaultResult = getFailureIntResult().getOrDefault(69)
        assert(defaultResult == 69) { "Wrong data" }
    }

    @Test
    fun `Should return data when passed on Success`() {
        val data = getSuccessIntResult().getOrDefault(69)
        assert(data == 42) { "Wrong data" }
    }

    @Test
    fun `Should return null in getOrNull on Failure`() {
        val data = getFailureIntResult().getOrNull()
        assert(data == null) { "Wrong data" }
    }

    @Test
    fun `Should return data in getOrNull on Success`() {
        val data = getSuccessIntResult().getOrNull()
        assert(data == 42) { "Wrong data" }
    }

    @Test
    fun `Should return error in errorOrNull on Failure`() {
        val error = getFailureIntResult().errorOrNull()
        assert(error == ResultError.Error2) { "Wrong error" }
    }

    @Test
    fun `Should return null in errorOrNull on Success`() {
        val error = getSuccessIntResult().errorOrNull()
        assert(error == null) { "Wrong error" }
    }

    private fun getSuccessIntResult(): ExResult<Int, ResultError> {
        return ExResult.Success(42)
    }

    private fun getFailureIntResult(): ExResult<Int, ResultError> {
        return ExResult.Failure(ResultError.Error2)
    }

    private fun getUnknownExceptionIntResult(exception: Exception): ExResult<Int, ResultError> {
        return ExResult.Failure(ResultError.UnknownError(exception))
    }

    sealed class ResultError : TasstaError {
        object Error1 : ResultError()
        object Error2 : ResultError()
        object Error3 : ResultError()
        data class UnknownError(override val cause: Exception) : ResultError()
    }
}