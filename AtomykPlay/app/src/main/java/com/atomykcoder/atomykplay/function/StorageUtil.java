package com.atomykcoder.atomykplay.function;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class StorageUtil {
    private final String STORAGE = "com.atomykcoder.atomykplay.STORAGE";
    private final String POSITION_STORAGE = "com.atomykcoder.atomykplay.STORAGE_POSITION";
    private final String REPEAT_STATUS_STORAGE = "com.atomykcoder.atomykplay.REPEAT_STATUS_STORAGE";
    private final String SHUFFLE_STORAGE = "com.atomykcoder.atomykplay.SHUFFLE_STORAGE";
    private final String FAVORITE_STORAGE = "com.atomykcoder.atomykplay.FAVORITE_STORAGE";
    private final Context context;
    private SharedPreferences sharedPreferences;

    //to save the list of audio
    public StorageUtil(Context context) {
        this.context = context;
    }

    public void saveMusicList(ArrayList<MusicDataCapsule> list) {
        sharedPreferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString("musicList", json);
        editor.apply();
    }

    public ArrayList<MusicDataCapsule> loadMusic() {

        sharedPreferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("musicList", null);
        Type type = new TypeToken<ArrayList<MusicDataCapsule>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void saveMusicIndex(int index) {
        sharedPreferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("musicIndex", index);
        editor.apply();
    }

    public int loadMusicIndex() {
        sharedPreferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return sharedPreferences.getInt("musicIndex", 0);
    }

    public void clearCacheAudioPlaylist() {
        sharedPreferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("musicList");
        editor.apply();
    }
 public void clearCacheAudioIndex() {
        sharedPreferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("musicIndex");
        editor.apply();
    }

    public int loadMusicLastPos() {
        sharedPreferences = context.getSharedPreferences(POSITION_STORAGE, Context.MODE_PRIVATE);
        return sharedPreferences.getInt("musicPosition", 0);
    }

    public void saveMusicLastPos(int position) {
        sharedPreferences = context.getSharedPreferences(POSITION_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("musicPosition", position);
        editor.apply();
    }

    public void clearCacheMusicLastPos() {
        sharedPreferences = context.getSharedPreferences(POSITION_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("musicPosition");
        editor.apply();
    }

    public String loadRepeatStatus() {
        sharedPreferences = context.getSharedPreferences(REPEAT_STATUS_STORAGE, Context.MODE_PRIVATE);
        return sharedPreferences.getString("repeatStatus", "no_repeat");
    }

    public void saveRepeatStatus(String name) {
        sharedPreferences = context.getSharedPreferences(REPEAT_STATUS_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("repeatStatus", name);
        editor.apply();
    }

    public String loadFavorite() {
        sharedPreferences = context.getSharedPreferences(FAVORITE_STORAGE, Context.MODE_PRIVATE);
        return sharedPreferences.getString("favorite", "no_favorite");
    }

    public void saveFavorite(String name) {
        sharedPreferences = context.getSharedPreferences(FAVORITE_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("favorite", name);
        editor.apply();
    }

    public String loadShuffle() {
        sharedPreferences = context.getSharedPreferences(SHUFFLE_STORAGE, Context.MODE_PRIVATE);
        return sharedPreferences.getString("shuffle", "no_shuffle");
    }

    public void saveShuffle(String name) {
        sharedPreferences = context.getSharedPreferences(SHUFFLE_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("shuffle", name);
        editor.apply();
    }

}
