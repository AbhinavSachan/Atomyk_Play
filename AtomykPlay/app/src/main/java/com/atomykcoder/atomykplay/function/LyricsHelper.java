package com.atomykcoder.atomykplay.function;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Helper class for lyrics
//Note: Helper classes are always static and it's alright
 public class LyricsHelper {
    private LyricsHelper()
    {
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
        LRCMap _lrcMap = new LRCMap();
        Pattern _pattern = Pattern.compile("\\[\\d\\d:\\d\\d");
        Matcher _timestamps = _pattern.matcher(lyrics);
        String _filteredLyrics = filter(lyrics);
        String[] lines = _filteredLyrics.split("\n");
        int i = 0;
        while (_timestamps.find() && i < lines.length) {
            _lrcMap.add(_timestamps.group() + "]", lines[i]);
            i++;
        }
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
}
