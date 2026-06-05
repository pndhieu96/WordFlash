package com.hieupnd.wordflash.presentation.stats

import com.hieupnd.wordflash.domain.model.DailyStats

data class StatsUiState(
    val dailyStats: List<DailyStats> = emptyList(),
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val isLoading: Boolean = false
)
