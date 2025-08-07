package com.example.cursormagic.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.cursormagic.data.VideoItem
import com.google.android.exoplayer2.ExoPlayer

@Composable
fun MainNavigation(
    videos: List<VideoItem>,
    exoPlayer: ExoPlayer,
    modifier: Modifier = Modifier
) {
    var currentRoute by remember { mutableStateOf("home") }

    when (currentRoute) {
        "home" -> {
            FullScreenVerticalVideoPager(
                videos = videos,
                exoPlayer = exoPlayer,
                modifier = modifier,
                currentRoute = currentRoute,
                onRouteSelected = { route -> currentRoute = route }
            )
        }
        "explore" -> {
            // Placeholder for explore screen
            Box(modifier = modifier.fillMaxSize()) {
                Text(text = "Explore Screen")
            }
        }
        "compose" -> {
            // Placeholder for compose screen
            Box(modifier = modifier.fillMaxSize()) {
                Text(text = "Compose Screen")
            }
        }
        "live" -> {
            // Placeholder for live screen
            Box(modifier = modifier.fillMaxSize()) {
                Text(text = "Live Screen")
            }
        }
        "profile" -> {
            // Placeholder for profile screen
            Box(modifier = modifier.fillMaxSize()) {
                Text(text = "Profile Screen")
            }
        }
    }
} 