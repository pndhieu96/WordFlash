package com.hieupnd.wordflash.presentation.review

import com.hieupnd.wordflash.domain.model.ReviewItem

data class ReviewUiState(
    val reviewItems: List<ReviewItem> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val isComplete: Boolean = false,
    val totalItems: Int = 0
) {
    val currentItem: ReviewItem? get() = reviewItems.getOrNull(currentIndex)
    val progress: Float get() = (currentIndex + 1).toFloat() / totalItems.coerceAtLeast(1)
}
