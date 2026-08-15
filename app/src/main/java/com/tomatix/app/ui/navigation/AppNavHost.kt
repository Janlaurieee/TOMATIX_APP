package com.tomatix.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tomatix.app.ui.chatbot.ChatbotOverlay
import com.tomatix.app.ui.controls.ControlsScreen
import com.tomatix.app.ui.dashboard.DashboardScreen
import com.tomatix.app.ui.settings.SettingsScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route
            ) {
                composable(Screen.Dashboard.route) {
                    DashboardScreen()
                }
                composable(Screen.Controls.route) {
                    ControlsScreen()
                }
                composable(Screen.Settings.route) {
                    SettingsScreen()
                }
            }
        }

        ChatbotOverlay()
    }
}
