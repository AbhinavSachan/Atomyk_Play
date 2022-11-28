package com.atomykcoder.atomykplay.helperFunctions;

import android.content.Context;
import android.content.SharedPreferences;

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
    private final String PLAYLISTS = "com.atomykcoder.atomykplay.PLAYLISTS";
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

    public void saveMusicList(ArrayList<MusicDataCapsule> list) {
        sharedPreferences = context.getSharedPreferences(MUSIC_LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(musicList, json);
        editor.apply();
    }

    public ArrayList<MusicDataCapsule> loadMusicList() {

        sharedPreferences = context.getSharedPreferences(MUSIC_LIST_STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(musicList, null);
        Type type = new TypeToken<ArrayList<MusicDataCapsule>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void saveInitialList(ArrayList<MusicDataCapsule> list) {
        sharedPreferences = context.getSharedPreferences(MUSIC_LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(initialList, json);
        editor.apply();
    }

    public ArrayList<MusicDataCapsule> loadInitialList() {

        sharedPreferences = context.getSharedPreferences(MUSIC_LIST_STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(initialList, null);
        Type type = new TypeToken<ArrayList<MusicDataCapsule>>() {
        }.getType();
        return gson.fromJson(json, type);
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

    public void clearMusicList() {
        sharedPreferences = context.getSharedPreferences(MUSIC_LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(musicList);
        editor.apply();
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

    public String loadFavorite(String id) {
        sharedPreferences = context.getSharedPreferences(FAVORITE_STORAGE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(id, no_favorite);
    }

    public Map<String, ?> getFavouriteList() {
        sharedPreferences = context.getSharedPreferences(FAVORITE_STORAGE, Context.MODE_PRIVATE);
        return sharedPreferences.getAll();
    }

    public void saveFavorite(String id) {
        sharedPreferences = context.getSharedPreferences(FAVORITE_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(id, favorite);
        editor.apply();
    }

    public void removeFavorite(String id) {
        sharedPreferences = context.getSharedPreferences(FAVORITE_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(id);
        editor.apply();
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

    public void saveTempMusicList(ArrayList<MusicDataCapsule> list) {
        sharedPreferences = context.getSharedPreferences(MUSIC_LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(tempList, json);
        editor.apply();
    }

    public ArrayList<MusicDataCapsule> loadTempMusicList() {
        sharedPreferences = context.getSharedPreferences(MUSIC_LIST_STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(tempList, null);
        Type type = new TypeToken<ArrayList<MusicDataCapsule>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void clearTempMusicList() {
        sharedPreferences = context.getSharedPreferences(MUSIC_LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(tempList);
        editor.apply();
    }

    public void saveLyrics(String songName, LRCMap _lrcMap) {
        sharedPreferences = context.getSharedPreferences(LYRICS_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(_lrcMap);
        editor.putString(songName, json);
        editor.apply();
    }

    public LRCMap loadLyrics(String songName) {
        sharedPreferences = context.getSharedPreferences(LYRICS_STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(songName, null);
        Type type = new TypeToken<LRCMap>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void removeLyrics(String songName) {
        sharedPreferences = context.getSharedPreferences(LYRICS_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(songName);
        editor.apply();
    }

    public void createPlayList(String playlistName) {
        sharedPreferences = context.getSharedPreferences(PLAYLISTS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(new Playlist(playlistName));
        editor.putString(playlistName, json);
        editor.apply();
    }

    public void removePlayList(String playlistName) {
        sharedPreferences = context.getSharedPreferences(PLAYLISTS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(playlistName);
        editor.apply();
    }

    public Playlist loadPlaylist (String playlistName) {
        sharedPreferences = context.getSharedPreferences(PLAYLISTS, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(playlistName, null);
        Type type = new TypeToken<Playlist>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public ArrayList<Playlist> getAllPlaylist() {
        sharedPreferences = context.getSharedPreferences(PLAYLISTS, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        ArrayList<Playlist> playlists = new ArrayList<>();
        Map<String, ?> keys = sharedPreferences.getAll();
        for(Map.Entry<String, ?> entry : keys.entrySet()) {
            String json = sharedPreferences.getString(entry.getKey(), null);
            Type type = new TypeToken<Playlist>() {}.getType();
            Playlist playlist = gson.fromJson(json, type);
            playlists.add(playlist);
        }
        return playlists;
    }

    public void addItemInPlayList(MusicDataCapsule music, String playlistName) {
        //Shared Preferences Stuff
        sharedPreferences = context.getSharedPreferences(PLAYLISTS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // New Gson object
        Gson gson = new Gson();
        // Retrieving playlist json
        String json = sharedPreferences.getString(playlistName, null);
        // Creating a playlist type
        Type type = new TypeToken<Playlist>() {}.getType();
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

    public void deleteItemInPlaylist(MusicDataCapsule music, String playlistName) {
        //Shared Preferences Stuff
        sharedPreferences = context.getSharedPreferences(PLAYLISTS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // New Gson object
        Gson gson = new Gson();
        // Retrieving playlist json
        String json = sharedPreferences.getString(playlistName, null);
        // Creating a playlist type
        Type type = new TypeToken<Playlist>() {}.getType();
        // converting json to gson then to playlist object
        Playlist playlist = gson.fromJson(json, type);
        //removing music to playlist object
        playlist.removeMusic(music);
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
        public void filterDur(int dur) {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("filter_dur", dur);
            editor.apply();
        }

        public int loadFilterDur() {
            sharedPreferences = context.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
            return sharedPreferences.getInt("filter_dur", 10);
        }

    }

}
