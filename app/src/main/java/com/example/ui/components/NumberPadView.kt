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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
    countsMap: Map<Int, Int>, // digit -> placed count (out of 9)
    activeDigit: Int?, // if in digit-first mode
    isDigitFirstMode: Boolean,
    onDigitClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val boardColors = SudokuThemeTokens.boardColors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (digit in 1..9) {
            val placedCount = countsMap[digit] ?: 0
            val remaining = maxOf(0, 9 - placedCount)
            val isCompleted = remaining == 0
            val isActive = isDigitFirstMode && activeDigit == digit

            val bgColor = when {
                isActive -> MaterialTheme.colorScheme.primary
                isCompleted -> boardColors.numberPadBackground.copy(alpha = 0.4f)
                else -> boardColors.numberPadBackground
            }

            val textColor = when {
                isActive -> MaterialTheme.colorScheme.onPrimary
                isCompleted -> boardColors.numberPadText.copy(alpha = 0.35f)
                else -> boardColors.numberPadText
            }

            val borderColor = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent

            Box(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor)
                    .border(1.5.dp, borderColor, RoundedCornerShape(10.dp))
                    .clickable(enabled = !isCompleted || isDigitFirstMode) {
                        onDigitClick(digit)
                    }
                    .testTag("num_pad_$digit"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = digit.toString(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = if (isActive) MaterialTheme.colorScheme.onPrimary else boardColors.noteColor,
                            modifier = Modifier.height(11.dp)
                        )
                    } else {
                        Text(
                            text = remaining.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isActive) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else boardColors.noteColor
                        )
                    }
                }
            }
        }
    }
}
