package com.moh.tv.ui.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Player : Screen("player/{channelId}/{channelUrl}/{channelName}") {
        fun createRoute(channelId: Long, channelUrl: String, channelName: String): String {
            return "player/$channelId/${java.net.URLEncoder.encode(channelUrl, "UTF-8")}/${java.net.URLEncoder.encode(channelName, "UTF-8")}"
        }
    }
    object Settings : Screen("settings")
}
