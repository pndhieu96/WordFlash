package com.hieupnd.wordflash.presentation.vocabulary

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hieupnd.wordflash.BuildConfig
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
    private val getWordSuggestionsUseCase: GetWordSuggestionsUseCase
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
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isLoadingGeminiInfo = false,
                    error = null,
                    suggestions = emptyList(),
                    dictionaryEntry = null,
                    isSaved = false,
                    manualExamples = emptyList(),
                    dictionaryExamples = emptyList(),
                    customImageUrl = "",
                    viMeaning = "",
                    ipaInput = ""
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
                                _uiState.update { it.copy(isLoadingGeminiInfo = false) }
                            }
                    }
                }
                .onFailure {
                    geminiDeferred?.cancel()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingGeminiInfo = false,
                            error = "Không tìm thấy từ '$query'. Hãy kiểm tra lại chính tả."
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

    fun saveVocabularyCard() {
        val state = _uiState.value
        val entry = state.dictionaryEntry ?: return
        viewModelScope.launch {
            val allExamples = state.dictionaryExamples + state.manualExamples
            val meaning = state.viMeaning.trim().ifEmpty {
                entry.meanings.firstOrNull()?.definitions?.firstOrNull()?.definition.orEmpty()
            }
            val card = VocabularyCard(
                id = UUID.randomUUID().toString(),
                word = entry.word,
                ipa = state.ipaInput.trim(),
                audioUrl = entry.audioUrl,
                meaning = meaning,
                examples = allExamples,
                memorizationLevel = 0,
                updatedAt = System.currentTimeMillis(),
                isSynced = false,
                wordType = entry.wordType,
                imageUrl = state.customImageUrl.trim()
            )
            runCatching { saveVocabularyCardUseCase(card) }
                .onSuccess { _uiState.update { it.copy(isSaved = true) } }
                .onFailure { _uiState.update { it.copy(saveError = "Lưu thất bại. Vui lòng thử lại.") } }
        }
    }

    fun updateMemorizationLevel(id: String, level: Int) {
        viewModelScope.launch {
            updateVocabularyMemorizationUseCase(id, level)
        }
    }

    fun onTabSelected(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
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
                .onFailure { _uiState.update { it.copy(saveError = "Cập nhật thất bại. Vui lòng thử lại.") } }
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
                .onFailure { _uiState.update { it.copy(deleteConfirmId = null, saveError = "Xoá thất bại. Vui lòng thử lại.") } }
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
