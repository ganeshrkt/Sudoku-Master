package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_settings")
data class GameSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK", "AMOLED"
    val maxMistakes: Int = 3, // 3 or 0 (unlimited)
    val highlightDuplicates: Boolean = true,
    val highlightSameNumbers: Boolean = true,
    val autoClearNotes: Boolean = true,
    val digitFirstMode: Boolean = false,
    val showTimer: Boolean = true,
    val vibrationEnabled: Boolean = true
)
