package com.atomykcoder.atomykplay.utils

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment.getExternalStorageDirectory
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.content.contentValuesOf
import androidx.core.net.toUri
import com.atomykcoder.atomykplay.data.Music
import java.io.File
import java.io.IOException
object MusicUtil {
    fun createShareSongFileIntent(context: Context, song: Music?): Intent? {
        if (song != null){
            return Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(
                    Intent.EXTRA_STREAM, try {
                        FileProvider.getUriForFile(
                            context,
                            context.applicationContext.packageName,
                            File(song.path)
                        )
                    } catch (e: IllegalArgumentException) {
                        getSongFileUri(song.id.toLong())
                    }
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                type = "audio/*"
            }
        }else{
            return null
        }
    }

    fun createAlbumArtFile(context: Context): File {
        return File(
            createAlbumArtDir(context),
            System.currentTimeMillis().toString()
        )
    }

    private fun createAlbumArtDir(context: Context): File {
        val albumArtDir = File(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) context.cacheDir else getExternalStorageDirectory(),
            "/albumthumbs/"
        )
        if (!albumArtDir.exists()) {
            albumArtDir.mkdirs()
            try {
                File(albumArtDir, ".nomedia").createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return albumArtDir
    }

    fun deleteAlbumArt(context: Context, albumId: Long) {
        val contentResolver = context.contentResolver
        val localUri = "content://media/external/audio/albumart".toUri()
        contentResolver.delete(ContentUris.withAppendedId(localUri, albumId), null, null)
        contentResolver.notifyChange(localUri, null)
    }

    @JvmStatic
    fun getMediaStoreAlbumCoverUri(albumId: Long): Uri {
        val sArtworkUri = "content://media/external/audio/albumart".toUri()
        return ContentUris.withAppendedId(sArtworkUri, albumId)
    }

    fun getSongFileUri(songId: Long): Uri {
        return ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            songId
        )
    }

    fun insertAlbumArt(
        context: Context,
        albumId: Long,
        path: String?,
    ) {
        val contentResolver = context.contentResolver
        val artworkUri = "content://media/external/audio/albumart".toUri()
        contentResolver.delete(ContentUris.withAppendedId(artworkUri, albumId), null, null)
        val values = contentValuesOf(
            "album_id" to albumId,
            "_data" to path
        )
        contentResolver.insert(artworkUri, values)
        contentResolver.notifyChange(artworkUri, null)
    }

}