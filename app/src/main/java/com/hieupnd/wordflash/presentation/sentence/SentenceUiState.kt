package com.hieupnd.wordflash.presentation.sentence

import androidx.annotation.StringRes
import com.hieupnd.wordflash.domain.model.Example
import com.hieupnd.wordflash.domain.model.SentenceCard

data class StructureItem(
    val displayName: String,
    val category: String,  // "wordtype" | "role" | "custom"
    val viName: String = "",
    /** Đặt cho thành phần dựng sẵn để tên bản địa đổi theo ngôn ngữ; null với mục tự nhập. */
    @StringRes val viNameRes: Int? = null,
    val description: String = ""
)

data class SentenceUiState(
    val structureItems: List<StructureItem> = emptyList(),
    val selectedComponentTab: Int = 0,
    val focusedWordType: String? = null,
    val focusedSentenceRole: String? = null,
    val customInputName: String = "",
    val customInputDesc: String = "",
    val description: String = "",
    val newExample: String = "",
    val newExampleVi: String = "",
    val relatedExamples: List<Example> = emptyList(),
    val savedCards: List<SentenceCard> = emptyList(),
    val saveSuccess: Boolean = false,
    val isLoadingGemini: Boolean = false,
    val error: String? = null,
    val selectedTab: Int = 0,
    val editingCard: SentenceCard? = null,
    val deleteConfirmId: String? = null,
    val collectionQuery: String = "",
    val geminiError: String? = null
) {
    val sentence: String get() = structureItems.joinToString(" + ") { it.displayName }
}
