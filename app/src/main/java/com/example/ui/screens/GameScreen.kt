package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Difficulty
import com.example.ui.components.GameOverDialog
import com.example.ui.components.GameControlsBar
import com.example.ui.components.HintDialog
import com.example.ui.components.NumberPadView
import com.example.ui.components.PauseOverlay
import com.example.ui.components.SudokuBoardView
import com.example.ui.components.VictoryDialog
import com.example.ui.components.formatSeconds
import com.example.ui.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    gameViewModel: GameViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by gameViewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(uiState.difficulty.color, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = uiState.difficulty.displayName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        if (uiState.settings.showTimer) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = formatSeconds(uiState.elapsedSeconds),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("game_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Mistakes badge
                    val maxMistakes = uiState.settings.maxMistakes
                    val mistakeText = if (maxMistakes > 0) {
                        "Mistakes ${uiState.mistakes}/$maxMistakes"
                    } else {
                        "Mistakes ${uiState.mistakes}"
                    }
                    val mistakeColor = if (maxMistakes > 0 && uiState.mistakes >= maxMistakes - 1) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    }

                    Text(
                        text = mistakeText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = mistakeColor,
                        modifier = Modifier.padding(end = 4.dp)
                    )

                    IconButton(
                        onClick = { gameViewModel.pauseGame() },
                        modifier = Modifier.testTag("game_pause_button")
                    ) {
                        Icon(imageVector = Icons.Default.Pause, contentDescription = "Pause")
                    }

                    IconButton(
                        onClick = { gameViewModel.restartGame() },
                        modifier = Modifier.testTag("game_restart_button")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Restart")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Generating unique puzzle...", fontWeight = FontWeight.Medium)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 9x9 Sudoku Board
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 480.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SudokuBoardView(
                        cells = uiState.cells,
                        selectedCellIndex = uiState.selectedCellIndex,
                        onCellClick = { gameViewModel.selectCell(it) },
                        highlightSameNumbers = uiState.settings.highlightSameNumbers,
                        highlightDuplicates = uiState.settings.highlightDuplicates
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Control Action Buttons (Undo, Notes, Mode, Hint)
                GameControlsBar(
                    canUndo = uiState.canUndo,
                    undoCount = uiState.undoCount,
                    isPencilMode = uiState.isPencilMode,
                    isDigitFirstMode = uiState.isDigitFirstMode,
                    onUndoClick = { gameViewModel.undo() },
                    onPencilToggle = { gameViewModel.togglePencilMode() },
                    onHintClick = { gameViewModel.requestHint() },
                    onDigitFirstToggle = { gameViewModel.toggleDigitFirstMode() },
                    modifier = Modifier.widthIn(max = 480.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 2x5 Number Pad (1-9 + Erase)
                val countsMap = (1..9).associateWith { digit ->
                    uiState.cells.count { it.value == digit }
                }
                NumberPadView(
                    countsMap = countsMap,
                    activeDigit = uiState.activeDigit,
                    isDigitFirstMode = uiState.isDigitFirstMode,
                    onDigitClick = { gameViewModel.onDigitPadClick(it) },
                    onEraseClick = { gameViewModel.eraseCell() },
                    modifier = Modifier.widthIn(max = 480.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // Victory Dialog
    if (uiState.isVictory) {
        VictoryDialog(
            difficulty = uiState.difficulty,
            timeSeconds = uiState.elapsedSeconds,
            mistakes = uiState.mistakes,
            hintsUsed = uiState.hintsUsed,
            isNewBest = uiState.isNewBest,
            onPlayAgain = { gameViewModel.startNewGame(uiState.difficulty) },
            onHomeClick = onNavigateBack
        )
    }

    // Game Over Dialog
    if (uiState.isGameOver) {
        GameOverDialog(
            mistakes = uiState.mistakes,
            maxMistakes = uiState.settings.maxMistakes,
            onRestart = { gameViewModel.restartGame() },
            onHomeClick = onNavigateBack
        )
    }

    // Pause Dialog
    if (uiState.isPaused) {
        PauseOverlay(
            onResume = { gameViewModel.resumeGame() },
            onRestart = { gameViewModel.restartGame() },
            onHomeClick = onNavigateBack
        )
    }

    // Hint Dialog
    if (uiState.activeHint != null) {
        HintDialog(
            explanation = uiState.activeHint!!.explanation,
            onApplyHint = { gameViewModel.applyHint() },
            onDismiss = { gameViewModel.dismissHint() }
        )
    }
}
