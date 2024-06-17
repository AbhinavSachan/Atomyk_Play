package com.atomykcoder.atomykplay.models

import java.io.Serializable

class Playlist : Serializable {
    @JvmField
    val name: String?

    @JvmField
    val coverUri: String?

    /**
     * get music list in arraylist format with no keys
     *
     * @return returns arraylist<MusicDataCapsule>
    </MusicDataCapsule> */
    @JvmField
    val musicList: ArrayList<Music>?

    constructor(_name: String?, _coverUri: String?, _musicList: ArrayList<Music>?) {
        name = _name
        coverUri = _coverUri
        musicList = _musicList
    }

    constructor(_name: String?) {
        name = _name
        coverUri = null
        musicList = ArrayList()
    }

    constructor(_name: String?, _coverUri: String?) {
        name = _name
        coverUri = _coverUri
        musicList = ArrayList()
    }

    /**
     * add music in playlist
     *
     * @param music music to be added
     */
    fun addMusic(music: Music?) {
        music?.let { musicList?.add(it) }
    }

    /**
     * remove music from playlist
     *
     * @param music music to be removed
     */
    fun removeMusic(music: Music?) {
        musicList?.remove(music)
    }

    /**
     * clear all songs from playlist
     */
    fun clearPlaylist() {
        musicList?.clear()
    }
}
