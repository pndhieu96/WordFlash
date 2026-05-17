package com.hieupnd.wordflash.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hieupnd.wordflash.AppConfig
import com.hieupnd.wordflash.data.local.dao.VocabularyCardDao
import com.hieupnd.wordflash.data.local.entity.ExampleJson
import com.hieupnd.wordflash.data.local.entity.VocabularyCardEntity
import com.hieupnd.wordflash.data.remote.api.DictionaryApi
import com.hieupnd.wordflash.data.remote.api.ImageSearchApi
import com.hieupnd.wordflash.data.remote.api.TranslationApi
import com.hieupnd.wordflash.domain.model.DictionaryEntry
import com.hieupnd.wordflash.domain.model.Example
import com.hieupnd.wordflash.domain.model.VocabularyCard
import com.hieupnd.wordflash.domain.model.WordDefinition
import com.hieupnd.wordflash.domain.model.WordMeaning
import com.hieupnd.wordflash.domain.repository.VocabularyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class VocabularyRepositoryImpl @Inject constructor(
    private val dao: VocabularyCardDao,
    private val api: DictionaryApi,
    private val translationApi: TranslationApi,
    private val imageSearchApi: ImageSearchApi,
    private val gson: Gson
) : VocabularyRepository {

    override suspend fun searchWord(word: String): Result<DictionaryEntry> = runCatching {
        val response = api.searchWord(word)
        val first = response.first()
        val ipa = first.phonetics?.firstOrNull { !it.text.isNullOrEmpty() }?.text
            ?: first.phonetic.orEmpty()
        val audioUrl = first.phonetics?.firstOrNull { !it.audio.isNullOrEmpty() }?.audio.orEmpty()
        val meanings = first.meanings?.map { m ->
            WordMeaning(
                partOfSpeech = m.partOfSpeech,
                definitions = m.definitions?.map { d ->
                    WordDefinition(definition = d.definition, example = d.example.orEmpty())
                } ?: emptyList()
            )
        } ?: emptyList()
        val wordType = meanings.firstOrNull()?.partOfSpeech.orEmpty()
        DictionaryEntry(word = first.word, ipa = ipa, audioUrl = audioUrl, meanings = meanings, wordType = wordType)
    }

    override suspend fun translateToVietnamese(text: String): Result<String> = runCatching {
        val response = translationApi.translate(text)
        response.responseData?.translatedText ?: ""
    }

    override suspend fun searchImages(query: String): Result<List<String>> = runCatching {
        val response = imageSearchApi.searchImages(
            apiKey = AppConfig.PIXABAY_API_KEY,
            query = query.trim()
        )
        response.hits.map { it.webformatURL }
    }

    override fun getAllCards(): Flow<List<VocabularyCard>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun saveCard(card: VocabularyCard) = dao.insert(card.toEntity())

    override suspend fun updateCard(card: VocabularyCard) = dao.update(card.toEntity())

    override suspend fun deleteCard(id: String) = dao.deleteById(id)

    override suspend fun updateMemorizationLevel(id: String, level: Int) {
        val now = System.currentTimeMillis()
        dao.updateMemorizationLevel(id, level, now, now)
    }

    override suspend fun getCardByWord(word: String): VocabularyCard? =
        dao.getByWord(word)?.toDomain()

    override suspend fun getAllCardsOnce(): List<VocabularyCard> =
        dao.getAllOnce().map { it.toDomain() }

    override suspend fun markAllSynced() = dao.markAllSynced()

    private fun VocabularyCardEntity.toDomain(): VocabularyCard {
        val type = object : TypeToken<List<ExampleJson>>() {}.type
        val exampleJsons: List<ExampleJson> = runCatching {
            gson.fromJson<List<ExampleJson>>(examples, type)
        }.getOrDefault(emptyList())
        return VocabularyCard(
            id = id,
            word = word,
            ipa = ipa,
            audioUrl = audioUrl,
            meaning = meaning,
            examples = exampleJsons.map { Example(it.enSentence, it.viSentence) },
            memorizationLevel = memorizationLevel,
            updatedAt = updatedAt,
            isSynced = isSynced,
            wordType = wordType,
            imageUrl = imageUrl,
            lastReviewedAt = lastReviewedAt
        )
    }

    private fun VocabularyCard.toEntity(): VocabularyCardEntity {
        val exampleJsons = examples.map { ExampleJson(it.enSentence, it.viSentence) }
        return VocabularyCardEntity(
            id = id,
            word = word,
            ipa = ipa,
            audioUrl = audioUrl,
            meaning = meaning,
            examples = gson.toJson(exampleJsons),
            memorizationLevel = memorizationLevel,
            updatedAt = updatedAt,
            isSynced = isSynced,
            wordType = wordType,
            imageUrl = imageUrl,
            lastReviewedAt = lastReviewedAt
        )
    }
}
