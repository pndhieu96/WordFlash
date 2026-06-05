package com.hieupnd.wordflash.domain.model

import java.time.LocalDate

data class DailyStats(
    val date: LocalDate,
    val vocabAdded: Int,
    val sentencesAdded: Int,
    val reviewCount: Int
)
