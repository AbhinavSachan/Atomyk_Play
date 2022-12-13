package com.atomykcoder.atomykplay.adapters;

import static com.atomykcoder.atomykplay.helperFunctions.MusicHelper.convertDuration;
import static com.atomykcoder.atomykplay.helperFunctions.StorageUtil.no_shuffle;
import static com.atomykcoder.atomykplay.helperFunctions.StorageUtil.shuffle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
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
    ArrayList<String> musicIdList = new ArrayList<>();
    OnDragStartListener onDragStartListener;
    long lastClickTime;
    // value in milliseconds
    int delay = 1000;

    public OpenPlayListAdapter(Context context, String playlistName, ArrayList<MusicDataCapsule> musicArrayList, OnDragStartListener onDragStartListener) {
        this.context = context;
        this.playlistName = playlistName;
        this.musicArrayList = musicArrayList;

        for(MusicDataCapsule music : musicArrayList) {
            musicIdList.add(music.getsId());
        }

        this.onDragStartListener = onDragStartListener;
        mainActivity = (MainActivity) context;
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
    public void onBindViewHolder(@NonNull OpenPlayListAdapter.OpenItemViewHolder holder, int position) {
        MusicDataCapsule currentItem = musicArrayList.get(position);

        StorageUtil storage = new StorageUtil(context);
        StorageUtil.SettingsStorage settingsStorage = new StorageUtil.SettingsStorage(context);
        Handler handler = new Handler(Looper.getMainLooper());

        holder.nameText.setText(currentItem.getsName());
        holder.artistText.setText(currentItem.getsArtist());
        holder.durationText.setText(convertDuration(currentItem.getsLength()));

        GlideBuilt.glide(context, currentItem.getsAlbumUri(), R.drawable.ic_music, holder.imageView, 128);

        holder.cardView.setOnClickListener(v -> {

            //region timer to stop extra clicks
            if(SystemClock.elapsedRealtime() < (lastClickTime + delay)) {
                Log.i("info", "too fast");
                return;
            }
            lastClickTime = SystemClock.elapsedRealtime();
            //endregion

            File file = new File(currentItem.getsPath());
            if (file.exists()) {
                //check is service active

                //if shuffle button is already on it will shuffle it from start
                if (settingsStorage.loadKeepShuffle()) {
                    ArrayList<MusicDataCapsule> shuffleList = new ArrayList<>(musicArrayList);
                    //saving list in temp for restore function in player fragment

                    storage.saveTempMusicList(musicIdList);
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
                        storage.saveQueueList(musicIdList);
                        storage.saveMusicIndex(0);
                        // post-execute code here
                        handler.post(() -> {
                            mainActivity.playAudio();
                            mainActivity.bottomSheetPlayerFragment.updateQueueAdapter(musicIdList);
                            mainActivity.openBottomPlayer();
                        });
                    });
                    service.shutdown();

                } else if (!settingsStorage.loadKeepShuffle()) {
                    //Store serializable music list to sharedPreference
                    ExecutorService service = Executors.newSingleThreadExecutor();
                    service.execute(() -> {
                        storage.saveShuffle(no_shuffle);
                        storage.saveQueueList(musicIdList);
                        storage.saveMusicIndex(position);
                        // post-execute code here
                        handler.post(() -> {
                            mainActivity.playAudio();
                            mainActivity.bottomSheetPlayerFragment.updateQueueAdapter(musicIdList);
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

        holder.optBtn.setOnClickListener(v -> mainActivity.openOptionMenu(currentItem,"openPlaylist"));

    }

    public void removeItem(MusicDataCapsule item) {
        StorageUtil storageUtil = new StorageUtil(context);
        int position = musicArrayList.indexOf(item);

        if (position != -1) {
            musicArrayList.remove(position);
            storageUtil.removeItemInPlaylist(item.getsId(), playlistName);
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
