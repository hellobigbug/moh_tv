package com.moh.tv.ui

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.moh.tv.player.PlayerManager
import com.moh.tv.ui.navigation.AppNavGraph
import com.moh.tv.ui.theme.AppleTVColors
import com.moh.tv.ui.theme.MOHTVTheme
import com.moh.tv.worker.SourceUpdateWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var playerManager: PlayerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 启动自动更新任务
        SourceUpdateWorker.schedule(this)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            MOHTVTheme {
                var isAppReady by remember { mutableStateOf(false) }
                var showSplash by remember { mutableStateOf(true) }
                
                LaunchedEffect(Unit) {
                    lifecycleScope.launch {
                        delay(800)
                        showSplash = false
                    }
                    lifecycleScope.launch {
                        delay(500)
                        isAppReady = true
                    }
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppleTVColors.Background)
                ) {
                    AnimatedVisibility(
                        visible = showSplash,
                        exit = fadeOut(animationSpec = androidx.compose.animation.core.tween(300))
                    ) {
                        SplashScreen()
                    }
                    
                    AnimatedVisibility(
                        visible = isAppReady && !showSplash,
                        enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(300))
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            val navController = rememberNavController()
                            AppNavGraph(
                                navController = navController,
                                playerManager = playerManager
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    override fun onDestroy() {
        super.onDestroy()
        playerManager.release()
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppleTVColors.Background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = AppleTVColors.Primary,
            strokeWidth = 4.dp
        )
    }
}
