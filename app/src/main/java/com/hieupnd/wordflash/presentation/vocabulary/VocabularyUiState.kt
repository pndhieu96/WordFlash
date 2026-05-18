package com.hieupnd.wordflash.presentation.vocabulary

import com.hieupnd.wordflash.domain.model.DictionaryEntry
import com.hieupnd.wordflash.domain.model.Example
import com.hieupnd.wordflash.domain.model.VocabularyCard

data class VocabularyUiState(
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isTranslating: Boolean = false,
    val isLoadingImages: Boolean = false,
    val dictionaryEntry: DictionaryEntry? = null,
    val error: String? = null,
    val viMeaning: String = "",
    val wordImages: List<String> = emptyList(),
    val selectedImageUrl: String = "",
    val customImageUrl: String = "",
    val savedCards: List<VocabularyCard> = emptyList(),
    val savedWordSet: Set<String> = emptySet(),
    val isSaved: Boolean = false,
    val selectedTab: Int = 0,
    val editingCard: VocabularyCard? = null,
    val deleteConfirmId: String? = null,
    val editDialogImages: List<String> = emptyList(),
    val isLoadingEditImages: Boolean = false,
    val manualExamples: List<Example> = emptyList(),
    val dictionaryExamples: List<Example> = emptyList(),
    val isTranslatingExamples: Boolean = false,
    val saveError: String? = null
)
