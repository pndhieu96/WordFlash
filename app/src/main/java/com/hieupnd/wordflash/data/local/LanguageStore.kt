package com.hieupnd.wordflash.data.local

import android.content.Context
import android.content.res.Configuration
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ngôn ngữ hiển thị của ứng dụng. Chỉ ảnh hưởng tới giao diện — nội dung thẻ và
 * prompt gửi cho Gemini vẫn giữ nguyên tiếng Việt.
 */
@Singleton
class LanguageStore @Inject constructor(@ApplicationContext private val context: Context) {

    fun getLanguage(): String = readTag(context)

    fun setLanguage(tag: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, if (tag in SUPPORTED) tag else DEFAULT_LANGUAGE)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "language_prefs"
        private const val KEY_LANGUAGE = "app_language"

        const val VIETNAMESE = "vi"
        const val ENGLISH = "en"

        /** Mặc định tiếng Việt để giữ nguyên hành vi cũ của ứng dụng. */
        const val DEFAULT_LANGUAGE = VIETNAMESE
        val SUPPORTED = listOf(VIETNAMESE, ENGLISH)

        /**
         * Đọc trực tiếp từ SharedPreferences — dùng được trong `attachBaseContext`,
         * nơi Hilt chưa sẵn sàng để inject.
         */
        fun readTag(context: Context): String =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_LANGUAGE, DEFAULT_LANGUAGE)
                ?: DEFAULT_LANGUAGE

        /** Bọc context bằng locale người dùng đã chọn. */
        fun wrap(base: Context): Context {
            val locale = Locale.forLanguageTag(readTag(base))
            Locale.setDefault(locale)
            val config = Configuration(base.resources.configuration).apply {
                setLocale(locale)
                setLayoutDirection(locale)
            }
            return base.createConfigurationContext(config)
        }
    }
}
