package com.atomykcoder.atomykplay.function;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

public class StorageUtil {
    private final String MUSIC_LIST_STORAGE = "com.atomykcoder.atomykplay.MUSIC_LIST_STORAGE";
    private final String QUEUE_LIST_STORAGE = "com.atomykcoder.atomykplay.QUEUE_LIST_STORAGE";
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
        sharedPreferences = context.getSharedPreferences(MUSIC_LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString("musicList", json);
        editor.apply();
    }

    public ArrayList<MusicDataCapsule> loadMusicList() {

        sharedPreferences = context.getSharedPreferences(MUSIC_LIST_STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("musicList", null);
        Type type = new TypeToken<ArrayList<MusicDataCapsule>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void saveMusicIndex(int index) {
        sharedPreferences = context.getSharedPreferences(MUSIC_LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("musicIndex", index);
        editor.apply();
    }

    public int loadMusicIndex() {
        sharedPreferences = context.getSharedPreferences(MUSIC_LIST_STORAGE, Context.MODE_PRIVATE);
        return sharedPreferences.getInt("musicIndex", 0);
    }

    public void clearAudioPlaylist() {
        sharedPreferences = context.getSharedPreferences(MUSIC_LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("musicList");
        editor.apply();
    }
 public void clearAudioIndex() {
        sharedPreferences = context.getSharedPreferences(MUSIC_LIST_STORAGE, Context.MODE_PRIVATE);
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

    public void clearMusicLastPos() {
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

    public String loadFavorite(String id) {
        sharedPreferences = context.getSharedPreferences(FAVORITE_STORAGE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(id, "no_favorite");
    }
    public Map<String, ?> getFavouriteList() {
        sharedPreferences = context.getSharedPreferences(FAVORITE_STORAGE, Context.MODE_PRIVATE);
        return sharedPreferences.getAll();
    }

    public void saveFavorite(String id) {
        sharedPreferences = context.getSharedPreferences(FAVORITE_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(id, "favorite");
        editor.apply();
    }

    public void removeFavorite(String id){
        sharedPreferences = context.getSharedPreferences(FAVORITE_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(id);
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

    public void saveShuffleIndexList(ArrayList<Integer> list) {
        sharedPreferences = context.getSharedPreferences(QUEUE_LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString("queueMusicList", json);
        editor.apply();
    }

    public ArrayList<Integer> loadShuffleIndexList() {

        sharedPreferences = context.getSharedPreferences(QUEUE_LIST_STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("queueMusicList", null);
        Type type = new TypeToken<ArrayList<Integer>>() {
        }.getType();
        return gson.fromJson(json, type);
    }
    public void clearShuffleIndexList() {
        sharedPreferences = context.getSharedPreferences(QUEUE_LIST_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("queueMusicList");
        editor.apply();
    }

}
