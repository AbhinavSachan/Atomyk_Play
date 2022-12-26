package com.atomykcoder.atomykplay.helperFunctions;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.atomykcoder.atomykplay.data.Music;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class FetchMusic {

    public static void fetchMusic(ArrayList<Music> dataList, Context context) {
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        @SuppressLint("InlinedApi")
        String[] proj = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.BITRATE,
                MediaStore.Audio.Media.GENRE,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.YEAR
        };
        //Creating a cursor to store all data of a song
        Cursor audioCursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                proj,
                selection,
                null,
                null
        );

        //If cursor is not null then storing data inside a data list.
        if (audioCursor != null) {
            if (audioCursor.moveToFirst()) {
                StorageUtil.SettingsStorage settingsStorage = new StorageUtil.SettingsStorage(context);
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
                        String sTitle = audioCursor.getString(0).trim();
//                                .replace("y2mate.com -", "")
//                                .replace("&#039;", "'")
//                                .replace("%20", " ")
//                                .replace("_", " ")
//                                .replace("&amp;", ",").trim();
                        String sArtist = audioCursor.getString(1).trim();
                        String sAlbumId = audioCursor.getString(2);
                        //converting duration in readable format
                        String sDuration = audioCursor.getString(3);
                        String sPath = audioCursor.getString(4);
                        String sAlbum = audioCursor.getString(5);
                        String sMimeType = audioCursor.getString(6);
                        String sSize = audioCursor.getString(7);
                        long dateInMillis = audioCursor.getLong(10);
                        String sId = audioCursor.getString(11);
                        String sYear = audioCursor.getString(12);

                        String sBitrate = "";
                        String sGenre = "";
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            sBitrate = audioCursor.getString(8);
                            sGenre = audioCursor.getString(9);
                        }

                        String sDateAdded = convertLongToDate(dateInMillis);
                        File file = new File(sPath);
                        int filter = new StorageUtil.SettingsStorage(context).loadFilterDur() * 1000;
                        if (file.exists()) {
                            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                            if (filter <= Integer.parseInt(sDuration)) {
                                Music music = Music.newBuilder().setName(sTitle).setArtist(sArtist)
                                        .setAlbum(sAlbum).setAlbumUri("").setDuration(sDuration)
                                        .setPath(sPath).setBitrate(sBitrate).setMimeType(sMimeType)
                                        .setSize(sSize).setGenre(sGenre != null ? sGenre : "").setId(sId).setDateAdded(sDateAdded)
                                        .build();

                                dataList.add(music);
                            }
                        }
                    }
                } while (audioCursor.moveToNext());
                audioCursor.close();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Collections.sort(dataList, Comparator.comparing(Music::getName));
                }
            }
        }
    }

    private static String convertLongToDate(long time) {
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
