package com.hieupnd.wordflash.domain.usecase.sentence

import com.hieupnd.wordflash.domain.model.SentenceCard
import com.hieupnd.wordflash.domain.repository.SentenceRepository
import javax.inject.Inject

class UpdateSentenceCardUseCase @Inject constructor(
    private val repository: SentenceRepository
) {
    suspend operator fun invoke(card: SentenceCard) = repository.updateCard(card)
}
