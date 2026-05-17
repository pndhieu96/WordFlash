package com.hieupnd.wordflash.domain.usecase.sentence

import com.hieupnd.wordflash.domain.model.SentenceCard
import com.hieupnd.wordflash.domain.repository.SentenceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSentenceCardsUseCase @Inject constructor(
    private val repository: SentenceRepository
) {
    operator fun invoke(): Flow<List<SentenceCard>> = repository.getAllCards()
}
