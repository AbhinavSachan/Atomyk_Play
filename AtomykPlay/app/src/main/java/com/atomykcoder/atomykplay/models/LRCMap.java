package com.atomykcoder.atomykplay.models;

import java.util.ArrayList;

public class LRCMap {

    private ArrayList<String> timestamps = new ArrayList<>();
    private ArrayList<String> lyrics = new ArrayList<>();

    /**
     * make a lrcmap with given timestamps and lyrics
     *
     * @param _timestamps timestamps
     * @param _lyrics     lyrics
     */
    public LRCMap(ArrayList<String> _timestamps, ArrayList<String> _lyrics) {
        timestamps = _timestamps;
        lyrics = _lyrics;
    }

    // default constructor
    public LRCMap() {
    }

    /**
     * Get Stamp at given index
     *
     * @param i index
     * @return stamp at given index
     */
    public String getStampAt(int i) {
        return timestamps.get(i);
    }

    /**
     * Get lyric at given index
     *
     * @param i index
     * @return lyric at given index
     */
    public String getLyricAt(int i) {
        return lyrics.get(i);
    }

    /**
     * get all timestamps
     *
     * @return timestamps array list
     */
    public ArrayList<String> getStamps() {
        return timestamps;
    }

    /**
     * get all lyrics
     *
     * @return lyrics array list
     */
    public ArrayList<String> getLyrics() {
        return lyrics;
    }

    /**
     * get lyrics assigned to given timestamp
     *
     * @param stamp stamp, which is used to find lyrics assigned to that stamp
     * @return lyric assigned to given stamp
     */
    public String get(String stamp) {
        if (timestamps.contains(stamp)) {
            int i = timestamps.indexOf(stamp);
            return lyrics.get(i);
        } else return null;
    }

    /**
     * get Index of given stamp
     *
     * @param stamp
     * @return returns index of given stamp if exist else returns -1
     */
    public int getIndexAtStamp(String stamp) {
        if (timestamps.contains(stamp))
            return timestamps.indexOf(stamp);
        else return -1;
    }

    /**
     * push given stamp and lyrics to the end of their respective array
     *
     * @param _stamp timestamp
     * @param _lyric lyrics
     */
    public void add(String _stamp, String _lyric) {
        timestamps.add(_stamp);
        lyrics.add(_lyric);
    }

    /**
     * push given arrays of stamps and lyrics  to the end of their respective array
     *
     * @param _stamps timestamps
     * @param _lyrics lyrics
     */
    public void addAll(ArrayList<String> _stamps, ArrayList<String> _lyrics) {
        timestamps.addAll(_stamps);
        lyrics.addAll(_lyrics);
    }

    /**
     * push a given lrc map to the end of already existing lrcmap
     *
     * @param lrcMap lrcmap object
     */
    public void addAll(LRCMap lrcMap) {
        timestamps.addAll(lrcMap.getStamps());
        lyrics.addAll(lrcMap.getLyrics());
    }

    /**
     * clear both timestamps and lyrics
     */
    public void clear() {
        timestamps.clear();
        lyrics.clear();
    }

    /**
     * checks if lyrics and timestamps is empty
     *
     * @return returns true if both lyrics and timestamps are empty, else returns false
     */
    public boolean isEmpty() {
        return (lyrics.isEmpty() && timestamps.isEmpty());
    }

    /**
     * checks if timestamps array contains given stamp
     *
     * @param stamp timestamp
     * @return returns true if given timestamp exist in timestamps array, else false
     */
    public boolean containsStamp(String stamp) {
        return timestamps.contains(stamp);
    }

    /**
     * get arraylist size
     *
     * @return arraylist size
     */
    public int size() {
        return timestamps.size();
    }
}
