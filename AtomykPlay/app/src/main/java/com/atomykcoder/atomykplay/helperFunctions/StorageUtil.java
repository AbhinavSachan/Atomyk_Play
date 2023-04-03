package com.atomykcoder.atomykplay.helperFunctions;

import static com.atomykcoder.atomykplay.helperFunctions.MusicHelper.decode;
import static com.atomykcoder.atomykplay.helperFunctions.MusicHelper.encode;

import android.content.Context;
import android.content.SharedPreferences;

import com.atomykcoder.atomykplay.data.Music;
import com.atomykcoder.atomykplay.dataModels.LRCMap;
import com.atomykcoder.atomykplay.dataModels.Playlist;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

public class StorageUtil {
    //values
    public static final String no_repeat = "no_repeat";
    public static final String repeat = "repeat";
    public static final String repeat_one = "repeat_one";
    //Storage Locations
    private final String MUSIC_LIST_STORAGE = "com.atomykcoder.atomykplay.MUSIC_LIST_STORAGE";
    private final String POSITION_STORAGE = "com.atomykcoder.atomykplay.STORAGE_POSITION";
    private final String REPEAT_STATUS_STORAGE = "com.atomykcoder.atomykplay.REPEAT_STATUS_STORAGE";
    private final String SHUFFLE_STORAGE = "com.atomykcoder.atomykplay.SHUFFLE_STORAGE";
    private final String FAVORITE_STORAGE = "com.atomykcoder.atomykplay.FAVORITE_STORAGE";
    private final String LYRICS_STORAGE = "com.atomykcoder.atomykplay.LYRICS_STORAGE";
    private final String PLAYLIST_STORAGE = "com.atomykcoder.atomykplay.PLAYLIST_STORAGE";
    private final String INITIAL_LIST_STORAGE = "com.atomykcoder.atomykplay.INITIAL_LIST_STORAGE";
    private final String MUSIC_INDEX_STORAGE = "com.atomykcoder.atomykplay.MUSIC_INDEX_STORAGE";
    private final String TEMP_MUSIC_LIST_STORAGE = "com.atomykcoder.atomykplay.TEMP_MUSIC_LIST_STORAGE";

    private final String musicIndex = "musicIndex";
    private final String musicPosition = "musicPosition";
    private final String repeatStatus = "repeatStatus";
    private final String shuffleStatus = "shuffleStatus";
    private final Context context;
    private SharedPreferences sharedPreferences;


    /**
     * constructor of storage util
     *
     * @param context context of activity or application is valid
     */
    public StorageUtil(Context context) {
        this.context = context;
    }


    //region music queue list code here

