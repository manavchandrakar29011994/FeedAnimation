package com.example.cursormagic.ui.components

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.example.cursormagic.R
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue


fun preloadVideos(videoUrls: List<String>, player: ExoPlayer) {
    videoUrls.forEach { url ->
        val mediaItem = MediaItem.fromUri(url)
        player.addMediaItem(mediaItem)
    }
    player.prepare()
}

@Composable
fun RecommendedBottomSheet(
    suggestions: List<EpisodicSuggestion>,
    exoPlayer: ExoPlayer?,
    isPlayerReady: Boolean
) {
    Log.d("RecommendedBottomSheet", "Composable entered at: ${System.currentTimeMillis()}")

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        Log.d("RecommendedBottomSheet", "UI Column composed at: ${System.currentTimeMillis()}")
        CloseButton()
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.Black,
                    shape = RoundedCornerShape(8.dp, 8.dp, 0.dp, 0.dp)
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            HeaderTexts("title", "This is a subtitle for the series")
            Spacer(modifier = Modifier.height(24.dp))
            if (isPlayerReady && exoPlayer != null) {
                Log.d("RecommendedBottomSheet", "VideoPlayerPager shown at: ${System.currentTimeMillis()}")
                VideoPlayerPager(videos = suggestions, exoPlayer = exoPlayer)
            } else {
                Log.d("RecommendedBottomSheet", "Showing placeholder at: ${System.currentTimeMillis()}")
                Box(
                    modifier = Modifier
                        .height(231.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun CloseButton() {
    Box(
        modifier = Modifier
            .padding(end = 16.dp)
            .size(30.dp)
            .background(color = Color.Black, shape = CircleShape)
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            tint = Color.White,
            contentDescription = "close",
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun HeaderTexts(title: String, subTitle: String) {
    Text(
        text = title,
        style = TextStyle(
            fontSize = 15.sp,
            fontWeight = FontWeight.W400,
            color = Color(0xFF999999),
            textAlign = TextAlign.Center,
            letterSpacing = 0.25.sp
        )
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = subTitle,
        style = TextStyle(
            fontSize = 20.sp,
            fontWeight = FontWeight.W700,
            color = Color(0xFFDEDEDE),
            textAlign = TextAlign.Center,
            letterSpacing = 0.15.sp
        )
    )
}

data class EpisodicSuggestionResponse(
    val title: String,
    val subTitle: String,
    val suggestions: List<EpisodicSuggestion>
)

@Immutable
data class EpisodicSuggestion(
    val postId: String,
    val seriesId: String,
    val seriesName: String,
    val videoUrl: String,
    val imageUrl: String
)


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoPlayerPager(
    videos: List<EpisodicSuggestion>,
    exoPlayer: ExoPlayer
) {
    Log.d("VideoPlayerPager", "Pager Composable entered with ${videos.size} videos")

    val pagerState = rememberPagerState(pageCount = { videos.size })
    val coroutineScope = rememberCoroutineScope()
    var lastPlayedUrl by remember { mutableStateOf<String?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Observe lifecycle to pause/resume player
    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            Log.d("VideoPlayerPager", "Lifecycle event: $event")

            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.playWhenReady = false
                }
                Lifecycle.Event.ON_RESUME -> {
                    exoPlayer.playWhenReady = true
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Attach ExoPlayer listener once
    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                Log.d("VideoPlayerPager", "Playback state changed: $state")

                if (state == Player.STATE_ENDED) {
                    val nextPage = pagerState.currentPage + 1
                    if (nextPage < videos.size) {
                        coroutineScope.launch {
                            Log.d("VideoPlayerPager", "Auto-advancing to page $nextPage")

                            pagerState.animateScrollToPage(
                                page = nextPage,
                                animationSpec = spring(
                                    stiffness = Spring.StiffnessLow,
                                    dampingRatio = Spring.DampingRatioNoBouncy
                                )
                            )
                        }
                    }
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    // Change media item on page change
    LaunchedEffect(pagerState.currentPage) {
        val video = videos[pagerState.currentPage]
        if (lastPlayedUrl != video.videoUrl) {
            Log.d("VideoPlayerPager", "Switching to video: ${video.videoUrl}")

            exoPlayer.setMediaItem(MediaItem.fromUri(video.videoUrl))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
            lastPlayedUrl = video.videoUrl
        }
    }

    // UI
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 80.dp),
            key = { videos[it].postId },
            flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                snapPositionalThreshold = 0.25f,
                snapAnimationSpec = spring(
                    stiffness = Spring.StiffnessLow,
                    dampingRatio = Spring.DampingRatioNoBouncy
                )
            )
        ) { page ->
            val isFocused = page == pagerState.currentPage
            Log.d("VideoPlayerPager", "Composing page $page, isFocused: $isFocused")

            VideoPlayerPagerItem(
                videoItem = videos[page],
                isInFocus = isFocused,
                exoPlayer = exoPlayer,
                page = page,
                pagerState = pagerState
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = videos[pagerState.currentPage].seriesName,
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.W700,
                color = Color(0xFFDEDEDE),
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            PaginationDots(pagerState = pagerState)
        }
        Spacer(modifier = Modifier.height(16.dp))
        WatchNowButton()
        Spacer(modifier = Modifier.height(16.dp))

    }
}





@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VideoPlayerPagerItem(
    videoItem: EpisodicSuggestion,
    page: Int,
    pagerState: PagerState,
    isInFocus: Boolean,
    exoPlayer: ExoPlayer
) {
    val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue

    val scale = lerp(0.75f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
    val alpha = lerp(0.5f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
    val translationY = lerp(40f, 0f, 1f - pageOffset.coerceIn(0f, 1f))

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
                this.translationY = translationY
            }
            .fillMaxWidth()
            .aspectRatio(0.75f),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,

        ) {
            VideoPlayer(
                videoItem = videoItem,
                isInFocus = isInFocus,
                exoPlayer = exoPlayer
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PaginationDots(pagerState: PagerState, modifier: Modifier = Modifier) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier) {
        repeat(pagerState.pageCount) { index ->
            val isSelected = index == pagerState.currentPage
            val dotWidth by animateDpAsState(
                targetValue = if (isSelected) 24.dp else 8.dp,
                label = ""
            )
            Box(
                modifier = Modifier
                    .size(dotWidth, 8.dp)
                    .background(color = Color(0xFF3C3C3F), shape = RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
private fun WatchNowButton(
) {
    Button(
        onClick = {
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .navigationBarsPadding()
            .height(48.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFFCD0A),
            contentColor = Color.Black
        )
    ) {
        Text(
            text = "View All",
            style = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.W700,
                color = Color.Black,
                letterSpacing = 0.25.sp
            )
        )
    }
}

//@Preview(showBackground = true)
//@Composable
//fun RecommendedBottomSheetPreview() {
//    RecommendedBottomSheet(
//        suggestions = listOf(
//            EpisodicSuggestion(
//                imageUrl = "https://media-cdn-stag.sharechat.com/3553fa5_1740663485448_thumbnail.jpeg?referrer=moj-explore-service&tenant=moj",
//                videoUrl = "https://media-cdn-stag.sharechat.com/3553fa5_1740663485448.mp4?referrer=moj-explore-service&tenant=moj",
//                seriesName = "Series 1",
//                seriesId = "1",
//                postId = "123"
//            ),
//            EpisodicSuggestion(
//                imageUrl = "https://media-cdn-stag.sharechat.com/1eae3c2b_1740663532354_thumbnail.jpeg?referrer=moj-explore-service&tenant=moj",
//                videoUrl = "https://media-cdn-stag.sharechat.com/1eae3c2b_1740663532354.mp4?referrer=moj-explore-service&tenant=moj",
//                seriesName = "Series 2",
//                seriesId = "2",
//                postId = "456"
//            ),
//            EpisodicSuggestion(
//                imageUrl = "https://cdn-stag.sharechat.com/3a2be18_1670328458844_thumbnail.jpeg?referrer=moj-explore-service&tenant=moj",
//                videoUrl = "https://cdn-stag.sharechat.com/3a2be18_1670328458844.mp4?referrer=moj-explore-service&tenant=moj",
//                seriesName = "Series 3",
//                seriesId = "5",
//                postId = "789"
//            ),
//            EpisodicSuggestion(
//                imageUrl = "https://media-cdn-stag.sharechat.com/369b9273_1694164716952_thumbnail.jpeg?referrer=moj-explore-service&tenant=moj",
//                videoUrl = "https://media-cdn-stag.sharechat.com/369b9273_1694164716952.mp4?referrer=moj-explore-service&tenant=moj",
//                seriesName = "Series 4",
//                seriesId = "6",
//                postId = "101112"
//            )
//        )
//    )
//}


private val CardCornerRadius = 8.dp
private val GoldenBorderColor = Color(0xFFFFC122)

@Composable
fun VideoPlayer(
    videoItem: EpisodicSuggestion,
    isInFocus: Boolean,
    onVideoCompleted: () -> Unit = {},
    modifier: Modifier = Modifier,
    onItemClicked: (EpisodicSuggestion) -> Unit = {},
    exoPlayer: ExoPlayer
) {
    Log.d("VideoPlayer", "Composing VideoPlayer for ${videoItem.seriesName}, isInFocus: $isInFocus")

    val context = LocalContext.current

    Box(
        modifier = modifier
            .width(231.dp)
            .aspectRatio(0.75f)
            .clip(RoundedCornerShape(CardCornerRadius))
            .border(
                width = 1.dp,
                color = GoldenBorderColor,
                shape = RoundedCornerShape(CardCornerRadius)
            )
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(videoItem.imageUrl)
                .crossfade(true)
                .scale(Scale.FILL)
                .build(),
            contentDescription = videoItem.seriesName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(CardCornerRadius))
                .clickable {
                    onItemClicked(videoItem)
                }
        )

        if (isInFocus) {
            ActualVideoPlayer(
                videoUrl = videoItem.videoUrl,
                onVideoCompleted = onVideoCompleted,
                isInFocus = isInFocus,
                exoPlayer = exoPlayer
            )
        }
    }
}

@Composable
fun ActualVideoPlayer(
    videoUrl: String,
    exoPlayer: ExoPlayer,
    isInFocus: Boolean,
    onVideoCompleted: () -> Unit
) {
    Log.d("ActualVideoPlayer", "Composing ActualVideoPlayer for $videoUrl, isInFocus: $isInFocus")

    val onVideoCompletedState = rememberUpdatedState(onVideoCompleted)
    val isFirstFrameRendered = remember { mutableStateOf(false) }

    // Attach and detach player logic based on focus
    DisposableEffect(isInFocus) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    // first frame will render soon
                    isFirstFrameRendered.value = true
                }
                if (state == Player.STATE_ENDED) {
                    onVideoCompletedState.value()
                }
            }
        }

        if (isInFocus) {
            val currentUri = exoPlayer.currentMediaItem?.localConfiguration?.uri
            if (currentUri != Uri.parse(videoUrl)) {
                exoPlayer.setMediaItem(MediaItem.fromUri(videoUrl))
                exoPlayer.prepare()
            }
            exoPlayer.playWhenReady = true
            exoPlayer.addListener(listener)
        } else {
            exoPlayer.playWhenReady = false
            exoPlayer.removeListener(listener)
            exoPlayer.clearVideoSurface() // Optional: clear surface when not focused
        }

        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    // Attach surface only if in focus
    if (isInFocus) {
        CenterCropVideoPlayer(exoPlayer = exoPlayer)
    }
}



@Composable
fun CenterCropVideoPlayer(exoPlayer: ExoPlayer) {
    val context = LocalContext.current

    val playerView = remember {
        (LayoutInflater.from(context)
            .inflate(R.layout.layout_exo_player, null) as? PlayerView)?.apply {
            // Optional visual settings
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            clipToOutline = true
            useController = false
        }
    }

    // Rebind player safely when exoPlayer changes
    LaunchedEffect(exoPlayer) {
        playerView?.player = exoPlayer
    }

    playerView?.let {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { playerView }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VideoPlayerPreview() {
    val context = LocalContext.current

    VideoPlayer(
        videoItem = EpisodicSuggestion(
            imageUrl = "https://media-cdn-stag.sharechat.com/3553fa5_1740663485448_thumbnail.jpeg?referrer=moj-explore-service&tenant=moj",
            videoUrl = "https://media-cdn-stag.sharechat.com/3553fa5_1740663485448.mp4?referrer=moj-explore-service&tenant=moj",
            seriesName = "Series 1",
            seriesId = "1",
            postId = "123"
        ),
        isInFocus = true,
        exoPlayer = remember {
            ExoPlayer.Builder(context).build().apply {
                playWhenReady = true
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun VideoPlayerPagerPreview() {
    val context = LocalContext.current
    val episodicSuggestionResponse = EpisodicSuggestionResponse(
        title = "Recommended Series",
        subTitle = "This is a subtitle for the series",
        suggestions = listOf(
            EpisodicSuggestion(
                imageUrl = "https://media-cdn-stag.sharechat.com/3553fa5_1740663485448_thumbnail.jpeg?referrer=moj-explore-service&tenant=moj",
                videoUrl = "https://media-cdn-stag.sharechat.com/3553fa5_1740663485448.mp4?referrer=moj-explore-service&tenant=moj",
                seriesName = "Series 1",
                seriesId = "1",
                postId = "123"
            ),
            EpisodicSuggestion(
                imageUrl = "https://media-cdn-stag.sharechat.com/1eae3c2b_1740663532354_thumbnail.jpeg?referrer=moj-explore-service&tenant=moj",
                videoUrl = "https://media-cdn-stag.sharechat.com/1eae3c2b_1740663532354.mp4?referrer=moj-explore-service&tenant=moj",
                seriesName = "Series 2",
                seriesId = "2",
                postId = "456"
            ),
            EpisodicSuggestion(
                imageUrl = "https://cdn-stag.sharechat.com/3a2be18_1670328458844_thumbnail.jpeg?referrer=moj-explore-service&tenant=moj",
                videoUrl = "https://cdn-stag.sharechat.com/3a2be18_1670328458844.mp4?referrer=moj-explore-service&tenant=moj",
                seriesName = "Series 3",
                seriesId = "5",
                postId = "789"
            ),
            EpisodicSuggestion(
                imageUrl = "https://media-cdn-stag.sharechat.com/369b9273_1694164716952_thumbnail.jpeg?referrer=moj-explore-service&tenant=moj",
                videoUrl = "https://media-cdn-stag.sharechat.com/369b9273_1694164716952.mp4?referrer=moj-explore-service&tenant=moj",
                seriesName = "Series 4",
                seriesId = "6",
                postId = "101112"
            )
        )
    )
    VideoPlayerPager(
        videos = episodicSuggestionResponse.suggestions,
        exoPlayer = remember {
            ExoPlayer.Builder(context).build().apply {
                playWhenReady = true
            }
        }
    )
}

