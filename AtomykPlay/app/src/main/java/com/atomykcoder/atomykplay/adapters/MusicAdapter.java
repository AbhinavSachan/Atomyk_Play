package com.atomykcoder.atomykplay.adapters;

import android.os.Handler;
import android.os.Looper;

import com.atomykcoder.atomykplay.adapters.generics.GenericRecyclerAdapter;
import com.atomykcoder.atomykplay.constants.ShuffleModes;
import com.atomykcoder.atomykplay.models.Music;
import com.atomykcoder.atomykplay.ui.MainActivity;
import com.atomykcoder.atomykplay.utils.StorageUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MusicAdapter extends GenericRecyclerAdapter<Music> {

    public final Handler handler = new Handler(Looper.getMainLooper());
    public boolean canPlay = true;
    private ArrayList<Music> musicList;

    protected void handlePlayMusic(MainActivity mainActivity, Music item) {
        mainActivity.playAudio(item);
        if (mainActivity.bottomSheetPlayerFragment != null) {
            mainActivity.bottomSheetPlayerFragment.updateQueueAdapter(musicList);
        }
        mainActivity.openBottomPlayer();
    }

    public boolean canPlay() {
        return canPlay;
    }

    protected void handleShuffle(MainActivity mainActivity, Music item, StorageUtil storage, int position, ArrayList<Music> musicList) {
        ExecutorService service = Executors.newFixedThreadPool(3);
        ArrayList<Music> shuffleList = new ArrayList<>(musicList);
        canPlay = false;
        service.execute(() -> {
            storage.saveShuffle(ShuffleModes.SHUFFLE_MODE_ALL);
            storage.saveTempMusicList(shuffleList);

            shuffleList.remove(item);

            //shuffling list
            Collections.shuffle(shuffleList);

            //adding the removed item in shuffled list on 0th index
            shuffleList.add(0, item);

            //saving list
            storage.saveQueueList(shuffleList);
            storage.saveMusicIndex(0);

            this.musicList = shuffleList;
            handler.post(() -> {
                handlePlayMusic(mainActivity, item);
                canPlay = true;
            });
        });
        service.shutdown();
    }

    protected void handleNoShuffle(MainActivity mainActivity, Music item, StorageUtil storage, int position, ArrayList<Music> musicList) {
        ExecutorService service = Executors.newFixedThreadPool(2);
        canPlay = false;
        service.execute(() -> {
            storage.saveShuffle(ShuffleModes.SHUFFLE_MODE_NONE);
            storage.saveQueueList(musicList);
            storage.saveMusicIndex(position);
            this.musicList = musicList;
            handler.post(() -> {
                handlePlayMusic(mainActivity, item);
                canPlay = true;
            });
        });
        service.shutdown();
    }

    protected boolean doesMusicExists(Music music) {
        File file = new File(music.getPath());
        return file.exists();
    }

}
