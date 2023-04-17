package com.atomykcoder.atomykplay.repository

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.TextUtils
import com.atomykcoder.atomykplay.data.Music
import com.atomykcoder.atomykplay.interfaces.MusicDaoI
import com.atomykcoder.atomykplay.utils.StorageUtil.SettingsStorage
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.CompletableFuture

class MusicDaoImpl : MusicDaoI {
    override fun fetchMusic(context: Context): CompletableFuture<ArrayList<Music>> {
        val albumArtBaseUri = Uri.parse("content://media/external/audio/albumart")
        val settingsStorage = SettingsStorage(context)
        val future = CompletableFuture<ArrayList<Music>>()
        var selection: String? = null
        if (!settingsStorage.loadScanAllMusic()) {
            //if selection is IS_MUSIC, ringtones will not appear in the list
            selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
        }
        val dataList = ArrayList<Music>()
        val projection: Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            arrayOf(
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.BITRATE,
                MediaStore.Audio.Media.GENRE
            )
        } else {
            arrayOf(
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.YEAR
            )
        }

        //Creating a cursor to store all data of a song
        val audioCursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null
        )
        //If cursor is not null then storing data inside a data list.
        if (audioCursor != null) {
            if (audioCursor.moveToFirst()) {
                val blackList = settingsStorage.loadBlackList()
                do {
                    var isSongBlacklisted = false
                    for (path in blackList) {
                        if (audioCursor.getString(4).contains(path!!)) {
                            isSongBlacklisted = true
                            break
                        }
                    }
                    if (!isSongBlacklisted) {

                        //Music title (name)
                        var sTitle: String
                        sTitle = audioCursor.getString(0)
                        if (settingsStorage.loadBeautifyName()) {
                            sTitle = sTitle.replace("&#039;", "'")
                                .replace("%20", " ")
                                .replace("_", " ")
                                .replace("&amp;", ",").trim { it <= ' ' }
                            val beautifyTags = settingsStorage.allBeautifyTag
                            val replacingTags = settingsStorage.allReplacingTag
                            if (beautifyTags.isNotEmpty() && replacingTags.isNotEmpty()) {
                                for (i in beautifyTags.indices) {
                                    sTitle = sTitle.replace(beautifyTags[i], replacingTags[i])
                                }
                            }
                        }

                        //Music Artist
                        val sArtist = audioCursor.getString(1)
                        //Music album art id
                        val sAlbumId = audioCursor.getString(2)
                        val sDuration = audioCursor.getString(3)
                        val sPath = audioCursor.getString(4)
                        val sAlbum = audioCursor.getString(5)
                        val sMimeType = audioCursor.getString(6)
                        val sSize = audioCursor.getString(7)
                        val dateInMillis = audioCursor.getLong(8)
                        val sId = audioCursor.getString(9)
                        val sYear = audioCursor.getString(10)
                        val sDateAdded = convertLongToDate(dateInMillis)
                        var sAlbumUri: String? = null
                        if (!TextUtils.isEmpty(sAlbumId)) {
                            sAlbumUri = Uri.withAppendedPath(albumArtBaseUri, sAlbumId).toString()
                        }
                        var sBitrate: String? = ""
                        var sGenre: String? = ""
                        //In android 10 or lower these fields are unavailable
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            sBitrate = audioCursor.getString(11)
                            sGenre = audioCursor.getString(12)
                        }
                        val file = File(sPath)
                        val filter = SettingsStorage(context).loadFilterDur() * 1000
                        if (file.exists()) {
                            if (sDuration != null && filter <= sDuration.toInt()) {
                                val music = Music.newBuilder()
                                    .setName(sTitle)
                                    .setArtist(sArtist)
                                    .setAlbum(sAlbum)
                                    .setAlbumUri(sAlbumUri)
                                    .setDuration(sDuration)
                                    .setPath(sPath)
                                    .setBitrate(sBitrate)
                                    .setMimeType(sMimeType)
                                    .setSize(sSize)
                                    .setGenre(sGenre ?: "")
                                    .setId(sId)
                                    .setDateAdded(sDateAdded)
                                    .setYear(sYear ?: "")
                                    .build()
                                dataList.add(music)
                            }
                        }
                    }
                } while (audioCursor.moveToNext())
                audioCursor.close()
                dataList.sortWith(Comparator.comparing { obj: Music -> obj.name })
                if (dataList.isNotEmpty()) {
                    future.complete(dataList)
                } else {
                    future.completeExceptionally(Throwable("Music's unavailable"))
                }
            } else {
                future.completeExceptionally(Throwable("Music's unavailable"))
            }
        } else {
            future.completeExceptionally(Throwable("Music's unavailable"))
        }
        return future
    }

    private fun convertLongToDate(time: Long): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ofPattern("dd MMMM yyyy").format(
                Instant.ofEpochMilli(time * 1000)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            )
        } else {
            SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                .format(Date(time * 1000))
        }
    }
}