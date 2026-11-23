package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audio.LocalSoundManager
import com.example.ui.AppDestination
import com.example.ui.GameViewModel
import com.example.ui.screens.DesignShowcaseScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PlayScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StagesScreen
import com.example.ui.theme.ArcadeBg
import com.example.ui.theme.VoltDashTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoltDashTheme {
                VoltDashRoot()
            }
        }
    }
}

@Composable
fun VoltDashRoot(viewModel: GameViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                viewModel.onAppBackgrounded()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    when (uiState.destination) {
        AppDestination.PLAY -> {
            BackHandler {
                if (uiState.isPaused || uiState.snapshot.isPlayerOut || uiState.snapshot.stageCleared) {
                    viewModel.navigateTo(AppDestination.HOME)
                } else {
                    viewModel.setPaused(true)
                }
            }
        }
        AppDestination.DESIGN, AppDestination.STAGES -> {
            BackHandler {
                viewModel.navigateTo(AppDestination.HOME)
            }
        }
        AppDestination.HOME -> {
            BackHandler {
                viewModel.navigateTo(AppDestination.SPLASH)
            }
        }
        AppDestination.SPLASH -> Unit
    }

    CompositionLocalProvider(LocalSoundManager provides viewModel.soundManager) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ArcadeBg)
        ) {
            AnimatedContent(
                targetState = uiState.destination,
                transitionSpec = {
                    fadeIn(animationSpec = tween(240)).togetherWith(fadeOut(animationSpec = tween(200)))
                },
                label = "root_destination_transition"
            ) { dest ->
                when (dest) {
                    AppDestination.SPLASH -> SplashScreen(
                        activeLevelNumber = uiState.activeLevelNumber,
                        equippedCharacterId = uiState.equippedCharacterId,
                        equippedRoadId = uiState.equippedRoadId,
                        onStartGame = { viewModel.startGameFromTitle() },
                        onOpenHome = { viewModel.enterHomeFromSplash() },
                        modifier = Modifier.fillMaxSize()
                    )
                    AppDestination.HOME -> HomeScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                    AppDestination.PLAY -> PlayScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                    AppDestination.DESIGN -> DesignShowcaseScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                    AppDestination.STAGES -> StagesScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
