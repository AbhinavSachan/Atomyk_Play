package com.atomykcoder.atomykplay.helperFunctions;

import android.annotation.SuppressLint;
import android.util.Base64;

import com.atomykcoder.atomykplay.data.Music;
import com.atomykcoder.atomykplay.dataModels.LRCMap;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Helper class for lyrics
//Note: Helper classes are always static and it's alright
public class MusicHelper {
    private MusicHelper() {
        // constructor required to avoid accidentally creating any instance of this class
    }

    /**
     * This function takes unfiltered lrc data and returns a linked hashmap with timestamp as
     * keys and their assigned lyrics as values.
     *
     * @param lyrics unfiltered lrc data retrieved from megalobiz
     * @return linked hashmap with timestamp as keys and their designated lyrics as values
     */
    public static LRCMap getLrcMap(String lyrics) {
        //required variables
        LRCMap _lrcMap = new LRCMap();
        Pattern _pattern = Pattern.compile("\\[\\d\\d:\\d\\d\\.\\d\\d]\\w.*");
        ArrayList<String> _lyricsWithTimestamps = new ArrayList<>();
        ArrayList<String> _timestamps = new ArrayList<>();
        ArrayList<String> _lyrics = new ArrayList<>();

        //Separate Lyrics with timestamps from rest of the garbage
        Matcher _matcher = _pattern.matcher(lyrics);
        while (_matcher.find()) {
            //store lyrics with timestamps in separate array
            _lyricsWithTimestamps.add(_matcher.group().trim());
        }

        //Separate timestamps and lyrics in their respective arrays
        Pattern _p = Pattern.compile("\\[\\d\\d:\\d\\d");
        Matcher _m;
        for (String lyric : _lyricsWithTimestamps) {
            _m = _p.matcher(lyric);
            if (_m.find()) {
                _timestamps.add(_m.group() + "]");
                _lyrics.add(filter(lyric));
            }
        }
        //store both timestamps and lyrics into lrc map object
        _lrcMap.addAll(_timestamps, _lyrics);

        //return lrc map object
        return _lrcMap;
    }

    public static String splitLyricsByNewLine(String lyrics) {
        Pattern p = Pattern.compile("\\[");
        String result = lyrics.replaceAll(p.pattern(), "\n\\[");
        result = result.trim();
        return result;
    }


    /**
     * filter() takes a string argument lyrics. It removes any un-needed information and returns
     * filtered string lyrics which only contains characters a-z, A-z, 0-9
     *
     * @param lyrics that need to be filtered.
     * @return filtered string lyrics.
     */
    public static String filter(String lyrics) {
        Pattern p = Pattern.compile("\\[(.*?)]");
        String result = lyrics.replaceAll(p.pattern(), "");
        result = result.trim();
        return result;
    }

    /**
     * converts millis to readable time
     *
     * @param duration duration in millis
     * @return readable duration
     */
    //converting duration from millis to readable time
    @SuppressLint("DefaultLocale")
    public static String convertDuration(String duration) {
        String out;
        int dur = Integer.parseInt(duration);

        int hours = (dur / 3600000);
        int mns = (dur / 60000) % 60;
        int scs = dur % 60000 / 1000;

        if (hours == 0) {
            out = String.format("%02d:%02d", mns, scs);
        } else {
            out = String.format("%02d:%02d:%02d", hours, mns, scs);
        }
        return out;
    }

    //endregion
    //converting readable duration to milliseconds
    public static int convertToMillis(String duration) {
        int out;
        String _duration = duration.replace("[", "").replace("]", "");
        String[] numbers = _duration.split(":");
        int min = Integer.parseInt(numbers[0]);
        int second = Integer.parseInt(numbers[1]);
        min = min * (60 * 1000);
        second = second * 1000;
        out = min + second;
        return out;
    }

    public static String encode(Music music) {
        byte[] serializedMessage = new byte[0];
        if (music!= null) {
            serializedMessage = music.toByteArray();
        }
        return Base64.encodeToString(serializedMessage, Base64.DEFAULT);
    }

    public static Music decode(String encoded) {
        byte[] serializedMessage = Base64.decode(encoded, Base64.DEFAULT);
        try {
            return Music.parseFrom(serializedMessage);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return null;
    }
}
