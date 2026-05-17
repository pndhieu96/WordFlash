package com.hieupnd.wordflash.domain.usecase.sentence

import com.hieupnd.wordflash.domain.repository.SentenceRepository
import javax.inject.Inject

class DeleteSentenceCardUseCase @Inject constructor(
    private val repository: SentenceRepository
) {
    suspend operator fun invoke(id: String) = repository.deleteCard(id)
}
