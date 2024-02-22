package com.atomykcoder.atomykplay.utils

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import com.atomykcoder.atomykplay.R
import com.atomykcoder.atomykplay.helperFunctions.AudioFileCover
import com.atomykcoder.atomykplay.helperFunctions.GlideApp
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions

fun ImageView.loadImageFromUri(
    uri: String?,
    placeholderImage: Int,
    image_measure: Int
) {

    Glide.with(context).load(uri).apply(
        RequestOptions().placeholder(placeholderImage).error(R.drawable.ic_music)
    )
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .skipMemoryCache(true)
        .transition(DrawableTransitionOptions.withCrossFade(300))
        .override(image_measure, image_measure)
        .into(this)
}

fun ImageView.loadImageFromRes(res: Int?) {
    Glide.with(context).load(res)
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .skipMemoryCache(true)
        .into(this)
}

fun ImageView.loadAlbumArt(
    path: String?,
    placeholderImage: Int,
    image_measure: Int,
    animate: Boolean
) {

    val drawableTransitionOptions: DrawableTransitionOptions = if (animate) {
        DrawableTransitionOptions.withCrossFade(300)
    } else {
        DrawableTransitionOptions().dontTransition()
    }
    GlideApp.with(context).load(path?.let { AudioFileCover(it) }).apply(
        RequestOptions().placeholder(
            placeholderImage
        ).error(R.drawable.ic_music)
    )
        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        .skipMemoryCache(true)
        .transition(drawableTransitionOptions)
        .override(image_measure, image_measure)
        .into(this)
}

fun ImageView.loadImageFromBitmap(
    bitmap: Bitmap?,
    placeholderImage: Int,
    image_measure: Int,
    animate: Boolean
) {

    val shadowed = if (bitmap == null) {
        placeholderImage
    } else {
        0
    }
    val drawableTransitionOptions: DrawableTransitionOptions = if (animate) {
        DrawableTransitionOptions.withCrossFade(300)
    } else {
        DrawableTransitionOptions().dontTransition()
    }
    try {
        Glide.with(context).load(bitmap).apply(
            RequestOptions().placeholder(
                shadowed
            ).error(R.drawable.ic_music)
        )
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .transition(drawableTransitionOptions)
            .override(image_measure, image_measure)
            .into(this)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.pauseGlideRequest() {
    if (!Glide.with(this).isPaused) {
        Glide.with(this).pauseRequests()
    }
}

fun Context.resumeGlideRequest() {
    if (Glide.with(this).isPaused) {
        Glide.with(this).resumeRequests()
    }
}
