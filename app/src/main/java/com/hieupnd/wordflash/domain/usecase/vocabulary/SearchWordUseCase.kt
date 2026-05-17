package com.hieupnd.wordflash.domain.usecase.vocabulary

import com.hieupnd.wordflash.domain.model.DictionaryEntry
import com.hieupnd.wordflash.domain.repository.VocabularyRepository
import javax.inject.Inject

class SearchWordUseCase @Inject constructor(
    private val repository: VocabularyRepository
) {
    suspend operator fun invoke(word: String): Result<DictionaryEntry> =
        repository.searchWord(word)
}
