package com.mytheclipse.quizbattle.data.local.entity


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class Question(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val questionText: String,
    val answer1: String,
    val answer2: String,
    val answer3: String,
    val answer4: String,
    val correctAnswerIndex: Int, // 0-3
    val category: String = "General", // e.g., "Science", "History", "General"
    val difficulty: String = "Medium", // "Easy", "Medium", "Hard"
    val isActive: Boolean = true
)
