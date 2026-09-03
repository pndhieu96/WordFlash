package com.hieupnd.wordflash.presentation.vocabulary

import com.hieupnd.wordflash.domain.model.DictionaryEntry
import com.hieupnd.wordflash.domain.model.Example
import com.hieupnd.wordflash.domain.model.VocabularyCard

data class VocabularyUiState(
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isLoadingGeminiInfo: Boolean = false,
    val dictionaryEntry: DictionaryEntry? = null,
    val error: String? = null,
    val suggestions: List<String> = emptyList(),
    val viMeaning: String = "",
    val ipaInput: String = "",
    val customImageUrl: String = "",
    val savedCards: List<VocabularyCard> = emptyList(),
    val savedWordSet: Set<String> = emptySet(),
    val isSaved: Boolean = false,
    val selectedTab: Int = 0,
    val editingCard: VocabularyCard? = null,
    val deleteConfirmId: String? = null,
    val manualExamples: List<Example> = emptyList(),
    val dictionaryExamples: List<Example> = emptyList(),
    val saveError: String? = null,
    val collectionQuery: String = "",
    val geminiError: String? = null,
    val isManualEntry: Boolean = false,
    val manualWord: String = "",
    val highlightCardId: String? = null
)
