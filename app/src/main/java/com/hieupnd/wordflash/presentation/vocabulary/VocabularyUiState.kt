package com.hieupnd.wordflash.presentation.vocabulary

import com.hieupnd.wordflash.domain.model.DictionaryEntry
import com.hieupnd.wordflash.domain.model.Example
import com.hieupnd.wordflash.domain.model.VocabularyCard

enum class ImageSourceMode { API, URL }

data class VocabularyUiState(
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isLoadingGeminiInfo: Boolean = false,
    val dictionaryEntry: DictionaryEntry? = null,
    val error: String? = null,
    val suggestions: List<String> = emptyList(),
    val viMeaning: String = "",
    val wordImages: List<String> = emptyList(),
    val selectedImageUrl: String = "",
    val customImageUrl: String = "",
    val imageSourceMode: ImageSourceMode = ImageSourceMode.API,
    val imageSearchError: String? = null,
    val savedCards: List<VocabularyCard> = emptyList(),
    val savedWordSet: Set<String> = emptySet(),
    val isSaved: Boolean = false,
    val selectedTab: Int = 0,
    val editingCard: VocabularyCard? = null,
    val deleteConfirmId: String? = null,
    val editDialogImages: List<String> = emptyList(),
    val isLoadingEditImages: Boolean = false,
    val editImagesError: String? = null,
    val manualExamples: List<Example> = emptyList(),
    val dictionaryExamples: List<Example> = emptyList(),
    val saveError: String? = null
)
