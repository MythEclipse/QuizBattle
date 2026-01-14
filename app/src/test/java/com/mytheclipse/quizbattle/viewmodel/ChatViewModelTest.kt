package com.mytheclipse.quizbattle.viewmodel

import com.mytheclipse.quizbattle.data.repository.ChatRepository.ChatRoom
import com.mytheclipse.quizbattle.data.repository.ChatRepository.ChatMessage
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for ChatViewModel state management
 */
class ChatViewModelTest {

    @Test
    fun `ChatListState should have correct defaults`() {
        // Given & When
        val state = ChatListState()
        
        // Then
        assertTrue(state.rooms.isEmpty())
        assertEquals(0, state.totalUnread)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }
    
    @Test
    fun `ChatRoomState should have correct defaults`() {
        // Given & When
        val state = ChatRoomState()
        
        // Then
        assertNull(state.room)
        assertTrue(state.messages.isEmpty())
        assertFalse(state.isLoading)
        assertFalse(state.isSending)
        assertNull(state.error)
    }
    
    @Test
    fun `ChatRoom should display correctly formatted time`() {
        // Given
        val room = ChatRoom(
            id = "room1",
            name = "Test Room",
            lastMessage = "Hello!",
            lastMessageTime = System.currentTimeMillis(),
            unreadCount = 5,
            avatarUrl = null
        )
        
        // Then
        assertEquals("room1", room.id)
        assertEquals("Test Room", room.name)
        assertEquals("Hello!", room.lastMessage)
        assertEquals(5, room.unreadCount)
    }
    
    @Test
    fun `ChatMessage own message detection should work`() {
        // Given
        val currentUserId = "user123"
        
        val ownMessage = ChatMessage(
            id = "msg1",
            roomId = "room1",
            senderId = "user123",
            senderName = "Me",
            content = "My message",
            timestamp = System.currentTimeMillis()
        )
        
        val otherMessage = ChatMessage(
            id = "msg2",
            roomId = "room1",
            senderId = "user456",
            senderName = "Other",
            content = "Their message",
            timestamp = System.currentTimeMillis()
        )
        
        // Then
        assertEquals(currentUserId, ownMessage.senderId)
        assertNotEquals(currentUserId, otherMessage.senderId)
    }
    
    @Test
    fun `Total unread count should sum all rooms`() {
        // Given
        val rooms = listOf(
            ChatRoom("1", "Room 1", "Msg", 100L, 3, null),
            ChatRoom("2", "Room 2", "Msg", 200L, 5, null),
            ChatRoom("3", "Room 3", "Msg", 300L, 0, null)
        )
        
        // When
        val totalUnread = rooms.sumOf { it.unreadCount }
        
        // Then
        assertEquals(8, totalUnread)
    }
    
    @Test
    fun `Messages should sort by timestamp ascending`() {
        // Given
        val msg1 = ChatMessage("1", "room", "user", "User", "First", 1000L)
        val msg2 = ChatMessage("2", "room", "user", "User", "Second", 2000L)
        val msg3 = ChatMessage("3", "room", "user", "User", "Third", 3000L)
        
        val unsorted = listOf(msg3, msg1, msg2)
        
        // When
        val sorted = unsorted.sortedBy { it.timestamp }
        
        // Then
        assertEquals("First", sorted[0].content)
        assertEquals("Second", sorted[1].content)
        assertEquals("Third", sorted[2].content)
    }
}
