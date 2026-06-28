package com.hieupnd.wordflash.presentation.settings

import androidx.lifecycle.ViewModel
import com.hieupnd.wordflash.data.local.GeminiApiKeyStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val geminiKeyStore: GeminiApiKeyStore
) : ViewModel() {

    fun getStoredApiKey(): String = geminiKeyStore.getApiKey()

    fun hasCustomKey(): Boolean = geminiKeyStore.hasCustomKey()

    fun saveApiKey(key: String) {
        if (key.isBlank()) geminiKeyStore.clearApiKey()
        else geminiKeyStore.saveApiKey(key)
    }
}
