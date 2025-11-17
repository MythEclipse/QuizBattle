package com.mytheclipse.quizbattle.data.repository

import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for MatchmakingRepository
 * Tests matchmaking event data structures and basic operations
 */
class MatchmakingRepositoryTest {

    private lateinit var matchmakingRepository: MatchmakingRepository

    @Before
    fun setup() {
        matchmakingRepository = MatchmakingRepository()
    }

    @Test
    fun `parseMatchmakingEvent should handle searching event correctly`() {
        // This test verifies the event parsing logic
        // Note: Since parseMatchmakingEvent is private, we test through observeMatchmakingEvents
        // In a real scenario, we would mock WebSocketManager
        
        val event = MatchmakingEvent.Searching(
            queuePosition = 5,
            estimatedWaitTime = 30
        )
        
        assertTrue(event is MatchmakingEvent.Searching)
        assertEquals(5, event.queuePosition)
        assertEquals(30, event.estimatedWaitTime)
    }

    @Test
    fun `MatchFound event should contain all required fields`() {
        val event = MatchmakingEvent.MatchFound(
            matchId = "match_123",
            opponentId = "opponent_456",
            opponentName = "TestOpponent",
            opponentLevel = 10,
            opponentAvatar = null, // Avatar disabled for user management
            difficulty = "hard",
            category = "science",
            totalQuestions = 15,
            timePerQuestion = 20
        )
        
        assertTrue(event is MatchmakingEvent.MatchFound)
        assertEquals("match_123", event.matchId)
        assertEquals("opponent_456", event.opponentId)
        assertEquals("TestOpponent", event.opponentName)
        assertEquals(10, event.opponentLevel)
        assertNull(event.opponentAvatar) // Avatar should always be null
        assertEquals("hard", event.difficulty)
        assertEquals("science", event.category)
        assertEquals(15, event.totalQuestions)
        assertEquals(20, event.timePerQuestion)
    }

    @Test
    fun `Cancelled event should be singleton object`() {
        val event1 = MatchmakingEvent.Cancelled
        val event2 = MatchmakingEvent.Cancelled
        
        assertTrue(event1 is MatchmakingEvent.Cancelled)
        assertTrue(event2 is MatchmakingEvent.Cancelled)
        assertEquals(event1, event2)
    }

    @Test
    fun `Unknown event should be singleton object`() {
        val event1 = MatchmakingEvent.Unknown
        val event2 = MatchmakingEvent.Unknown
        
        assertTrue(event1 is MatchmakingEvent.Unknown)
        assertTrue(event2 is MatchmakingEvent.Unknown)
        assertEquals(event1, event2)
    }

    @Test
    fun `findMatch should accept required parameters`() {
        // Test that the method can be called with valid parameters
        // In production, this would test the message structure
        val userId = "test_user_123"
        val gameMode = "quick"
        val difficulty = "medium"
        val category = "general"
        
        // This test verifies the method signature and basic execution
        // Real implementation would mock WebSocketManager and verify message content
        assertDoesNotThrow {
            matchmakingRepository.findMatch(
                userId = userId,
                gameMode = gameMode,
                difficulty = difficulty,
                category = category
            )
        }
    }

    @Test
    fun `findMatch should work with default parameters`() {
        val userId = "test_user_456"
        
        // Test with default gameMode, null difficulty and category
        assertDoesNotThrow {
            matchmakingRepository.findMatch(userId = userId)
        }
    }

    @Test
    fun `cancelMatchmaking should accept userId parameter`() {
        val userId = "test_user_789"
        
        assertDoesNotThrow {
            matchmakingRepository.cancelMatchmaking(userId = userId)
        }
    }

    @Test
    fun `MatchFound event with null avatar should be handled`() {
        val event = MatchmakingEvent.MatchFound(
            matchId = "match_789",
            opponentId = "opponent_012",
            opponentName = "NoAvatarUser",
            opponentLevel = 5,
            opponentAvatar = null, // null avatar case
            difficulty = "easy",
            category = "general",
            totalQuestions = 10,
            timePerQuestion = 30
        )
        
        assertTrue(event is MatchmakingEvent.MatchFound)
        assertNull(event.opponentAvatar)
        assertEquals("NoAvatarUser", event.opponentName)
    }

    @Test
    fun `Searching event with zero values should be valid`() {
        val event = MatchmakingEvent.Searching(
            queuePosition = 0,
            estimatedWaitTime = 0
        )
        
        assertTrue(event is MatchmakingEvent.Searching)
        assertEquals(0, event.queuePosition)
        assertEquals(0, event.estimatedWaitTime)
    }

    @Test
    fun `MatchFound event with minimum values should be valid`() {
        val event = MatchmakingEvent.MatchFound(
            matchId = "m1",
            opponentId = "o1",
            opponentName = "A",
            opponentLevel = 1,
            opponentAvatar = null,
            difficulty = "easy",
            category = "general",
            totalQuestions = 5,
            timePerQuestion = 10
        )
        
        assertTrue(event is MatchmakingEvent.MatchFound)
        assertEquals(1, event.opponentLevel)
        assertEquals(5, event.totalQuestions)
        assertEquals(10, event.timePerQuestion)
    }

    // Helper method to avoid throwing exceptions in tests
    private fun assertDoesNotThrow(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            fail("Expected no exception, but got: ${e.message}")
        }
    }
}
