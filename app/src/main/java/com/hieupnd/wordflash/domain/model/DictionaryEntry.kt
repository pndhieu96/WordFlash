package com.hieupnd.wordflash.domain.model

data class DictionaryEntry(
    val word: String,
    val ipa: String,
    val audioUrl: String,
    val meanings: List<WordMeaning>,
    val wordType: String = ""
)

data class WordMeaning(
    val partOfSpeech: String,
    val definitions: List<WordDefinition>
)

data class WordDefinition(
    val definition: String,
    val example: String
)
