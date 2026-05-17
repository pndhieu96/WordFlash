package com.hieupnd.wordflash.presentation.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hieupnd.wordflash.domain.model.ReviewItem
import com.hieupnd.wordflash.domain.repository.SentenceRepository
import com.hieupnd.wordflash.domain.repository.VocabularyRepository
import com.hieupnd.wordflash.domain.usecase.review.GetReviewCardsUseCase
import com.hieupnd.wordflash.domain.usecase.sentence.UpdateSentenceMemorizationUseCase
import com.hieupnd.wordflash.domain.usecase.vocabulary.UpdateVocabularyMemorizationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SESSION_SIZE = 20

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val getReviewCardsUseCase: GetReviewCardsUseCase,
    private val updateVocabMemorizationUseCase: UpdateVocabularyMemorizationUseCase,
    private val updateSentenceMemorizationUseCase: UpdateSentenceMemorizationUseCase,
    private val vocabRepository: VocabularyRepository,
    private val sentenceRepository: SentenceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    init {
        loadSession()
    }

    private fun loadSession() {
        viewModelScope.launch {
            applyDecay()
            val items = getReviewCardsUseCase().first().take(SESSION_SIZE)
            _uiState.update {
                it.copy(
                    reviewItems = items,
                    totalItems = items.size,
                    currentIndex = 0,
                    isFlipped = false,
                    isComplete = items.isEmpty()
                )
            }
        }
    }

    private suspend fun applyDecay() {
        val now = System.currentTimeMillis()
        val decayThreshold = 7 * 24 * 60 * 60 * 1000L

        vocabRepository.getAllCards().first().forEach { card ->
            val lastReviewed = card.lastReviewedAt.takeIf { it > 0 } ?: card.updatedAt
            if (now - lastReviewed > decayThreshold && card.memorizationLevel > 0) {
                updateVocabMemorizationUseCase(card.id, card.memorizationLevel - 1)
            }
        }

        sentenceRepository.getAllCards().first().forEach { card ->
            val lastReviewed = card.lastReviewedAt.takeIf { it > 0 } ?: card.updatedAt
            if (now - lastReviewed > decayThreshold && card.memorizationLevel > 0) {
                updateSentenceMemorizationUseCase(card.id, card.memorizationLevel - 1)
            }
        }
    }

    fun flipCard() {
        _uiState.update { it.copy(isFlipped = !it.isFlipped) }
    }

    fun rateCard(level: Int) {
        val state = _uiState.value
        val current = state.currentItem ?: return
        viewModelScope.launch {
            when (current) {
                is ReviewItem.VocabItem -> updateVocabMemorizationUseCase(current.id, level)
                is ReviewItem.SentenceItem -> updateSentenceMemorizationUseCase(current.id, level)
            }
            val nextIndex = state.currentIndex + 1
            if (nextIndex >= state.reviewItems.size) {
                _uiState.update { it.copy(isComplete = true) }
            } else {
                _uiState.update { it.copy(currentIndex = nextIndex, isFlipped = false) }
            }
        }
    }

    fun restartSession() {
        loadSession()
    }
}
