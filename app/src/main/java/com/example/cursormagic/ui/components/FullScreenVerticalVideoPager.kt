package com.example.cursormagic.ui.components

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
import com.example.cursormagic.data.VideoItem
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullScreenVerticalVideoPager(
    videos: List<VideoItem>,
    exoPlayer: ExoPlayer,
    modifier: Modifier = Modifier,
    currentRoute: String = "home",
    onRouteSelected: (String) -> Unit = {}
) {
    val pagerState = rememberPagerState(pageCount = { videos.size })
    val coroutineScope = rememberCoroutineScope()
    var lastPlayedUrl by remember { mutableStateOf<String?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Optimized animation states - reduced state variables
    var showThumbnailTransition by remember { mutableStateOf(false) }
    var thumbnailVideoIndex by remember { mutableStateOf(0) }
    var swipeDirection by remember { mutableStateOf(0) }

    // Debounced page change handler to reduce operations during scroll
    var lastPageChangeTime by remember { mutableStateOf(0L) }

    // Enhanced page change handling - immediate video playback
    LaunchedEffect(pagerState.currentPage) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastPageChangeTime > 100) { // Reduced debounce for faster response
            val currentPage = pagerState.currentPage
            val pageDifference = currentPage - (currentPage - 1)

            if (pageDifference != 0) {
                swipeDirection = if (pageDifference > 0) 1 else -1

                // Determine which video to show in the transition
                thumbnailVideoIndex = when {
                    pageDifference > 0 -> currentPage - 1
                    pageDifference < 0 -> currentPage + 1
                    else -> currentPage
                }

                // Show the animation without blocking video
                if (thumbnailVideoIndex >= 0 && thumbnailVideoIndex < videos.size) {
                    showThumbnailTransition = true
                    // Animation: 1s main + 3s bounce + 1s collapse = 5s total
                    delay(5000) // Total animation duration
                    showThumbnailTransition = false
                }

                lastPageChangeTime = currentTime
            }
        }
    }

    // Optimized lifecycle observer
    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> exoPlayer.playWhenReady = false
                Lifecycle.Event.ON_RESUME -> exoPlayer.playWhenReady = true
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Optimized player listener - only attach once
    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    val nextPage = pagerState.currentPage + 1
                    if (nextPage < videos.size) {
                        coroutineScope.launch {
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

    // Immediate media item switching - video plays instantly
    LaunchedEffect(pagerState.currentPage) {
        val video = videos[pagerState.currentPage]
        if (lastPlayedUrl != video.videoUrl) {
            exoPlayer.setMediaItem(MediaItem.fromUri(video.videoUrl))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
            lastPlayedUrl = video.videoUrl
        }
    }

    // Optimized pager with better fling behavior
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        VerticalPager(
            state = pagerState,
            key = { videos[it].id },
            flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                snapPositionalThreshold = 0.3f, // Increased threshold
                snapAnimationSpec = spring(
                    stiffness = Spring.StiffnessMedium, // Faster spring
                    dampingRatio = Spring.DampingRatioMediumBouncy
                )
            )
        ) { page ->
            val isFocused = page == pagerState.currentPage

            FullScreenVideoItem(
                videoItem = videos[page],
                isInFocus = isFocused,
                exoPlayer = exoPlayer
            )
        }

        // Optimized overlay UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = videos[pagerState.currentPage].title,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.W600,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Video counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${pagerState.currentPage + 1}/${videos.size}",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W500,
                        color = Color.White
                    )
                )
            }
        }

        // Bottom Navigation
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            VideoBottomNavigation(
                currentRoute = currentRoute,
                onRouteSelected = onRouteSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Non-blocking thumbnail transition - video plays immediately
        if (showThumbnailTransition && thumbnailVideoIndex >= 0 && thumbnailVideoIndex < videos.size) {
            EnhancedThumbnailTransition(
                videoItem = videos[thumbnailVideoIndex],
                swipeDirection = swipeDirection,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun EnhancedThumbnailTransition(
    videoItem: VideoItem,
    swipeDirection: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var animationProgress by remember { mutableStateOf(0f) }

    // Animation with bounce and collapse into Explore icon
    LaunchedEffect(Unit) {
        // First: Complete the main animation (0-1 second)
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        ) { value, _ ->
            animationProgress = value
        }

        // Second: Bounce 3 times (1-4 seconds) - 3 seconds total
        repeat(3) {
            // Bounce up
            animate(
                initialValue = 1f,
                targetValue = 1.2f,
                animationSpec = tween(500, easing = FastOutSlowInEasing)
            ) { value, _ ->
                animationProgress = value
            }
            // Bounce down
            animate(
                initialValue = 1.2f,
                targetValue = 1f,
                animationSpec = tween(500, easing = FastOutSlowInEasing)
            ) { value, _ ->
                animationProgress = value
            }
        }

        // Third: Collapse into Explore icon (4-5 seconds)
        animate(
            initialValue = 4f,
            targetValue = 5f,
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        ) { value, _ ->
            animationProgress = value
        }
    }

    // Calculate dimensions and positions for each stage
    val scaleX: Float
    val scaleY: Float
    val translationX: Float
    val translationY: Float
    val cornerRadius: Float
    val alpha: Float
    val rotation: Float

    when {
        animationProgress <= 1f -> {
            // Stage 1: Full screen shrinking to final position
            scaleX = lerp(1f, 0.18f, animationProgress)
            scaleY = lerp(1f, 0.18f, animationProgress)
            translationX = lerp(0f, -180f, animationProgress)
            translationY = lerp(0f, 640f, animationProgress)
            cornerRadius = lerp(0f, 28f, animationProgress)
            alpha = lerp(1f, 1f, animationProgress)
            rotation = 0f
        }
        animationProgress <= 4f -> {
            // Stage 2: Bounce phase - calculate bounce effect
            val bounceTime = (animationProgress - 1f) / 3f // 0 to 1 over 3 seconds
            val bounceCycle = (bounceTime * 3).toInt() // Which bounce cycle (0, 1, 2)
            val cycleProgress = (bounceTime * 3) % 1f // Progress within current cycle

            // Calculate vertical bounce movement only (no scaling)
            val bounceY = if (cycleProgress < 0.5f) {
                // Bounce up - move upward
                lerp(640f, 540f, cycleProgress * 2f)
            } else {
                // Bounce down - move back to original position
                lerp(540f, 640f, (cycleProgress - 0.5f) * 2f)
            }

            scaleX = 0.18f // Fixed scale, no bouncing
            scaleY = 0.18f // Fixed scale, no bouncing
            translationX = -180f
            translationY = bounceY
            cornerRadius = 28f
            alpha = 1f
            rotation = 0f

            // Debug log
            Log.d("Animation", "Bounce phase - progress: $animationProgress, bounceY: $bounceY")
        }
        else -> {
            // Stage 3: Collapse into Explore icon with "going inside" effect
            val collapseProgress = (animationProgress - 4f) / 1f

            // Create a "sucking" effect - scale down while moving to icon
            val scale = lerp(0.18f, 0.02f, collapseProgress) // Much smaller final scale
            scaleX = scale
            scaleY = scale

            // Move to Explore icon position with slight curve
            val curveProgress = collapseProgress * collapseProgress // Easing curve
            translationX = lerp(-180f, -180f, curveProgress)
            translationY = lerp(640f, 890f, curveProgress) // Move much lower to Explore icon

            // Round corners more as it shrinks
            cornerRadius = lerp(28f, 4f, collapseProgress)

            // Fade out as it goes inside
            alpha = lerp(1f, 0.3f, collapseProgress * 0.7f) // Start fading early


            // Debug log
            Log.d("Animation", "Collapse progress: $collapseProgress, scale: $scale, alpha: $alpha")
        }
    }



    Box(
        modifier = modifier
            .graphicsLayer {
                this.scaleX = scaleX
                this.scaleY = scaleY
                this.transformOrigin = TransformOrigin(
                    pivotFractionX = 0.5f,
                    pivotFractionY = 0.5f
                )
                this.translationX = translationX
                this.translationY = translationY
                this.alpha = alpha
                // this.rotationZ = rotation
            }
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(Color(0xFFF5F5F5)) // Light grey background like screenshot
            .border(
                width = 1.dp,
                color = Color.DarkGray.copy(alpha = 0.4f),
                shape = RoundedCornerShape(cornerRadius.dp)
            )
    ) {
        // Video thumbnail
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(videoItem.thumbnailUrl)
                .crossfade(false)
                .scale(Scale.FILL)
                .build(),
            contentDescription = videoItem.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Continue button - visible throughout animation with fade-in
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .graphicsLayer {
                    // Fade in from 0.3 to 1.0 over the animation
                    this.alpha = lerp(0.3f, 1f, animationProgress)
                }
        ) {
            Button(
                onClick = { /* Handle continue action */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700) // Yellow color
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Continue",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 48.sp
                )
            }
        }


        // Close button - visible throughout animation with fade-in
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .graphicsLayer {
                    // Fade in from 0.3 to 1.0 over the animation
                    this.alpha = lerp(0.1f, 1f, animationProgress)
                }
        ) {
            IconButton(
                onClick = { /* Handle close action */ },
                modifier = Modifier
                    .wrapContentSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(140.dp)
                )
            }
        }
    }

}

