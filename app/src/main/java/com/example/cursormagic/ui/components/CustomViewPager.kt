package com.example.cursormagic.ui.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * Custom ViewPager implementation from scratch
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomViewPager(
    pageCount: Int,
    modifier: Modifier = Modifier,
    orientation: Orientation = Orientation.Vertical,
    initialPage: Int = 0,
    onPageChanged: (Int) -> Unit = {},
    content: @Composable (page: Int) -> Unit
) {
    require(pageCount > 0) { "Page count must be greater than 0" }
    require(initialPage in 0 until pageCount) { "Initial page must be in range [0, $pageCount)" }

    val pagerState = rememberCustomPagerState(
        pageCount = pageCount,
        initialPage = initialPage,
        onPageChanged = onPageChanged
    )

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val density = LocalDensity.current
        val maxSize = if (orientation == Orientation.Vertical) {
            with(density) { constraints.maxHeight.toDp() }
        } else {
            with(density) { constraints.maxWidth.toDp() }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
        ) {
            repeat(pageCount) { page ->
                val pageOffset = pagerState.getPageOffset(page)
                val isVisible = pageOffset.absoluteValue < 1.5f

                if (isVisible) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset {
                                val offset = if (orientation == Orientation.Vertical) {
                                    IntOffset(0, (pageOffset * maxSize.value).roundToInt())
                                } else {
                                    IntOffset((pageOffset * maxSize.value).roundToInt(), 0)
                                }
                                offset
                            }
                            .graphicsLayer {
                                alpha = 1f - pageOffset.absoluteValue.coerceIn(0f, 0.5f)
                                scaleX = 1f - pageOffset.absoluteValue.coerceIn(0f, 0.1f)
                                scaleY = 1f - pageOffset.absoluteValue.coerceIn(0f, 0.1f)
                            }
                    ) {
                        content(page)
                    }
                }
            }
        }

        // Gesture handling
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .draggable(
//                    state = rememberDraggableState { delta ->
//                        pagerState.dispatchRawDelta(delta)
//                    },
//                    orientation = orientation,
//                    onDragStopped = { velocity ->
//                        pagerState.fling(velocity)
//                    }
//                )
//        )
    }
}

@Composable
fun rememberCustomPagerState(
    pageCount: Int,
    initialPage: Int = 0,
    onPageChanged: (Int) -> Unit = {}
): CustomPagerState {
    return remember {
        CustomPagerState(
            pageCount = pageCount,
            initialPage = initialPage,
            onPageChanged = onPageChanged
        )
    }
}

class CustomPagerState(
    private val pageCount: Int,
    initialPage: Int = 0,
    private val onPageChanged: (Int) -> Unit = {}
) {
    private var _currentPage by mutableStateOf(initialPage)
    private var _currentPageOffset by mutableStateOf(0f)
    private var _isDragging by mutableStateOf(false)

    val currentPage: Int get() = _currentPage
    val currentPageOffset: Float get() = _currentPageOffset
    val isDragging: Boolean get() = _isDragging

    fun getPageOffset(page: Int): Float {
        return (page - currentPage) + currentPageOffset
    }

    fun dispatchRawDelta(delta: Float) {
        _isDragging = true
        _currentPageOffset -= delta / 1000f // Adjust sensitivity
        
        // Snap to nearest page
        if (_currentPageOffset.absoluteValue > 0.5f) {
            val newPage = if (_currentPageOffset > 0) {
                (_currentPage - 1).coerceAtLeast(0)
            } else {
                (_currentPage + 1).coerceAtMost(pageCount - 1)
            }
            
            if (newPage != _currentPage) {
                _currentPage = newPage
                onPageChanged(newPage)
            }
            _currentPageOffset = 0f
        }
    }

    fun fling(velocity: Float) {
        _isDragging = false
        
        // Determine target page based on velocity and current offset
        val targetPage = when {
            velocity > 500f || _currentPageOffset < -0.3f -> {
                (_currentPage + 1).coerceAtMost(pageCount - 1)
            }
            velocity < -500f || _currentPageOffset > 0.3f -> {
                (_currentPage - 1).coerceAtLeast(0)
            }
            else -> currentPage
        }
        
        if (targetPage != _currentPage) {
            _currentPage = targetPage
            onPageChanged(targetPage)
        }
        _currentPageOffset = 0f
    }

    fun animateToPage(
        page: Int,
        animationSpec: AnimationSpec<Float> = TweenSpec(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    ) {
        if (page in 0 until pageCount && page != _currentPage) {
            _currentPage = page
            onPageChanged(page)
            _currentPageOffset = 0f
        }
    }
}

/**
 * Vertical ViewPager with video support
 */
@Composable
fun CustomVerticalVideoPager(
    videos: List<String>,
    modifier: Modifier = Modifier,
    onPageChanged: (Int) -> Unit = {},
    content: @Composable (page: Int, videoUrl: String) -> Unit
) {
    CustomViewPager(
        pageCount = videos.size,
        modifier = modifier,
        orientation = Orientation.Vertical,
        onPageChanged = onPageChanged
    ) { page ->
        content(page, videos[page])
    }
}

/**
 * Horizontal ViewPager with video support
 */
@Composable
fun CustomHorizontalVideoPager(
    videos: List<String>,
    modifier: Modifier = Modifier,
    onPageChanged: (Int) -> Unit = {},
    content: @Composable (page: Int, videoUrl: String) -> Unit
) {
    CustomViewPager(
        pageCount = videos.size,
        modifier = modifier,
        orientation = Orientation.Horizontal,
        onPageChanged = onPageChanged
    ) { page ->
        content(page, videos[page])
    }
} 