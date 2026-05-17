package com.hieupnd.wordflash.domain.usecase.sentence

import com.hieupnd.wordflash.domain.repository.SentenceRepository
import javax.inject.Inject

class UpdateSentenceMemorizationUseCase @Inject constructor(
    private val repository: SentenceRepository
) {
    suspend operator fun invoke(id: String, level: Int) =
        repository.updateMemorizationLevel(id, level)
}
