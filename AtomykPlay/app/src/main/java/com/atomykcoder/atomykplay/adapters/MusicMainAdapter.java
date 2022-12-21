package com.atomykcoder.atomykplay.adapters;

import static com.atomykcoder.atomykplay.helperFunctions.MusicHelper.convertDuration;
import static com.atomykcoder.atomykplay.helperFunctions.StorageUtil.no_shuffle;
import static com.atomykcoder.atomykplay.helperFunctions.StorageUtil.shuffle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.classes.GlideBuilt;
import com.atomykcoder.atomykplay.data.Music;
import com.atomykcoder.atomykplay.helperFunctions.MusicDiffCallback;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;
import com.turingtechnologies.materialscrollbar.INameableAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MusicMainAdapter extends RecyclerView.Adapter<MusicMainAdapter.MusicViewAdapter> implements INameableAdapter {
    Context context;
    ArrayList<Music> musicArrayList;
    MainActivity mainActivity;
    StorageUtil storage;
    long lastClickTime;
    // value in milliseconds
    int delay = 500;


    public MusicMainAdapter(Context _context, ArrayList<Music> _musicArrayList) {
        context = _context;
        musicArrayList = _musicArrayList;
        mainActivity = (MainActivity) context;
        storage = new StorageUtil(context);
    }

    public void updateMusicListItems(ArrayList<Music> newMusicArrayList) {
        final MusicDiffCallback diffCallback = new MusicDiffCallback(musicArrayList, newMusicArrayList);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        musicArrayList.clear();
        musicArrayList.addAll(newMusicArrayList);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public MusicViewAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.music_item_layout, parent, false);
        return new MusicViewAdapter(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull MusicViewAdapter holder, @SuppressLint("RecyclerView") int pos) {
        Music currentItem =  musicArrayList.get(pos);
        final Bitmap[] image = {null};
        ExecutorService service1 = Executors.newSingleThreadExecutor();
        Handler handler = new Handler();
        service1.execute(() -> {

            //image decoder
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(currentItem.getPath());
            byte[] art = mediaMetadataRetriever.getEmbeddedPicture();

            try {
                image[0] = BitmapFactory.decodeByteArray(art, 0, art.length);
            } catch (Exception ignored) {
            }
            handler.post(() -> GlideBuilt.glideBitmap(context, image[0], R.drawable.ic_music, holder.imageView, 128));
        });
        service1.shutdown();

        int position = holder.getAbsoluteAdapterPosition();
        holder.cardView.setOnClickListener(v -> {

            //region timer to stop extra clicks
            if(SystemClock.elapsedRealtime() < (lastClickTime + delay)) {
                Log.i("info", "too fast");
                return;
            }
            lastClickTime = SystemClock.elapsedRealtime();
            //endregion

            File file = new File(currentItem.getPath());
            if (file.exists()) {

                StorageUtil.SettingsStorage settingsStorage = new StorageUtil.SettingsStorage(context);

                //if shuffle button is already on it will shuffle it from start
                if (settingsStorage.loadKeepShuffle()) {
                    ArrayList<Music> shuffleList = new ArrayList<>(musicArrayList);
                    storage.saveTempMusicList(shuffleList);
                    storage.saveShuffle(shuffle);

                    ExecutorService service = Executors.newSingleThreadExecutor();
                    service.execute(() -> {

                        //removing current item from list
                        shuffleList.remove(position);

                        //shuffling list
                        Collections.shuffle(shuffleList);

                        //adding the removed item in shuffled list on 0th index
                        shuffleList.add(0, currentItem);

                        //saving list
                        storage.saveQueueList(shuffleList);
                        storage.saveMusicIndex(0);

                        // post-execute code here
                        handler.post(() -> {
                            mainActivity.playAudio(currentItem);
                            mainActivity.bottomSheetPlayerFragment.updateQueueAdapter(shuffleList);
                            mainActivity.openBottomPlayer();
                        });
                    });
                    service.shutdown();
                } else if (!settingsStorage.loadKeepShuffle()) {

                    //Store serializable music list to sharedPreference
                    ExecutorService service = Executors.newSingleThreadExecutor();
                    service.execute(() -> {
                        storage.saveShuffle(no_shuffle);
                        storage.saveQueueList(musicArrayList);
                        storage.saveMusicIndex(position);

                        // post-execute code here
                        handler.post(() -> {
                            mainActivity.playAudio(currentItem);
                            mainActivity.bottomSheetPlayerFragment.updateQueueAdapter(musicArrayList);
                            mainActivity.openBottomPlayer();
                        });
                    });
                    service.shutdown();
                }
            } else {
                Toast.makeText(context, "Song is unavailable", Toast.LENGTH_SHORT).show();
                removeItem(currentItem);
            }
        });

        //add bottom sheet functions in three dot click
        holder.imageButton.setOnClickListener(view -> {
            File file = new File(currentItem.getPath());
            if (file.exists()) {
                mainActivity.openOptionMenu(currentItem, "mainList");
            } else {
                Toast.makeText(context, "Song is unavailable", Toast.LENGTH_SHORT).show();
                removeItem(currentItem);
            }
        });


        holder.nameText.setText(currentItem.getName());
        holder.artistText.setText(currentItem.getArtist());
        holder.durationText.setText(convertDuration(currentItem.getDuration()));
    }

    @Override
    public int getItemCount() {
        return musicArrayList.size();
    }

    @Override
    public Character getCharacterForElement(int element) {
        return null;
    }

    /**
     * remove item from list after deleting it from device
     *
     * @param item selected delete item
     */
    public void removeItem(Music item) {
        StorageUtil storageUtil = new StorageUtil(context);
        int position = musicArrayList.indexOf(item);
        int savedIndex = storageUtil.loadMusicIndex();

        if (position < savedIndex) {
            storageUtil.saveMusicIndex(savedIndex - 1);
        }
        if (position != -1) {
            storageUtil.removeFromInitialList(item);
            musicArrayList.remove(position);
        }

        notifyItemRangeChanged(position, musicArrayList.size() - (position + 1));
        notifyItemRemoved(position);

        if (position == savedIndex) {
            mainActivity.playAudio(item);
        }
        mainActivity.bottomSheetPlayerFragment.queueAdapter.removeItem(item);

    }

    public static class MusicViewAdapter extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final ImageView imageButton;
        private final RelativeLayout cardView;
        private final TextView nameText, artistText, durationText;

        public MusicViewAdapter(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.song_album_cover);
            imageButton = itemView.findViewById(R.id.option);
            cardView = itemView.findViewById(R.id.cv_song_play);
            nameText = itemView.findViewById(R.id.song_name);
            artistText = itemView.findViewById(R.id.song_artist_name);
            durationText = itemView.findViewById(R.id.song_length);

        }
    }
}
