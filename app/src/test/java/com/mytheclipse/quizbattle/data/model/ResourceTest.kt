package com.mytheclipse.quizbattle.data.model

import org.junit.Assert.*
import org.junit.Test

class ResourceTest {

    @Test
    fun successState_behavesCorrectly() {
        val res: Resource<Int> = Resource.Success(42)
        assertTrue(res.isSuccess())
        assertFalse(res.isError())
        assertFalse(res.isLoading())
        assertEquals(42, res.getDataOrNull())

        var onSuccessCalled = false
        res.onSuccess {
            onSuccessCalled = true
            assertEquals(42, it)
        }.onError { _, _ ->
            fail("onError should not be called for Success")
        }.onLoading {
            fail("onLoading should not be called for Success")
        }
        assertTrue(onSuccessCalled)

        val mapped = res.map { it.toString() }
        assertTrue(mapped is Resource.Success)
        assertEquals("42", (mapped as Resource.Success).data)
    }

    @Test
    fun errorState_behavesCorrectly() {
        val err: Resource<Any> = Resource.Error("Boom", RuntimeException("x"))
        assertTrue(err.isError())
        assertFalse(err.isSuccess())
        assertFalse(err.isLoading())
        assertNull(err.getDataOrNull())

        var onErrorCalled = false
        err.onError { message, throwable ->
            onErrorCalled = true
            assertEquals("Boom", message)
            assertNotNull(throwable)
        }.onSuccess {
            fail("onSuccess should not be called for Error")
        }.onLoading {
            fail("onLoading should not be called for Error")
        }
        assertTrue(onErrorCalled)

        val mapped = err.map { 1 }
        assertTrue(mapped is Resource.Error)
        assertEquals("Boom", (mapped as Resource.Error).message)
    }

    @Test
    fun loadingState_behavesCorrectly() {
        val loading: Resource<Any> = Resource.Loading
        assertTrue(loading.isLoading())
        assertFalse(loading.isError())
        assertFalse(loading.isSuccess())
        assertNull(loading.getDataOrNull())

        var onLoadingCalled = false
        loading.onLoading {
            onLoadingCalled = true
        }.onSuccess {
            fail("onSuccess should not be called for Loading")
        }.onError { _, _ ->
            fail("onError should not be called for Loading")
        }
        assertTrue(onLoadingCalled)
    }
}