    /**
     * save music queue idList in arraylist of strings
     *
     * @param list music list needed to be saved
     */
    public void saveQueueList(ArrayList<Music> list) {
        clearQueueList();
        sharedPreferences = context.getSharedPreferences(MUSIC_LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for (int i = 0; i < list.size(); i++) {
            Music music = list.get(i);
            String encodedMessage = encode(music);
            editor.putString(String.valueOf(i), encodedMessage);
        }
        editor.apply();
    }

    /**
     * load saved music queue list (IDS) from storage
     *
     * @return returns saved music queue list (IDS) in an arraylist of strings
     */
    public ArrayList<Music> loadQueueList() {
        sharedPreferences = context.getSharedPreferences(MUSIC_LIST_STORAGE, Context.MODE_PRIVATE);
        ArrayList<Music> musicList = new ArrayList<>();
        Map<String, ?> map = sharedPreferences.getAll();

        for (int i = 0; i < map.size(); i++) {
            String encodedData = sharedPreferences.getString(String.valueOf(i), null);
            Music music = decode(encodedData);
            musicList.add(music);
        }

        return musicList;
    }

    /**
     * clear saved music queue list
     */
    public void clearQueueList() {
        sharedPreferences = context.getSharedPreferences(MUSIC_LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
    //endregion


    //region initial list code here

    /**
     * save initial music list in storage
     *
     * @param list initial list of Music
     */
    public void saveInitialList(ArrayList<Music> list) {
        clearInitialList();
        sharedPreferences = context.getSharedPreferences(INITIAL_LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();

        //don't use for each loop it will throw ConcurrentModificationException some times

        for (int i = 0; i < list.size(); i++) {
            Music music = list.get(i);
            String encodedMessage = encode(music);
            editor.putString(music.getId(), encodedMessage);
        }
        editor.apply();
    }

    /**
     * load initial music list from storage
     *
     * @return returns arraylist of Music
     */
    public ArrayList<Music> loadInitialList() {
        sharedPreferences = context.getSharedPreferences(INITIAL_LIST_STORAGE, Context.MODE_PRIVATE);
        Map<String, ?> map = sharedPreferences.getAll();

        ArrayList<Music> musicList = new ArrayList<>();

        for (Map.Entry<String, ?> entry : map.entrySet()) {
            String encodedMessage = sharedPreferences.getString(entry.getKey(), null);
            Music music = decode(encodedMessage);
            musicList.add(music);
        }
        musicList.sort(Comparator.comparing(Music::getName));

        return musicList;
    }

    /**
     * clear saved music Initial list
     */
    public void clearInitialList() {
        sharedPreferences = context.getSharedPreferences(INITIAL_LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public void removeFromInitialList(Music music) {
        sharedPreferences = context.getSharedPreferences(INITIAL_LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(music.getId());
        editor.apply();
    }

    //region music index code here

    /**
     * save Music index of queue array list
     *
     * @param index index of queue array list
     */
    public void saveMusicIndex(int index) {
        sharedPreferences = context.getSharedPreferences(MUSIC_INDEX_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(musicIndex, index);
        editor.apply();
    }

    /**
     * load Music index of queue array list
     */
    public int loadMusicIndex() {
        sharedPreferences = context.getSharedPreferences(MUSIC_INDEX_STORAGE, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(musicIndex, 0);
    }

    public void clearMusicIndex() {
        sharedPreferences = context.getSharedPreferences(MUSIC_INDEX_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(musicIndex);
        editor.apply();
    }
    //endregion


    //region music last position code here

    /**
     * save music last played position on seekbar
     *
     * @param position last played position on seekbar
     */
    public void saveMusicLastPos(int position) {
        sharedPreferences = context.getSharedPreferences(POSITION_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(musicPosition, position);
        editor.apply();
    }

    /**
     * load last saved position of music on seekbar
     *
     * @return returns last saved position of music on seekbar
     */
    public int loadMusicLastPos() {
        sharedPreferences = context.getSharedPreferences(POSITION_STORAGE, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(musicPosition, 0);
    }

    /**
     * clear last saved position of music on seekbar
     */
    public void clearMusicLastPos() {
        sharedPreferences = context.getSharedPreferences(POSITION_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(musicPosition);
        editor.apply();
    }
    //endregion


    //region player repeat status code here

    /**
     * save player repeat status
     *
     * @param status player status = (repeat, repeat_one, no_repeat)
     */
    public void saveRepeatStatus(String status) {
        sharedPreferences = context.getSharedPreferences(REPEAT_STATUS_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(repeatStatus, status);
        editor.apply();
    }

    /**
     * get player repeat status
     *
     * @return string => (repeat, repeat_one, no_repeat)
     */
    public String loadRepeatStatus() {
        sharedPreferences = context.getSharedPreferences(REPEAT_STATUS_STORAGE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(repeatStatus, no_repeat);
    }
    //endregion


    //region favourite music related code here

    /**
     * save favourite music to storage
     *
     * @param music music to be saved
     */
    public void saveFavorite(Music music) {
        sharedPreferences = context.getSharedPreferences(FAVORITE_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String encodedMessage = encode(music);
        editor.putBoolean(encodedMessage, true);
        editor.apply();
    }

    /**
     * check if given music exist in favourite storage
     *
     * @param music music to be searched
     * @return returns either "favorite" or "no_favorite" string
     */
    public boolean checkFavourite(Music music) {
        sharedPreferences = context.getSharedPreferences(FAVORITE_STORAGE, Context.MODE_PRIVATE);
        String encodedMessage = null;
        if (music != null) {
            encodedMessage = encode(music);
        }
        return sharedPreferences.getBoolean(encodedMessage, false);
    }

    /**
     * remove given music from favourite storage
     *
     * @param music music to be removed
     */
    public void removeFavorite(Music music) {
        sharedPreferences = context.getSharedPreferences(FAVORITE_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String encodedMessage = encode(music);
        editor.remove(encodedMessage);
        editor.apply();
    }

    /**
     * get favourite music list in an arraylist of strings
     *
     * @return returns arraylist of music
     */
    public ArrayList<Music> getFavouriteList() {
        sharedPreferences = context.getSharedPreferences(FAVORITE_STORAGE, Context.MODE_PRIVATE);

        ArrayList<Music> favouriteList = new ArrayList<>();

        Map<String, ?> keys = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            Music music = decode(entry.getKey());
            favouriteList.add(music);
        }
        return favouriteList;
    }

    //endregion


    //region player shuffle status code here

    /**
     * save player shuffle status
     *
     * @param status boolean
     */
    public void saveShuffle(boolean status) {
        sharedPreferences = context.getSharedPreferences(SHUFFLE_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(shuffleStatus, status);
        editor.apply();
    }

    /**
     * load player shuffle status from storage
     *
     * @return returns boolean
     */
    public boolean loadShuffle() {
        sharedPreferences = context.getSharedPreferences(SHUFFLE_STORAGE, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(shuffleStatus, false);
    }

    //endregion


    //region temporary music list code here

    /**
     * save temporary music list in storage
     *
     * @param list temporary music list to be saved
     */
    public void saveTempMusicList(ArrayList<Music> list) {
        clearTempMusicList();
        sharedPreferences = context.getSharedPreferences(TEMP_MUSIC_LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for (int i = 0; i < list.size(); i++) {
            Music music = list.get(i);
            String encodedMessage = encode(music);
            editor.putString(String.valueOf(i), encodedMessage);
        }
        editor.apply();
    }

    /**
     * load temporary music-id list from storage
     *
     * @return returns arraylist of music-ids (String)
     */
    public ArrayList<Music> loadTempMusicList() {
        sharedPreferences = context.getSharedPreferences(TEMP_MUSIC_LIST_STORAGE, Context.MODE_PRIVATE);
        ArrayList<Music> list = new ArrayList<>();
        Map<String, ?> map = sharedPreferences.getAll();

        for (int i = 0; i < map.size(); i++) {
            String encodedMessage = sharedPreferences.getString(String.valueOf(i), null);
            Music music = decode(encodedMessage);
            list.add(music);
        }
        return list;
    }

    /**
     * clear temporary music-id list from storage
     */
    public void clearTempMusicList() {
        sharedPreferences = context.getSharedPreferences(TEMP_MUSIC_LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    //endregion


    //region music-lyrics code here

    /**
     * save lyrics of given music in storage
     *
     * @param musicId music-id (string) as key
     * @param _lrcMap LRC-MAP object as value
     */
    public void saveLyrics(String musicId, LRCMap _lrcMap) {
        sharedPreferences = context.getSharedPreferences(LYRICS_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(_lrcMap);
        editor.putString(musicId, json);
        editor.apply();
    }

    /**
     * load lyrics of given music-id from storage
     *
     * @param musicId to get lyrics of
     * @return returns LRC-MAP of given music
     */
    public LRCMap loadLyrics(String musicId) {
        sharedPreferences = context.getSharedPreferences(LYRICS_STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(musicId, null);
        Type type = new TypeToken<LRCMap>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    /**
     * remove LRC-MAP or lyrics of given music-id
     *
     * @param musicId music-id of a music
     */
    public void removeLyrics(String musicId) {
        sharedPreferences = context.getSharedPreferences(LYRICS_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(musicId);
        editor.apply();
    }

    //endregion


    //region playlist code here

    /**
     * create and save playlist in storage
     *
     * @param playlistName playlist name
     * @param coverUri     cover uri for playlist
     */
    public void createPlaylist(String playlistName, String coverUri) {
        sharedPreferences = context.getSharedPreferences(PLAYLIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(new Playlist(playlistName, coverUri));
        editor.putString(playlistName, json);
        editor.apply();
    }

    /**
     * create and save playlist in storage
     *
     * @param playlistName playlist name
     * @param coverUri     cover uri for playlist
     * @param musicList    songs in arraylist<string> format
     */
    public void createPlaylist(String playlistName, String coverUri, ArrayList<Music> musicList) {
        sharedPreferences = context.getSharedPreferences(PLAYLIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(new Playlist(playlistName, coverUri, musicList));
        editor.putString(playlistName, json);
        editor.apply();
    }


    /**
     * replaces old playlist with new playlist without losing music-id list
     *
     * @param oldPlaylist  old playlist
     * @param playlistName new playlist name (use Empty string for optional use)
     * @param coverUri     new cover uri (use Empty string for optional use)
     */
    public void replacePlaylist(Playlist oldPlaylist, String playlistName, String coverUri) {
        removePlayList(oldPlaylist.getName());
        createPlaylist(playlistName, coverUri, oldPlaylist.getMusicList());
    }

    /**
     * remove given playlist from storage
     *
     * @param playlistName name of the playlist which is to be removed
     */
    public void removePlayList(String playlistName) {
        sharedPreferences = context.getSharedPreferences(PLAYLIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(playlistName);
        editor.apply();
    }

    /**
     * load a playlist with given name
     *
     * @param playlistName name of the playlist which to be returned
     * @return returns a playlist with given name
     */
    public Playlist loadPlaylist(String playlistName) {
        sharedPreferences = context.getSharedPreferences(PLAYLIST_STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(playlistName, null);
        Type type = new TypeToken<Playlist>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    /**
     * get all available playlist from storage
     *
     * @return returns an arraylist of available playlist from storage
     */
    public ArrayList<Playlist> getAllPlaylist() {
        sharedPreferences = context.getSharedPreferences(PLAYLIST_STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        ArrayList<Playlist> playlists = new ArrayList<>();
        Map<String, ?> keys = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            String json = sharedPreferences.getString(entry.getKey(), null);
            Type type = new TypeToken<Playlist>() {
            }.getType();
            Playlist playlist = gson.fromJson(json, type);
            playlists.add(playlist);
        }
        return playlists;
    }

    /**
     * add Item in playlist
     *
     * @param music        music to be added
     * @param playlistName playlist in which, music is to be added
     */
    public void saveItemInPlayList(Music music, String playlistName) {
        //Shared Preferences Stuff
        sharedPreferences = context.getSharedPreferences(PLAYLIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // New Gson object
        Gson gson = new Gson();
        // Retrieving playlist json
        String json = sharedPreferences.getString(playlistName, null);
        // Creating a playlist type
        Type type = new TypeToken<Playlist>() {
        }.getType();
        // converting json to gson then to playlist object
        Playlist playlist = gson.fromJson(json, type);
        //adding music to playlist object
        playlist.addMusic(music);
        // creating new json to save updated playlist
        String newJson = gson.toJson(playlist);
        // assign new json to given playlist key
        editor.putString(playlistName, newJson);
        // apply editor
        editor.apply();
    }

    /**
     * remove a music from playlist
     *
     * @param music        music of the music which to be removed
     * @param playlistName name of the playlist that music belongs to
     */
    public void removeItemInPlaylist(Music music, String playlistName) {
        //Shared Preferences Stuff
        sharedPreferences = context.getSharedPreferences(PLAYLIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // New Gson object
        Gson gson = new Gson();
        // Retrieving playlist json
        String json = sharedPreferences.getString(playlistName, null);
        // Creating a playlist type
        Type type = new TypeToken<Playlist>() {
        }.getType();
        // converting json to gson then to playlist object
        Playlist playlist = gson.fromJson(json, type);
        //removing music from playlist object
        playlist.removeMusic(music);
        // creating new json to save updated playlist
        String newJson = gson.toJson(playlist);
        // assign new json to given playlist key
        editor.putString(playlistName, newJson);
        // apply editor
        editor.apply();
    }

    //endregion


    /**
     * This class is only for settings page don't use it anywhere else
     */
    public static class SettingsStorage {

        private final String SETTINGS_STORAGE = "com.atomykcoder.atomykplay.settings.SETTINGS_STORAGE";
        private final String BEAUTIFY_TAGS_STORAGE = "com.atomykcoder.atomykplay.settings.BEAUTIFY_TAGS_STORAGE";
        private final String REPLACING_TAGS_STORAGE = "com.atomykcoder.atomykplay.settings.REPLACING_TAGS_STORAGE";
        private final String BLACKLIST_STORAGE = "com.atomykcoder.atomykplay.settings.BLACKLIST_STORAGE";
        private final Context context;
        private SharedPreferences sharedPreferences;

        public SettingsStorage(Context context) {
            this.context = context;
        }

        public void saveThemeDark(boolean theme) {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("theme_name", theme);
            editor.apply();
        }

        public boolean loadIsThemeDark() {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            return sharedPreferences.getBoolean("theme_name", false);
        }

        public void showInfo(boolean show) {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("show_info", show);
            editor.apply();
        }

        public boolean loadShowInfo() {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            return sharedPreferences.getBoolean("show_info", false);
        }

        public void showArtist(boolean show) {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("show_artist", show);
            editor.apply();
        }

        public boolean loadShowArtist() {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            return sharedPreferences.getBoolean("show_artist", false);
        }

        public void showExtraCon(boolean show) {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("show_extra", show);
            editor.apply();
        }

        public boolean loadExtraCon() {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            return sharedPreferences.getBoolean("show_extra", false);
        }

        public void autoPlay(boolean b) {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("auto_play", b);
            editor.apply();
        }

        public boolean loadAutoPlay() {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            return sharedPreferences.getBoolean("auto_play", false);
        }

        public void keepShuffle(boolean b) {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("keep_shuffle", b);
            editor.apply();
        }

        public boolean loadKeepShuffle() {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            return sharedPreferences.getBoolean("keep_shuffle", false);
        }

        public void lowerVol(boolean b) {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("lower_vol", b);
            editor.apply();
        }

        public boolean loadLowerVol() {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            return sharedPreferences.getBoolean("lower_vol", true);
        }

        public void setSelfStop(boolean b) {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("self_stop", b);
            editor.apply();
        }

        public boolean loadSelfStop() {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            return sharedPreferences.getBoolean("self_stop", true);
        }

        public void setLastAddedDur(int i) {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("last_added_dur", i);
            editor.apply();
        }

        public int loadLastAddedDur() {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            return sharedPreferences.getInt("last_added_dur", 1);
        }

        public void keepScreenOn(boolean b) {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("keep_screen_on", b);
            editor.apply();
        }

        public boolean loadKeepScreenOn() {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            return sharedPreferences.getBoolean("keep_screen_on", false);
        }

        public void oneClickSkip(boolean b) {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("one_click_skip", b);
            editor.apply();
        }

        public boolean loadOneClickSkip() {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            return sharedPreferences.getBoolean("one_click_skip", false);
        }

        public void scanAllMusic(boolean b) {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("should_scan_only_music", b);
            editor.apply();
        }

        public boolean loadScanAllMusic() {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            return sharedPreferences.getBoolean("should_scan_only_music", false);
        }

        public void beautifyName(boolean b) {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("beautify_name", b);
            editor.apply();
        }

        public boolean loadBeautifyName() {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            return sharedPreferences.getBoolean("beautify_name", false);
        }

        public void addBeautifyTag(String s) {
            sharedPreferences = context.getSharedPreferences(BEAUTIFY_TAGS_STORAGE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            ArrayList<String> list = getAllBeautifyTag();
            int index = list.size();
            if (!list.contains(s)) {
                editor.putString(String.valueOf(index), s);
                editor.apply();
            }
        }

        public void addReplacingTag(String s) {
            sharedPreferences = context.getSharedPreferences(REPLACING_TAGS_STORAGE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            ArrayList<String> list = getAllReplacingTag();
            int index = list.size();
            if (!list.contains(s)) {
                editor.putString(String.valueOf(index), s);
                editor.apply();
            }
        }

        public ArrayList<String> getAllBeautifyTag() {
            sharedPreferences = context.getSharedPreferences(BEAUTIFY_TAGS_STORAGE, Context.MODE_PRIVATE);
            Map<String, ?> map = sharedPreferences.getAll();
            ArrayList<String> tags = new ArrayList<>();
            for (Map.Entry<String, ?> entry : map.entrySet()) {
                tags.add((String) entry.getValue());
            }
            return tags;
        }

        public ArrayList<String> getAllReplacingTag() {
            sharedPreferences = context.getSharedPreferences(REPLACING_TAGS_STORAGE, Context.MODE_PRIVATE);
            Map<String, ?> map = sharedPreferences.getAll();
            ArrayList<String> tags = new ArrayList<>();
            for (Map.Entry<String, ?> entry : map.entrySet()) {
                tags.add((String) entry.getValue());
            }
            return tags;
        }

        public void removeFromReplacingList(String key) {
            sharedPreferences = context.getSharedPreferences(REPLACING_TAGS_STORAGE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(key);
            editor.apply();
        }

        public void removeFromBeautifyList(String key) {
            sharedPreferences = context.getSharedPreferences(BEAUTIFY_TAGS_STORAGE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(key);
            editor.apply();
        }

        public void clearBeautifyTags() {
            sharedPreferences = context.getSharedPreferences(BEAUTIFY_TAGS_STORAGE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
        }

        public void clearReplacingTags() {
            sharedPreferences = context.getSharedPreferences(REPLACING_TAGS_STORAGE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
        }

        /**
         * @param dur it should be between 10 to 120 (120 is max duration you can filter)
         */
        public void saveFilterDur(int dur) {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("filter_dur", dur);
            editor.apply();
        }

        public int loadFilterDur() {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            return sharedPreferences.getInt("filter_dur", 0);
        }

        /**
         * save given path in black list storage
         *
         * @param path path to be saved in black list storage
         */
        public void saveInBlackList(String path) {
            sharedPreferences = context.getSharedPreferences(BLACKLIST_STORAGE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(path, "black_list");
            editor.apply();
        }

        /**
         * get all paths in arraylist<String> format from black list storage
         *
         * @return returns paths in arraylist<String> format
         */
        public ArrayList<String> loadBlackList() {
            sharedPreferences = context.getSharedPreferences(BLACKLIST_STORAGE, Context.MODE_PRIVATE);
            Map<String, ?> map = sharedPreferences.getAll();
            ArrayList<String> paths = new ArrayList<>();
            for (Map.Entry<String, ?> entry : map.entrySet()) {
                paths.add(entry.getKey());
            }
            return paths;
        }

        /**
         * remove given path from black list storage
         *
         * @param path path to be removed (URI)
         */
        public void removeFromBlackList(String path) {
            sharedPreferences = context.getSharedPreferences(BLACKLIST_STORAGE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(path);
            editor.apply();
        }

    }

}
