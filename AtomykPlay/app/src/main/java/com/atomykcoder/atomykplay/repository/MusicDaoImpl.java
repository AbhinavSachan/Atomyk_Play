package com.atomykcoder.atomykplay.repository;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.atomykcoder.atomykplay.data.Music;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;
import com.atomykcoder.atomykplay.interfaces.MusicDaoI;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class MusicDaoImpl implements MusicDaoI {
    @Override
    public CompletableFuture<ArrayList<Music>> fetchMusic(Context context) {
        Uri albumArtBaseUri = Uri.parse("content://media/external/audio/albumart");
        StorageUtil.SettingsStorage settingsStorage = new StorageUtil.SettingsStorage(context);
        CompletableFuture<ArrayList<Music>> future = new CompletableFuture<>();
        String selection = null;
        if (!settingsStorage.loadScanAllMusic()) {
            //if selection is IS_MUSIC, ringtones will not appear in the list
            selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        }
        ArrayList<Music> dataList = new ArrayList<>();
        String[] projection;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            projection = new String[]{
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
            };
        } else {
            projection = new String[]{
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
            };
        }

        //Creating a cursor to store all data of a song
        Cursor audioCursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null
        );
        //If cursor is not null then storing data inside a data list.
        if (audioCursor != null) {
            if (audioCursor.moveToFirst()) {
                ArrayList<String> blackList = settingsStorage.loadBlackList();
                do {
                    boolean isSongBlacklisted = false;
                    for (String path : blackList) {
                        if (audioCursor.getString(4).contains(path)) {
                            isSongBlacklisted = true;
                            break;
                        }
                    }


                    if (!isSongBlacklisted) {

                        //Music title (name)
                        String sTitle;
                        sTitle = audioCursor.getString(0);
                        if (settingsStorage.loadBeautifyName()) {
                            sTitle = sTitle.replace("&#039;", "'")
                                    .replace("%20", " ")
                                    .replace("_", " ")
                                    .replace("&amp;", ",").trim();
                            ArrayList<String> beautifyTags = settingsStorage.getAllBeautifyTag();
                            ArrayList<String> replacingTags = settingsStorage.getAllReplacingTag();
                            if (!beautifyTags.isEmpty() && !replacingTags.isEmpty()) {
                                for (int i = 0; i < beautifyTags.size(); i++) {
                                    sTitle = sTitle.replace(beautifyTags.get(i), replacingTags.get(i));
                                }
                            }
                        }

                        //Music Artist
                        String sArtist = audioCursor.getString(1);
                        //Music album art id
                        String sAlbumId = audioCursor.getString(2);
                        String sDuration = audioCursor.getString(3);
                        String sPath = audioCursor.getString(4);
                        String sAlbum = audioCursor.getString(5);
                        String sMimeType = audioCursor.getString(6);
                        String sSize = audioCursor.getString(7);
                        long dateInMillis = audioCursor.getLong(8);
                        String sId = audioCursor.getString(9);
                        String sYear = audioCursor.getString(10);
                        String sDateAdded = convertLongToDate(dateInMillis);

                        String sAlbumUri = null;
                        if (!TextUtils.isEmpty(sAlbumId)) {
                            sAlbumUri = Uri.withAppendedPath(albumArtBaseUri, sAlbumId).toString();
                        }

                        String sBitrate = "";
                        String sGenre = "";
                        //In android 10 or lower these fields are unavailable
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            sBitrate = audioCursor.getString(11);
                            sGenre = audioCursor.getString(12);
                        }

                        File file = new File(sPath);
                        int filter = new StorageUtil.SettingsStorage(context).loadFilterDur() * 1000;
                        if (file.exists()) {
                            if (sDuration != null && filter <= Integer.parseInt(sDuration)) {
                                Music music = Music.newBuilder()
                                        .setName(sTitle)
                                        .setArtist(sArtist)
                                        .setAlbum(sAlbum)
                                        .setAlbumUri(sAlbumUri)
                                        .setDuration(sDuration)
                                        .setPath(sPath)
                                        .setBitrate(sBitrate)
                                        .setMimeType(sMimeType)
                                        .setSize(sSize)
                                        .setGenre(sGenre != null ? sGenre : "")
                                        .setId(sId)
                                        .setDateAdded(sDateAdded)
                                        .setYear(sYear != null ? sYear : "")
                                        .build();

                                dataList.add(music);
                            }
                        }
                    }
                } while (audioCursor.moveToNext());
                audioCursor.close();
                dataList.sort(Comparator.comparing(Music::getName));
                if (!dataList.isEmpty()) {
                    future.complete(dataList);
                } else {
                    future.completeExceptionally(new Throwable("Music's unavailable"));
                }
            } else {
                future.completeExceptionally(new Throwable("Music's unavailable"));
            }
        } else {
            future.completeExceptionally(new Throwable("Music's unavailable"));
        }
        return future;
    }

    private String convertLongToDate(long time) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            return DateTimeFormatter.ofPattern("dd MMMM yyyy").format(
                    Instant.ofEpochMilli(time * 1000)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate());
        } else {
            return new java.text.SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new java.util.Date(time * 1000));
        }
    }

}
