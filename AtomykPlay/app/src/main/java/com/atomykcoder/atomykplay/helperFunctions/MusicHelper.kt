package com.atomykcoder.atomykplay.helperFunctions
import android.annotation.SuppressLint
import com.atomykcoder.atomykplay.models.LRCMap
import com.atomykcoder.atomykplay.models.Music
import com.google.gson.Gson
import java.util.regex.Pattern

/**
 * Helper class for working with music-related functionalities, including lyrics and duration conversion.
 *
 * Note: Helper classes are always static, and it's alright.
 */
object MusicHelper {

    /**
     * This function takes unfiltered lrc data and returns a linked hashmap with timestamp as
     * keys and their assigned lyrics as values.
     *
     * @param lyrics Unfiltered lrc data retrieved from megalobiz.
     * @return Linked hashmap with timestamp as keys and their designated lyrics as values.
     */
    @JvmStatic
    fun getLrcMap(lyrics: String): LRCMap {
        val lrcMap = LRCMap()
        val pattern = Pattern.compile("\\[(\\d\\d):(\\d\\d\\.\\d\\d)]\\w.*")
        val lyricsWithTimestamps = ArrayList<String>()
        val timestamps = ArrayList<String>()
        val lyricsList = ArrayList<String>()

        val matcher = pattern.matcher(lyrics)
        while (matcher.find()) {
            lyricsWithTimestamps.add(matcher.group().trim())
        }

        val timestampPattern = Pattern.compile("\\[(\\d\\d):(\\d\\d\\.\\d\\d)]")
        for (lyric in lyricsWithTimestamps) {
            val timestampMatcher = timestampPattern.matcher(lyric)
            if (timestampMatcher.find()) {
                timestamps.add(timestampMatcher.group())
                lyricsList.add(filter(lyric))
            }
        }

        lrcMap.addAll(timestamps, lyricsList)

        return lrcMap
    }

    /**
     * Splits lyrics by new line characters and adds a new line before each timestamp.
     *
     * @param lyrics Lyrics to be split.
     * @return Lyrics with new lines before timestamps.
     */
    @JvmStatic
    fun splitLyricsByNewLine(lyrics: String): String {
        val p = Pattern.compile("\\[")
        var result = lyrics.replace(p.toRegex(), "\n\\[")
        result = result.trim()
        return result
    }

    /**
     * Filter function removes any unneeded information from the lyrics.
     *
     * @param lyrics Lyrics that need to be filtered.
     * @return Filtered lyrics containing only characters a-z, A-Z, 0-9.
     */
    @JvmStatic
    fun filter(lyrics: String): String {
        val p = Pattern.compile("\\[(.*?)]")
        var result = lyrics.replace(p.toRegex(), "")
        result = result.trim()
        return result
    }

    /**
     * Converts duration from milliseconds to a readable time format for lyrics.
     *
     * @param duration Duration in milliseconds.
     * @return Readable duration format.
     */
    @JvmStatic
    @SuppressLint("DefaultLocale")
    fun convertDurationForLyrics(duration: String): String {
        val dur = duration.toInt()

        val minutes = (dur / 60000) % 60
        val seconds = (dur % 60000) / 1000
        val milliseconds = dur % 1000

        return String.format("%02d:%02d.%02d", minutes, seconds, milliseconds / 10)
    }

    /**
     * Converts duration from milliseconds to a readable time format.
     *
     * @param duration Duration in milliseconds.
     * @return Readable duration format.
     */
    @JvmStatic
    @SuppressLint("DefaultLocale")
    fun convertDuration(duration: String): String {
        val dur = duration.toInt()
        val hours = dur / 3600000
        val mns = (dur / 60000) % 60
        val scs = dur % 60000 / 1000

        return if (hours == 0) {
            String.format("%02d:%02d", mns, scs)
        } else {
            String.format("%02d:%02d:%02d", hours, mns, scs)
        }
    }

    /**
     * Converts a formatted duration string to milliseconds.
     *
     * @param duration Formatted duration string.
     * @return Duration in milliseconds.
     */
    @JvmStatic
    fun convertToMillis(duration: String): Int {
        val _duration = duration.replace("[", "").replace("]", "")
        val numbers = _duration.split(":")
        val minutes = numbers[0].toInt()
        val secondsAndMillis = numbers[1].split("\\.")
        val seconds = secondsAndMillis[0].toInt()
        val milliseconds = secondsAndMillis[1].toInt()

        return (minutes * 60 * 1000) + (seconds * 1000) + milliseconds
    }

    /**
     * Encodes a Music object into a JSON-encoded string.
     *
     * @param music Music object to be encoded.
     * @return JSON-encoded string.
     */
    @JvmStatic
    fun encode(music: Music?): String {
        return Gson().toJson(music)
    }

    /**
     * Decodes a JSON-encoded string into a Music object.
     *
     * @param encoded JSON-encoded string.
     * @return Decoded Music object or null if decoding fails.
     */
    @JvmStatic
    fun decode(encoded: String?): Music? {
        if (encoded == null) {
            return null
        }
        return try {
            Gson().fromJson(encoded, Music::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

