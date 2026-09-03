package com.hieupnd.wordflash.presentation.vocabulary

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hieupnd.wordflash.BuildConfig
import com.hieupnd.wordflash.R
import com.hieupnd.wordflash.domain.model.Example
import com.hieupnd.wordflash.domain.model.VocabularyCard
import com.hieupnd.wordflash.domain.usecase.vocabulary.DeleteVocabularyCardUseCase
import com.hieupnd.wordflash.domain.usecase.vocabulary.GetVocabularyCardsUseCase
import com.hieupnd.wordflash.domain.usecase.vocabulary.GetWordInfoFromGeminiUseCase
import com.hieupnd.wordflash.domain.usecase.vocabulary.GetWordSuggestionsUseCase
import com.hieupnd.wordflash.domain.usecase.vocabulary.SaveVocabularyCardUseCase
import com.hieupnd.wordflash.domain.usecase.vocabulary.SearchWordUseCase
import com.hieupnd.wordflash.domain.usecase.vocabulary.UpdateVocabularyCardUseCase
import com.hieupnd.wordflash.domain.usecase.vocabulary.UpdateVocabularyMemorizationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class VocabularyViewModel @Inject constructor(
    private val searchWordUseCase: SearchWordUseCase,
    private val getWordInfoFromGeminiUseCase: GetWordInfoFromGeminiUseCase,
    private val saveVocabularyCardUseCase: SaveVocabularyCardUseCase,
    private val updateVocabularyCardUseCase: UpdateVocabularyCardUseCase,
    private val deleteVocabularyCardUseCase: DeleteVocabularyCardUseCase,
    private val getVocabularyCardsUseCase: GetVocabularyCardsUseCase,
    private val updateVocabularyMemorizationUseCase: UpdateVocabularyMemorizationUseCase,
    private val getWordSuggestionsUseCase: GetWordSuggestionsUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(VocabularyUiState())
    val uiState: StateFlow<VocabularyUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getVocabularyCardsUseCase().collect { cards ->
                _uiState.update {
                    it.copy(
                        savedCards = cards,
                        savedWordSet = cards.map { c -> c.word.lowercase() }.toSet()
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query, error = null) }
    }

    fun searchWord() {
        val query = _uiState.value.searchQuery.trim()
        if (query.isEmpty()) return

        val existing = _uiState.value.savedCards.firstOrNull { it.word.equals(query, ignoreCase = true) }
        if (existing != null) {
            _uiState.update {
                it.copy(
                    selectedTab = 0,
                    collectionQuery = existing.word,
                    highlightCardId = existing.id,
                    isLoading = false,
                    isLoadingGeminiInfo = false,
                    error = null,
                    geminiError = null,
                    suggestions = emptyList()
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isLoadingGeminiInfo = false,
                    error = null,
                    geminiError = null,
                    suggestions = emptyList(),
                    dictionaryEntry = null,
                    isSaved = false,
                    manualExamples = emptyList(),
                    dictionaryExamples = emptyList(),
                    customImageUrl = "",
                    viMeaning = "",
                    ipaInput = "",
                    isManualEntry = false,
                    manualWord = ""
                )
            }

            val dictionaryDeferred = async { searchWordUseCase(query) }
            val geminiDeferred = if (BuildConfig.GEMINI_API_KEY.isBlank()) null
                                 else async { getWordInfoFromGeminiUseCase(query) }

            dictionaryDeferred.await()
                .onSuccess { entry ->
                    val capitalizedEntry = entry.copy(word = entry.word.replaceFirstChar { it.uppercaseChar() })
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingGeminiInfo = geminiDeferred != null,
                            dictionaryEntry = capitalizedEntry,
                            ipaInput = capitalizedEntry.ipa,
                            isSaved = it.savedWordSet.contains(capitalizedEntry.word.lowercase())
                        )
                    }

                    if (geminiDeferred == null) {
                        _uiState.update { it.copy(isLoadingGeminiInfo = false) }
                    } else {
                        geminiDeferred.await()
                            .onSuccess { geminiInfo ->
                                _uiState.update {
                                    it.copy(
                                        isLoadingGeminiInfo = false,
                                        viMeaning = geminiInfo.meaning,
                                        dictionaryExamples = geminiInfo.examples
                                    )
                                }
                            }
                            .onFailure { error ->
                                Log.e("VocabularyViewModel", "Gemini error", error)
                                _uiState.update {
                                    it.copy(
                                        isLoadingGeminiInfo = false,
                                        geminiError = context.getString(R.string.vocab_gemini_error, error.message.orEmpty())
                                    )
                                }
                            }
                    }
                }
                .onFailure {
                    geminiDeferred?.cancel()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingGeminiInfo = false,
                            error = context.getString(R.string.vocab_not_found, query)
                        )
                    }
                    fetchSuggestions(query)
                }
        }
    }

    fun onViMeaningChange(meaning: String) {
        _uiState.update { it.copy(viMeaning = meaning) }
    }

    fun onIpaChange(ipa: String) {
        _uiState.update { it.copy(ipaInput = ipa) }
    }

    fun onCustomImageUrlChange(url: String) {
        _uiState.update { it.copy(customImageUrl = url) }
    }

    fun addManualExample(example: Example) {
        _uiState.update { it.copy(manualExamples = it.manualExamples + example) }
    }

    fun removeManualExample(index: Int) {
        _uiState.update {
            it.copy(manualExamples = it.manualExamples.toMutableList().also { list -> list.removeAt(index) })
        }
    }

    fun enterManualMode() {
        val word = _uiState.value.searchQuery.trim().replaceFirstChar { it.uppercaseChar() }
        val hasGemini = BuildConfig.GEMINI_API_KEY.isNotBlank()
        _uiState.update {
            it.copy(
                isManualEntry = true,
                manualWord = word,
                ipaInput = "",
                viMeaning = "",
                customImageUrl = "",
                manualExamples = emptyList(),
                dictionaryExamples = emptyList(),
                geminiError = null,
                isSaved = false,
                isLoadingGeminiInfo = hasGemini && word.isNotEmpty()
            )
        }
        if (hasGemini && word.isNotEmpty()) {
            viewModelScope.launch {
                getWordInfoFromGeminiUseCase(word)
                    .onSuccess { geminiInfo ->
                        _uiState.update {
                            it.copy(
                                isLoadingGeminiInfo = false,
                                viMeaning = geminiInfo.meaning,
                                dictionaryExamples = geminiInfo.examples
                            )
                        }
                    }
                    .onFailure {
                        _uiState.update { it.copy(isLoadingGeminiInfo = false) }
                    }
            }
        }
    }

    fun onManualWordChange(word: String) {
        _uiState.update { it.copy(manualWord = word) }
    }

    fun saveVocabularyCard() {
        val state = _uiState.value
        viewModelScope.launch {
            val word: String
            val audioUrl: String
            val wordType: String
            val allExamples: List<Example>
            val meaning: String

            if (state.isManualEntry) {
                word = state.manualWord.trim().replaceFirstChar { it.uppercaseChar() }
                if (word.isEmpty()) return@launch
                audioUrl = ""
                wordType = ""
                allExamples = state.manualExamples
                meaning = state.viMeaning.trim()
            } else {
                val entry = state.dictionaryEntry ?: return@launch
                word = entry.word
                audioUrl = entry.audioUrl
                wordType = entry.wordType
                allExamples = state.dictionaryExamples + state.manualExamples
                meaning = state.viMeaning.trim().ifEmpty {
                    entry.meanings.firstOrNull()?.definitions?.firstOrNull()?.definition.orEmpty()
                }
            }

            val card = VocabularyCard(
                id = UUID.randomUUID().toString(),
                word = word,
                ipa = state.ipaInput.trim(),
                audioUrl = audioUrl,
                meaning = meaning,
                examples = allExamples,
                memorizationLevel = 0,
                updatedAt = System.currentTimeMillis(),
                isSynced = false,
                wordType = wordType,
                imageUrl = state.customImageUrl.trim()
            )
            runCatching { saveVocabularyCardUseCase(card) }
                .onSuccess { _uiState.update { it.copy(isSaved = true) } }
                .onFailure { _uiState.update { it.copy(saveError = context.getString(R.string.error_save_failed)) } }
        }
    }

    fun updateMemorizationLevel(id: String, level: Int) {
        viewModelScope.launch {
            updateVocabularyMemorizationUseCase(id, level)
        }
    }

    fun onTabSelected(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab, highlightCardId = null) }
    }

    fun onCollectionQueryChange(query: String) {
        _uiState.update { it.copy(collectionQuery = query, highlightCardId = null) }
    }

    fun clearHighlight() {
        _uiState.update { it.copy(highlightCardId = null) }
    }

    fun startEdit(card: VocabularyCard) {
        _uiState.update { it.copy(editingCard = card) }
    }

    fun cancelEdit() {
        _uiState.update { it.copy(editingCard = null) }
    }

    fun saveEdit(updated: VocabularyCard) {
        viewModelScope.launch {
            runCatching { updateVocabularyCardUseCase(updated) }
                .onSuccess { _uiState.update { it.copy(editingCard = null) } }
                .onFailure { _uiState.update { it.copy(saveError = context.getString(R.string.error_update_failed)) } }
        }
    }

    fun requestDelete(id: String) {
        _uiState.update { it.copy(deleteConfirmId = id) }
    }

    fun confirmDelete() {
        val id = _uiState.value.deleteConfirmId ?: return
        viewModelScope.launch {
            runCatching { deleteVocabularyCardUseCase(id) }
                .onSuccess { _uiState.update { it.copy(deleteConfirmId = null) } }
                .onFailure { _uiState.update { it.copy(deleteConfirmId = null, saveError = context.getString(R.string.error_delete_failed)) } }
        }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(deleteConfirmId = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSaveError() {
        _uiState.update { it.copy(saveError = null) }
    }

    private fun fetchSuggestions(query: String) {
        viewModelScope.launch {
            getWordSuggestionsUseCase(query)
                .onSuccess { words -> _uiState.update { it.copy(suggestions = words) } }
        }
    }
}
