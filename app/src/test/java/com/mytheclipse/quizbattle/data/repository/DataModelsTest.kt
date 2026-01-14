package com.mytheclipse.quizbattle.data.repository

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for DataModels classes
 */
class DataModelsTest {

    @Test
    fun `LeaderboardEntry should calculate data correctly`() {
        // Given & When
        val entry = DataModels.LeaderboardEntry(
            userId = "user1",
            userName = "Player1",
            score = 1000,
            wins = 10,
            losses = 5,
            mmr = 1500,
            isCurrentUser = false
        )
        
        // Then
        assertEquals("user1", entry.userId)
        assertEquals("Player1", entry.userName)
        assertEquals(1000, entry.score)
        assertEquals(10, entry.wins)
        assertEquals(5, entry.losses)
        assertEquals(1500, entry.mmr)
        assertFalse(entry.isCurrentUser)
    }
    
    @Test
    fun `LeaderboardEntry isCurrentUser default should be false`() {
        // Given & When
        val entry = DataModels.LeaderboardEntry(
            userId = "user2",
            userName = "Player2",
            score = 500,
            wins = 5,
            losses = 10,
            mmr = 1200
        )
        
        // Then
        assertFalse(entry.isCurrentUser)
    }
    
    @Test
    fun `NotificationInfo should store data correctly`() {
        // Given & When
        val notification = DataModels.NotificationInfo(
            notificationId = "n1",
            type = "battle_invite",
            title = "Battle Invitation",
            message = "Player X wants to battle!",
            isRead = false,
            createdAt = System.currentTimeMillis()
        )
        
        // Then
        assertEquals("n1", notification.notificationId)
        assertEquals("battle_invite", notification.type)
        assertEquals("Battle Invitation", notification.title)
        assertFalse(notification.isRead)
    }
    
    @Test
    fun `LobbyInfo should track player counts`() {
        // Given & When
        val lobby = DataModels.LobbyInfo(
            lobbyId = "lobby1",
            lobbyName = "Cool Room",
            hostId = "host1",
            hostName = "HostPlayer",
            currentPlayers = 2,
            maxPlayers = 4,
            isPrivate = false,
            status = "waiting"
        )
        
        // Then
        assertEquals(2, lobby.currentPlayers)
        assertEquals(4, lobby.maxPlayers)
        assertFalse(lobby.isPrivate)
        assertEquals("waiting", lobby.status)
    }
    
    @Test
    fun `PlayerInfo should track ready status`() {
        // Given
        val hostPlayer = DataModels.PlayerInfo(
            userId = "user1",
            userName = "HostPlayer",
            isHost = true,
            isReady = true
        )
        
        val guestPlayer = DataModels.PlayerInfo(
            userId = "user2",
            userName = "GuestPlayer",
            isHost = false,
            isReady = false
        )
        
        // Then
        assertTrue(hostPlayer.isHost)
        assertTrue(hostPlayer.isReady)
        assertFalse(guestPlayer.isHost)
        assertFalse(guestPlayer.isReady)
    }
    
    @Test
    fun `Post should track like status`() {
        // Given & When
        val likedPost = DataModels.Post(
            postId = "p1",
            userId = "u1",
            userName = "User1",
            content = "Hello world!",
            imageUrl = null,
            likesCount = 5,
            commentsCount = 2,
            isLikedByUser = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        // Then
        assertEquals(5, likedPost.likesCount)
        assertEquals(2, likedPost.commentsCount)
        assertTrue(likedPost.isLikedByUser)
        assertNull(likedPost.imageUrl)
    }
}
