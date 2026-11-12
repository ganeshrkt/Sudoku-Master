package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_records")
data class GameRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val difficulty: String,
    val timeSeconds: Int,
    val mistakes: Int,
    val hintsUsed: Int,
    val dateMillis: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = true // true = won, false = abandoned/game over
)
