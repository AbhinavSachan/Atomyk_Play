package com.atomykcoder.atomykplay.helperFunctions;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FetchMusic {

    public static void fetchMusic(ArrayList<MusicDataCapsule> dataList, Context context) {
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
                MediaStore.Audio.Media._ID
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
                        for(String path : blackList) {
                            if (audioCursor.getString(4).contains(path))
                                isSongBlacklisted = true;
                                break;
                        }

                    if(!isSongBlacklisted) {
                        String sTitle = audioCursor.getString(0)
                                .replace("y2mate.com -", "")
                                .replace("&#039;", "'")
                                .replace("%20", " ")
                                .replace("_", " ")
                                .replace("&amp;", ",").trim();
                        String sArtist = audioCursor.getString(1).trim();
                        String sAlbumId = audioCursor.getString(2);
                        //converting duration in readable format
                        String sLength = audioCursor.getString(3);
                        String sPath = audioCursor.getString(4);
                        String sAlbum = audioCursor.getString(5);
                        String sMimeType = audioCursor.getString(6);
                        String sSize = audioCursor.getString(7);
                        String sId = audioCursor.getString(11);

                        String sBitrate = "";
                        String sGenre = "";
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            sBitrate = audioCursor.getString(8);
                            sGenre = audioCursor.getString(9);
                        }


                        Uri uri = Uri.parse("content://media/external/audio/albumart");
                        String sAlbumUri = Uri.withAppendedPath(uri, sAlbumId).toString();

                        int filter = new StorageUtil.SettingsStorage(context).loadFilterDur() * 1000;
                        MusicDataCapsule music = new MusicDataCapsule(sTitle, sArtist,
                                sAlbum, sAlbumUri, sLength, sPath, sBitrate, sMimeType, sSize, sGenre, sId);
                        File file = new File(music.getsPath());
                        if (file.exists()) {
                            if (filter <= Integer.parseInt(music.getsLength())) {
                                dataList.add(music);
                            }
                        }
                    }
                } while (audioCursor.moveToNext());
                audioCursor.close();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Collections.sort(dataList, Comparator.comparing(MusicDataCapsule::getsName));
                }
            }
        }

    }

}
