package com.atomykcoder.atomykplay.helperFunctions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.widget.ImageView
import com.atomykcoder.atomykplay.classes.GlideBuilt
import com.atomykcoder.atomykplay.data.Music
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class ImageLoader(context: Context) {

    /**
     * Loads image must call this method from background thread
     *
     * @param music music model
     * @return
     */
    fun loadImage( music: Music):Bitmap? {
        return loadFromMedia(music)
    }
    private fun loadFromMedia(item: Music): Bitmap? {
        var image: Bitmap? = null
        var art: ByteArray? = null // initialize to null

        if (File(item.path).exists()) {
            try {
                MediaMetadataRetriever().use { mediaMetadataRetriever ->
                    mediaMetadataRetriever.setDataSource(item.path)
                    art = mediaMetadataRetriever.embeddedPicture
                }
            } catch (ignored: IOException) {
            }

            if (art != null) { // add null check
                try {
                    image = BitmapFactory.decodeByteArray(art, 0, art!!.size)
                } catch (ignored: Exception) {
                }
            }
        }
        return image
    }
}