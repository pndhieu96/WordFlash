package com.hieupnd.wordflash.presentation.sentence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hieupnd.wordflash.domain.model.SentenceCard
import com.hieupnd.wordflash.domain.usecase.sentence.DeleteSentenceCardUseCase
import com.hieupnd.wordflash.domain.usecase.sentence.GetSentenceCardsUseCase
import com.hieupnd.wordflash.domain.usecase.sentence.SaveSentenceCardUseCase
import com.hieupnd.wordflash.domain.usecase.sentence.UpdateSentenceCardUseCase
import com.hieupnd.wordflash.domain.usecase.sentence.UpdateSentenceMemorizationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SentenceViewModel @Inject constructor(
    private val saveSentenceCardUseCase: SaveSentenceCardUseCase,
    private val updateSentenceCardUseCase: UpdateSentenceCardUseCase,
    private val deleteSentenceCardUseCase: DeleteSentenceCardUseCase,
    private val getSentenceCardsUseCase: GetSentenceCardsUseCase,
    private val updateSentenceMemorizationUseCase: UpdateSentenceMemorizationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SentenceUiState())
    val uiState: StateFlow<SentenceUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getSentenceCardsUseCase().collect { cards ->
                _uiState.update { it.copy(savedCards = cards) }
            }
        }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onNewExampleChange(example: String) {
        _uiState.update { it.copy(newExample = example) }
    }

    fun addExample() {
        val example = _uiState.value.newExample.trim()
        if (example.isEmpty()) return
        _uiState.update { it.copy(relatedExamples = it.relatedExamples + example, newExample = "") }
    }

    fun removeExampleAt(index: Int) {
        _uiState.update { state ->
            val list = state.relatedExamples.toMutableList().also { it.removeAt(index) }
            state.copy(relatedExamples = list)
        }
    }

    fun addWordType(typeKey: String) {
        val type = EnglishWordTypes.ALL.find { it.key == typeKey } ?: return
        _uiState.update {
            it.copy(
                structureItems = it.structureItems + StructureItem(
                    displayName = type.enName,
                    category = "wordtype",
                    viName = type.viName,
                    description = type.description
                )
            )
        }
    }

    fun addSentenceRole(roleKey: String) {
        val role = EnglishSentenceRoles.ALL.find { it.key == roleKey } ?: return
        _uiState.update {
            it.copy(
                structureItems = it.structureItems + StructureItem(
                    displayName = role.enName,
                    category = "role",
                    viName = role.viName,
                    description = role.description
                )
            )
        }
    }

    fun addCustomItem() {
        val name = _uiState.value.customInputName.trim()
        if (name.isEmpty()) return
        val desc = _uiState.value.customInputDesc.trim()
        _uiState.update {
            it.copy(
                structureItems = it.structureItems + StructureItem(
                    displayName = name,
                    category = "custom",
                    description = desc
                ),
                customInputName = "",
                customInputDesc = ""
            )
        }
    }

    fun removeStructureItemAt(index: Int) {
        _uiState.update { state ->
            val list = state.structureItems.toMutableList().also { it.removeAt(index) }
            state.copy(structureItems = list)
        }
    }

    fun onWordTypeFocused(typeKey: String) {
        _uiState.update { it.copy(focusedWordType = if (it.focusedWordType == typeKey) null else typeKey) }
    }

    fun onSentenceRoleFocused(roleKey: String) {
        _uiState.update { it.copy(focusedSentenceRole = if (it.focusedSentenceRole == roleKey) null else roleKey) }
    }

    fun onComponentTabSelected(tab: Int) {
        _uiState.update { it.copy(selectedComponentTab = tab, focusedWordType = null, focusedSentenceRole = null) }
    }

    fun onCustomInputNameChange(name: String) {
        _uiState.update { it.copy(customInputName = name) }
    }

    fun onCustomInputDescChange(desc: String) {
        _uiState.update { it.copy(customInputDesc = desc) }
    }

    fun saveSentence() {
        val state = _uiState.value
        if (state.structureItems.isEmpty()) {
            _uiState.update { it.copy(error = "Vui lòng thêm ít nhất 1 thành phần để tạo cấu trúc.") }
            return
        }
        viewModelScope.launch {
            val card = SentenceCard(
                id = UUID.randomUUID().toString(),
                sentence = state.sentence,
                description = state.description,
                relatedExamples = state.relatedExamples,
                memorizationLevel = 0,
                updatedAt = System.currentTimeMillis(),
                isSynced = false
            )
            saveSentenceCardUseCase(card)
            _uiState.update {
                it.copy(
                    structureItems = emptyList(),
                    description = "",
                    newExample = "",
                    relatedExamples = emptyList(),
                    saveSuccess = true,
                    error = null,
                    customInputName = "",
                    customInputDesc = ""
                )
            }
        }
    }

    fun updateMemorizationLevel(id: String, level: Int) {
        viewModelScope.launch { updateSentenceMemorizationUseCase(id, level) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    fun onTabSelected(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun startEdit(card: SentenceCard) {
        _uiState.update { it.copy(editingCard = card) }
    }

    fun cancelEdit() {
        _uiState.update { it.copy(editingCard = null) }
    }

    fun saveEdit(updated: SentenceCard) {
        viewModelScope.launch {
            updateSentenceCardUseCase(updated)
            _uiState.update { it.copy(editingCard = null) }
        }
    }

    fun requestDelete(id: String) {
        _uiState.update { it.copy(deleteConfirmId = id) }
    }

    fun confirmDelete() {
        val id = _uiState.value.deleteConfirmId ?: return
        viewModelScope.launch {
            deleteSentenceCardUseCase(id)
            _uiState.update { it.copy(deleteConfirmId = null) }
        }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(deleteConfirmId = null) }
    }
}
