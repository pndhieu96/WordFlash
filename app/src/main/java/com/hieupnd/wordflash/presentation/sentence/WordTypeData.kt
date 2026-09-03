package com.hieupnd.wordflash.presentation.sentence

import androidx.annotation.StringRes
import com.hieupnd.wordflash.R

data class WordType(
    val key: String,
    val enName: String,
    @StringRes val viNameRes: Int,
    @StringRes val descriptionRes: Int,
    @StringRes val positionNoteRes: Int
)

object EnglishWordTypes {
    val ALL = listOf(
        WordType(
            "noun", "Noun",
            R.string.type_noun_name,
            R.string.type_noun_desc,
            R.string.type_noun_pos
        ),
        WordType(
            "verb", "Verb",
            R.string.type_verb_name,
            R.string.type_verb_desc,
            R.string.type_verb_pos
        ),
        WordType(
            "adjective", "Adjective",
            R.string.type_adjective_name,
            R.string.type_adjective_desc,
            R.string.type_adjective_pos
        ),
        WordType(
            "adverb", "Adverb",
            R.string.type_adverb_name,
            R.string.type_adverb_desc,
            R.string.type_adverb_pos
        ),
        WordType(
            "pronoun", "Pronoun",
            R.string.type_pronoun_name,
            R.string.type_pronoun_desc,
            R.string.type_pronoun_pos
        ),
        WordType(
            "preposition", "Preposition",
            R.string.type_preposition_name,
            R.string.type_preposition_desc,
            R.string.type_preposition_pos
        ),
        WordType(
            "conjunction", "Conjunction",
            R.string.type_conjunction_name,
            R.string.type_conjunction_desc,
            R.string.type_conjunction_pos
        ),
        WordType(
            "article", "Article",
            R.string.type_article_name,
            R.string.type_article_desc,
            R.string.type_article_pos
        ),
        WordType(
            "determiner", "Determiner",
            R.string.type_determiner_name,
            R.string.type_determiner_desc,
            R.string.type_determiner_pos
        ),
        WordType(
            "numeral", "Numeral",
            R.string.type_numeral_name,
            R.string.type_numeral_desc,
            R.string.type_numeral_pos
        ),
        WordType(
            "interjection", "Interjection",
            R.string.type_interjection_name,
            R.string.type_interjection_desc,
            R.string.type_interjection_pos
        ),
        WordType(
            "particle", "Particle",
            R.string.type_particle_name,
            R.string.type_particle_desc,
            R.string.type_particle_pos
        ),
    )
}
