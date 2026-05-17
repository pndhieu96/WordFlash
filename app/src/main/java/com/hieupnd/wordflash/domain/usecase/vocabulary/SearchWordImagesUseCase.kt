package com.hieupnd.wordflash.domain.usecase.vocabulary

import com.hieupnd.wordflash.domain.repository.VocabularyRepository
import javax.inject.Inject

class SearchWordImagesUseCase @Inject constructor(
    private val repository: VocabularyRepository
) {
    suspend operator fun invoke(query: String): Result<List<String>> =
        repository.searchImages(query)
}
