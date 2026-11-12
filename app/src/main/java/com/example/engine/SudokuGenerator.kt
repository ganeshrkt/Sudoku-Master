package com.example.engine

import com.example.data.model.Difficulty
import kotlin.random.Random

data class GeneratedPuzzle(
    val initialBoard: String, // 81 chars, '0'..'9'
    val solutionBoard: String, // 81 chars, '1'..'9'
    val difficulty: Difficulty
)

object SudokuGenerator {

    private const val GRID_SIZE = 9
    private const val SUBGRID_SIZE = 3

    fun generate(difficulty: Difficulty): GeneratedPuzzle {
        // Step 1: Create a solved 9x9 board
        val solvedBoard = IntArray(81) { 0 }
        fillDiagonalBlocks(solvedBoard)
        solve(solvedBoard)

        val solutionString = solvedBoard.joinToString("")

        // Step 2: Remove cells to match desired difficulty
        val puzzleBoard = solvedBoard.copyOf()
        val targetClues = difficulty.cluesCount

        val cellIndices = (0 until 81).shuffled(Random(System.currentTimeMillis())).toMutableList()
        var currentClues = 81

        for (index in cellIndices) {
            if (currentClues <= targetClues) break

            val backup = puzzleBoard[index]
            puzzleBoard[index] = 0

            // Check if still has unique solution
            val countSolutions = countSolutions(puzzleBoard.copyOf(), 2)
            if (countSolutions == 1) {
                currentClues--
            } else {
                // Revert
                puzzleBoard[index] = backup
            }
        }

        val initialString = puzzleBoard.joinToString("")
        return GeneratedPuzzle(
            initialBoard = initialString,
            solutionBoard = solutionString,
            difficulty = difficulty
        )
    }

    private fun fillDiagonalBlocks(board: IntArray) {
        for (i in 0 until GRID_SIZE step SUBGRID_SIZE) {
            fillBox(board, i, i)
        }
    }

    private fun fillBox(board: IntArray, row: Int, col: Int) {
        val numbers = (1..9).shuffled(Random(System.currentTimeMillis())).iterator()
        for (r in 0 until SUBGRID_SIZE) {
            for (c in 0 until SUBGRID_SIZE) {
                board[(row + r) * GRID_SIZE + (col + c)] = numbers.next()
            }
        }
    }

    fun isValidPlacement(board: IntArray, row: Int, col: Int, num: Int): Boolean {
        // Check row
        for (c in 0 until GRID_SIZE) {
            if (board[row * GRID_SIZE + c] == num) return false
        }
        // Check column
        for (r in 0 until GRID_SIZE) {
            if (board[r * GRID_SIZE + col] == num) return false
        }
        // Check 3x3 block
        val startRow = (row / SUBGRID_SIZE) * SUBGRID_SIZE
        val startCol = (col / SUBGRID_SIZE) * SUBGRID_SIZE
        for (r in 0 until SUBGRID_SIZE) {
            for (c in 0 until SUBGRID_SIZE) {
                if (board[(startRow + r) * GRID_SIZE + (startCol + c)] == num) return false
            }
        }
        return true
    }

    private fun solve(board: IntArray): Boolean {
        for (r in 0 until GRID_SIZE) {
            for (c in 0 until GRID_SIZE) {
                val idx = r * GRID_SIZE + c
                if (board[idx] == 0) {
                    val numbers = (1..9).shuffled(Random(System.currentTimeMillis()))
                    for (num in numbers) {
                        if (isValidPlacement(board, r, c, num)) {
                            board[idx] = num
                            if (solve(board)) return true
                            board[idx] = 0
                        }
                    }
                    return false
                }
            }
        }
        return true
    }

    private fun countSolutions(board: IntArray, limit: Int): Int {
        var count = 0

        fun search(): Boolean {
            var emptyRow = -1
            var emptyCol = -1
            for (r in 0 until GRID_SIZE) {
                for (c in 0 until GRID_SIZE) {
                    if (board[r * GRID_SIZE + c] == 0) {
                        emptyRow = r
                        emptyCol = c
                        break
                    }
                }
                if (emptyRow != -1) break
            }

            if (emptyRow == -1) {
                count++
                return count >= limit
            }

            for (num in 1..9) {
                if (isValidPlacement(board, emptyRow, emptyCol, num)) {
                    board[emptyRow * GRID_SIZE + emptyCol] = num
                    if (search()) return true
                    board[emptyRow * GRID_SIZE + emptyCol] = 0
                }
            }
            return false
        }

        search()
        return count
    }

    // Hint generator: finds an empty cell with candidate or reveals correct solution
    fun findHint(currentBoard: IntArray, solutionBoard: IntArray): HintResult? {
        // Find cell with fewest candidates or first mistake
        // 1. Check for errors
        for (i in 0 until 81) {
            if (currentBoard[i] != 0 && currentBoard[i] != solutionBoard[i]) {
                val row = i / 9
                val col = i % 9
                return HintResult(
                    index = i,
                    row = row,
                    col = col,
                    correctValue = solutionBoard[i],
                    explanation = "Cell at Row ${row + 1}, Col ${col + 1} has an incorrect number. The correct number is ${solutionBoard[i]}."
                )
            }
        }

        // 2. Find naked single candidate
        var bestIndex = -1
        var minCandidates = 10
        for (i in 0 until 81) {
            if (currentBoard[i] == 0) {
                val row = i / 9
                val col = i % 9
                val candidates = (1..9).filter { isValidPlacement(currentBoard, row, col, it) }
                if (candidates.size == 1) {
                    return HintResult(
                        index = i,
                        row = row,
                        col = col,
                        correctValue = candidates.first(),
                        explanation = "Naked Single: Only ${candidates.first()} can fit in Row ${row + 1}, Col ${col + 1}!"
                    )
                }
                if (candidates.size in 2 until minCandidates) {
                    minCandidates = candidates.size
                    bestIndex = i
                }
            }
        }

        if (bestIndex != -1) {
            val row = bestIndex / 9
            val col = bestIndex % 9
            return HintResult(
                index = bestIndex,
                row = row,
                col = col,
                correctValue = solutionBoard[bestIndex],
                explanation = "Logical Hint: Try placing ${solutionBoard[bestIndex]} at Row ${row + 1}, Col ${col + 1}."
            )
        }

        return null
    }
}

data class HintResult(
    val index: Int,
    val row: Int,
    val col: Int,
    val correctValue: Int,
    val explanation: String
)
