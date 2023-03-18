package com.atomykcoder.atomykplay.interfaces;

import android.content.Context;

import com.atomykcoder.atomykplay.data.Music;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public interface MusicDaoI {
    CompletableFuture<ArrayList<Music>> fetchMusic(Context context);
}
