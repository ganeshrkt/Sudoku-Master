package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.CellData
import com.example.ui.theme.SudokuThemeTokens

@Composable
fun SudokuBoardView(
    cells: List<CellData>,
    selectedCellIndex: Int?,
    onCellClick: (Int) -> Unit,
    highlightSameNumbers: Boolean = true,
    highlightDuplicates: Boolean = true,
    modifier: Modifier = Modifier
) {
    val boardColors = SudokuThemeTokens.boardColors
    val selectedCell = selectedCellIndex?.let { if (it in cells.indices) cells[it] else null }
    val selectedValue = selectedCell?.value?.takeIf { it > 0 }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(boardColors.boardBackground)
            .border(2.5.dp, boardColors.cellBorderThick, RoundedCornerShape(12.dp))
            .testTag("sudoku_board")
    ) {
        val totalSize = maxWidth

        Column(modifier = Modifier.fillMaxSize()) {
            for (blockRow in 0 until 3) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    for (blockCol in 0 until 3) {
                        val blockIndex = blockRow * 3 + blockCol
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .border(
                                    width = 1.5.dp,
                                    color = boardColors.cellBorderThick
                                )
                        ) {
                            // 3x3 cells inside this block
                            Column(modifier = Modifier.fillMaxSize()) {
                                for (innerRow in 0 until 3) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                    ) {
                                        for (innerCol in 0 until 3) {
                                            val row = blockRow * 3 + innerRow
                                            val col = blockCol * 3 + innerCol
                                            val cellIndex = row * 9 + col
                                            val cell = if (cellIndex in cells.indices) cells[cellIndex] else CellData(cellIndex, row, col)

                                            val isSelected = cellIndex == selectedCellIndex
                                            val isRelated = selectedCell != null && (
                                                    selectedCell.row == cell.row ||
                                                            selectedCell.col == cell.col ||
                                                            selectedCell.blockIndex == cell.blockIndex
                                                    )
                                            val isSameValue = highlightSameNumbers &&
                                                    selectedValue != null &&
                                                    cell.value == selectedValue &&
                                                    cell.value > 0

                                            val cellBg = when {
                                                cell.isError && highlightDuplicates -> boardColors.errorCell
                                                isSelected -> boardColors.selectedCell
                                                isSameValue -> boardColors.sameNumberCell
                                                isRelated -> boardColors.relatedCell
                                                else -> boardColors.cellBackground
                                            }

                                            SudokuCell(
                                                cell = cell,
                                                backgroundColor = cellBg,
                                                isSelected = isSelected,
                                                borderColor = boardColors.cellBorderThin,
                                                onClick = { onCellClick(cellIndex) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxSize()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SudokuCell(
    cell: CellData,
    backgroundColor: Color,
    isSelected: Boolean,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val boardColors = SudokuThemeTokens.boardColors
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .background(backgroundColor)
            .border(0.5.dp, borderColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("cell_${cell.row}_${cell.col}"),
        contentAlignment = Alignment.Center
    ) {
        if (cell.value > 0) {
            val textColor = when {
                cell.isError -> boardColors.errorNumberColor
                cell.isGiven -> boardColors.givenNumberColor
                else -> boardColors.userNumberColor
            }
            Text(
                text = cell.value.toString(),
                color = textColor,
                fontSize = 24.sp,
                fontWeight = if (cell.isGiven) FontWeight.Bold else FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        } else if (cell.notes.isNotEmpty()) {
            // Render 3x3 notes grid inside cell
            PencilNotesGrid(
                notes = cell.notes,
                color = boardColors.noteColor,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp)
            )
        }
    }
}

@Composable
private fun PencilNotesGrid(
    notes: Set<Int>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        for (r in 0 until 3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (c in 0 until 3) {
                    val digit = r * 3 + c + 1
                    Text(
                        text = if (notes.contains(digit)) digit.toString() else "",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = color,
                        textAlign = TextAlign.Center,
                        lineHeight = 9.sp
                    )
                }
            }
        }
    }
}
