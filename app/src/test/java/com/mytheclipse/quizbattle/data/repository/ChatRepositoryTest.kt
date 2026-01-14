package com.mytheclipse.quizbattle.data.repository

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for ChatRepository data classes
 */
class ChatRepositoryTest {

    @Test
    fun `ChatRoom should store values correctly`() {
        // Given
        val roomId = "room123"
        val roomName = "General Chat"
        val roomType = "group"
        val lastMessage = "Hello!"
        val unreadCount = 5
        val createdAt = 1234567890L
        
        // When
        val chatRoom = ChatRoom(
            roomId = roomId,
            roomName = roomName,
            roomType = roomType,
            lastMessage = lastMessage,
            unreadCount = unreadCount,
            createdAt = createdAt
        )
        
        // Then
        assertEquals(roomId, chatRoom.roomId)
        assertEquals(roomName, chatRoom.roomName)
        assertEquals(roomType, chatRoom.roomType)
        assertEquals(lastMessage, chatRoom.lastMessage)
        assertEquals(unreadCount, chatRoom.unreadCount)
        assertEquals(createdAt, chatRoom.createdAt)
    }
    
    @Test
    fun `ChatRoom with null lastMessage should work`() {
        // Given & When
        val chatRoom = ChatRoom(
            roomId = "room1",
            roomName = "Empty Room",
            roomType = "private",
            lastMessage = null,
            unreadCount = 0,
            createdAt = 1234567890L
        )
        
        // Then
        assertNull(chatRoom.lastMessage)
    }
    
    @Test
    fun `ChatMessage should store values correctly`() {
        // Given
        val messageId = "msg123"
        val userId = "user456"
        val userName = "John"
        val message = "Hello World!"
        val createdAt = 1234567890L
        
        // When
        val chatMessage = ChatMessage(
            messageId = messageId,
            userId = userId,
            userName = userName,
            message = message,
            createdAt = createdAt
        )
        
        // Then
        assertEquals(messageId, chatMessage.messageId)
        assertEquals(userId, chatMessage.userId)
        assertEquals(userName, chatMessage.userName)
        assertEquals(message, chatMessage.message)
        assertEquals(createdAt, chatMessage.createdAt)
    }
    
    @Test
    fun `ChatMessageEvent GlobalMessage should contain correct data`() {
        // Given & When
        val event = ChatMessageEvent.GlobalMessage(
            messageId = "msg1",
            senderId = "user1",
            senderName = "Alice",
            message = "Hello everyone!",
            timestamp = System.currentTimeMillis()
        )
        
        // Then
        assertEquals("msg1", event.messageId)
        assertEquals("user1", event.senderId)
        assertEquals("Alice", event.senderName)
        assertEquals("Hello everyone!", event.message)
        assertTrue(event.timestamp > 0)
    }
    
    @Test
    fun `ChatMessageEvent PrivateMessage should contain correct data`() {
        // Given & When
        val event = ChatMessageEvent.PrivateMessage(
            messageId = "msg2",
            conversationId = "conv1",
            senderId = "user1",
            senderName = "Bob",
            message = "Hi there!",
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        
        // Then
        assertEquals("msg2", event.messageId)
        assertEquals("conv1", event.conversationId)
        assertFalse(event.isRead)
    }
    
    @Test
    fun `ChatMessageEvent TypingIndicator should track typing status`() {
        // Given & When
        val typingEvent = ChatMessageEvent.TypingIndicator(
            userId = "user1",
            username = "Charlie",
            isTyping = true
        )
        
        val notTypingEvent = ChatMessageEvent.TypingIndicator(
            userId = "user1",
            username = "Charlie",
            isTyping = false
        )
        
        // Then
        assertTrue(typingEvent.isTyping)
        assertFalse(notTypingEvent.isTyping)
    }
}
