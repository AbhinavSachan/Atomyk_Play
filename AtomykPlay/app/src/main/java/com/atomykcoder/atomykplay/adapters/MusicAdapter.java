package com.atomykcoder.atomykplay.adapters;

import android.content.Context;
import android.widget.ImageView;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.adapters.Generics.GenericRecyclerAdapter;
import com.atomykcoder.atomykplay.classes.GlideBuilt;
import com.atomykcoder.atomykplay.data.Music;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;
import com.atomykcoder.atomykplay.repository.MusicUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class MusicAdapter extends GenericRecyclerAdapter<Music> {

    private ArrayList<Music> musicList;
    private GlideBuilt glideBuilt;

    protected void handlePlayMusic(MainActivity mainActivity, Music item) {
        mainActivity.playAudio(item);
        mainActivity.bottomSheetPlayerFragment.updateQueueAdapter(musicList);
        mainActivity.openBottomPlayer();
    }


    protected void loadImage(Context context, Music item, int position, ImageView albumCoverIV) {
        glideBuilt = new GlideBuilt(context);
        glideBuilt.glide(item.getAlbumUri(), R.drawable.ic_music, albumCoverIV, 128);
    }

    protected void handleShuffle(StorageUtil storage, int position, ArrayList<Music> musicList) {

        ArrayList<Music> shuffleList = new ArrayList<>(musicList);
        if (MusicUtils.getInstance().shouldChangeShuffleMode()) {
            storage.saveShuffle(true);
        }

        storage.saveTempMusicList(shuffleList);

        //removing current item from list
        Music item = shuffleList.remove(position);

        //shuffling list
        Collections.shuffle(shuffleList);

        //adding the removed item in shuffled list on 0th index
        shuffleList.add(0, item);

        //saving list
        storage.saveQueueList(shuffleList);
        storage.saveMusicIndex(0);

        this.musicList = shuffleList;
    }

    protected void handleNoShuffle(StorageUtil storage, int position, ArrayList<Music> musicList) {
        if (MusicUtils.getInstance().shouldChangeShuffleMode()) {
            storage.saveShuffle(false);
        }
        storage.saveQueueList(musicList);
        storage.saveMusicIndex(position);
        this.musicList = musicList;
    }

    protected boolean doesMusicExists(Music music) {
        File file = new File(music.getPath());
        return file.exists();
    }

    public void removeItem(Music item) {
    }
}
