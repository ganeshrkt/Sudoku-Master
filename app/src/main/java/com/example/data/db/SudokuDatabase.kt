package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ActiveGameEntity
import com.example.data.model.GameRecordEntity
import com.example.data.model.GameSettingsEntity

@Database(
    entities = [
        GameRecordEntity::class,
        ActiveGameEntity::class,
        GameSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SudokuDatabase : RoomDatabase() {

    abstract fun gameRecordDao(): GameRecordDao
    abstract fun activeGameDao(): ActiveGameDao
    abstract fun gameSettingsDao(): GameSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: SudokuDatabase? = null

        fun getInstance(context: Context): SudokuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SudokuDatabase::class.java,
                    "sudoku_master.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
