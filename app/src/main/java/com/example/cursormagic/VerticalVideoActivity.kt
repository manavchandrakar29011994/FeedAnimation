package com.example.cursormagic

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.cursormagic.data.VideoItem
import com.example.cursormagic.ui.components.MainNavigation
import com.example.cursormagic.ui.theme.CursorMagicTheme
import com.google.android.exoplayer2.ExoPlayer

class VerticalVideoActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val sampleVideos = listOf(
            VideoItem(
                id = "1",
                title = "Amazing Nature Video",
                thumbnailUrl = "https://media-cdn-stag.sharechat.com/3553fa5_1740663485448_thumbnail.jpeg?referrer=moj-explore-service&tenant=moj",
                videoUrl = "https://media-cdn-stag.sharechat.com/3553fa5_1740663485448.mp4?referrer=moj-explore-service&tenant=moj",
                isLive = false
            ),
            VideoItem(
                id = "2",
                title = "Adventure Time",
                thumbnailUrl = "https://media-cdn-stag.sharechat.com/1eae3c2b_1740663532354_thumbnail.jpeg?referrer=moj-explore-service&tenant=moj",
                videoUrl = "https://media-cdn-stag.sharechat.com/1eae3c2b_1740663532354.mp4?referrer=moj-explore-service&tenant=moj",
                isLive = false
            ),
            VideoItem(
                id = "3",
                title = "City Life",
                thumbnailUrl = "https://cdn-stag.sharechat.com/3a2be18_1670328458844_thumbnail.jpeg?referrer=moj-explore-service&tenant=moj",
                videoUrl = "https://cdn-stag.sharechat.com/3a2be18_1670328458844.mp4?referrer=moj-explore-service&tenant=moj",
                isLive = false
            ),
            VideoItem(
                id = "4",
                title = "Ocean Waves",
                thumbnailUrl = "https://media-cdn-stag.sharechat.com/369b9273_1694164716952_thumbnail.jpeg?referrer=moj-explore-service&tenant=moj",
                videoUrl = "https://media-cdn-stag.sharechat.com/369b9273_1694164716952.mp4?referrer=moj-explore-service&tenant=moj",
                isLive = false
            )
        )

        setContent {
            CursorMagicTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    
                    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
                    
                    LaunchedEffect(Unit) {
                        exoPlayer = ExoPlayer.Builder(context).build().apply {
                            playWhenReady = true
                        }
                    }
                    
                    DisposableEffect(Unit) {
                        onDispose {
                            exoPlayer?.release()
                            exoPlayer = null
                        }
                    }
                    
                    exoPlayer?.let { player ->
                        MainNavigation(
                            videos = sampleVideos,
                            exoPlayer = player,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}