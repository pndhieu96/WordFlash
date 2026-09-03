package com.hieupnd.wordflash.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hieupnd.wordflash.presentation.review.ReviewScreen
import com.hieupnd.wordflash.presentation.sentence.SentenceScreen
import com.hieupnd.wordflash.presentation.settings.SettingsScreen
import com.hieupnd.wordflash.presentation.vocabulary.VocabularyScreen
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import com.hieupnd.wordflash.R

private data class NavItem(
    val screen: Screen,
    @StringRes val labelRes: Int,
    val icon: ImageVector
)

private val navItems = listOf(
    NavItem(Screen.Vocabulary, R.string.nav_vocabulary, Icons.Default.AutoStories),
    NavItem(Screen.Sentence, R.string.nav_sentence, Icons.Default.FormatQuote),
    NavItem(Screen.Review, R.string.nav_review, Icons.Default.School)
)

@Composable
fun AppNavigation(
    pendingRoute: String? = null,
    onRoutePending: () -> Unit = {}
) {
    val navController = rememberNavController()

    LaunchedEffect(pendingRoute) {
        pendingRoute?.let {
            navController.navigate(it) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
            onRoutePending()
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != Screen.Settings.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val currentDestination = navBackStackEntry?.destination
                    navItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = stringResource(item.labelRes)) },
                            label = { Text(stringResource(item.labelRes)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Vocabulary.route
        ) {
            composable(Screen.Vocabulary.route) {
                VocabularyScreen(
                    innerPadding = innerPadding,
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }
            composable(Screen.Sentence.route) {
                SentenceScreen(
                    innerPadding = innerPadding,
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }
            composable(Screen.Review.route) {
                ReviewScreen(
                    innerPadding = innerPadding,
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    innerPadding = innerPadding,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
