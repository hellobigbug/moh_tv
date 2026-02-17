package com.moh.tv.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.moh.tv.player.PlayerManager
import com.moh.tv.ui.screen.main.MainScreen
import com.moh.tv.ui.screen.player.PlayerScreen
import com.moh.tv.ui.screen.settings.SettingsScreen
import java.net.URLDecoder

@Composable
fun AppNavGraph(
    navController: NavHostController,
    playerManager: PlayerManager,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route,
        modifier = modifier
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                onChannelClick = { channel ->
                    navController.navigate(
                        Screen.Player.createRoute(
                            channelId = channel.id,
                            channelUrl = channel.url,
                            channelName = channel.name
                        )
                    )
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.Player.route,
            arguments = listOf(
                navArgument("channelId") { type = NavType.LongType },
                navArgument("channelUrl") { type = NavType.StringType },
                navArgument("channelName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val channelId = backStackEntry.arguments?.getLong("channelId") ?: 0L
            val channelUrl = URLDecoder.decode(
                backStackEntry.arguments?.getString("channelUrl") ?: "",
                "UTF-8"
            )
            val channelName = URLDecoder.decode(
                backStackEntry.arguments?.getString("channelName") ?: "",
                "UTF-8"
            )

            PlayerScreen(
                onBack = { navController.popBackStack() },
                channelId = channelId,
                channelUrl = channelUrl,
                channelName = channelName,
                playerManager = playerManager
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
