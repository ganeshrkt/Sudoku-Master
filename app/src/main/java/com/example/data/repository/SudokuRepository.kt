package com.example.data.repository

import com.example.data.db.SudokuDatabase
import com.example.data.model.ActiveGameEntity
import com.example.data.model.Difficulty
import com.example.data.model.GameRecordEntity
import com.example.data.model.GameSettingsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DifficultyStats(
    val difficulty: String,
    val gamesPlayed: Int,
    val gamesWon: Int,
    val winRate: Float,
    val bestTimeSeconds: Int?,
    val averageTimeSeconds: Int?
)

data class TimeHistoryPoint(
    val id: Long,
    val dateMillis: Long,
    val dateLabel: String,
    val timeSeconds: Int,
    val difficulty: Difficulty,
    val mistakes: Int
)

data class DailyActivity(
    val dayLabel: String, // e.g. "Mon", "Tue"
    val fullDate: String, // e.g. "2026-11-12"
    val count: Int,
    val isToday: Boolean
)

data class OverallStatistics(
    val totalGamesPlayed: Int,
    val totalGamesWon: Int,
    val overallWinRate: Float,
    val currentStreak: Int,
    val bestStreak: Int,
    val statsByDifficulty: Map<String, DifficultyStats>,
    val timeHistory: List<TimeHistoryPoint>,
    val dailyProgress: List<DailyActivity>
)

class SudokuRepository(private val db: SudokuDatabase) {

    private val recordDao = db.gameRecordDao()
    private val activeDao = db.activeGameDao()
    private val settingsDao = db.gameSettingsDao()

    val allRecordsFlow: Flow<List<GameRecordEntity>> = recordDao.getAllRecordsFlow()

    fun getRecordsByDifficulty(difficulty: String): Flow<List<GameRecordEntity>> {
        return recordDao.getRecordsByDifficultyFlow(difficulty)
    }

    val activeGameFlow: Flow<ActiveGameEntity?> = activeDao.getActiveGameFlow()

    suspend fun getActiveGame(): ActiveGameEntity? = withContext(Dispatchers.IO) {
        activeDao.getActiveGame()
    }

    suspend fun saveActiveGame(game: ActiveGameEntity) = withContext(Dispatchers.IO) {
        activeDao.saveActiveGame(game)
    }

    suspend fun clearActiveGame() = withContext(Dispatchers.IO) {
        activeDao.clearActiveGame()
    }

    suspend fun saveGameRecord(
        difficulty: Difficulty,
        timeSeconds: Int,
        mistakes: Int,
        hintsUsed: Int,
        isCompleted: Boolean
    ): Long = withContext(Dispatchers.IO) {
        val entity = GameRecordEntity(
            difficulty = difficulty.key,
            timeSeconds = timeSeconds,
            mistakes = mistakes,
            hintsUsed = hintsUsed,
            dateMillis = System.currentTimeMillis(),
            isCompleted = isCompleted
        )
        recordDao.insertRecord(entity)
    }

    suspend fun deleteRecord(id: Long) = withContext(Dispatchers.IO) {
        recordDao.deleteRecord(id)
    }

    suspend fun clearAllRecords() = withContext(Dispatchers.IO) {
        recordDao.clearAllRecords()
    }

    // Settings
    val settingsFlow: Flow<GameSettingsEntity> = settingsDao.getSettingsFlow().map { entity ->
        entity ?: GameSettingsEntity()
    }

    suspend fun getSettings(): GameSettingsEntity = withContext(Dispatchers.IO) {
        settingsDao.getSettings() ?: GameSettingsEntity()
    }

    suspend fun updateSettings(settings: GameSettingsEntity) = withContext(Dispatchers.IO) {
        settingsDao.saveSettings(settings)
    }

    // Comprehensive Stats Aggregator
    fun observeStatistics(): Flow<OverallStatistics> {
        return allRecordsFlow.map { records ->
            computeStatistics(records)
        }
    }

