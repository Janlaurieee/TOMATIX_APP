package com.tomatix.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tomatix.app.R
import com.tomatix.app.ui.chatbot.ChatbotOverlay
import com.tomatix.app.ui.controls.ControlsScreen
import com.tomatix.app.ui.dashboard.DashboardScreen
import com.tomatix.app.ui.settings.SettingsScreen
import com.tomatix.app.ui.theme.Green100
import com.tomatix.app.ui.theme.Gray500
import com.tomatix.app.ui.theme.Gray800
import com.tomatix.app.ui.theme.White

@Composable
fun AppNavHost(navController: NavHostController) {
    Scaffold(
        // Custom bars share the real safe area, including side navigation in landscape.
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout)),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { TomatixHeader() },
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
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

            ChatbotOverlay()
        }
    }
}

@Composable
private fun TomatixHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(13.dp))
                        .background(Green100),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        painter = painterResource(R.drawable.tomatix_ai_mark),
                        contentDescription = "Tomatix",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(33.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = "Tomatix",
                        color = Gray800,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Smart Agriculture System",
                        color = Gray500
                    )
                }
            }
        }
    }
}
