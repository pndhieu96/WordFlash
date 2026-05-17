package com.hieupnd.wordflash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.hieupnd.wordflash.presentation.navigation.AppNavigation
import com.hieupnd.wordflash.ui.theme.WordFlashTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WordFlashTheme {
                AppNavigation()
            }
        }
    }
}
