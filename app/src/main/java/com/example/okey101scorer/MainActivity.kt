package com.example.okey101scorer

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.example.okey101scorer.ui.theme.Okey101ScorerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize system splash screen for cold start transition
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Remove system splash screen immediately to show Compose animation without any flicker/delay
        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            splashScreenViewProvider.remove()
        }
        
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()

        val viewModel = ViewModelProvider(this)[ScoreViewModel::class.java]

        setContent {
            Okey101ScorerTheme {
                SplashNavHost(viewModel = viewModel)
            }
        }
    }
}