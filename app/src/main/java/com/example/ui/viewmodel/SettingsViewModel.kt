package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.SudokuDatabase
import com.example.data.model.GameSettingsEntity
import com.example.data.repository.SudokuRepository
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SudokuRepository(SudokuDatabase.getInstance(application))

    val settings: StateFlow<GameSettingsEntity> = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GameSettingsEntity()
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            val current = repository.getSettings()
            repository.updateSettings(current.copy(themeMode = mode.name))
        }
    }

    fun setMaxMistakes(limit: Int) {
        viewModelScope.launch {
            val current = repository.getSettings()
            repository.updateSettings(current.copy(maxMistakes = limit))
        }
    }

    fun toggleHighlightDuplicates(enabled: Boolean) {
        viewModelScope.launch {
            val current = repository.getSettings()
            repository.updateSettings(current.copy(highlightDuplicates = enabled))
        }
    }

    fun toggleHighlightSameNumbers(enabled: Boolean) {
        viewModelScope.launch {
            val current = repository.getSettings()
            repository.updateSettings(current.copy(highlightSameNumbers = enabled))
        }
    }

    fun toggleAutoClearNotes(enabled: Boolean) {
        viewModelScope.launch {
            val current = repository.getSettings()
            repository.updateSettings(current.copy(autoClearNotes = enabled))
        }
    }

    fun toggleDigitFirstMode(enabled: Boolean) {
        viewModelScope.launch {
            val current = repository.getSettings()
            repository.updateSettings(current.copy(digitFirstMode = enabled))
        }
    }

    fun toggleShowTimer(enabled: Boolean) {
        viewModelScope.launch {
            val current = repository.getSettings()
            repository.updateSettings(current.copy(showTimer = enabled))
        }
    }

    fun toggleVibration(enabled: Boolean) {
        viewModelScope.launch {
            val current = repository.getSettings()
            repository.updateSettings(current.copy(vibrationEnabled = enabled))
        }
    }
}
