package com.example.cursormagic.ui.components

import android.content.Context
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource

fun buildPreloadingExoPlayer(context: Context): ExoPlayer {
    val cache = CacheProvider.getCache(context)

    val dataSourceFactory = DefaultDataSource.Factory(context).let {
        CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(it)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    return ExoPlayer.Builder(context)
        .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
        .build()
}

