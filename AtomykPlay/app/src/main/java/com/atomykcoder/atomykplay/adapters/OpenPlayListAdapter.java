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
import android.os.Looper;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.classes.GlideBuilt;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;
import com.atomykcoder.atomykplay.interfaces.ItemTouchHelperAdapter;
import com.atomykcoder.atomykplay.interfaces.OnDragStartListener;
import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OpenPlayListAdapter extends RecyclerView.Adapter<OpenPlayListAdapter.OpenItemViewHolder> implements ItemTouchHelperAdapter {
    private final Context context;
    private final MainActivity mainActivity;
    String playlistName;
    ArrayList<String> musicIDList;
    OnDragStartListener onDragStartListener;
    StorageUtil storage;
    long lastClickTime;
    // value in milliseconds
    int delay = 500;

    public OpenPlayListAdapter(Context _context, String _playlistName, ArrayList<String> _musicIDList, OnDragStartListener onDragStartListener) {
        context = _context;
        playlistName = _playlistName;
        musicIDList = _musicIDList;

        this.onDragStartListener = onDragStartListener;
        mainActivity = (MainActivity) context;
        storage = new StorageUtil(context);
    }

    //when item starts to move it will change positions of every item in real time
    @Override
    public void onItemMove(int fromPos, int toPos) {
    }

    //removing item on swipe
    @Override
    public void onItemDismiss(int position) {
        //if any item has been removed this will save new list on temp list
        if (musicIDList != null) {
            if (position != -1 && position < musicIDList.size()) {
                String currentID = musicIDList.get(position);
                removeItem(currentID);
            }
        }
    }

    @NonNull
    @Override
    public OpenPlayListAdapter.OpenItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.open_playlist_music_item, parent, false);
        return new OpenItemViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull OpenPlayListAdapter.OpenItemViewHolder holder, int position) {
        MusicDataCapsule currentItem = storage.getItemFromInitialList(musicIDList.get(position));

        StorageUtil.SettingsStorage settingsStorage = new StorageUtil.SettingsStorage(context);

        holder.nameText.setText(currentItem.getsName());
        holder.artistText.setText(currentItem.getsArtist());
        holder.durationText.setText(convertDuration(currentItem.getsDuration()));

        final Bitmap[] image = {null};
        ExecutorService service1 = Executors.newSingleThreadExecutor();
        Handler handler = new Handler();
        service1.execute(() -> {

            //image decoder
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(currentItem.getsPath());
            byte[] art = mediaMetadataRetriever.getEmbeddedPicture();

            try {
                image[0] = BitmapFactory.decodeByteArray(art, 0, art.length);
            } catch (Exception ignored) {
            }
            handler.post(() -> GlideBuilt.glideBitmap(context, image[0], R.drawable.ic_music, holder.imageView, 128));
        });
        service1.shutdown();

        holder.cardView.setOnClickListener(v -> {

            //region timer to stop extra clicks
            if (SystemClock.elapsedRealtime() < (lastClickTime + delay)) {
                return;
            }
            lastClickTime = SystemClock.elapsedRealtime();
            //endregion

            File file = new File(currentItem.getsPath());

            if (file.exists()) {
                //check is service active
                //if shuffle button is already on it will shuffle it from start
                ExecutorService service = Executors.newSingleThreadExecutor();
                if (settingsStorage.loadKeepShuffle()) {
                    ArrayList<String> shuffleIDList = new ArrayList<>(musicIDList);
                    //saving list in temp for restore function in player fragment

                    storage.saveTempMusicList(musicIDList);
                    storage.saveShuffle(shuffle);

                    service.execute(() -> {
                        //removing current item from list
                        shuffleIDList.remove(position);
                        //shuffling list
                        Collections.shuffle(shuffleIDList);
                        //adding the removed item in shuffled list on 0th index
                        shuffleIDList.add(0, currentItem.getsId());
                        //saving list
                        storage.saveQueueList(musicIDList);
                        storage.saveMusicIndex(0);
                        // post-execute code here
                        handler.post(() -> {
                            mainActivity.playAudio(currentItem);
                            mainActivity.bottomSheetPlayerFragment.updateQueueAdapter(musicIDList);
                            mainActivity.openBottomPlayer();
                        });
                    });
                    service.shutdown();

                } else if (!settingsStorage.loadKeepShuffle()) {
                    //Store serializable music list to sharedPreference
                    service.execute(() -> {
                        storage.saveShuffle(no_shuffle);
                        storage.saveQueueList(musicIDList);
                        storage.saveMusicIndex(position);
                        // post-execute code here
                        handler.post(() -> {
                            mainActivity.playAudio(currentItem);
                            mainActivity.bottomSheetPlayerFragment.updateQueueAdapter(musicIDList);
                            mainActivity.openBottomPlayer();
                        });
                    });
                    service.shutdown();
                }
            } else {
                Toast.makeText(context, "Song is unavailable", Toast.LENGTH_SHORT).show();
                removeItem(currentItem.getsId());
            }
        });

        holder.optBtn.setOnClickListener(v -> mainActivity.openOptionMenu(currentItem, "openPlaylist"));

    }

    public void removeItem(String musicID) {
        StorageUtil storageUtil = new StorageUtil(context);
        int position = musicIDList.indexOf(musicID);

        if (position != -1) {
            musicIDList.remove(position);
            storageUtil.removeItemInPlaylist(musicID, playlistName);
        }

        notifyItemRangeChanged(position, musicIDList.size() - (position + 1));
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return musicIDList.size();
    }

    public static class OpenItemViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final ImageView optBtn;
        private final View cardView;
        private final TextView nameText, artistText, durationText;

        public OpenItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.song_album_cover_opl);
            optBtn = itemView.findViewById(R.id.playlist_itemOpt);
            cardView = itemView.findViewById(R.id.cv_song_play_opl);
            nameText = itemView.findViewById(R.id.song_name_opl);
            artistText = itemView.findViewById(R.id.song_artist_name_opl);
            durationText = itemView.findViewById(R.id.song_length_opl);
        }
    }
}
