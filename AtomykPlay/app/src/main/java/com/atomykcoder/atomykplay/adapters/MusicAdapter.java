package com.atomykcoder.atomykplay.adapters;

import android.os.Handler;
import android.os.Looper;

import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.adapters.Generics.GenericRecyclerAdapter;
import com.atomykcoder.atomykplay.data.Music;
import com.atomykcoder.atomykplay.utils.StorageUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MusicAdapter extends GenericRecyclerAdapter<Music> {

    private ArrayList<Music> musicList;
    public boolean canPlay = true;
    public final Handler handler = new Handler(Looper.getMainLooper());

    protected void handlePlayMusic(MainActivity mainActivity, Music item) {
        mainActivity.playAudio(item);
        mainActivity.bottomSheetPlayerFragment.updateQueueAdapter(musicList);
        mainActivity.openBottomPlayer();
    }

    public boolean canPlay() {
        return canPlay;
    }

    protected void handleShuffle(MainActivity mainActivity, Music item, StorageUtil storage, int position, ArrayList<Music> musicList) {
        ExecutorService service = Executors.newSingleThreadExecutor();
        ArrayList<Music> shuffleList = new ArrayList<>(musicList);
        canPlay = false;
        service.execute(()->{
            storage.saveShuffle(true);
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
            handler.post(()-> {
                canPlay = true;
                handlePlayMusic(mainActivity, item);
            });
        });
        service.shutdown();
    }

    protected void handleNoShuffle(MainActivity mainActivity,Music item,StorageUtil storage, int position, ArrayList<Music> musicList) {
        ExecutorService service = Executors.newSingleThreadExecutor();
        canPlay = false;
        service.execute(()->{
            storage.saveShuffle(false);
            storage.saveQueueList(musicList);
            storage.saveMusicIndex(position);
            this.musicList = musicList;
            handler.post(()-> {
                canPlay = true;
                handlePlayMusic(mainActivity, item);
            });
        });
        service.shutdown();
    }

    protected boolean doesMusicExists(Music music) {
        File file = new File(music.getPath());
        return file.exists();
    }

}
