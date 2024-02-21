package com.atomykcoder.atomykplay.models

class LRCMap {
    /**
     * get all timestamps
     *
     * @return timestamps array list
     */
    var stamps = ArrayList<String>()
        private set

    /**
     * get all lyrics
     *
     * @return lyrics array list
     */
    var lyrics = ArrayList<String>()
        private set

    /**
     * make a lrcmap with given timestamps and lyrics
     *
     * @param _timestamps timestamps
     * @param _lyrics     lyrics
     */
    constructor(_timestamps: ArrayList<String>, _lyrics: ArrayList<String>) {
        stamps = _timestamps
        lyrics = _lyrics
    }

    // default constructor
    constructor()

    /**
     * Get Stamp at given index
     *
     * @param i index
     * @return stamp at given index
     */
    fun getStampAt(i: Int): String {
        return stamps[i]
    }

    /**
     * Get lyric at given index
     *
     * @param i index
     * @return lyric at given index
     */
    fun getLyricAt(i: Int): String {
        return lyrics[i]
    }

    /**
     * get lyrics assigned to given timestamp
     *
     * @param stamp stamp, which is used to find lyrics assigned to that stamp
     * @return lyric assigned to given stamp
     */
    operator fun get(stamp: String): String? {
        return if (stamps.contains(stamp)) {
            val i = stamps.indexOf(stamp)
            lyrics[i]
        } else null
    }

    /**
     * get Index of given stamp
     *
     * @param stamp
     * @return returns index of given stamp if exist else returns -1
     */
    fun getIndexAtStamp(stamp: String): Int {
        return if (stamps.contains(stamp)) stamps.indexOf(stamp) else -1
    }

    /**
     * push given stamp and lyrics to the end of their respective array
     *
     * @param _stamp timestamp
     * @param _lyric lyrics
     */
    fun add(_stamp: String, _lyric: String) {
        stamps.add(_stamp)
        lyrics.add(_lyric)
    }

    /**
     * push given arrays of stamps and lyrics  to the end of their respective array
     *
     * @param _stamps timestamps
     * @param _lyrics lyrics
     */
    fun addAll(_stamps: ArrayList<String>?, _lyrics: ArrayList<String>?) {
        stamps.addAll(_stamps!!)
        lyrics.addAll(_lyrics!!)
    }

    /**
     * push a given lrc map to the end of already existing lrcmap
     *
     * @param lrcMap lrcmap object
     */
    fun addAll(lrcMap: LRCMap) {
        stamps.addAll(lrcMap.stamps)
        lyrics.addAll(lrcMap.lyrics)
    }

    /**
     * clear both timestamps and lyrics
     */
    fun clear() {
        stamps.clear()
        lyrics.clear()
    }

    val isEmpty: Boolean
        /**
         * checks if lyrics and timestamps is empty
         *
         * @return returns true if both lyrics and timestamps are empty, else returns false
         */
        get() = lyrics.isEmpty() && stamps.isEmpty()

    /**
     * checks if timestamps array contains given stamp
     *
     * @param stamp timestamp
     * @return returns true if given timestamp exist in timestamps array, else false
     */
    fun containsStamp(stamp: String): Boolean {
        return stamps.contains(stamp)
    }

    /**
     * get arraylist size
     *
     * @return arraylist size
     */
    fun size(): Int {
        return stamps.size
    }
}
