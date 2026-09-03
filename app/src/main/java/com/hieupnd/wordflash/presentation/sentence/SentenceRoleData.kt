package com.hieupnd.wordflash.presentation.sentence

import androidx.annotation.StringRes
import com.hieupnd.wordflash.R

data class SentenceRole(
    val key: String,
    val enName: String,
    @StringRes val viNameRes: Int,
    @StringRes val descriptionRes: Int
)

object EnglishSentenceRoles {
    val ALL = listOf(
        SentenceRole(
            "subject", "Subject",
            R.string.role_subject_name,
            R.string.role_subject_desc
        ),
        SentenceRole(
            "predicate", "Predicate",
            R.string.role_predicate_name,
            R.string.role_predicate_desc
        ),
        SentenceRole(
            "direct_object", "Direct Object",
            R.string.role_direct_object_name,
            R.string.role_direct_object_desc
        ),
        SentenceRole(
            "indirect_object", "Indirect Object",
            R.string.role_indirect_object_name,
            R.string.role_indirect_object_desc
        ),
        SentenceRole(
            "complement", "Complement",
            R.string.role_complement_name,
            R.string.role_complement_desc
        ),
        SentenceRole(
            "adverbial", "Adverbial",
            R.string.role_adverbial_name,
            R.string.role_adverbial_desc
        ),
        SentenceRole(
            "modifier", "Modifier",
            R.string.role_modifier_name,
            R.string.role_modifier_desc
        ),
        SentenceRole(
            "appositive", "Appositive",
            R.string.role_appositive_name,
            R.string.role_appositive_desc
        ),
        SentenceRole(
            "relative_clause", "Relative Clause",
            R.string.role_relative_clause_name,
            R.string.role_relative_clause_desc
        ),
        SentenceRole(
            "noun_clause", "Noun Clause",
            R.string.role_noun_clause_name,
            R.string.role_noun_clause_desc
        ),
        SentenceRole(
            "adverb_clause", "Adverb Clause",
            R.string.role_adverb_clause_name,
            R.string.role_adverb_clause_desc
        ),
    )
}
