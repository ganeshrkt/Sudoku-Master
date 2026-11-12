package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.GameRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameRecordDao {

    @Query("SELECT * FROM game_records ORDER BY dateMillis DESC")
    fun getAllRecordsFlow(): Flow<List<GameRecordEntity>>

    @Query("SELECT * FROM game_records WHERE difficulty = :difficulty ORDER BY dateMillis DESC")
    fun getRecordsByDifficultyFlow(difficulty: String): Flow<List<GameRecordEntity>>

    @Query("SELECT * FROM game_records ORDER BY dateMillis DESC")
    suspend fun getAllRecords(): List<GameRecordEntity>

    @Query("SELECT * FROM game_records WHERE difficulty = :difficulty AND isCompleted = 1 ORDER BY timeSeconds ASC LIMIT 1")
    suspend fun getBestTimeRecord(difficulty: String): GameRecordEntity?

    @Query("SELECT COUNT(*) FROM game_records WHERE isCompleted = 1")
    fun getTotalWinsFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: GameRecordEntity): Long

    @Query("DELETE FROM game_records WHERE id = :id")
    suspend fun deleteRecord(id: Long)

    @Query("DELETE FROM game_records")
    suspend fun clearAllRecords()
}
