package com.atomykcoder.atomykplay.repository;

import android.content.Context;

import com.atomykcoder.atomykplay.data.Music;
import com.atomykcoder.atomykplay.interfaces.MusicDaoI;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class MusicRepository {
    private final MusicDaoI musicDaoI;
    private final Context context;

    public MusicRepository(Context context) {
        musicDaoI = new MusicDaoImpl();
        this.context = context;
    }

    public CompletableFuture<ArrayList<Music>> fetchMusicList() {
        CompletableFuture<ArrayList<Music>> future = new CompletableFuture<>();
        musicDaoI.fetchMusic(context).thenAccept(future::complete).exceptionally(it -> {
            future.completeExceptionally(it);
            return null;
        });
        return future;
    }
}
