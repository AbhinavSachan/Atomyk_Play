package com.atomykcoder.atomykplay.helperFunctions;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


import com.atomykcoder.atomykplay.viewModals.LRCMap;
import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;
import com.atomykcoder.atomykplay.viewModals.Playlist;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

public class StorageUtil {
    //values
    public static final String no_repeat = "no_repeat";
    public static final String repeat = "repeat";
    public static final String repeat_one = "repeat_one";
    public static final String no_favorite = "no_favorite";
    public static final String favorite = "favorite";
    public static final String shuffle = "shuffle";
    public static final String no_shuffle = "no_shuffle";
    public static final String dark = "dark";
    public static final String no_dark = "no_dark";
    //Storage Locations
    private final String MUSIC_LIST_STORAGE = "com.atomykcoder.atomykplay.MUSIC_LIST_STORAGE";
    private final String POSITION_STORAGE = "com.atomykcoder.atomykplay.STORAGE_POSITION";
    private final String REPEAT_STATUS_STORAGE = "com.atomykcoder.atomykplay.REPEAT_STATUS_STORAGE";
    private final String SHUFFLE_STORAGE = "com.atomykcoder.atomykplay.SHUFFLE_STORAGE";
    private final String FAVORITE_STORAGE = "com.atomykcoder.atomykplay.FAVORITE_STORAGE";
    private final String LYRICS_STORAGE = "com.atomykcoder.atomykplay.LYRICS_STORAGE";
    private final String PLAYLIST_STORAGE = "com.atomykcoder.atomykplay.PLAYLIST_STORAGE";
    private final String INITIAL_LIST_STORAGE = "com.atomykcoder.atomykplay.INITIAL_LIST_STORAGE";
    //Keys
    private final String musicList = "musicList";
    private final String initialList = "initialList";
    private final String musicIndex = "musicIndex";
    private final String musicPosition = "musicPosition";
    private final String repeatStatus = "repeatStatus";
    private final String shuffleStatus = "shuffleStatus";
    private final String tempList = "tempList";
    private final Context context;
    private SharedPreferences sharedPreferences;


    //to save the list of audio
    public StorageUtil(Context context) {
        this.context = context;
    }

