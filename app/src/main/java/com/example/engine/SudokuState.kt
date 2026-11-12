package com.example.engine

data class CellData(
    val index: Int,
    val row: Int,
    val col: Int,
    val value: Int = 0,
    val isGiven: Boolean = false,
    val isError: Boolean = false,
    val notes: Set<Int> = emptySet(),
    val isHinted: Boolean = false
) {
    val blockIndex: Int get() = (row / 3) * 3 + (col / 3)
}

data class SudokuMove(
    val cellIndex: Int,
    val oldValue: Int,
    val oldNotes: Set<Int>,
    val newValue: Int,
    val newNotes: Set<Int>,
    val wasMistake: Boolean = false
)

object SudokuSerialization {

    // Serializes list of 81 note sets into pipe-delimited string "1,2|3||4,5"
    fun serializeNotes(cells: List<CellData>): String {
        return cells.joinToString("|") { cell ->
            cell.notes.sorted().joinToString(",")
        }
    }

    // Deserializes string back into Map of index -> Set<Int>
    fun deserializeNotes(notesString: String): Map<Int, Set<Int>> {
        if (notesString.isBlank()) return emptyMap()
        val parts = notesString.split("|")
        val map = mutableMapOf<Int, Set<Int>>()
        for (i in 0 until minOf(81, parts.size)) {
            val part = parts[i]
            if (part.isNotBlank()) {
                val notes = part.split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .toSet()
                if (notes.isNotEmpty()) {
                    map[i] = notes
                }
            }
        }
        return map
    }
}
