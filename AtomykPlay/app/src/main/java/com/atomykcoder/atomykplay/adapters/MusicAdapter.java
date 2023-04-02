package com.atomykcoder.atomykplay.adapters;

import static com.atomykcoder.atomykplay.activities.MainActivity.service_bound;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.adapters.Generics.GenericRecyclerAdapter;
import com.atomykcoder.atomykplay.classes.GlideBuilt;
import com.atomykcoder.atomykplay.data.Music;
import com.atomykcoder.atomykplay.events.SetMainLayoutEvent;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;
import com.atomykcoder.atomykplay.repository.MusicUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MusicAdapter extends GenericRecyclerAdapter<Music> {

    private ArrayList<Music> musicList;
    private Handler handler = new Handler(Looper.getMainLooper());

    protected void handlePlayMusic(MainActivity mainActivity, Music item) {
        mainActivity.playAudio(item);
        mainActivity.bottomSheetPlayerFragment.updateQueueAdapter(musicList);
        mainActivity.openBottomPlayer();
    }


    protected void loadImage(Context context, Music item, int position, ImageView albumCoverIV) {
        GlideBuilt glideBuilt = new GlideBuilt(context);
        Bitmap[] image = {null};
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(() -> {
            //image decoder
            byte[] art = new byte[0];
            try (MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever()) {
                mediaMetadataRetriever.setDataSource(item.getPath());
                art = mediaMetadataRetriever.getEmbeddedPicture();
            } catch (IOException ignored) {
            }

            try {
                image[0] = BitmapFactory.decodeByteArray(art, 0, art.length);
            } catch (Exception ignored) {
            }

            handler.post(() -> glideBuilt.glideBitmap(image[0], R.drawable.ic_music, albumCoverIV, 128,false));
        });


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

}
