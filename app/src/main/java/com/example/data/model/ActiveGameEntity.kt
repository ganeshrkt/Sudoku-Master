package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_game")
data class ActiveGameEntity(
    @PrimaryKey
    val id: Int = 1, // Single active slot
    val difficulty: String,
    val initialBoard: String, // 81 digits (0 for empty)
    val currentBoard: String, // 81 digits
    val solutionBoard: String, // 81 digits
    val notesSerialized: String, // Pipe-separated notes for 81 cells, e.g. "1,2|3||4,5,6"
    val elapsedSeconds: Int,
    val mistakes: Int,
    val hintsUsed: Int,
    val lastPlayedAt: Long = System.currentTimeMillis()
)
