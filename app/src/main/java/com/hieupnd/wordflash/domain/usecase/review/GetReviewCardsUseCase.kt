package com.hieupnd.wordflash.domain.usecase.review

import com.hieupnd.wordflash.domain.model.MemorizationLevel
import com.hieupnd.wordflash.domain.model.ReviewItem
import com.hieupnd.wordflash.domain.repository.SentenceRepository
import com.hieupnd.wordflash.domain.repository.VocabularyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetReviewCardsUseCase @Inject constructor(
    private val vocabRepository: VocabularyRepository,
    private val sentenceRepository: SentenceRepository
) {
    operator fun invoke(): Flow<List<ReviewItem>> =
        vocabRepository.getAllCards().combine(sentenceRepository.getAllCards()) { vocabs, sentences ->
            val all: List<ReviewItem> =
                vocabs.map { ReviewItem.VocabItem(it) } +
                sentences.map { ReviewItem.SentenceItem(it) }
            buildWeightedList(all)
        }

    private fun buildWeightedList(items: List<ReviewItem>): List<ReviewItem> {
        val now = System.currentTimeMillis()
        return items.flatMap { item ->
            val lastReviewed = when (item) {
                is ReviewItem.VocabItem -> item.card.lastReviewedAt.takeIf { it > 0 } ?: item.card.updatedAt
                is ReviewItem.SentenceItem -> item.card.lastReviewedAt.takeIf { it > 0 } ?: item.card.updatedAt
            }
            val daysSince = ((now - lastReviewed) / 86400000L).coerceIn(0, 14).toInt()
            val baseWeight = MemorizationLevel.weightOf(item.memorizationLevel)
            val totalWeight = baseWeight + daysSince
            List(totalWeight) { item }
        }.shuffled().distinctBy { it.id }
    }
}
