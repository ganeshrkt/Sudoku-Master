package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.DifficultyEasy
import com.example.ui.theme.DifficultyHard
import com.example.ui.theme.DifficultyMedium

enum class Difficulty(
    val key: String,
    val displayName: String,
    val cluesCount: Int,
    val color: Color
) {
    EASY("EASY", "Easy", 40, DifficultyEasy),
    MEDIUM("MEDIUM", "Medium", 32, DifficultyMedium),
    DIFFICULT("DIFFICULT", "Difficult", 26, DifficultyHard);

    companion object {
        fun fromKey(key: String): Difficulty {
            return entries.find { it.key.equals(key, ignoreCase = true) } ?: MEDIUM
        }
    }
}
