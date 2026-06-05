package com.hieupnd.wordflash.domain.usecase.stats

import com.hieupnd.wordflash.domain.model.DailyStats
import com.hieupnd.wordflash.domain.repository.SentenceRepository
import com.hieupnd.wordflash.domain.repository.VocabularyRepository
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class GetDailyStatsUseCase @Inject constructor(
    private val vocabRepository: VocabularyRepository,
    private val sentenceRepository: SentenceRepository
) {
    suspend operator fun invoke(days: Int = 7): List<DailyStats> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()

        return (0 until days).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            val from = date.atStartOfDay(zone).toInstant().toEpochMilli()
            val to = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

            val vocabAdded = vocabRepository.getCardsCreatedBetween(from, to).size
            val sentencesAdded = sentenceRepository.getCardsCreatedBetween(from, to).size
            val reviewCount = vocabRepository.getCardsReviewedBetween(from, to).size

            DailyStats(
                date = date,
                vocabAdded = vocabAdded,
                sentencesAdded = sentencesAdded,
                reviewCount = reviewCount
            )
        }.reversed()
    }
}
