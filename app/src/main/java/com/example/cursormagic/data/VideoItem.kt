package com.example.cursormagic.data

data class VideoItem(
    val id: String,
    val title: String,
    val thumbnailUrl: String,
    val videoUrl: String,
    val isLive: Boolean = false
) 