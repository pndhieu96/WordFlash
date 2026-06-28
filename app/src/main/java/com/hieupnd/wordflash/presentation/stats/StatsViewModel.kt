package com.hieupnd.wordflash.presentation.stats

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hieupnd.wordflash.domain.usecase.stats.GetDailyStatsUseCase
import com.hieupnd.wordflash.notification.DailyReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val getDailyStatsUseCase: GetDailyStatsUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val prefs = context.getSharedPreferences(DailyReminderWorker.PREFS_NAME, Context.MODE_PRIVATE)
            val currentStreak = prefs.getInt(DailyReminderWorker.KEY_CURRENT_STREAK, 0)
            val longestStreak = prefs.getInt(DailyReminderWorker.KEY_LONGEST_STREAK, 0)
            val stats = getDailyStatsUseCase(days = 7)
            _uiState.update {
                it.copy(
                    dailyStats = stats,
                    currentStreak = currentStreak,
                    longestStreak = longestStreak,
                    isLoading = false
                )
            }
        }
    }
}
