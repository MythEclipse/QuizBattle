package com.mytheclipse.quizbattle.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a friend relationship
 */
@Entity(tableName = "friends")
data class Friend(
    @PrimaryKey
    val id: String,
    val friendId: String,
    val friendName: String,
    val friendEmail: String? = null,
    val friendAvatarUrl: String? = null,
    val points: Int = 0,
    val wins: Int = 0,
    val status: FriendStatus = FriendStatus.ACCEPTED,
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val addedAt: Long = System.currentTimeMillis()
)

enum class FriendStatus {
    PENDING_SENT,     // Request sent, waiting for response
    PENDING_RECEIVED, // Request received, waiting for user action
    ACCEPTED,         // Friends
    REJECTED          // Request was rejected
}
