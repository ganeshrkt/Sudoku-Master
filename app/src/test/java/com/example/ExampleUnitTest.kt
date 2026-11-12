package com.example

import com.example.data.model.Difficulty
import com.example.engine.SudokuGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun sudokuGenerator_generatesValidPuzzle() {
        val puzzle = SudokuGenerator.generate(Difficulty.EASY)
        assertEquals(81, puzzle.initialBoard.length)
        assertEquals(81, puzzle.solutionBoard.length)

        val nonZeroCount = puzzle.initialBoard.count { it != '0' }
        assertTrue("Easy puzzle should have clues, got $nonZeroCount", nonZeroCount in 24..50)

        // Verify solution has no zeros
        assertTrue("Solution should have no empty cells", puzzle.solutionBoard.none { it == '0' })
    }
}
