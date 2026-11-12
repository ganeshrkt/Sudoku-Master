package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Primary Brand Colors
val BluePrimary = Color(0xFF2563EB)
val BlueSecondary = Color(0xFF3B82F6)
val BlueTertiary = Color(0xFF60A5FA)

val SkyPrimary = Color(0xFF38BDF8)
val SkySecondary = Color(0xFF0284C7)
val SkyTertiary = Color(0xFF7DD3FC)

// Light Palette
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightOnBackground = Color(0xFF0F172A)
val LightOnSurface = Color(0xFF1E293B)
val LightOutline = Color(0xFFCBD5E1)

// Dark Palette (Slate)
val DarkBackground = Color(0xFF0F172A)
val DarkSurface = Color(0xFF1E293B)
val DarkSurfaceVariant = Color(0xFF334155)
val DarkOnBackground = Color(0xFFF8FAFC)
val DarkOnSurface = Color(0xFFF1F5F9)
val DarkOutline = Color(0xFF475569)

// AMOLED Palette (True Black)
val AmoledBackground = Color(0xFF000000)
val AmoledSurface = Color(0xFF0D0D0D)
val AmoledSurfaceVariant = Color(0xFF1A1A1A)
val AmoledOnBackground = Color(0xFFFFFFFF)
val AmoledOnSurface = Color(0xFFF5F5F5)
val AmoledOutline = Color(0xFF333333)

// Difficulty Colors
val DifficultyEasy = Color(0xFF10B981) // Emerald Green
val DifficultyMedium = Color(0xFFF59E0B) // Amber
val DifficultyHard = Color(0xFFEF4444) // Rose Red

// Board Specific Theme Colors
@Immutable
data class SudokuBoardColors(
    val boardBackground: Color,
    val cellBackground: Color,
    val selectedCell: Color,
    val relatedCell: Color,
    val sameNumberCell: Color,
    val errorCell: Color,
    val cellBorderThin: Color,
    val cellBorderThick: Color,
    val givenNumberColor: Color,
    val userNumberColor: Color,
    val errorNumberColor: Color,
    val noteColor: Color,
    val numberPadBackground: Color,
    val numberPadText: Color
)

val LightBoardColors = SudokuBoardColors(
    boardBackground = Color(0xFFFFFFFF),
    cellBackground = Color(0xFFFFFFFF),
    selectedCell = Color(0xFFBFDBFE), // Soft blue highlight
    relatedCell = Color(0xFFEFF6FF), // Very soft row/col/block tint
    sameNumberCell = Color(0xFFDBEAFE),
    errorCell = Color(0xFFFEE2E2),
    cellBorderThin = Color(0xFFE2E8F0),
    cellBorderThick = Color(0xFF334155),
    givenNumberColor = Color(0xFF0F172A),
    userNumberColor = Color(0xFF2563EB),
    errorNumberColor = Color(0xFFDC2626),
    noteColor = Color(0xFF64748B),
    numberPadBackground = Color(0xFFF1F5F9),
    numberPadText = Color(0xFF0F172A)
)

val DarkBoardColors = SudokuBoardColors(
    boardBackground = Color(0xFF1E293B),
    cellBackground = Color(0xFF1E293B),
    selectedCell = Color(0xFF1E3A8A),
    relatedCell = Color(0xFF172554),
    sameNumberCell = Color(0xFF1E293B),
    errorCell = Color(0xFF450A0A),
    cellBorderThin = Color(0xFF334155),
    cellBorderThick = Color(0xFF94A3B8),
    givenNumberColor = Color(0xFFF8FAFC),
    userNumberColor = Color(0xFF38BDF8),
    errorNumberColor = Color(0xFFF87171),
    noteColor = Color(0xFF94A3B8),
    numberPadBackground = Color(0xFF334155),
    numberPadText = Color(0xFFF8FAFC)
)

val AmoledBoardColors = SudokuBoardColors(
    boardBackground = Color(0xFF000000),
    cellBackground = Color(0xFF000000),
    selectedCell = Color(0xFF172554),
    relatedCell = Color(0xFF0F172A),
    sameNumberCell = Color(0xFF1E293B),
    errorCell = Color(0xFF450A0A),
    cellBorderThin = Color(0xFF262626),
    cellBorderThick = Color(0xFF525252),
    givenNumberColor = Color(0xFFFFFFFF),
    userNumberColor = Color(0xFF60A5FA),
    errorNumberColor = Color(0xFFEF4444),
    noteColor = Color(0xFFA3A3A3),
    numberPadBackground = Color(0xFF171717),
    numberPadText = Color(0xFFFFFFFF)
)
