package com.hieupnd.wordflash.domain.model

sealed class ReviewItem {
    abstract val id: String
    abstract val memorizationLevel: Int

    data class VocabItem(val card: VocabularyCard) : ReviewItem() {
        override val id: String = card.id
        override val memorizationLevel: Int = card.memorizationLevel
    }

    data class SentenceItem(val card: SentenceCard) : ReviewItem() {
        override val id: String = card.id
        override val memorizationLevel: Int = card.memorizationLevel
    }
}
