package com.atomykcoder.atomykplay.helperFunctions;

import android.content.Context;
import android.content.SharedPreferences;

import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

public class StorageUtil {
    private static final String THEME_STORAGE = "com.atomykcoder.atomykplay.THEME_STORAGE" ;
    private final String LIST_STORAGE = "com.atomykcoder.atomykplay.MUSIC_LIST_STORAGE";
    private final String POSITION_STORAGE = "com.atomykcoder.atomykplay.STORAGE_POSITION";
    private final String REPEAT_STATUS_STORAGE = "com.atomykcoder.atomykplay.REPEAT_STATUS_STORAGE";
    private final String SHUFFLE_STORAGE = "com.atomykcoder.atomykplay.SHUFFLE_STORAGE";
    private final String FAVORITE_STORAGE = "com.atomykcoder.atomykplay.FAVORITE_STORAGE";
    private final String LYRICS_STORAGE = "com.atomykcoder.atomykplay.LYRICS_STORAGE";
    private final Context context;
    private SharedPreferences sharedPreferences;

    public static final String musicList = "musicList";
    public static final String musicIndex = "musicIndex";
    public static final String musicPosition = "musicPosition";
    public static final String repeatStatus = "repeatStatus";
    public static final String no_repeat = "no_repeat";
    public static final String no_favorite = "no_favorite";
    public static final String favorite = "favorite";
    public static final String shuffle = "shuffle";
    public static final String no_shuffle = "no_shuffle";
    public static final String initialList = "initialList";
    public static final String tempList = "tempList";
    public static final String repeat = "repeat";
    public static final String repeat_one = "repeat_one";
    public static final String theme_name = "theme_name";
    public static final String system_follow = "system_follow";
    public static final String dark = "dark";
    public static final String no_dark = "no_dark";


    //to save the list of audio
    public StorageUtil(Context context) {
        this.context = context;
    }

    public void saveMusicList(ArrayList<MusicDataCapsule> list) {
        sharedPreferences = context.getSharedPreferences(LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(musicList, json);
        editor.apply();
    }

    public ArrayList<MusicDataCapsule> loadMusicList() {

        sharedPreferences = context.getSharedPreferences(LIST_STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(musicList, null);
        Type type = new TypeToken<ArrayList<MusicDataCapsule>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void saveMusicIndex(int index) {
        sharedPreferences = context.getSharedPreferences(LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(musicIndex, index);
        editor.apply();
    }

    public int loadMusicIndex() {
        sharedPreferences = context.getSharedPreferences(LIST_STORAGE, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(musicIndex, 0);
    }

    public void clearMusicList() {
        sharedPreferences = context.getSharedPreferences(LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(musicList);
        editor.apply();
    }

    public void clearAudioIndex() {
        sharedPreferences = context.getSharedPreferences(LIST_STORAGE, Context.MODE_PRIVATE);
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
        return sharedPreferences.getString(shuffle, no_shuffle);
    }

    public void saveShuffle(String name) {
        sharedPreferences = context.getSharedPreferences(SHUFFLE_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(shuffle, name);
        editor.apply();
    }
/*
    public void saveInitialMusicList(ArrayList<MusicDataCapsule> list) {
        sharedPreferences = context.getSharedPreferences(LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(initialList, json);
        editor.apply();
    }

    public ArrayList<MusicDataCapsule> loadInitialMusicList() {
        sharedPreferences = context.getSharedPreferences(LIST_STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(initialList, null);
        Type type = new TypeToken<ArrayList<MusicDataCapsule>>() {
        }.getType();
        return gson.fromJson(json, type);
    }
*/

    public void saveTempMusicList(ArrayList<MusicDataCapsule> list) {
        sharedPreferences = context.getSharedPreferences(LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(tempList, json);
        editor.apply();
    }

    public ArrayList<MusicDataCapsule> loadTempMusicList() {
        sharedPreferences = context.getSharedPreferences(LIST_STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(tempList, null);
        Type type = new TypeToken<ArrayList<MusicDataCapsule>>() {
        }.getType();
        return gson.fromJson(json, type);
    }
    public void clearTempMusicList() {
        sharedPreferences = context.getSharedPreferences(LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(tempList);
        editor.apply();
    }

    public void saveLyrics(String songName,LRCMap _lrcMap) {
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
        Type type = new TypeToken<LRCMap>(){
        }.getType();
        return gson.fromJson(json, type);
    }

    public void removeLyrics (String songName) {
        sharedPreferences = context.getSharedPreferences(LYRICS_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(songName);
        editor.apply();
    }
    public void saveTheme(String theme) {
        sharedPreferences = context.getSharedPreferences(THEME_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(theme_name,theme);
        editor.apply();
    }

    public String  loadTheme() {
        sharedPreferences = context.getSharedPreferences(THEME_STORAGE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(theme_name,system_follow);
    }

}