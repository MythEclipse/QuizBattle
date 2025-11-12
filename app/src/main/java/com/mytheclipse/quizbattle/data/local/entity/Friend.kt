package com.mytheclipse.quizbattle.data.local.entity


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends")
data class Friend(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long, // The user who owns this friend list
    val friendUsername: String,
    val friendEmail: String,
    val friendPoints: Int = 0,
    val status: String = "pending", // "pending", "accepted", "blocked"
    val addedAt: Long = System.currentTimeMillis()
)
