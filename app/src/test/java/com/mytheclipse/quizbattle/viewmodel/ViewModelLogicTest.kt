package com.mytheclipse.quizbattle.viewmodel

import com.mytheclipse.quizbattle.data.model.Resource
import com.mytheclipse.quizbattle.data.model.isSuccess
import com.mytheclipse.quizbattle.data.model.getDataOrNull
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for ViewModel logic patterns
 * Tests state management and resource handling
 */
class ViewModelLogicTest {

    @Test
    fun `Resource Loading state should have correct type`() {
        // Given & When
        val loading: Resource<String> = Resource.Loading
        
        // Then
        assertTrue(loading is Resource.Loading)
        assertFalse(loading is Resource.Success)
        assertFalse(loading is Resource.Error)
    }

    @Test
    fun `Resource Success state should contain data`() {
        // Given
        val testData = "Test Data"
        
        // When
        val success = Resource.Success(testData)
        
        // Then
        assertTrue(success is Resource.Success)
        assertEquals(testData, (success as Resource.Success).data)
    }

    @Test
    fun `Resource Error state should contain message`() {
        // Given
        val errorMessage = "Network error occurred"
        
        // When
        val error = Resource.Error(errorMessage)
        
        // Then
        assertTrue(error is Resource.Error)
        assertEquals(errorMessage, error.message)
    }

    @Test
    fun `Resource states should be distinguishable`() {
        // Given
        val loading: Resource<Int> = Resource.Loading
        val success: Resource<Int> = Resource.Success(42)
        val error: Resource<Int> = Resource.Error("Error")
        
        // When & Then
        assertTrue(loading is Resource.Loading && loading !is Resource.Success && loading !is Resource.Error)
        assertTrue(success is Resource.Success && success !is Resource.Loading && success !is Resource.Error)
        assertTrue(error is Resource.Error && error !is Resource.Loading && error !is Resource.Success)
    }

    @Test
    fun `Resource Success with null data should be handled`() {
        // Given & When
        val successWithNull: Resource<String?> = Resource.Success<String?>(null)
        
        // Then
        assertTrue(successWithNull is Resource.Success)
        assertNull((successWithNull as Resource.Success).data)
    }

    @Test
    fun `Resource Error with empty message should be handled`() {
        // Given & When
        val errorWithEmptyMessage = Resource.Error("")
        
        // Then
        assertTrue(errorWithEmptyMessage is Resource.Error)
        assertEquals("", errorWithEmptyMessage.message)
    }

    @Test
    fun `isSuccess extension should work correctly`() {
        // Given
        val success = Resource.Success("data")
        val loading: Resource<String> = Resource.Loading
        val error: Resource<String> = Resource.Error("error")
        
        // When & Then
        assertTrue(success.isSuccess())
        assertFalse(loading.isSuccess())
        assertFalse(error.isSuccess())
    }

    @Test
    fun `getDataOrNull should return data for Success`() {
        // Given
        val success = Resource.Success("test data")
        
        // When
        val data = success.getDataOrNull()
        
        // Then
        assertNotNull(data)
        assertEquals("test data", data)
    }

    @Test
    fun `getDataOrNull should return null for Loading`() {
        // Given
        val loading: Resource<String> = Resource.Loading
        
        // When
        val data = loading.getDataOrNull()
        
        // Then
        assertNull(data)
    }

    @Test
    fun `getDataOrNull should return null for Error`() {
        // Given
        val error: Resource<String> = Resource.Error("error")
        
        // When
        val data = error.getDataOrNull()
        
        // Then
        assertNull(data)
    }

    @Test
    fun `Resource Success can contain complex objects`() {
        // Given
        data class User(val id: String, val name: String)
        val user = User("123", "Test User")
        
        // When
        val success: Resource<User> = Resource.Success(user)
        
        // Then
        assertTrue(success is Resource.Success)
        assertEquals("123", (success as Resource.Success).data.id)
        assertEquals("Test User", success.data.name)
    }

    @Test
    fun `Resource Success can contain list data`() {
        // Given
        val dataList = listOf(1, 2, 3, 4, 5)
        
        // When
        val success: Resource<List<Int>> = Resource.Success(dataList)
        
        // Then
        assertTrue(success is Resource.Success)
        assertEquals(5, (success as Resource.Success).data.size)
        assertTrue(success.data.contains(3))
    }

    @Test
    fun `Resource Error can contain detailed error information`() {
        // Given
        val detailedError = "HTTP 404: Not Found - Resource /api/users/123 does not exist"
        
        // When
        val error = Resource.Error(detailedError)
        
        // Then
        assertTrue(error is Resource.Error)
        assertTrue(error.message.contains("404"))
        assertTrue(error.message.contains("Not Found"))
    }

    @Test
    fun `Resource Loading state should be singleton`() {
        // Given & When
        val loading1: Resource<String> = Resource.Loading
        val loading2: Resource<Int> = Resource.Loading
        
        // Then
        assertTrue(loading1 is Resource.Loading)
        assertTrue(loading2 is Resource.Loading)
        assertSame(loading1, loading2) // Same singleton object
    }

    @Test
    fun `Resource pattern can represent state transitions`() {
        // Given
        var state: Resource<String> = Resource.Loading
        
        // When - simulate loading
        assertTrue(state is Resource.Loading)
        
        // When - simulate success
        state = Resource.Success("Loaded data")
        assertTrue(state is Resource.Success)
        assertEquals("Loaded data", (state as Resource.Success).data)
        
        // When - simulate error
        state = Resource.Error("Failed to load")
        assertTrue(state is Resource.Error)
        assertEquals("Failed to load", (state as Resource.Error).message)
    }

    @Test
    fun `Resource Success with map data should be accessible`() {
        // Given
        val mapData = mapOf("key1" to "value1", "key2" to "value2")
        
        // When
        val success: Resource<Map<String, String>> = Resource.Success(mapData)
        
        // Then
        assertTrue(success is Resource.Success)
        assertEquals("value1", (success as Resource.Success).data["key1"])
        assertEquals(2, success.data.size)
    }

    @Test
    fun `Resource Error with throwable should store exception`() {
        // Given
        val exception = RuntimeException("Test exception")
        val errorMessage = "An error occurred"
        
        // When
        val error = Resource.Error(errorMessage, exception)
        
        // Then
        assertTrue(error is Resource.Error)
        assertEquals(errorMessage, error.message)
        assertNotNull(error.throwable)
        assertEquals("Test exception", error.throwable?.message)
    }

    @Test
    fun `Resource Success with empty list should be valid`() {
        // Given & When
        val success: Resource<List<String>> = Resource.Success(emptyList<String>())
        
        // Then
        assertTrue(success is Resource.Success)
        assertTrue((success as Resource.Success).data.isEmpty())
        assertEquals(0, success.data.size)
    }

    @Test
    fun `Multiple Resource Success instances with same data type`() {
        // Given
        val success1 = Resource.Success("data1")
        val success2 = Resource.Success("data2")
        
        // Then
        assertTrue(success1 is Resource.Success)
        assertTrue(success2 is Resource.Success)
        assertNotEquals((success1 as Resource.Success).data, (success2 as Resource.Success).data)
    }
}

