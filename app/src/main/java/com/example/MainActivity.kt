package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.MainContainerScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.ExpenseFlowTheme
import com.example.viewmodel.ExpenseViewModel

enum class AppStage {
    SPLASH,
    ONBOARDING,
    AUTH,
    MAIN
}

class MainActivity : ComponentActivity() {

    private val viewModel: ExpenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExpenseFlowTheme {
                ExpenseFlowApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ExpenseFlowApp(viewModel: ExpenseViewModel) {
    var appStage by remember { mutableStateOf(AppStage.SPLASH) }

    Crossfade(targetState = appStage, label = "app_stage_transition") { stage ->
        when (stage) {
            AppStage.SPLASH -> {
                SplashScreen(
                    onSplashFinished = {
                        // After splash, navigate to onboarding on fresh start
                        appStage = AppStage.ONBOARDING
                    }
                )
            }
            AppStage.ONBOARDING -> {
                OnboardingScreen(
                    onFinished = {
                        appStage = AppStage.MAIN
                    }
                )
            }
            AppStage.AUTH -> {
                AuthScreen(
                    onAuthSuccess = {
                        appStage = AppStage.MAIN
                    }
                )
            }
            AppStage.MAIN -> {
                MainContainerScreen(
                    viewModel = viewModel,
                    onLogout = {
                        appStage = AppStage.AUTH
                    }
                )
            }
        }
    }
}
