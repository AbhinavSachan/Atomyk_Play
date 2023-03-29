package com.atomykcoder.atomykplay.repository;

import android.app.Activity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.atomykcoder.atomykplay.data.Music;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class MusicUtils {
    private static MusicUtils instance;
    private ArrayList<Music> initialMusicList = new ArrayList<>();
    private final MutableLiveData<LoadingStatus> status = new MutableLiveData<>();

    public static MusicUtils getInstance() {
        if (instance == null) {
            instance = new MusicUtils();
        }
        return instance;
    }

    public CompletableFuture<Void> fetchMusic(Activity activity) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        MusicRepository repository = new MusicRepository(activity.getApplicationContext());
        activity.runOnUiThread(() -> status.setValue(LoadingStatus.LOADING));
        repository.fetchMusicList().thenAccept(it -> {
            initialMusicList = new ArrayList<>(it);
            activity.runOnUiThread(() -> status.setValue(LoadingStatus.SUCCESS));
            future.complete(null);
        }).exceptionally(it -> {
            activity.runOnUiThread(() -> status.setValue(LoadingStatus.FAILURE));
            future.completeExceptionally(it);
            return null;
        });
        return future;
    }
    public ArrayList<Music> getInitialMusicList() {
        return initialMusicList;
    }

    public void removeFromList(Music music) {
        initialMusicList.remove(music);
    }

    public LiveData<LoadingStatus> getStatus() {
        return status;
    }

}
