package com.hieupnd.wordflash.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewSettingsStore @Inject constructor(@ApplicationContext private val context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getVocabSessionSize(): Int = prefs.getInt(KEY_VOCAB_SIZE, DEFAULT_VOCAB_SIZE)

    fun getSentenceSessionSize(): Int = prefs.getInt(KEY_SENTENCE_SIZE, DEFAULT_SENTENCE_SIZE)

    fun setVocabSessionSize(size: Int) {
        prefs.edit().putInt(KEY_VOCAB_SIZE, size.coerceIn(VOCAB_MIN, VOCAB_MAX)).apply()
    }

    fun setSentenceSessionSize(size: Int) {
        prefs.edit().putInt(KEY_SENTENCE_SIZE, size.coerceIn(SENTENCE_MIN, SENTENCE_MAX)).apply()
    }

    companion object {
        private const val PREFS_NAME = "review_prefs"
        private const val KEY_VOCAB_SIZE = "vocab_session_size"
        private const val KEY_SENTENCE_SIZE = "sentence_session_size"

        const val DEFAULT_VOCAB_SIZE = 20
        const val DEFAULT_SENTENCE_SIZE = 5

        const val VOCAB_MIN = 5
        const val VOCAB_MAX = 50
        const val SENTENCE_MIN = 0
        const val SENTENCE_MAX = 20
    }
}
