package com.atomykcoder.atomykplay.helperFunctions;

import android.annotation.SuppressLint;
import android.util.Base64;

import com.atomykcoder.atomykplay.data.Music;
import com.atomykcoder.atomykplay.models.LRCMap;
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
        LRCMap lrcMap = new LRCMap();
        Pattern pattern = Pattern.compile("\\[(\\d\\d):(\\d\\d\\.\\d\\d)]\\w.*");
        ArrayList<String> lyricsWithTimestamps = new ArrayList<>();
        ArrayList<String> timestamps = new ArrayList<>();
        ArrayList<String> lyricsList = new ArrayList<>();

        Matcher matcher = pattern.matcher(lyrics);
        while (matcher.find()) {
            lyricsWithTimestamps.add(matcher.group().trim());
        }

        Pattern timestampPattern = Pattern.compile("\\[(\\d\\d):(\\d\\d\\.\\d\\d)]");
        for (String lyric : lyricsWithTimestamps) {
            Matcher timestampMatcher = timestampPattern.matcher(lyric);
            if (timestampMatcher.find()) {
                timestamps.add(timestampMatcher.group());
                lyricsList.add(filter(lyric));
            }
        }

        lrcMap.addAll(timestamps, lyricsList);

        return lrcMap;
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

    @SuppressLint("DefaultLocale")
    public static String convertDurationForLyrics(String duration) {
        int dur = Integer.parseInt(duration);

        int minutes = (dur / 60000) % 60;
        int seconds = (dur % 60000) / 1000;
        int milliseconds = dur % 1000;

        return String.format("%02d:%02d.%02d", minutes, seconds, milliseconds / 10);
    }

    /**
     * converts millis to readable time for text purpose
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

    public static int convertToMillis(String duration) {
        String _duration = duration.replace("[", "").replace("]", "");

        String[] numbers = _duration.split(":");
        int minutes = Integer.parseInt(numbers[0]);
        String[] secondsAndMillis = numbers[1].split("\\.");
        int seconds = Integer.parseInt(secondsAndMillis[0]);
        int milliseconds = Integer.parseInt(secondsAndMillis[1]);

        return (minutes * 60 * 1000) + (seconds * 1000) + milliseconds;
    }

    public static String encode(Music music) {
        byte[] serializedMessage = new byte[0];
        if (music != null) {
            serializedMessage = music.toByteArray();
        }
        return Base64.encodeToString(serializedMessage, Base64.DEFAULT);
    }

    public static Music decode(String encoded) {
        if (encoded == null) {
            return null;
        }
        byte[] serializedMessage = Base64.decode(encoded, Base64.DEFAULT);
        try {
            return Music.parseFrom(serializedMessage);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return null;
    }
}
