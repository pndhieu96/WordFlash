package com.hieupnd.wordflash.presentation.settings

import androidx.lifecycle.ViewModel
import com.hieupnd.wordflash.data.local.GeminiApiKeyStore
import com.hieupnd.wordflash.data.local.LanguageStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val geminiKeyStore: GeminiApiKeyStore,
    private val languageStore: LanguageStore
) : ViewModel() {

    fun getStoredApiKey(): String = geminiKeyStore.getApiKey()

    fun hasCustomKey(): Boolean = geminiKeyStore.hasCustomKey()

    fun getLanguage(): String = languageStore.getLanguage()

    /** Ghi lựa chọn; Activity phải recreate() để áp dụng locale mới. */
    fun setLanguage(tag: String) = languageStore.setLanguage(tag)

    fun saveApiKey(key: String) {
        if (key.isBlank()) geminiKeyStore.clearApiKey()
        else geminiKeyStore.saveApiKey(key)
    }
}
