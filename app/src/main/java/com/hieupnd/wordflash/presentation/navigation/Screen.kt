package com.hieupnd.wordflash.presentation.navigation

sealed class Screen(val route: String) {
    object Vocabulary : Screen("vocabulary")
    object Sentence : Screen("sentence")
    object Review : Screen("review")
}
