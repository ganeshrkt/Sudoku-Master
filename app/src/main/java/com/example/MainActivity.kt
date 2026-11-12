package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.db.SudokuDatabase
import com.example.data.repository.SudokuRepository
import com.example.ui.navigation.NavDestination
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.theme.SudokuMasterTheme
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.StatisticsViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val gameViewModel: GameViewModel = viewModel()
            val statsViewModel: StatisticsViewModel = viewModel()
            val settingsViewModel: SettingsViewModel = viewModel()

            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
            val currentThemeMode = try {
                ThemeMode.valueOf(settings.themeMode)
            } catch (_: Exception) {
                ThemeMode.SYSTEM
            }

            SudokuMasterTheme(themeMode = currentThemeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SudokuAppNavHost(
                        gameViewModel = gameViewModel,
                        statsViewModel = statsViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun SudokuAppNavHost(
    gameViewModel: GameViewModel,
    statsViewModel: StatisticsViewModel,
    settingsViewModel: SettingsViewModel
) {
    var currentDestination by remember { mutableStateOf(NavDestination.HOME) }

    val repository = remember {
        val app = gameViewModel.getApplication<android.app.Application>()
        SudokuRepository(SudokuDatabase.getInstance(app))
    }
    val activeGame by repository.activeGameFlow.collectAsStateWithLifecycle(initialValue = null)

    AnimatedContent(
        targetState = currentDestination,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "ScreenTransition"
    ) { destination ->
        when (destination) {
            NavDestination.HOME -> {
                HomeScreen(
                    activeGame = activeGame,
                    statsViewModel = statsViewModel,
                    onStartNewGame = { difficulty ->
                        gameViewModel.startNewGame(difficulty)
                        currentDestination = NavDestination.GAME
                    },
                    onResumeGame = { active ->
                        gameViewModel.resumeActiveGame(active)
                        currentDestination = NavDestination.GAME
                    },
                    onNavigateToStats = { currentDestination = NavDestination.STATISTICS },
                    onNavigateToHistory = { currentDestination = NavDestination.HISTORY },
                    onNavigateToSettings = { currentDestination = NavDestination.SETTINGS }
                )
            }

            NavDestination.GAME -> {
                GameScreen(
                    gameViewModel = gameViewModel,
                    onNavigateBack = { currentDestination = NavDestination.HOME }
                )
            }

            NavDestination.STATISTICS -> {
                StatisticsScreen(
                    statsViewModel = statsViewModel,
                    onNavigateBack = { currentDestination = NavDestination.HOME }
                )
            }

            NavDestination.HISTORY -> {
                HistoryScreen(
                    statsViewModel = statsViewModel,
                    onNavigateBack = { currentDestination = NavDestination.HOME }
                )
            }

            NavDestination.SETTINGS -> {
                SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    onNavigateBack = { currentDestination = NavDestination.HOME }
                )
            }
        }
    }
}
