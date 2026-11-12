package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.SudokuDatabase
import com.example.data.model.Difficulty
import com.example.data.model.GameRecordEntity
import com.example.data.repository.DailyActivity
import com.example.data.repository.OverallStatistics
import com.example.data.repository.SudokuRepository
import com.example.data.repository.TimeHistoryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StatsFilterState(
    val selectedDifficulty: String = "ALL", // "ALL", "EASY", "MEDIUM", "DIFFICULT"
    val allRecords: List<GameRecordEntity> = emptyList(),
    val overallStats: OverallStatistics = OverallStatistics(
        totalGamesPlayed = 0,
        totalGamesWon = 0,
        overallWinRate = 0f,
        currentStreak = 0,
        bestStreak = 0,
        statsByDifficulty = emptyMap(),
        timeHistory = emptyList(),
        dailyProgress = emptyList()
    )
) {
    val filteredTimeHistory: List<TimeHistoryPoint>
        get() {
            return if (selectedDifficulty == "ALL") {
                overallStats.timeHistory
            } else {
                overallStats.timeHistory.filter { it.difficulty.key.equals(selectedDifficulty, ignoreCase = true) }
            }
        }

    val displayGamesPlayed: Int
        get() {
            return if (selectedDifficulty == "ALL") {
                overallStats.totalGamesPlayed
            } else {
                overallStats.statsByDifficulty[selectedDifficulty]?.gamesPlayed ?: 0
            }
        }

    val displayGamesWon: Int
        get() {
            return if (selectedDifficulty == "ALL") {
                overallStats.totalGamesWon
            } else {
                overallStats.statsByDifficulty[selectedDifficulty]?.gamesWon ?: 0
            }
        }

    val displayWinRate: Float
        get() {
            return if (selectedDifficulty == "ALL") {
                overallStats.overallWinRate
            } else {
                overallStats.statsByDifficulty[selectedDifficulty]?.winRate ?: 0f
            }
        }

    val displayBestTime: Int?
        get() {
            return if (selectedDifficulty == "ALL") {
                overallStats.statsByDifficulty.values.mapNotNull { it.bestTimeSeconds }.minOrNull()
            } else {
                overallStats.statsByDifficulty[selectedDifficulty]?.bestTimeSeconds
            }
        }

    val displayAvgTime: Int?
        get() {
            return if (selectedDifficulty == "ALL") {
                val times = overallStats.statsByDifficulty.values.mapNotNull { it.averageTimeSeconds }
                if (times.isNotEmpty()) times.average().toInt() else null
            } else {
                overallStats.statsByDifficulty[selectedDifficulty]?.averageTimeSeconds
            }
        }
}

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SudokuRepository(SudokuDatabase.getInstance(application))

    private val _selectedDifficulty = MutableStateFlow("ALL")

    val state: StateFlow<StatsFilterState> = combine(
        _selectedDifficulty,
        repository.observeStatistics(),
        repository.allRecordsFlow
    ) { filter, stats, records ->
        StatsFilterState(
            selectedDifficulty = filter,
            overallStats = stats,
            allRecords = records
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsFilterState()
    )

    fun setFilter(difficultyKey: String) {
        _selectedDifficulty.value = difficultyKey
    }

    fun deleteRecord(id: Long) {
        viewModelScope.launch {
            repository.deleteRecord(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllRecords()
        }
    }
}
