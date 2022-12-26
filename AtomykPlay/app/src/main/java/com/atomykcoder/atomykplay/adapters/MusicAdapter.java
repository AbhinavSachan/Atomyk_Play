package com.atomykcoder.atomykplay.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.widget.ImageView;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.adapters.Generics.GenericRecyclerAdapter;
import com.atomykcoder.atomykplay.classes.GlideBuilt;
import com.atomykcoder.atomykplay.data.Music;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MusicAdapter extends GenericRecyclerAdapter<Music> {

    private ArrayList<Music> musicList;
    private HashMap<Integer, Bitmap> map = new HashMap<>();

    protected void handlePlayMusic(MainActivity mainActivity, Music item) {
        mainActivity.playAudio(item);
        mainActivity.bottomSheetPlayerFragment.updateQueueAdapter(musicList);
        mainActivity.openBottomPlayer();
    }

    protected void loadImage(Context context, Music item, int position, ImageView albumCoverIV) {
        Handler handler = new Handler();
        if (map.containsKey(position)) {
            handler.post(() -> GlideBuilt.glideBitmap(context, map.get(position), R.drawable.ic_music, albumCoverIV, 128));
            return;
        }

        final Bitmap[] image = {null};
        ExecutorService service = Executors.newSingleThreadExecutor();

        service.execute(() -> {
            //image decoder
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(item.getPath());
            byte[] art = mediaMetadataRetriever.getEmbeddedPicture();

            try {
                image[0] = BitmapFactory.decodeByteArray(art, 0, art.length);
                map.put(position, image[0]);
            } catch (Exception ignored) {}

            handler.post(() -> GlideBuilt.glideBitmap(context, image[0], R.drawable.ic_music, albumCoverIV, 128));
        });
        service.shutdown();
    }

    protected void handleShuffle(StorageUtil storage, int position, ArrayList<Music> musicList) {

        ArrayList<Music> shuffleList = new ArrayList<>(musicList);
        storage.saveShuffle(true);

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
        storage.saveShuffle(false);
        storage.saveQueueList(musicList);
        storage.saveMusicIndex(position);
        this.musicList = musicList;
    }

    protected boolean doesMusicExists(Music music) {
        File file = new File(music.getPath());
        return file.exists();
    }

    public void removeItem(Music item) {}
}
