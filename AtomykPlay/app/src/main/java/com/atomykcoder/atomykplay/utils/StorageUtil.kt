package com.atomykcoder.atomykplay.utils

import android.content.Context
import android.content.SharedPreferences
import com.atomykcoder.atomykplay.data.Music
import com.atomykcoder.atomykplay.dataModels.LRCMap
import com.atomykcoder.atomykplay.dataModels.Playlist
import com.atomykcoder.atomykplay.helperFunctions.MusicHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StorageUtil
/**
 * constructor of storage util
 *
 * @param context context of activity or application is valid
 */(private val context: Context) {
    //Storage Locations
    private val MUSIC_LIST_STORAGE = "com.atomykcoder.atomykplay.MUSIC_LIST_STORAGE"
    private val POSITION_STORAGE = "com.atomykcoder.atomykplay.STORAGE_POSITION"
    private val REPEAT_STATUS_STORAGE = "com.atomykcoder.atomykplay.REPEAT_STATUS_STORAGE"
    private val SHUFFLE_STORAGE = "com.atomykcoder.atomykplay.SHUFFLE_STORAGE"
    private val FAVORITE_STORAGE = "com.atomykcoder.atomykplay.FAVORITE_STORAGE"
    private val LYRICS_STORAGE = "com.atomykcoder.atomykplay.LYRICS_STORAGE"
    private val PLAYLIST_STORAGE = "com.atomykcoder.atomykplay.PLAYLIST_STORAGE"
    private val INITIAL_LIST_STORAGE = "com.atomykcoder.atomykplay.INITIAL_LIST_STORAGE"
    private val MUSIC_INDEX_STORAGE = "com.atomykcoder.atomykplay.MUSIC_INDEX_STORAGE"
    private val TEMP_MUSIC_LIST_STORAGE = "com.atomykcoder.atomykplay.TEMP_MUSIC_LIST_STORAGE"
    private val musicIndex = "musicIndex"
    private val musicPosition = "musicPosition"
    private val repeatStatus = "repeatStatus"
    private val shuffleStatus = "shuffleStatus"
    //region music queue list code here
    /**
     * save music queue idList in arraylist of strings
     *
     * @param list music list needed to be saved
     */
    fun saveQueueList(list: ArrayList<Music>) {
        clearQueueList()
        queueSharedPref = context.getSharedPreferences(MUSIC_LIST_STORAGE, Context.MODE_PRIVATE)
        val editor = queueSharedPref.edit()
        for (i in list.indices) {
            val music = list[i]
            val encodedMessage = MusicHelper.encode(music)
            editor.putString(i.toString(), encodedMessage)
        }
        editor.apply()
    }

    /**
     * load saved music queue list (IDS) from storage
     *
     * @return returns saved music queue list (IDS) in an arraylist of strings
     */
    fun loadQueueList(): ArrayList<Music> {
        queueSharedPref = context.getSharedPreferences(MUSIC_LIST_STORAGE, Context.MODE_PRIVATE)
        val musicList = ArrayList<Music>()
        val map = queueSharedPref.all
        for (i in 0 until map.size) {
            val encodedData = queueSharedPref.getString(i.toString(), null)
            val music = MusicHelper.decode(encodedData)
            musicList.add(music)
        }
        return musicList
    }

    /**
     * clear saved music queue list
     */
    fun clearQueueList() {
        queueSharedPref =
            context.getSharedPreferences(MUSIC_LIST_STORAGE, Context.MODE_PRIVATE)
        val editor = queueSharedPref.edit()
        editor.clear()
        editor.apply()
    }
    //endregion
    //region initial list code here
    /**
     * save initial music list in storage
     *
     * @param list initial list of Music
     */
    fun saveInitialList(list: ArrayList<Music>) {
        clearInitialList()
        initialSharedPref =
            context.getSharedPreferences(INITIAL_LIST_STORAGE, Context.MODE_PRIVATE)
        val editor = initialSharedPref.edit()
        editor.clear()
        //don't use for each loop it will throw ConcurrentModificationException some times
        for (i in list.indices) {
            val music = list[i]
            val encodedMessage = MusicHelper.encode(music)
            editor.putString(i.toString(), encodedMessage)
        }
        editor.apply()
    }

    /**
     * load initial music list from storage
     *
     * @return returns arraylist of Music
     */
    fun loadInitialList(): ArrayList<Music> {
        val musicList = ArrayList<Music>()
        initialSharedPref =
            context.getSharedPreferences(INITIAL_LIST_STORAGE, Context.MODE_PRIVATE)
        try {
            val keys: Set<String> = initialSharedPref.all.keys
            for (key in keys) {
                if (initialSharedPref.contains(key)) {
                    val encodedMessage = initialSharedPref.getString(key, null)
                    val music = MusicHelper.decode(encodedMessage)
                    if (music != null) {
                        musicList.add(music)
                    }
                }
            }
        } catch (e: Exception) {
            // Handle the exception appropriately
        }
        musicList.sortWith(Comparator.comparing { obj: Music -> obj.name })
        return musicList
    }

    /**
     * clear saved music Initial list
     */
    private fun clearInitialList() {
        initialSharedPref =
            context.getSharedPreferences(INITIAL_LIST_STORAGE, Context.MODE_PRIVATE)
        val editor = initialSharedPref.edit()
        editor.clear()
        editor.apply()
    }

    fun removeFromInitialList(music: Music) {
        initialSharedPref =
            context.getSharedPreferences(INITIAL_LIST_STORAGE, Context.MODE_PRIVATE)
        val editor = initialSharedPref.edit()
        editor.remove(music.id)
        editor.apply()
    }
    //region music index code here
    /**
     * save Music index of queue array list
     *
     * @param index index of queue array list
     */
    fun saveMusicIndex(index: Int) {
        indexSharedPref =
            context.getSharedPreferences(MUSIC_INDEX_STORAGE, Context.MODE_PRIVATE)
        val editor = indexSharedPref.edit()
        editor.putInt(musicIndex, index)
        editor.apply()
    }

    /**
     * load Music index of queue array list
     */
    fun loadMusicIndex(): Int {
        indexSharedPref =
            context.getSharedPreferences(MUSIC_INDEX_STORAGE, Context.MODE_PRIVATE)
        return indexSharedPref.getInt(musicIndex, 0)
    }

    fun clearMusicIndex() {
        indexSharedPref =
            context.getSharedPreferences(MUSIC_INDEX_STORAGE, Context.MODE_PRIVATE)
        val editor = indexSharedPref.edit()
        editor.remove(musicIndex)
        editor.apply()
    }
    //endregion
    //region music last position code here
    /**
     * save music last played position on seekbar
     *
     * @param position last played position on seekbar
     */
    fun saveMusicLastPos(position: Int) {
        indexSharedPref = context.getSharedPreferences(MUSIC_INDEX_STORAGE, Context.MODE_PRIVATE)
        val editor = indexSharedPref.edit()
        editor.putInt(musicPosition, position)
        editor.apply()
    }

    /**
     * load last saved position of music on seekbar
     *
     * @return returns last saved position of music on seekbar
     */
    fun loadMusicLastPos(): Int {
        indexSharedPref = context.getSharedPreferences(MUSIC_INDEX_STORAGE, Context.MODE_PRIVATE)
        return indexSharedPref.getInt(musicPosition, 0)
    }

    /**
     * clear last saved position of music on seekbar
     */
    fun clearMusicLastPos() {
        indexSharedPref = context.getSharedPreferences(MUSIC_INDEX_STORAGE, Context.MODE_PRIVATE)
        val editor = indexSharedPref.edit()
        editor.remove(musicPosition)
        editor.apply()
    }
    //endregion
    //region player repeat status code here
    /**
     * save player repeat status
     *
     * @param status player status = (repeat, repeat_one, no_repeat)
     */
    fun saveRepeatStatus(status: String?) {
        indexSharedPref =
            context.getSharedPreferences(MUSIC_INDEX_STORAGE, Context.MODE_PRIVATE)
        val editor = indexSharedPref.edit()
        editor.putString(repeatStatus, status)
        editor.apply()
    }

    /**
     * get player repeat status
     *
     * @return string => (repeat, repeat_one, no_repeat)
     */
    fun loadRepeatStatus(): String? {
        indexSharedPref =
            context.getSharedPreferences(MUSIC_INDEX_STORAGE, Context.MODE_PRIVATE)
        return indexSharedPref.getString(repeatStatus, no_repeat)
    }
    //endregion
    //region favourite music related code here
    /**
     * save favourite music to storage
     *
     * @param music music to be saved
     */
    fun saveFavorite(music: Music?) {
        favouriteSharedPref = context.getSharedPreferences(FAVORITE_STORAGE, Context.MODE_PRIVATE)
        val editor = favouriteSharedPref.edit()
        val encodedMessage = MusicHelper.encode(music)
        editor.putBoolean(encodedMessage, true)
        editor.apply()
    }

    /**
     * check if given music exist in favourite storage
     *
     * @param music music to be searched
     * @return returns either "favorite" or "no_favorite" string
     */
    fun checkFavourite(music: Music?): Boolean {
        favouriteSharedPref = context.getSharedPreferences(FAVORITE_STORAGE, Context.MODE_PRIVATE)
        var encodedMessage: String? = null
        if (music != null) {
            encodedMessage = MusicHelper.encode(music)
        }
        return favouriteSharedPref.getBoolean(encodedMessage, false)
    }

    /**
     * remove given music from favourite storage
     *
     * @param music music to be removed
     */
    fun removeFavorite(music: Music?) {
        favouriteSharedPref = context.getSharedPreferences(FAVORITE_STORAGE, Context.MODE_PRIVATE)
        val editor = favouriteSharedPref.edit()
        val encodedMessage = MusicHelper.encode(music)
        editor.remove(encodedMessage)
        editor.apply()
    }

    /**
     * get favourite music list in an arraylist of strings
     *
     * @return returns arraylist of music
     */
    val favouriteList: ArrayList<Music>
        get() {
            favouriteSharedPref =
                context.getSharedPreferences(FAVORITE_STORAGE, Context.MODE_PRIVATE)
            val favouriteList = ArrayList<Music>()
            val keys = favouriteSharedPref.all
            for ((key) in keys) {
                val music = MusicHelper.decode(key)
                favouriteList.add(music)
            }
            return favouriteList
        }
    //endregion
    //region player shuffle status code here
    /**
     * save player shuffle status
     *
     * @param status boolean
     */
    fun saveShuffle(status: Boolean) {
        indexSharedPref = context.getSharedPreferences(MUSIC_INDEX_STORAGE, Context.MODE_PRIVATE)
        val editor = indexSharedPref.edit()
        editor.putBoolean(shuffleStatus, status)
        editor.apply()
    }

    /**
     * load player shuffle status from storage
     *
     * @return returns boolean
     */
    fun loadShuffle(): Boolean {
        indexSharedPref = context.getSharedPreferences(MUSIC_INDEX_STORAGE, Context.MODE_PRIVATE)
        return indexSharedPref.getBoolean(shuffleStatus, false)
    }
    //endregion
    //region temporary music list code here
    /**
     * save temporary music list in storage
     *
     * @param list temporary music list to be saved
     */
    fun saveTempMusicList(list: ArrayList<Music>) {
        clearTempMusicList()
        tempListSharedPref =
            context.getSharedPreferences(TEMP_MUSIC_LIST_STORAGE, Context.MODE_PRIVATE)
        val editor = tempListSharedPref.edit()
        for (i in list.indices) {
            val music = list[i]
            val encodedMessage = MusicHelper.encode(music)
            editor.putString(i.toString(), encodedMessage)
        }
        editor.apply()
    }

    /**
     * load temporary music-id list from storage
     *
     * @return returns arraylist of music-ids (String)
     */
    fun loadTempMusicList(): ArrayList<Music> {
        tempListSharedPref =
            context.getSharedPreferences(TEMP_MUSIC_LIST_STORAGE, Context.MODE_PRIVATE)
        val list = ArrayList<Music>()
        val map = tempListSharedPref.all
        for (i in 0 until map.size) {
            val encodedMessage = tempListSharedPref.getString(i.toString(), null)
            val music = MusicHelper.decode(encodedMessage)
            list.add(music)
        }
        return list
    }

    /**
     * clear temporary music-id list from storage
     */
    fun clearTempMusicList() {
        tempListSharedPref =
            context.getSharedPreferences(TEMP_MUSIC_LIST_STORAGE, Context.MODE_PRIVATE)
        val editor = tempListSharedPref.edit()
        editor.clear()
        editor.apply()
    }
    //endregion
    //region music-lyrics code here
    /**
     * save lyrics of given music in storage
     *
     * @param musicId music-id (string) as key
     * @param _lrcMap LRC-MAP object as value
     */
    fun saveLyrics(musicId: String?, _lrcMap: LRCMap?) {
        lyricsSharedPref = context.getSharedPreferences(LYRICS_STORAGE, Context.MODE_PRIVATE)
        val editor = lyricsSharedPref.edit()
        val gson = Gson()
        val json = gson.toJson(_lrcMap)
        editor.putString(musicId, json)
        editor.apply()
    }

    /**
     * load lyrics of given music-id from storage
     *
     * @param musicId to get lyrics of
     * @return returns LRC-MAP of given music
     */
    fun loadLyrics(musicId: String?): LRCMap? {
        try {
            lyricsSharedPref = context.getSharedPreferences(LYRICS_STORAGE, Context.MODE_PRIVATE)
            val gson = Gson()
            val json = lyricsSharedPref.getString(musicId, null)
            val type = TypeToken.getParameterized(LRCMap::class.java).type
            json?.let {
                return gson.fromJson(it, type)
            }
        } catch (_: Exception) {
            return null
        }
        return null
    }

    /**
     * remove LRC-MAP or lyrics of given music-id
     *
     * @param musicId music-id of a music
     */
    fun removeLyrics(musicId: String?) {
        lyricsSharedPref = context.getSharedPreferences(LYRICS_STORAGE, Context.MODE_PRIVATE)
        val editor = lyricsSharedPref.edit()
        editor.remove(musicId)
        editor.apply()
    }
    //endregion
    //region playlist code here
    /**
     * create and save playlist in storage
     *
     * @param playlistName playlist name
     * @param coverUri     cover uri for playlist
     */
    fun createPlaylist(playlistName: String?, coverUri: String?) {
        playlistSharedPref = context.getSharedPreferences(PLAYLIST_STORAGE, Context.MODE_PRIVATE)
        val editor = playlistSharedPref.edit()
        val gson = Gson()
        val json = gson.toJson(Playlist(playlistName, coverUri))
        editor.putString(playlistName, json)
        editor.apply()
    }

    /**
     * create and save playlist in storage
     *
     * @param playlistName playlist name
     * @param coverUri     cover uri for playlist
     * @param musicList    songs in arraylist<string> format
    </string> */
    fun createPlaylist(playlistName: String?, coverUri: String?, musicList: ArrayList<Music?>?) {
        playlistSharedPref = context.getSharedPreferences(PLAYLIST_STORAGE, Context.MODE_PRIVATE)
        val editor = playlistSharedPref.edit()
        val gson = Gson()
        val json = gson.toJson(Playlist(playlistName, coverUri, musicList))
        editor.putString(playlistName, json)
        editor.apply()
    }

    /**
     * replaces old playlist with new playlist without losing music-id list
     *
     * @param oldPlaylist  old playlist
     * @param playlistName new playlist name (use Empty string for optional use)
     * @param coverUri     new cover uri (use Empty string for optional use)
     */
    fun replacePlaylist(oldPlaylist: Playlist, playlistName: String?, coverUri: String?) {
        removePlayList(oldPlaylist.name)
        createPlaylist(playlistName, coverUri, oldPlaylist.musicList)
    }

    /**
     * remove given playlist from storage
     *
     * @param playlistName name of the playlist which is to be removed
     */
    fun removePlayList(playlistName: String?) {
        playlistSharedPref = context.getSharedPreferences(PLAYLIST_STORAGE, Context.MODE_PRIVATE)
        val editor = playlistSharedPref.edit()
        editor.remove(playlistName)
        editor.apply()
    }

    /**
     * load a playlist with given name
     *
     * @param playlistName name of the playlist which to be returned
     * @return returns a playlist with given name
     */
    fun loadPlaylist(playlistName: String?): Playlist {
        playlistSharedPref = context.getSharedPreferences(PLAYLIST_STORAGE, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = playlistSharedPref.getString(playlistName, null)
        val type = TypeToken.getParameterized(Playlist::class.java).type
        return gson.fromJson(json, type)
    }

    /**
     * get all available playlist from storage
     *
     * @return returns an arraylist of available playlist from storage
     */
    val allPlaylist: ArrayList<Playlist>
        get() {
            playlistSharedPref =
                context.getSharedPreferences(PLAYLIST_STORAGE, Context.MODE_PRIVATE)
            val gson = Gson()
            val playlists = ArrayList<Playlist>()
            val keys = playlistSharedPref.all
            for ((key) in keys) {
                val json = playlistSharedPref.getString(key, null)
                val type = TypeToken.getParameterized(Playlist::class.java).type
                val playlist = gson.fromJson<Playlist>(json, type)
                playlists.add(playlist)
            }
            return playlists
        }

    /**
     * add Item in playlist
     *
     * @param music        music to be added
     * @param playlistName playlist in which, music is to be added
     */
    fun saveItemInPlayList(music: Music?, playlistName: String?) {
        //Shared Preferences Stuff
        playlistSharedPref = context.getSharedPreferences(PLAYLIST_STORAGE, Context.MODE_PRIVATE)
        val editor = playlistSharedPref.edit()
        // New Gson object
        val gson = Gson()
        // Retrieving playlist json
        val json = playlistSharedPref.getString(playlistName, null)
        // Creating a playlist type
        val type = TypeToken.getParameterized(Playlist::class.java).type
        // converting json to gson then to playlist object
        val playlist = gson.fromJson<Playlist>(json, type)
        //adding music to playlist object
        playlist.addMusic(music)
        // creating new json to save updated playlist
        val newJson = gson.toJson(playlist)
        // assign new json to given playlist key
        editor.putString(playlistName, newJson)
        // apply editor
        editor.apply()
    }

    /**
     * remove a music from playlist
     *
     * @param music        music of the music which to be removed
     * @param playlistName name of the playlist that music belongs to
     */
    fun removeItemInPlaylist(music: Music?, playlistName: String?) {
        //Shared Preferences Stuff
        playlistSharedPref = context.getSharedPreferences(PLAYLIST_STORAGE, Context.MODE_PRIVATE)
        val editor = playlistSharedPref.edit()
        // New Gson object
        val gson = Gson()
        // Retrieving playlist json
        val json = playlistSharedPref.getString(playlistName, null)
        // Creating a playlist type
        val type = TypeToken.getParameterized(Playlist::class.java).type
        // converting json to gson then to playlist object
        val playlist = gson.fromJson<Playlist>(json, type)
        //removing music from playlist object
        playlist.removeMusic(music)
        // creating new json to save updated playlist
        val newJson = gson.toJson(playlist)
        // assign new json to given playlist key
        editor.putString(playlistName, newJson)
        // apply editor
        editor.apply()
    }
    //endregion
    /**
     * This class is only for settings page don't use it anywhere else
     */
    class SettingsStorage(private val context: Context) {
        private val SETTINGS_STORAGE = "com.atomykcoder.atomykplay.settings.SETTINGS_STORAGE"
        private val BEAUTIFY_TAGS_STORAGE =
            "com.atomykcoder.atomykplay.settings.BEAUTIFY_TAGS_STORAGE"
        private val REPLACING_TAGS_STORAGE =
            "com.atomykcoder.atomykplay.settings.REPLACING_TAGS_STORAGE"
        private val BLACKLIST_STORAGE = "com.atomykcoder.atomykplay.settings.BLACKLIST_STORAGE"
        fun saveThemeDark(theme: Boolean) {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("theme_name", theme)
            editor.apply()
        }

        fun loadIsThemeDark(): Boolean {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean("theme_name", false)
        }

        fun saveHideStatusBar(b: Boolean) {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("hidden_status_bar", b)
            editor.apply()
        }

        fun loadIsStatusBarHidden(): Boolean {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean("hidden_status_bar", false)
        }

        fun saveHideNavBar(b: Boolean) {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("hidden_navigation_bar", b)
            editor.apply()
        }

        fun loadIsNavBarHidden(): Boolean {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean("hidden_navigation_bar", false)
        }

        fun showInfo(show: Boolean) {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("show_info", show)
            editor.apply()
        }

        fun loadShowInfo(): Boolean {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean("show_info", true)
        }

        fun showArtist(show: Boolean) {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("show_artist", show)
            editor.apply()
        }

        fun loadShowArtist(): Boolean {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean("show_artist", true)
        }

        fun showExtraCon(show: Boolean) {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("show_extra", show)
            editor.apply()
        }

        fun loadExtraCon(): Boolean {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean("show_extra", false)
        }

        fun autoPlay(b: Boolean) {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("auto_play", b)
            editor.apply()
        }

        fun loadAutoPlay(): Boolean {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean("auto_play", false)
        }

        fun keepShuffle(b: Boolean) {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("keep_shuffle", b)
            editor.apply()
        }

        fun loadKeepShuffle(): Boolean {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean("keep_shuffle", false)
        }

        fun lowerVol(b: Boolean) {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("lower_vol", b)
            editor.apply()
        }

        fun loadLowerVol(): Boolean {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean("lower_vol", true)
        }

        fun setSelfStop(b: Boolean) {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("self_stop", b)
            editor.apply()
        }

        fun loadSelfStop(): Boolean {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean("self_stop", true)
        }

        fun setLastAddedDur(i: Int) {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putInt("last_added_dur", i)
            editor.apply()
        }

        fun loadLastAddedDur(): Int {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            return sharedPreferences.getInt("last_added_dur", 0)
        }

        fun keepScreenOn(b: Boolean) {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("keep_screen_on", b)
            editor.apply()
        }

        fun loadKeepScreenOn(): Boolean {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean("keep_screen_on", false)
        }

        fun oneClickSkip(b: Boolean) {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("one_click_skip", b)
            editor.apply()
        }

        fun loadOneClickSkip(): Boolean {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean("one_click_skip", false)
        }

        fun scanAllMusic(b: Boolean) {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("should_scan_only_music", b)
            editor.apply()
        }

        fun loadScanAllMusic(): Boolean {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean("should_scan_only_music", false)
        }

        fun beautifyName(b: Boolean) {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("beautify_name", b)
            editor.apply()
        }

        fun loadBeautifyName(): Boolean {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean("beautify_name", true)
        }

        fun addBeautifyTag(s: String) {
            val sharedPreferences =
                context.getSharedPreferences(BEAUTIFY_TAGS_STORAGE, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val list = allBeautifyTag
            val index = list.size
            if (!list.contains(s)) {
                editor.putString(index.toString(), s)
                editor.apply()
            }
        }

        fun addReplacingTag(s: String) {
            val sharedPreferences =
                context.getSharedPreferences(REPLACING_TAGS_STORAGE, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val list = allReplacingTag
            val index = list.size
            if (!list.contains(s)) {
                editor.putString(index.toString(), s)
                editor.apply()
            }
        }

        val allBeautifyTag: ArrayList<String>
            get() {
                val sharedPreferences =
                    context.getSharedPreferences(BEAUTIFY_TAGS_STORAGE, Context.MODE_PRIVATE)
                val map = sharedPreferences.all
                val tags = ArrayList<String>()
                for ((_, value) in map) {
                    tags.add(value as String)
                }
                return tags
            }
        val allReplacingTag: ArrayList<String>
            get() {
                val sharedPreferences =
                    context.getSharedPreferences(REPLACING_TAGS_STORAGE, Context.MODE_PRIVATE)
                val map = sharedPreferences.all
                val tags = ArrayList<String>()
                for ((_, value) in map) {
                    tags.add(value as String)
                }
                return tags
            }

        fun removeFromReplacingList(key: String?) {
            val sharedPreferences =
                context.getSharedPreferences(REPLACING_TAGS_STORAGE, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.remove(key)
            editor.apply()
        }

        fun removeFromBeautifyList(key: String?) {
            val sharedPreferences =
                context.getSharedPreferences(BEAUTIFY_TAGS_STORAGE, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.remove(key)
            editor.apply()
        }

        fun clearBeautifyTags() {
            val sharedPreferences =
                context.getSharedPreferences(BEAUTIFY_TAGS_STORAGE, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
        }

        fun clearReplacingTags() {
            val sharedPreferences =
                context.getSharedPreferences(REPLACING_TAGS_STORAGE, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
        }

        /**
         * @param dur it should be between 10 to 120 (120 is max duration you can filter)
         */
        fun saveFilterDur(dur: Int) {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putInt("filter_dur", dur)
            editor.apply()
        }

        fun loadFilterDur(): Int {
            val sharedPreferences =
                context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE)
            return sharedPreferences.getInt("filter_dur", 0)
        }

        /**
         * save given path in black list storage
         *
         * @param path path to be saved in black list storage
         */
        fun saveInBlackList(path: String?) {
            val sharedPreferences =
                context.getSharedPreferences(BLACKLIST_STORAGE, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(path, "black_list")
            editor.apply()
        }

        /**
         * get all paths in arraylist<String> format from black list storage
         *
         * @return returns paths in arraylist<String> format
        </String></String> */
        fun loadBlackList(): ArrayList<String> {
            val sharedPreferences =
                context.getSharedPreferences(BLACKLIST_STORAGE, Context.MODE_PRIVATE)
            val map = sharedPreferences.all
            val paths = ArrayList<String>()
            for ((key) in map) {
                paths.add(key)
            }
            return paths
        }

        /**
         * remove given path from black list storage
         *
         * @param path path to be removed (URI)
         */
        fun removeFromBlackList(path: String?) {
            val sharedPreferences =
                context.getSharedPreferences(BLACKLIST_STORAGE, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.remove(path)
            editor.apply()
        }
    }

    companion object {
        //values
        const val no_repeat = "no_repeat"
        const val repeat = "repeat"
        const val repeat_one = "repeat_one"
        lateinit var queueSharedPref: SharedPreferences
        lateinit var initialSharedPref: SharedPreferences
        lateinit var indexSharedPref: SharedPreferences
        lateinit var lyricsSharedPref: SharedPreferences
        lateinit var favouriteSharedPref: SharedPreferences
        lateinit var tempListSharedPref: SharedPreferences
        lateinit var playlistSharedPref: SharedPreferences

    }
}