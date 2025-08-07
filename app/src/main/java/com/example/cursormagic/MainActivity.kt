package com.example.cursormagic

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cursormagic.data.VideoItem
import com.example.cursormagic.ui.components.EpisodicSuggestion
import com.example.cursormagic.ui.components.RecommendedBottomSheet
import com.example.cursormagic.ui.components.RecommendedSeriesBottomSheetDialog
import com.example.cursormagic.ui.theme.CursorMagicTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sampleVideos = listOf(
            VideoItem(
                id = "1",
                title = "Video 1",
                thumbnailUrl = "https://media-cdn-stag.sharechat.com/3553fa5_1740663485448_thumbnail.jpeg?referrer=moj-explore-service&tenant=moj",
                videoUrl = "https://media-cdn-stag.sharechat.com/3553fa5_1740663485448.mp4?referrer=moj-explore-service&tenant=moj",
                isLive = false
            ),
            VideoItem(
                id = "2",
                title = "Video 2",
                thumbnailUrl = "https://media-cdn-stag.sharechat.com/1eae3c2b_1740663532354_thumbnail.jpeg?referrer=moj-explore-service&tenant=moj",
                videoUrl = "https://media-cdn-stag.sharechat.com/1eae3c2b_1740663532354.mp4?referrer=moj-explore-service&tenant=moj",
                isLive = false
            ),
            VideoItem(
                id = "5",
                title = "Video 5",
                thumbnailUrl = "https://cdn-stag.sharechat.com/3a2be18_1670328458844_thumbnail.jpeg?referrer=moj-explore-service&tenant=moj",
                videoUrl = "https://cdn-stag.sharechat.com/3a2be18_1670328458844.mp4?referrer=moj-explore-service&tenant=moj",
                isLive = false
            ),
            VideoItem(
                id = "6",
                title = "Video 6",
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

                    val suggestions = listOf(
                        EpisodicSuggestion(imageUrl = "https://media-cdn-stag.sharechat.com/3553fa5_1740663485448_thumbnail.jpeg?referrer=moj-explore-service&tenant=moj", videoUrl = "https://media-cdn-stag.sharechat.com/3553fa5_1740663485448.mp4?referrer=moj-explore-service&tenant=moj", seriesName = "Series 1", seriesId = "1", postId = "123" ),
                        EpisodicSuggestion(imageUrl = "https://media-cdn-stag.sharechat.com/1eae3c2b_1740663532354_thumbnail.jpeg?referrer=moj-explore-service&tenant=moj", videoUrl = "https://media-cdn-stag.sharechat.com/1eae3c2b_1740663532354.mp4?referrer=moj-explore-service&tenant=moj", seriesName = "Series 2", seriesId = "2", postId = "456" ),
                        EpisodicSuggestion(imageUrl = "https://cdn-stag.sharechat.com/3a2be18_1670328458844_thumbnail.jpeg?referrer=moj-explore-service&tenant=moj", videoUrl = "https://cdn-stag.sharechat.com/3a2be18_1670328458844.mp4?referrer=moj-explore-service&tenant=moj", seriesName = "Series 3", seriesId = "5", postId = "789" ),
                        EpisodicSuggestion(imageUrl = "https://media-cdn-stag.sharechat.com/369b9273_1694164716952_thumbnail.jpeg?referrer=moj-explore-service&tenant=moj", videoUrl = "https://media-cdn-stag.sharechat.com/369b9273_1694164716952.mp4?referrer=moj-explore-service&tenant=moj", seriesName = "Series 4", seriesId = "6", postId = "101112" )
                    )
                    
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { showVideoPlayerBottomSheet(suggestions) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFCD0A),
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .padding(16.dp)
                                .height(56.dp)
                        ) {
                            Text(
                                text = "Show Horizontal Video Player",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { 
                                val intent = Intent(this@MainActivity, VerticalVideoActivity::class.java)
                                startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .padding(16.dp)
                                .height(56.dp)
                        ) {
                            Text(
                                text = "Show Vertical Video Pager",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    private fun showVideoPlayerBottomSheet(videos: List<EpisodicSuggestion>) {
        val bottomSheet = RecommendedSeriesBottomSheetDialog(suggestion = videos)
        bottomSheet.show(supportFragmentManager, RecommendedSeriesBottomSheetDialog.TAG)
    }
} 