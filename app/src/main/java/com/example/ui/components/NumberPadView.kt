package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SudokuThemeTokens

@Composable
fun NumberPadView(
    countsMap: Map<Int, Int>,
    activeDigit: Int?,
    isDigitFirstMode: Boolean,
    onDigitClick: (Int) -> Unit,
    onEraseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val boardColors = SudokuThemeTokens.boardColors
    val buttonShape = RoundedCornerShape(14.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1: 1, 2, 3, 4, 5
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (digit in 1..5) {
                DigitKeyButton(
                    digit = digit,
                    placedCount = countsMap[digit] ?: 0,
                    isActive = isDigitFirstMode && activeDigit == digit,
                    isDigitFirstMode = isDigitFirstMode,
                    shape = buttonShape,
                    boardColors = boardColors,
                    onClick = { onDigitClick(digit) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Row 2: 6, 7, 8, 9, Erase
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (digit in 6..9) {
                DigitKeyButton(
                    digit = digit,
                    placedCount = countsMap[digit] ?: 0,
                    isActive = isDigitFirstMode && activeDigit == digit,
                    isDigitFirstMode = isDigitFirstMode,
                    shape = buttonShape,
                    boardColors = boardColors,
                    onClick = { onDigitClick(digit) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Erase button (10th key in bottom-right)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 56.dp)
                    .height(58.dp)
                    .clip(buttonShape)
                    .background(boardColors.numberPadBackground)
                    .clickable(onClick = onEraseClick)
                    .testTag("num_pad_erase"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Erase",
                    tint = boardColors.numberPadText,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun DigitKeyButton(
    digit: Int,
    placedCount: Int,
    isActive: Boolean,
    isDigitFirstMode: Boolean,
    shape: RoundedCornerShape,
    boardColors: com.example.ui.theme.SudokuBoardColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompleted = placedCount >= 9

    val bgColor = when {
        isActive -> MaterialTheme.colorScheme.primary
        isCompleted -> boardColors.numberPadBackground.copy(alpha = 0.38f)
        else -> boardColors.numberPadBackground
    }

    val textColor = when {
        isActive -> MaterialTheme.colorScheme.onPrimary
        isCompleted -> boardColors.numberPadText.copy(alpha = 0.3f)
        else -> boardColors.numberPadText
    }

    val borderColor = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 56.dp)
            .height(58.dp)
            .clip(shape)
            .background(bgColor)
            .border(1.5.dp, borderColor, shape)
            .clickable(enabled = !isCompleted || isDigitFirstMode, onClick = onClick)
            .testTag("num_pad_$digit"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digit.toString(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}
