package com.hieupnd.wordflash.data.remote.model

import com.google.gson.annotations.SerializedName

data class DictionaryResponse(
    @SerializedName("word") val word: String,
    @SerializedName("phonetic") val phonetic: String?,
    @SerializedName("phonetics") val phonetics: List<PhoneticResponse>?,
    @SerializedName("meanings") val meanings: List<MeaningResponse>?
)

data class PhoneticResponse(
    @SerializedName("text") val text: String?,
    @SerializedName("audio") val audio: String?
)

data class MeaningResponse(
    @SerializedName("partOfSpeech") val partOfSpeech: String,
    @SerializedName("definitions") val definitions: List<DefinitionResponse>?
)

data class DefinitionResponse(
    @SerializedName("definition") val definition: String,
    @SerializedName("example") val example: String?
)
