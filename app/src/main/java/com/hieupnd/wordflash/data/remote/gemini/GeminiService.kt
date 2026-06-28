package com.hieupnd.wordflash.data.remote.gemini

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.QuotaExceededException
import com.google.ai.client.generativeai.type.generationConfig
import com.google.gson.Gson
import com.hieupnd.wordflash.data.local.GeminiApiKeyStore
import com.hieupnd.wordflash.domain.model.Example
import com.hieupnd.wordflash.domain.model.GeminiSentenceInfo
import com.hieupnd.wordflash.domain.model.GeminiWordInfo
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.CancellationException

@Singleton
class GeminiService @Inject constructor(
    private val geminiKeyStore: GeminiApiKeyStore,
    private val gson: Gson
) {
    private val currentModelIndex = AtomicInteger(0)
    private var cachedKey: String = ""
    @Volatile private var cachedModels: List<GenerativeModel> = emptyList()

    @Synchronized
    private fun getModels(): List<GenerativeModel> {
        val key = geminiKeyStore.getApiKey()
        if (key != cachedKey || cachedModels.isEmpty()) {
            cachedKey = key
            val config = generationConfig {
                responseMimeType = "application/json"
                temperature = 0.2f
            }
            cachedModels = MODEL_NAMES.map { name -> GenerativeModel(name, key, config) }
            currentModelIndex.set(0)
        }
        return cachedModels
    }

    suspend fun getWordInfo(word: String): GeminiWordInfo {
        val prompt = buildPrompt(word)
        val models = getModels()
        val modelCount = models.size
        val startIndex = currentModelIndex.get()
        var lastException: Exception? = null

        for (modelAttempt in 0 until modelCount) {
            val index = (startIndex + modelAttempt) % modelCount
            val model = models[index]

            for (retry in 0 until MAX_RETRIES_PER_MODEL) {
                try {
                    val response = model.generateContent(prompt)
                    val rawText = response.text
                        ?: throw IllegalStateException("Empty Gemini response for '$word'")
                    Log.d(TAG, "Success with model[$index], retry=$retry")
                    currentModelIndex.set(index)
                    return parseResponse(rawText, word)
                } catch (e: QuotaExceededException) {
                    lastException = e
                    if (retry < MAX_RETRIES_PER_MODEL - 1) {
                        val delayMs = RETRY_DELAYS[retry]
                        Log.w(TAG, "Model[$index] rate limited, retry ${retry + 1} in ${delayMs}ms")
                        delay(delayMs)
                    } else {
                        Log.w(TAG, "Model[$index] exhausted after $MAX_RETRIES_PER_MODEL retries, rotating to next model")
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    lastException = e
                    Log.e(TAG, "Non-rate-limit error on model[$index], retry=$retry", e)

                    if (e.message?.contains("404") == true) {
                        Log.e(TAG, "Model[$index] returned 404, rotating immediately")
                        break
                    }

                    if (retry < MAX_RETRIES_PER_MODEL - 1) {
                        val delayMs = RETRY_DELAYS[retry]
                        delay(delayMs)
                    } else {
                        Log.e(TAG, "Model[$index] failed after $MAX_RETRIES_PER_MODEL retries, rotating to next model")
                        break
                    }
                }
            }
        }

        throw lastException ?: QuotaExceededException("All $modelCount Gemini model(s) are rate limited for '$word'")
    }

    suspend fun getSentenceStructureInfo(sentence: String): GeminiSentenceInfo {
        val prompt = buildSentencePrompt(sentence)
        val models = getModels()
        val modelCount = models.size
        val startIndex = currentModelIndex.get()
        var lastException: Exception? = null

        for (modelAttempt in 0 until modelCount) {
            val index = (startIndex + modelAttempt) % modelCount
            val model = models[index]
            for (retry in 0 until MAX_RETRIES_PER_MODEL) {
                try {
                    val response = model.generateContent(prompt)
                    val rawText = response.text
                        ?: throw IllegalStateException("Empty Gemini response for sentence '$sentence'")
                    currentModelIndex.set(index)
                    return parseSentenceResponse(rawText)
                } catch (e: QuotaExceededException) {
                    lastException = e
                    if (retry < MAX_RETRIES_PER_MODEL - 1) delay(RETRY_DELAYS[retry])
                    else Log.w(TAG, "Model[$index] exhausted for sentence structure")
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    lastException = e
                    Log.e(TAG, "Error on model[$index] for sentence structure", e)
                    break
                }
            }
        }
        throw lastException ?: QuotaExceededException("All models rate limited for sentence '$sentence'")
    }

    private fun buildSentencePrompt(sentence: String): String = """
        You are a Vietnamese English grammar assistant.
        For the English sentence structure "$sentence", return ONLY a raw JSON object with NO markdown, NO code fences, NO explanation:
        {
          "meaning": "Mô tả ngắn gọn cách dùng cấu trúc này bằng tiếng Việt (2-3 câu)",
          "examples": [
            {"enSentence": "Câu ví dụ tiếng Anh sử dụng cấu trúc này", "viSentence": "Bản dịch tiếng Việt tự nhiên"},
            {"enSentence": "Câu ví dụ thứ 2", "viSentence": "Bản dịch"},
            {"enSentence": "Câu ví dụ thứ 3", "viSentence": "Bản dịch"}
          ]
        }
        Rules:
        - description: 2-3 sentences in Vietnamese explaining when and how to use this structure
        - examples: exactly 3 natural English sentences following the structure, each with Vietnamese translation
        - Return ONLY the JSON object, no other text
    """.trimIndent()

    private fun parseSentenceResponse(raw: String): GeminiSentenceInfo {
        val cleaned = raw.trim()
            .removePrefix("```json").removePrefix("```")
            .removeSuffix("```").trim()
        val dto = gson.fromJson(cleaned, GeminiWordInfoDto::class.java)
        return GeminiSentenceInfo(
            description = dto.meaning.orEmpty(),
            examples = dto.examples?.map {
                Example(enSentence = it.enSentence.orEmpty(), viSentence = it.viSentence.orEmpty())
            } ?: emptyList()
        )
    }

    private fun buildPrompt(word: String): String = """
        You are a Vietnamese English vocabulary assistant.
        For the English word "$word", return ONLY a raw JSON object with NO markdown, NO code fences, NO explanation:
        {
          "meaning": "Nghĩa tiếng Việt ngắn gọn (kèm từ loại, ví dụ: 'Con mèo (danh từ)')",
          "examples": [
            {"enSentence": "Natural English sentence using '$word'", "viSentence": "Bản dịch tiếng Việt tự nhiên"},
            {"enSentence": "Second natural sentence", "viSentence": "Bản dịch tiếng Việt"},
            {"enSentence": "Third natural sentence", "viSentence": "Bản dịch tiếng Việt"}
          ]
        }
        Rules:
        - meaning: concise Vietnamese phrase with part of speech in parentheses
        - examples: exactly 3 items, each with enSentence and viSentence
        - Return ONLY the JSON object, no other text
    """.trimIndent()

    private fun parseResponse(raw: String, word: String): GeminiWordInfo {
        val cleaned = raw.trim()
            .removePrefix("```json").removePrefix("```")
            .removeSuffix("```").trim()
        Log.d(TAG, "Cleaned JSON: $cleaned")
        return try {
            val dto = gson.fromJson(cleaned, GeminiWordInfoDto::class.java)
            Log.d(TAG, "Parsed meaning: ${dto.meaning}, examples: ${dto.examples?.size}")
            GeminiWordInfo(
                meaning = dto.meaning.orEmpty(),
                examples = dto.examples?.map {
                    Example(enSentence = it.enSentence.orEmpty(), viSentence = it.viSentence.orEmpty())
                } ?: emptyList()
            )
        } catch (e: Exception) {
            Log.e(TAG, "JSON parse error: ${e.message}")
            throw e
        }
    }

    companion object {
        private const val TAG = "GeminiService"
        private const val MAX_RETRIES_PER_MODEL = 3
        private val RETRY_DELAYS = listOf(1000L, 2000L, 4000L)
        private val MODEL_NAMES = listOf(
            "gemini-3.5-flash",
            "gemini-3.1-flash-Lite",
            "gemini-2.5-flash",
            "gemini-2.5-flash-lite"
        )
    }
}

private data class GeminiWordInfoDto(
    val meaning: String?,
    val examples: List<ExampleDto>?
)

private data class ExampleDto(
    val enSentence: String?,
    val viSentence: String?
)
