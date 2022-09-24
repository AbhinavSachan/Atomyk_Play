package com.atomykcoder.atomykplay.function;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.util.ArrayList;

public class FetchMusic {

    public static ArrayList<MusicDataCapsule> fetchMusic(ArrayList<MusicDataCapsule> dataList, Context context) {
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        @SuppressLint("InlinedApi") String[] proj = {
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
                do {
                    String sTitle = audioCursor.getString(0);
                    String sArtist = audioCursor.getString(1);
                    String sAlbumId = audioCursor.getString(2);
                    //converting duration in readable format
                    String sLength = convertDuration(audioCursor.getString(3));
                    String sPath = audioCursor.getString(4);
                    String sAlbum = audioCursor.getString(5);
                    String sMimeType = audioCursor.getString(6);
                    String sSize = audioCursor.getString(7);
                    String sBitrate = "";
                    String sGenre = "";
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                        sBitrate = audioCursor.getString(8);
                        sGenre = audioCursor.getString(9);
                    }

                    Uri uri = Uri.parse("content://media/external/audio/albumart");
                    String sAlbumUri = Uri.withAppendedPath(uri, sAlbumId).toString();

                    MusicDataCapsule music;
                    music = new MusicDataCapsule(sTitle, sArtist, sAlbum, sAlbumUri, sLength, sPath, sBitrate, sMimeType, sSize, sGenre);
                    File file = new File(music.getsPath());
                    if (file.exists()) {
                        dataList.add(music);
                    }
                } while (audioCursor.moveToNext());
                audioCursor.close();
            }
        }
        return dataList;
    }


    //converting duration from millis to readable time
    @SuppressLint("DefaultLocale")
    public static String convertDuration(String duration) {
        String out;
        int dur = Integer.parseInt(duration);

        int hours = (dur / 3600000);
        int mns = (dur / 60000) % 60000;
        int scs = dur % 60000 / 1000;

        if (hours == 0) {
            out = String.format("%02d:%02d", mns, scs);
        } else {
            out = String.format("%02d:%02d:%02d", hours, mns, scs);
        }
        return out;
    }


}
