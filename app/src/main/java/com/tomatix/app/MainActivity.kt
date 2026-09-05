package com.tomatix.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.tomatix.app.ui.navigation.AppNavHost
import com.tomatix.app.ui.theme.TomatixTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            splashScreenView.iconView.animate()
                .scaleX(1.18f)
                .scaleY(1.18f)
                .alpha(0f)
                .setDuration(280L)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()

            splashScreenView.view.animate()
                .alpha(0f)
                .setDuration(360L)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction { splashScreenView.remove() }
                .start()
        }
        enableEdgeToEdge()
        requestNotificationPermission()
        setContent {
            TomatixTheme {
                val navController = rememberNavController()
                AppNavHost(navController = navController)
            }
        }
    }

    private fun requestNotificationPermission() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_REQUEST)
        }
    }

    private companion object {
        const val NOTIFICATION_PERMISSION_REQUEST = 1001
    }
}
