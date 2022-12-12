package com.atomykcoder.atomykplay.adapters;

import static com.atomykcoder.atomykplay.helperFunctions.MusicHelper.convertDuration;
import static com.atomykcoder.atomykplay.helperFunctions.StorageUtil.no_shuffle;
import static com.atomykcoder.atomykplay.helperFunctions.StorageUtil.shuffle;

import android.annotation.SuppressLint;
import android.content.Context;
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
    ArrayList<MusicDataCapsule> musicArrayList;
    OnDragStartListener onDragStartListener;
    StorageUtil storageUtil;
    long lastClickTime;
    // value in milliseconds
    int delay = 1000;

    public OpenPlayListAdapter(Context context, String playlistName, ArrayList<MusicDataCapsule> musicArrayList, OnDragStartListener onDragStartListener) {
        this.context = context;
        this.playlistName = playlistName;
        this.musicArrayList = musicArrayList;
        this.onDragStartListener = onDragStartListener;
        mainActivity = (MainActivity) context;
        storageUtil = new StorageUtil(context);
    }

    //when item starts to move it will change positions of every item in real time
    @Override
    public void onItemMove(int fromPos, int toPos) {
    }

    //removing item on swipe
    @Override
    public void onItemDismiss(int position) {
        //if any item has been removed this will save new list on temp list
        if (musicArrayList != null) {
            if (position != -1 && position < musicArrayList.size()) {
                MusicDataCapsule currentItem = musicArrayList.get(position);
                removeItem(currentItem);
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
    public void onBindViewHolder(@NonNull OpenItemViewHolder holder, int position) {
        MusicDataCapsule currentItem = musicArrayList.get(position);

        StorageUtil.SettingsStorage settingsStorage = new StorageUtil.SettingsStorage(context);
        Handler handler = new Handler(Looper.getMainLooper());

        holder.nameText.setText(currentItem.getsName());
        holder.artistText.setText(currentItem.getsArtist());
        holder.durationText.setText(convertDuration(currentItem.getsLength()));

        GlideBuilt.glide(context, currentItem.getsAlbumUri(), R.drawable.ic_music, holder.imageView, 128);

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
                    ArrayList<MusicDataCapsule> shuffleList = new ArrayList<>(musicArrayList);
                    //saving list in temp for restore function in player fragment
                    storageUtil.saveTempMusicList(musicArrayList);
                    storageUtil.saveShuffle(shuffle);

                    service.execute(() -> {
                        //removing current item from list
                        shuffleList.remove(position);
                        //shuffling list
                        Collections.shuffle(shuffleList);
                        //adding the removed item in shuffled list on 0th index
                        shuffleList.add(0, currentItem);
                        //saving list
                        storageUtil.saveQueueList(shuffleList);
                        storageUtil.saveMusicIndex(0);
                        // post-execute code here
                        handler.post(() -> {
                            mainActivity.playAudio();
                            mainActivity.openBottomPlayer();
                            mainActivity.bottomSheetPlayerFragment.updateQueueAdapter(shuffleList);
                        });
                    });
                    service.shutdown();

                } else if (!settingsStorage.loadKeepShuffle()) {
                    //Store serializable music list to sharedPreference
                    service.execute(() -> {
                        storageUtil.saveShuffle(no_shuffle);
                        storageUtil.saveQueueList(musicArrayList);
                        storageUtil.saveMusicIndex(position);
                        // post-execute code here
                        handler.post(() -> {
                            mainActivity.playAudio();
                            mainActivity.openBottomPlayer();
                            mainActivity.bottomSheetPlayerFragment.updateQueueAdapter(musicArrayList);
                        });
                    });
                    service.shutdown();
                }
            } else {
                Toast.makeText(context, "Song is unavailable", Toast.LENGTH_SHORT).show();
                removeItem(currentItem);
            }
        });

        holder.optBtn.setOnClickListener(v -> mainActivity.openOptionMenu(currentItem, "openPlaylist"));

    }

    public void removeItem(MusicDataCapsule item) {
        int position = musicArrayList.indexOf(item);

        if (position != -1) {
            musicArrayList.remove(position);
            storageUtil.removeItemInPlaylist(item, playlistName);
        }

        notifyItemRangeChanged(position, musicArrayList.size() - (position + 1));
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return musicArrayList.size();
    }

    public class OpenItemViewHolder extends RecyclerView.ViewHolder {
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
