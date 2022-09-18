package com.atomykcoder.atomykplay.function;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class StorageUtil {
    private final String STORAGE = "com.atomykcoder.atomykplay.STORAGE";
    private SharedPreferences sharedPreferences;
    private final Context context;

    //to save the list of audio
    public StorageUtil(Context context) {
        this.context = context;
    }

    public void storeMusicList(ArrayList<MusicDataCapsule> list){
        sharedPreferences = context.getSharedPreferences(STORAGE,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString("musicList",json);
        editor.apply();
    }
    public ArrayList<MusicDataCapsule> loadMusic() {

        sharedPreferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("musicList", null);
        Type type = new TypeToken<ArrayList<MusicDataCapsule>>(){}.getType();
        return gson.fromJson(json,type);
    }

    public void storeMusicIndex(int index){
        sharedPreferences = context.getSharedPreferences(STORAGE,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("musicIndex",index);
        editor.apply();
    }

    public int loadMusicIndex(){
        sharedPreferences = context.getSharedPreferences(STORAGE,Context.MODE_PRIVATE);
        return sharedPreferences.getInt("musicIndex",0);
    }
    private void clearCacheAudioPlaylist(){
        sharedPreferences = context.getSharedPreferences(STORAGE,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
