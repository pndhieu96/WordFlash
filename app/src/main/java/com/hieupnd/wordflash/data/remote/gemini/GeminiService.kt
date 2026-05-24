package com.hieupnd.wordflash.data.remote.gemini

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.gson.Gson
import com.hieupnd.wordflash.domain.model.Example
import com.hieupnd.wordflash.domain.model.GeminiWordInfo
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiService @Inject constructor(
    private val generativeModel: GenerativeModel,
    private val gson: Gson
) {
    suspend fun getWordInfo(word: String): GeminiWordInfo {
        val prompt = buildPrompt(word)
        Log.d(TAG, "Sending prompt for word: '$word'")
        return try {
            val response = generativeModel.generateContent(prompt)
            val rawText = response.text
            Log.d(TAG, "Raw Gemini response: $rawText")
            if (rawText == null) throw IllegalStateException("Empty Gemini response for '$word'")
            parseResponse(rawText, word)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating content for '$word': ${e.message}", e)
            throw e
        }
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
          ],
          "imageKeywords": ["keyword1", "keyword2", "keyword3", "keyword4", "keyword5"]
        }
        Rules:
        - meaning: concise Vietnamese phrase with part of speech in parentheses
        - examples: exactly 3 items, each with enSentence and viSentence
        - imageKeywords: exactly 5 SHORT English words (1-2 words max, NO phrases) representing DIFFERENT visual aspects of "$word", suitable for Flickr image search. Each keyword must be distinct and concrete. Example for "book": ["reading", "library", "textbook", "literature", "bookshelf"]. Example for "computer": ["laptop", "keyboard", "monitor", "coding", "technology"]
        - Return ONLY the JSON object, no other text
    """.trimIndent()

    private fun parseResponse(raw: String, word: String): GeminiWordInfo {
        val cleaned = raw.trim()
            .removePrefix("```json").removePrefix("```")
            .removeSuffix("```").trim()
        Log.d(TAG, "Cleaned JSON: $cleaned")
        return try {
            val dto = gson.fromJson(cleaned, GeminiWordInfoDto::class.java)
            Log.d(TAG, "Parsed meaning: ${dto.meaning}, examples: ${dto.examples?.size}, keywords: ${dto.imageKeywords?.size}")
            val imageUrls = dto.imageKeywords
                ?.filterNotNull()
                ?.filter { it.isNotBlank() }
                ?.map { keyword ->
                    val encoded = URLEncoder.encode(keyword.trim(), "UTF-8")
                    "https://loremflickr.com/400/300/$encoded"
                } ?: emptyList()
            GeminiWordInfo(
                meaning = dto.meaning.orEmpty(),
                examples = dto.examples?.map {
                    Example(enSentence = it.enSentence.orEmpty(), viSentence = it.viSentence.orEmpty())
                } ?: emptyList(),
                imageUrls = imageUrls
            )
        } catch (e: Exception) {
            Log.e(TAG, "JSON parse error: ${e.message}")
            throw e
        }
    }

    companion object {
        private const val TAG = "GeminiService"
    }
}

private data class GeminiWordInfoDto(
    val meaning: String?,
    val examples: List<ExampleDto>?,
    val imageKeywords: List<String?>?
)

private data class ExampleDto(
    val enSentence: String?,
    val viSentence: String?
)
