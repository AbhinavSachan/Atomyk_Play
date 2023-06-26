package com.atomykcoder.atomykplay.classes

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

class GlideBuilt(private val context: Context) {
    private val requestOptions = RequestOptions()
    fun loadFromUri(
        uri: String?,
        placeholderImage: Int,
        imageView: ImageView?,
        image_measure: Int
    ) {
        Glide.with(context).load(uri).apply(
            RequestOptions().placeholder(placeholderImage).error(R.drawable.ic_music)
        )
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .transition(DrawableTransitionOptions.withCrossFade(300))
            .override(image_measure, image_measure)
            .into(imageView!!)
    }

    fun loadFromRes(res: Int?, imageView: ImageView?) {
        Glide.with(context).load(res)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(imageView!!)
    }

    fun loadAlbumArt(
        path: String?,
        placeholderImage: Int,
        imageView: ImageView?,
        image_measure: Int,
        animate: Boolean
    ) {

        val drawableTransitionOptions: DrawableTransitionOptions = if (animate) {
            DrawableTransitionOptions.withCrossFade(300)
        } else {
            DrawableTransitionOptions().dontTransition()
        }
        imageView?.let {
            GlideApp.with(context).load(path?.let { AudioFileCover(it) }).apply(
                requestOptions.placeholder(
                    placeholderImage
                ).error(R.drawable.ic_music)
            )
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .skipMemoryCache(true)
                .transition(drawableTransitionOptions)
                .override(image_measure, image_measure)
                .into(it)
        }
    }

    fun loadFromBitmap(
        bitmap: Bitmap?,
        placeholderImage: Int,
        imageView: ImageView?,
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
            imageView?.let {
                Glide.with(context).load(bitmap).apply(
                    requestOptions.placeholder(
                        shadowed
                    ).error(R.drawable.ic_music)
                )
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .transition(drawableTransitionOptions)
                    .override(image_measure, image_measure)
                    .into(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pauseRequest() {
        if (!Glide.with(context).isPaused) {
            Glide.with(context).pauseRequests()
        }
    }

    fun resumeRequest() {
        if (Glide.with(context).isPaused) {
            Glide.with(context).resumeRequests()
        }
    }
}