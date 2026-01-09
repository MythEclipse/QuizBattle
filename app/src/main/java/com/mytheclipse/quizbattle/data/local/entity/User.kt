package com.mytheclipse.quizbattle.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val email: String,
    val password: String,
    val points: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val totalGames: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isLoggedIn: Boolean = false,
    val image: String? = null
)

