package com.mytheclipse.quizbattle.viewmodel

import com.mytheclipse.quizbattle.data.repository.NotificationInfo
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for NotificationViewModel state management
 */
class NotificationViewModelTest {

    @Test
    fun `NotificationState should have correct defaults`() {
        // Given & When
        val state = NotificationState()
        
        // Then
        assertTrue(state.notifications.isEmpty())
        assertEquals(0, state.unreadCount)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }
    
    @Test
    fun `NotificationState copy should update only specified fields`() {
        // Given
        val original = NotificationState()
        
        // When
        val withLoading = original.copy(isLoading = true)
        val withError = original.copy(error = "Network error")
        
        // Then
        assertTrue(withLoading.isLoading)
        assertEquals("Network error", withError.error)
        assertTrue(withError.notifications.isEmpty())
    }
    
    @Test
    fun `NotificationInfo should calculate isRead correctly`() {
        // Given
        val unreadNotification = NotificationInfo(
            id = "1",
            type = "battle_invite",
            title = "Battle Invite",
            message = "You were invited to battle",
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        
        val readNotification = NotificationInfo(
            id = "2",
            type = "reward",
            title = "Reward Collected",
            message = "You got 100 coins",
            timestamp = System.currentTimeMillis(),
            isRead = true
        )
        
        // Then
        assertFalse(unreadNotification.isRead)
        assertTrue(readNotification.isRead)
    }
    
    @Test
    fun `Unread count should be calculated correctly`() {
        // Given
        val notifications = listOf(
            NotificationInfo("1", "invite", "Title", "Msg", 100L, false),
            NotificationInfo("2", "reward", "Title", "Msg", 200L, true),
            NotificationInfo("3", "system", "Title", "Msg", 300L, false),
            NotificationInfo("4", "chat", "Title", "Msg", 400L, true)
        )
        
        // When
        val unreadCount = notifications.count { !it.isRead }
        
        // Then
        assertEquals(2, unreadCount)
    }
    
    @Test
    fun `Notification sorting by timestamp should work`() {
        // Given
        val older = NotificationInfo("1", "type", "Old", "Msg", 1000L, false)
        val newer = NotificationInfo("2", "type", "New", "Msg", 2000L, false)
        val newest = NotificationInfo("3", "type", "Newest", "Msg", 3000L, false)
        
        val unsorted = listOf(newer, oldest, newest)
        
        // When
        val sortedDesc = unsorted.sortedByDescending { it.timestamp }
        
        // Then
        assertEquals("Newest", sortedDesc[0].title)
        assertEquals("New", sortedDesc[1].title)
        assertEquals("Old", sortedDesc[2].title)
    }
    
    private val oldest = NotificationInfo("1", "type", "Old", "Msg", 1000L, false)
}
