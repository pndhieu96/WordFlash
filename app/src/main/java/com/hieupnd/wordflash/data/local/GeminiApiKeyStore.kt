package com.hieupnd.wordflash.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiApiKeyStore @Inject constructor(@ApplicationContext private val context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getApiKey(): String = prefs.getString(KEY_API_KEY, "").orEmpty()

    fun saveApiKey(key: String) {
        prefs.edit().putString(KEY_API_KEY, key.trim()).apply()
    }

    fun clearApiKey() {
        prefs.edit().remove(KEY_API_KEY).apply()
    }

    fun hasCustomKey(): Boolean = prefs.getString(KEY_API_KEY, "").orEmpty().isNotBlank()

    companion object {
        private const val PREFS_NAME = "gemini_prefs"
        const val KEY_API_KEY = "gemini_api_key"
    }
}