    public void saveQueueList(ArrayList<String> idList) {
        sharedPreferences = context.getSharedPreferences(MUSIC_LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(idList);
        editor.putString(musicList, json);
        editor.apply();
    }

    public ArrayList<String> loadQueueList() {
        sharedPreferences = context.getSharedPreferences(MUSIC_LIST_STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(musicList, null);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void clearQueueList() {
        sharedPreferences = context.getSharedPreferences(MUSIC_LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(musicList);
        editor.apply();
    }

    public void saveInitialList(ArrayList<MusicDataCapsule> list) {
        sharedPreferences = context.getSharedPreferences(INITIAL_LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        for(MusicDataCapsule music : list) {
            String json = gson.toJson(music);
            editor.putString(music.getsId(), json);
        }
        editor.apply();
    }

    public MusicDataCapsule getItemFromInitialList(String musicID) {
        sharedPreferences = context.getSharedPreferences(INITIAL_LIST_STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(musicID, null);
        Type type = new TypeToken<MusicDataCapsule>(){}.getType();
        return gson.fromJson(json, type);
    }

    public ArrayList<MusicDataCapsule> getItemListFromInitialList(ArrayList<String> idList) {
        sharedPreferences = context.getSharedPreferences(INITIAL_LIST_STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        ArrayList<MusicDataCapsule> musicList = new ArrayList<>();
        for (String id : idList) {
            String json = sharedPreferences.getString(id, null);
            Type type = new TypeToken<MusicDataCapsule>(){}.getType();
            MusicDataCapsule music = gson.fromJson(json, type);
            musicList.add(music);
        }
        return musicList;
    }

    public void addToInitialList(MusicDataCapsule music) {
        sharedPreferences = context.getSharedPreferences(INITIAL_LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(music);
        editor.putString(music.getsId(), json);
        editor.apply();
    }

    public void removeFromInitialList(String musicID) {
        sharedPreferences = context.getSharedPreferences(INITIAL_LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(musicID);
        editor.apply();
    }

    public ArrayList<MusicDataCapsule> loadInitialList() {
        sharedPreferences = context.getSharedPreferences(INITIAL_LIST_STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        Map<String, ?> map = sharedPreferences.getAll();
        ArrayList<MusicDataCapsule> musicList = new ArrayList<>();
        Type type = new TypeToken<MusicDataCapsule>() {
        }.getType();
        for(Map.Entry<String, ?> entry : map.entrySet()) {
            String json = sharedPreferences.getString(entry.getKey(), null);
            MusicDataCapsule music = gson.fromJson(json, type);
            musicList.add(music);
        }
        return musicList;
    }

    public void saveMusicIndex(int index) {
        sharedPreferences = context.getSharedPreferences(MUSIC_LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(musicIndex, index);
        editor.apply();
    }

    public int loadMusicIndex() {
        sharedPreferences = context.getSharedPreferences(MUSIC_LIST_STORAGE, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(musicIndex, 0);
    }



    public void clearInitialList() {
        sharedPreferences = context.getSharedPreferences(MUSIC_LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(initialList);
        editor.apply();
    }

    public void clearAudioIndex() {
        sharedPreferences = context.getSharedPreferences(MUSIC_LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(musicIndex);
        editor.apply();
    }

    public int loadMusicLastPos() {
        sharedPreferences = context.getSharedPreferences(POSITION_STORAGE, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(musicPosition, 0);
    }

    public void saveMusicLastPos(int position) {
        sharedPreferences = context.getSharedPreferences(POSITION_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(musicPosition, position);
        editor.apply();
    }

    public void clearMusicLastPos() {
        sharedPreferences = context.getSharedPreferences(POSITION_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(musicPosition);
        editor.apply();
    }

    public String loadRepeatStatus() {
        sharedPreferences = context.getSharedPreferences(REPEAT_STATUS_STORAGE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(repeatStatus, no_repeat);
    }

    public void saveRepeatStatus(String name) {
        sharedPreferences = context.getSharedPreferences(REPEAT_STATUS_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(repeatStatus, name);
        editor.apply();
    }

    public void saveFavorite(String musicID) {
        sharedPreferences = context.getSharedPreferences(FAVORITE_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(musicID, favorite);
        editor.apply();
    }

    public String checkFavourite(String musicID) {
        sharedPreferences = context.getSharedPreferences(FAVORITE_STORAGE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(musicID, no_favorite);
    }

    public void removeFavorite(String musicID) {
        sharedPreferences = context.getSharedPreferences(FAVORITE_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(musicID);
        editor.apply();
    }

    public ArrayList<String> getFavouriteList() {
        sharedPreferences = context.getSharedPreferences(FAVORITE_STORAGE, Context.MODE_PRIVATE);

        ArrayList<String> favouriteIDList = new ArrayList<>();

        Map<String, ?> keys = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            favouriteIDList.add(entry.getKey());
        }
        return favouriteIDList;
    }

    public String loadShuffle() {
        sharedPreferences = context.getSharedPreferences(SHUFFLE_STORAGE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(shuffleStatus, no_shuffle);
    }

    public void saveShuffle(String name) {
        sharedPreferences = context.getSharedPreferences(SHUFFLE_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(shuffleStatus, name);
        editor.apply();
    }

    public void saveTempMusicList(ArrayList<String> idList) {
        sharedPreferences = context.getSharedPreferences(MUSIC_LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(idList);
        editor.putString(tempList, json);
        editor.apply();
    }

    public ArrayList<String> loadTempMusicList() {
        sharedPreferences = context.getSharedPreferences(MUSIC_LIST_STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(tempList, null);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void clearTempMusicList() {
        sharedPreferences = context.getSharedPreferences(MUSIC_LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(tempList);
        editor.apply();
    }

    public void saveLyrics(String musicId, LRCMap _lrcMap) {
        sharedPreferences = context.getSharedPreferences(LYRICS_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(_lrcMap);
        editor.putString(musicId, json);
        editor.apply();
    }

    public LRCMap loadLyrics(String musicId) {
        sharedPreferences = context.getSharedPreferences(LYRICS_STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(musicId, null);
        Type type = new TypeToken<LRCMap>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void removeLyrics(String musicId) {
        sharedPreferences = context.getSharedPreferences(LYRICS_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(musicId);
        editor.apply();
    }

    public void savePlayList(String playlistName, String coverUri) {
        sharedPreferences = context.getSharedPreferences(PLAYLIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(new Playlist(playlistName, coverUri));
        editor.putString(playlistName, json);
        editor.apply();
    }

    /**
     * overloaded create playlist
     *
     * @param playlistName playlist name
     * @param coverUri     cover uri for playlist
     * @param musicIds     songs ids in arraylist<string> format
     */
    public void createPlayList(String playlistName, String coverUri, ArrayList<String> musicIds) {
        sharedPreferences = context.getSharedPreferences(PLAYLIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(new Playlist(playlistName, coverUri, musicIds));
        editor.putString(playlistName, json);
        editor.apply();
    }


    /**
     * replaces old playlist with new playlist without losing music list
     *
     * @param oldPlaylist  old playlist
     * @param playlistName new playlist name (use Empty string for optional use)
     * @param coverUri     new cover uri (use Empty string for optional use)
     */
    public void replacePlaylist(Playlist oldPlaylist, String playlistName, String coverUri) {
        removePlayList(oldPlaylist.getName());
        createPlayList(playlistName, coverUri, oldPlaylist.getMusicIDList());
    }


    public void removePlayList(String playlistName) {
        sharedPreferences = context.getSharedPreferences(PLAYLIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(playlistName);
        editor.apply();
    }


    public Playlist loadPlaylist(String playlistName) {
        sharedPreferences = context.getSharedPreferences(PLAYLIST_STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(playlistName, null);
        Type type = new TypeToken<Playlist>() {
        }.getType();
        return gson.fromJson(json, type);
    }

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
     * @param musicID music id to be added
     * @param playlistName playlist in which, music is to be added
     */
    public void saveItemInPlayList(String musicID, String playlistName) {
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
        //adding music id to playlist object
        playlist.addMusic(musicID);
        // creating new json to save updated playlist
        String newJson = gson.toJson(playlist);
        // assign new json to given playlist key
        editor.putString(playlistName, newJson);
        // apply editor
        editor.apply();
    }

    public void removeItemInPlaylist(String musicID, String playlistName) {
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
        //removing music id from playlist object
        playlist.removeMusic(musicID);
        // creating new json to save updated playlist
        String newJson = gson.toJson(playlist);
        // assign new json to given playlist key
        editor.putString(playlistName, newJson);
        // apply editor
        editor.apply();
    }


    /**
     * This class is only for settings page don't use it anywhere else
     */
    public static class SettingsStorage {

        private final String SETTINGS_STORAGE = "com.atomykcoder.atomykplay.settings.SETTINGS_STORAGE";
        private final String BLACKLIST_STORAGE = "com.atomykcoder.atomykplay.settings.BLACKLIST_STORAGE";
        private final Context context;
        private SharedPreferences sharedPreferences;

        public SettingsStorage(Context context) {
            this.context = context;
        }

        public void saveTheme(boolean theme) {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("theme_name", theme);
            editor.apply();
        }

        public boolean loadTheme() {
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

        public void showOptionMenu(boolean show) {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("show_opt_menu", show);
            editor.apply();
        }

        public boolean loadOptionMenu() {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            return sharedPreferences.getBoolean("show_opt_menu", true);
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
         * @return returns paths in arraylist<String> format
         */
        public ArrayList<String> loadBlackList() {
            sharedPreferences = context.getSharedPreferences(BLACKLIST_STORAGE, Context.MODE_PRIVATE);
            Map<String, ?> map = sharedPreferences.getAll();
            ArrayList<String> paths = new ArrayList<>();
            for(Map.Entry<String, ?> entry : map.entrySet()) {
                paths.add(entry.getKey());
            }
            return paths;
        }

        /**
         * remove given path from black list storage
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
