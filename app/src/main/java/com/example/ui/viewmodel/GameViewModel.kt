package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.SudokuDatabase
import com.example.data.model.ActiveGameEntity
import com.example.data.model.Difficulty
import com.example.data.model.GameSettingsEntity
import com.example.data.repository.SudokuRepository
import com.example.engine.CellData
import com.example.engine.HintResult
import com.example.engine.SudokuGenerator
import com.example.engine.SudokuMove
import com.example.engine.SudokuSerialization
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class GameUiState(
    val isLoading: Boolean = false,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val cells: List<CellData> = emptyList(),
    val selectedCellIndex: Int? = null,
    val activeDigit: Int? = null,
    val isPencilMode: Boolean = false,
    val isDigitFirstMode: Boolean = false,
    val elapsedSeconds: Int = 0,
    val isPaused: Boolean = false,
    val mistakes: Int = 0,
    val hintsUsed: Int = 0,
    val isVictory: Boolean = false,
    val isGameOver: Boolean = false,
    val isNewBest: Boolean = false,
    val canUndo: Boolean = false,
    val undoCount: Int = 0,
    val activeHint: HintResult? = null,
    val settings: GameSettingsEntity = GameSettingsEntity()
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SudokuRepository(SudokuDatabase.getInstance(application))
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var initialBoard = ""
    private var solutionBoard = ""
    private val undoStack = mutableListOf<SudokuMove>()
    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            repository.settingsFlow.collect { settings ->
                _uiState.update { it.copy(settings = settings, isDigitFirstMode = settings.digitFirstMode) }
            }
        }
    }

    fun startNewGame(difficulty: Difficulty) {
        timerJob?.cancel()
        undoStack.clear()

        _uiState.update {
            it.copy(
                isLoading = true,
                difficulty = difficulty,
                selectedCellIndex = null,
                activeDigit = null,
                isPencilMode = false,
                elapsedSeconds = 0,
                isPaused = false,
                mistakes = 0,
                hintsUsed = 0,
                isVictory = false,
                isGameOver = false,
                isNewBest = false,
                canUndo = false,
                undoCount = 0,
                activeHint = null
            )
        }

        viewModelScope.launch {
            val puzzle = SudokuGenerator.generate(difficulty)
            initialBoard = puzzle.initialBoard
            solutionBoard = puzzle.solutionBoard

            val cells = (0 until 81).map { index ->
                val row = index / 9
                val col = index % 9
                val digit = puzzle.initialBoard[index].digitToInt()
                CellData(
                    index = index,
                    row = row,
                    col = col,
                    value = digit,
                    isGiven = digit > 0
                )
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    cells = cells
                )
            }

            saveActiveState()
            startTimer()
        }
    }

    fun resumeActiveGame(active: ActiveGameEntity) {
        timerJob?.cancel()
        undoStack.clear()

        val difficulty = Difficulty.fromKey(active.difficulty)
        initialBoard = active.initialBoard
        solutionBoard = active.solutionBoard

        val notesMap = SudokuSerialization.deserializeNotes(active.notesSerialized)

        val cells = (0 until 81).map { index ->
            val row = index / 9
            val col = index % 9
            val currentDigit = active.currentBoard[index].digitToInt()
            val initialDigit = active.initialBoard[index].digitToInt()
            val isGiven = initialDigit > 0
            val isError = currentDigit > 0 && currentDigit != active.solutionBoard[index].digitToInt()

            CellData(
                index = index,
                row = row,
                col = col,
                value = currentDigit,
                isGiven = isGiven,
                isError = isError,
                notes = if (currentDigit == 0) (notesMap[index] ?: emptySet()) else emptySet()
            )
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                difficulty = difficulty,
                cells = cells,
                selectedCellIndex = null,
                activeDigit = null,
                elapsedSeconds = active.elapsedSeconds,
                mistakes = active.mistakes,
                hintsUsed = active.hintsUsed,
                isPaused = false,
                isVictory = false,
                isGameOver = false,
                canUndo = false,
                undoCount = 0
            )
        }

        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (!_uiState.value.isPaused && !_uiState.value.isVictory && !_uiState.value.isGameOver) {
                    _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
                    if (_uiState.value.elapsedSeconds % 5 == 0) {
                        saveActiveState()
                    }
                }
            }
        }
    }

    fun selectCell(index: Int) {
        if (_uiState.value.isPaused || _uiState.value.isVictory || _uiState.value.isGameOver) return

        if (_uiState.value.isDigitFirstMode && _uiState.value.activeDigit != null) {
            // Apply active digit to tapped cell
            _uiState.update { it.copy(selectedCellIndex = index) }
            enterDigit(_uiState.value.activeDigit!!)
        } else {
            _uiState.update { it.copy(selectedCellIndex = index) }
            triggerHaptic(5)
        }
    }

    fun onDigitPadClick(digit: Int) {
        if (_uiState.value.isPaused || _uiState.value.isVictory || _uiState.value.isGameOver) return

        if (_uiState.value.isDigitFirstMode) {
            // Set active digit
            _uiState.update {
                it.copy(activeDigit = if (it.activeDigit == digit) null else digit)
            }
            triggerHaptic(10)
        } else {
            enterDigit(digit)
        }
    }

    fun enterDigit(digit: Int) {
        val state = _uiState.value
        val cellIdx = state.selectedCellIndex ?: return
        if (cellIdx !in state.cells.indices) return
        val currentCell = state.cells[cellIdx]

        if (currentCell.isGiven) return // cannot change given cell

        if (state.isPencilMode) {
            // Toggle note
            val newNotes = if (currentCell.notes.contains(digit)) {
                currentCell.notes - digit
            } else {
                currentCell.notes + digit
            }

            val move = SudokuMove(
                cellIndex = cellIdx,
                oldValue = currentCell.value,
                oldNotes = currentCell.notes,
                newValue = currentCell.value,
                newNotes = newNotes
            )
            undoStack.add(move)

            val updatedCells = state.cells.toMutableList()
            updatedCells[cellIdx] = currentCell.copy(notes = newNotes)

            _uiState.update {
                it.copy(
                    cells = updatedCells,
                    canUndo = true,
                    undoCount = undoStack.size
                )
            }
            triggerHaptic(10)
            saveActiveState()
            return
        }

        // Normal placement
        val correctDigit = solutionBoard[cellIdx].digitToInt()
        val isMistake = digit != correctDigit

        val move = SudokuMove(
            cellIndex = cellIdx,
            oldValue = currentCell.value,
            oldNotes = currentCell.notes,
            newValue = digit,
            newNotes = emptySet(),
            wasMistake = isMistake
        )
        undoStack.add(move)

        val updatedCells = state.cells.toMutableList()
        updatedCells[cellIdx] = currentCell.copy(
            value = digit,
            isError = isMistake,
            notes = emptySet()
        )

        // Auto remove notes if enabled
        if (state.settings.autoClearNotes && !isMistake) {
            clearNotesAround(updatedCells, currentCell.row, currentCell.col, currentCell.blockIndex, digit)
        }

        val newMistakes = if (isMistake) state.mistakes + 1 else state.mistakes
        val maxMistakes = state.settings.maxMistakes
        val isGameOver = maxMistakes > 0 && newMistakes >= maxMistakes

        if (isMistake) {
            triggerErrorHaptic()
        } else {
            triggerHaptic(15)
        }

        _uiState.update {
            it.copy(
                cells = updatedCells,
                mistakes = newMistakes,
                isGameOver = isGameOver,
                canUndo = true,
                undoCount = undoStack.size
            )
        }

        if (isGameOver) {
            timerJob?.cancel()
            viewModelScope.launch {
                repository.clearActiveGame()
                repository.saveGameRecord(
                    difficulty = state.difficulty,
                    timeSeconds = state.elapsedSeconds,
                    mistakes = newMistakes,
                    hintsUsed = state.hintsUsed,
                    isCompleted = false
                )
            }
        } else {
            checkVictoryCondition(updatedCells)
            saveActiveState()
        }
    }

    private fun clearNotesAround(cells: MutableList<CellData>, row: Int, col: Int, blockIndex: Int, digit: Int) {
        for (i in cells.indices) {
            val c = cells[i]
            if (c.value == 0 && (c.row == row || c.col == col || c.blockIndex == blockIndex)) {
                if (c.notes.contains(digit)) {
                    cells[i] = c.copy(notes = c.notes - digit)
                }
            }
        }
    }

    fun eraseCell() {
        val state = _uiState.value
        val cellIdx = state.selectedCellIndex ?: return
        if (cellIdx !in state.cells.indices) return
        val currentCell = state.cells[cellIdx]

        if (currentCell.isGiven || (currentCell.value == 0 && currentCell.notes.isEmpty())) return

        val move = SudokuMove(
            cellIndex = cellIdx,
            oldValue = currentCell.value,
            oldNotes = currentCell.notes,
            newValue = 0,
            newNotes = emptySet()
        )
        undoStack.add(move)

        val updatedCells = state.cells.toMutableList()
        updatedCells[cellIdx] = currentCell.copy(
            value = 0,
            isError = false,
            notes = emptySet()
        )

        _uiState.update {
            it.copy(
                cells = updatedCells,
                canUndo = true,
                undoCount = undoStack.size
            )
        }
        triggerHaptic(10)
        saveActiveState()
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val lastMove = undoStack.removeAt(undoStack.lastIndex)
        val state = _uiState.value

        val updatedCells = state.cells.toMutableList()
        val cell = updatedCells[lastMove.cellIndex]

        val restoredMistakes = if (lastMove.wasMistake && state.mistakes > 0) state.mistakes - 1 else state.mistakes

        val correctDigit = solutionBoard[lastMove.cellIndex].digitToInt()
        val isError = lastMove.oldValue > 0 && lastMove.oldValue != correctDigit

        updatedCells[lastMove.cellIndex] = cell.copy(
            value = lastMove.oldValue,
            isError = isError,
            notes = lastMove.oldNotes
        )

        _uiState.update {
            it.copy(
                cells = updatedCells,
                mistakes = restoredMistakes,
                canUndo = undoStack.isNotEmpty(),
                undoCount = undoStack.size,
                selectedCellIndex = lastMove.cellIndex
            )
        }
        triggerHaptic(10)
        saveActiveState()
    }

    fun togglePencilMode() {
        _uiState.update { it.copy(isPencilMode = !it.isPencilMode) }
        triggerHaptic(15)
    }

    fun toggleDigitFirstMode() {
        _uiState.update {
            it.copy(
                isDigitFirstMode = !it.isDigitFirstMode,
                activeDigit = null
            )
        }
        triggerHaptic(15)
    }

    fun requestHint() {
        val state = _uiState.value
        if (state.isVictory || state.isGameOver) return

        val currentArray = IntArray(81) { state.cells[it].value }
        val solutionArray = IntArray(81) { solutionBoard[it].digitToInt() }

        val hint = SudokuGenerator.findHint(currentArray, solutionArray)
        if (hint != null) {
            _uiState.update {
                it.copy(
                    activeHint = hint,
                    selectedCellIndex = hint.index
                )
            }
        }
    }

    fun applyHint() {
        val hint = _uiState.value.activeHint ?: return
        val state = _uiState.value
        val updatedCells = state.cells.toMutableList()
        val cell = updatedCells[hint.index]

        updatedCells[hint.index] = cell.copy(
            value = hint.correctValue,
            isError = false,
            notes = emptySet()
        )

        _uiState.update {
            it.copy(
                cells = updatedCells,
                hintsUsed = it.hintsUsed + 1,
                activeHint = null,
                selectedCellIndex = hint.index
            )
        }
        triggerHaptic(20)
        checkVictoryCondition(updatedCells)
        saveActiveState()
    }

    fun dismissHint() {
        _uiState.update { it.copy(activeHint = null) }
    }

    fun pauseGame() {
        _uiState.update { it.copy(isPaused = true) }
    }

    fun resumeGame() {
        _uiState.update { it.copy(isPaused = false) }
    }

    fun restartGame() {
        if (initialBoard.length == 81) {
            undoStack.clear()
            val cells = (0 until 81).map { index ->
                val row = index / 9
                val col = index % 9
                val digit = initialBoard[index].digitToInt()
                CellData(
                    index = index,
                    row = row,
                    col = col,
                    value = digit,
                    isGiven = digit > 0
                )
            }

            _uiState.update {
                it.copy(
                    cells = cells,
                    elapsedSeconds = 0,
                    mistakes = 0,
                    hintsUsed = 0,
                    isPaused = false,
                    isVictory = false,
                    isGameOver = false,
                    canUndo = false,
                    undoCount = 0,
                    selectedCellIndex = null,
                    activeDigit = null
                )
            }
            saveActiveState()
        } else {
            startNewGame(_uiState.value.difficulty)
        }
    }

    private fun checkVictoryCondition(cells: List<CellData>) {
        if (solutionBoard.length != 81) return
        val isAllFilledCorrectly = cells.indices.all { i ->
            cells[i].value == solutionBoard[i].digitToInt()
        }

        if (isAllFilledCorrectly) {
            timerJob?.cancel()
            val state = _uiState.value
            triggerVictoryHaptic()

            viewModelScope.launch {
                // Check if new best time
                val currentBest = repository.getSettings() // or check record
                val recs = repository.allRecordsFlow
                // Save game victory
                repository.saveGameRecord(
                    difficulty = state.difficulty,
                    timeSeconds = state.elapsedSeconds,
                    mistakes = state.mistakes,
                    hintsUsed = state.hintsUsed,
                    isCompleted = true
                )
                repository.clearActiveGame()

                _uiState.update {
                    it.copy(
                        isVictory = true,
                        isNewBest = false // Will be reflected in stats
                    )
                }
            }
        }
    }

    private fun saveActiveState() {
        val state = _uiState.value
        if (state.cells.size != 81 || state.isVictory || state.isGameOver) return

        viewModelScope.launch {
            val currentStr = state.cells.map { it.value }.joinToString("")
            val notesStr = SudokuSerialization.serializeNotes(state.cells)
            val active = ActiveGameEntity(
                id = 1,
                difficulty = state.difficulty.key,
                initialBoard = initialBoard,
                currentBoard = currentStr,
                solutionBoard = solutionBoard,
                notesSerialized = notesStr,
                elapsedSeconds = state.elapsedSeconds,
                mistakes = state.mistakes,
                hintsUsed = state.hintsUsed
            )
            repository.saveActiveGame(active)
        }
    }

    private fun triggerHaptic(durationMs: Long) {
        if (!_uiState.value.settings.vibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun triggerErrorHaptic() {
        if (!_uiState.value.settings.vibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 50, 40, 50), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(100)
            }
        } catch (_: Exception) {}
    }

    private fun triggerVictoryHaptic() {
        if (!_uiState.value.settings.vibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 60, 100, 60, 150), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(250)
            }
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