    private fun computeStatistics(records: List<GameRecordEntity>): OverallStatistics {
        val totalPlayed = records.size
        val wonRecords = records.filter { it.isCompleted }
        val totalWon = wonRecords.size
        val winRate = if (totalPlayed > 0) (totalWon.toFloat() / totalPlayed) * 100f else 0f

        val difficulties = listOf(Difficulty.EASY.key, Difficulty.MEDIUM.key, Difficulty.DIFFICULT.key)
        val statsMap = mutableMapOf<String, DifficultyStats>()

        for (diff in difficulties) {
            val diffRecords = records.filter { it.difficulty.equals(diff, ignoreCase = true) }
            val diffWon = diffRecords.filter { it.isCompleted }
            val diffPlayed = diffRecords.size
            val diffWinRate = if (diffPlayed > 0) (diffWon.size.toFloat() / diffPlayed) * 100f else 0f
            val bestTime = diffWon.minOfOrNull { it.timeSeconds }
            val avgTime = if (diffWon.isNotEmpty()) diffWon.map { it.timeSeconds }.average().toInt() else null

            statsMap[diff] = DifficultyStats(
                difficulty = diff,
                gamesPlayed = diffPlayed,
                gamesWon = diffWon.size,
                winRate = diffWinRate,
                bestTimeSeconds = bestTime,
                averageTimeSeconds = avgTime
            )
        }

        // Calculate streaks (consecutive days with at least 1 won game)
        val (currentStreak, bestStreak) = calculateDailyStreaks(wonRecords)

        // Time history points (up to 30 most recent completed games, ordered chronologically for the graph)
        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        val timeHistory = wonRecords
            .take(30)
            .reversed()
            .map { rec ->
                TimeHistoryPoint(
                    id = rec.id,
                    dateMillis = rec.dateMillis,
                    dateLabel = dateFormat.format(Date(rec.dateMillis)),
                    timeSeconds = rec.timeSeconds,
                    difficulty = Difficulty.fromKey(rec.difficulty),
                    mistakes = rec.mistakes
                )
            }

        // Daily Progress (Last 7 days activity)
        val dailyProgress = calculateLast7DaysActivity(wonRecords)

        return OverallStatistics(
            totalGamesPlayed = totalPlayed,
            totalGamesWon = totalWon,
            overallWinRate = winRate,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            statsByDifficulty = statsMap,
            timeHistory = timeHistory,
            dailyProgress = dailyProgress
        )
    }

    private fun calculateDailyStreaks(wonRecords: List<GameRecordEntity>): Pair<Int, Int> {
        if (wonRecords.isEmpty()) return 0 to 0

        val calendar = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val daysWithWins = wonRecords
            .map { dayFormat.format(Date(it.dateMillis)) }
            .toSortedSet()

        if (daysWithWins.isEmpty()) return 0 to 0

        var bestStreak = 0
        var currentStreak = 0
        var tempStreak = 0
        var previousCalendar: Calendar? = null

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        for (dayStr in daysWithWins) {
            val date = sdf.parse(dayStr) ?: continue
            val cal = Calendar.getInstance().apply { time = date }

            if (previousCalendar == null) {
                tempStreak = 1
            } else {
                val diffDays = (cal.timeInMillis - previousCalendar.timeInMillis) / (1000 * 60 * 60 * 24)
                if (diffDays == 1L) {
                    tempStreak++
                } else if (diffDays > 1L) {
                    tempStreak = 1
                }
            }
            if (tempStreak > bestStreak) bestStreak = tempStreak
            previousCalendar = cal
        }

        // Check current streak up to today or yesterday
        val todayStr = dayFormat.format(Date())
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = dayFormat.format(calendar.time)

        if (daysWithWins.contains(todayStr) || daysWithWins.contains(yesterdayStr)) {
            // Count backwards from latest
            var streakCount = 0
            val checkCal = Calendar.getInstance()
            if (!daysWithWins.contains(todayStr)) {
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
            }
            while (true) {
                val checkStr = dayFormat.format(checkCal.time)
                if (daysWithWins.contains(checkStr)) {
                    streakCount++
                    checkCal.add(Calendar.DAY_OF_YEAR, -1)
                } else {
                    break
                }
            }
            currentStreak = streakCount
        }

        return currentStreak to maxOf(bestStreak, currentStreak)
    }

    private fun calculateLast7DaysActivity(wonRecords: List<GameRecordEntity>): List<DailyActivity> {
        val result = mutableListOf<DailyActivity>()
        val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val weekdayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        val calendar = Calendar.getInstance()
        val todayStr = dayFormat.format(Date())

        // Create entries for 6 days ago up to today
        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                time = Date()
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val dateStr = dayFormat.format(dayCal.time)
            val dayLabel = weekdayFormat.format(dayCal.time)
            val count = wonRecords.count { dayFormat.format(Date(it.dateMillis)) == dateStr }
            result.add(
                DailyActivity(
                    dayLabel = dayLabel,
                    fullDate = dateStr,
                    count = count,
                    isToday = dateStr == todayStr
                )
            )
        }

        return result
    }
}
