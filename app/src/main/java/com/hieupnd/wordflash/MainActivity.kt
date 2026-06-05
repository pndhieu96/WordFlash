package com.hieupnd.wordflash

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.hieupnd.wordflash.notification.NotificationHelper
import com.hieupnd.wordflash.presentation.navigation.AppNavigation
import com.hieupnd.wordflash.presentation.navigation.Screen
import com.hieupnd.wordflash.ui.theme.WordFlashTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val _navigateToRoute = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            WordFlashTheme {
                AppNavigation(
                    pendingRoute = _navigateToRoute.value,
                    onRoutePending = { _navigateToRoute.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == NotificationHelper.ACTION_NAVIGATE_TO_REVIEW) {
            _navigateToRoute.value = Screen.Review.route
        }
    }
}