@Composable
private fun FullScreenVideoItem(
    videoItem: VideoItem,
    isInFocus: Boolean,
    exoPlayer: ExoPlayer
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Show thumbnail when not in focus
        if (!isInFocus) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(videoItem.thumbnailUrl)
                    .crossfade(false) // Disabled crossfade
                    .scale(Scale.FILL)
                    .build(),
                contentDescription = videoItem.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Show video player when in focus
        if (isInFocus) {
            FullScreenVideoPlayer(
                videoUrl = videoItem.videoUrl,
                isInFocus = isInFocus,
                exoPlayer = exoPlayer
            )
        }
    }
}

@Composable
fun FullScreenVideoPlayer(
    videoUrl: String,
    exoPlayer: ExoPlayer,
    isInFocus: Boolean
) {
    // Immediate video player attachment - no delays
    if (isInFocus) {
        LaunchedEffect(videoUrl) {
            val currentUri = exoPlayer.currentMediaItem?.localConfiguration?.uri
            if (currentUri != Uri.parse(videoUrl)) {
                exoPlayer.setMediaItem(MediaItem.fromUri(videoUrl))
                exoPlayer.prepare()
            }
            exoPlayer.playWhenReady = true
        }

        FullScreenCenterCropVideoPlayer(exoPlayer = exoPlayer)
    }
}

@Composable
fun FullScreenCenterCropVideoPlayer(exoPlayer: ExoPlayer) {
    val context = LocalContext.current

    val playerView = remember {
        (LayoutInflater.from(context)
            .inflate(R.layout.layout_exo_player, null) as? PlayerView)?.apply {
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            clipToOutline = true
            useController = false
        }
    }

    // Optimized player binding
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