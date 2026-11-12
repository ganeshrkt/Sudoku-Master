package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ActiveGameEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActiveGameDao {

    @Query("SELECT * FROM active_game WHERE id = 1 LIMIT 1")
    fun getActiveGameFlow(): Flow<ActiveGameEntity?>

    @Query("SELECT * FROM active_game WHERE id = 1 LIMIT 1")
    suspend fun getActiveGame(): ActiveGameEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveActiveGame(game: ActiveGameEntity)

    @Query("DELETE FROM active_game WHERE id = 1")
    suspend fun clearActiveGame()
}
