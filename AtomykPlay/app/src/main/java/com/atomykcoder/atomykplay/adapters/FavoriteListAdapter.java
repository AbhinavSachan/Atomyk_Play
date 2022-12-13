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
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;
import com.atomykcoder.atomykplay.interfaces.ItemTouchHelperAdapter;
import com.atomykcoder.atomykplay.interfaces.OnDragStartListener;
import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoriteListAdapter extends RecyclerView.Adapter<FavoriteListAdapter.FavoriteViewHolder> implements ItemTouchHelperAdapter {
    private final Context context;
    MainActivity mainActivity;
    ArrayList<String> musicIdList;
    OnDragStartListener onDragStartListener;
    StorageUtil storageUtil;
    long lastClickTime;
    // value in milliseconds
    int delay = 1000;

    public FavoriteListAdapter(Context context, ArrayList<String> _musicIdList, OnDragStartListener onDragStartListener) {
        this.context = context;
        musicIdList = _musicIdList;
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
        if (musicIdList != null) {
            if (position != -1 && position < musicIdList.size()) {
                removeItem(musicIdList.get(position));
            }
        }
    }

    @NonNull
    @Override
    public FavoriteListAdapter.FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.favorite_item_layout, parent, false);
        return new FavoriteViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull FavoriteListAdapter.FavoriteViewHolder holder, int position) {

        MusicDataCapsule currentItem = storageUtil.getItemFromInitialList(musicIdList.get(position));

        StorageUtil storage = new StorageUtil(context);
        StorageUtil.SettingsStorage settingsStorage = new StorageUtil.SettingsStorage(context);
        Handler handler = new Handler(Looper.getMainLooper());

        holder.nameText.setText(currentItem.getsName());
        holder.artistText.setText(currentItem.getsArtist());
        holder.durationText.setText(convertDuration(currentItem.getsLength()));


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
                    //saving list in temp for restore function in player fragment
                    ArrayList<String> shuffleIDList = new ArrayList<>(musicIdList);

                    storage.saveTempMusicList(musicIdList);
                    storage.saveShuffle(shuffle);

                    ExecutorService service = Executors.newSingleThreadExecutor();
                    service.execute(() -> {
                        //removing current item from list
                        shuffleIDList.remove(position);
                        //shuffling list
                        Collections.shuffle(shuffleIDList);
                        //adding the removed item in shuffled list on 0th index
                        shuffleIDList.add(0, currentItem.getsId());
                        //saving list
                        storage.saveQueueList(shuffleIDList);
                        storage.saveMusicIndex(0);
                        // post-execute code here
                        handler.post(() -> {
                            mainActivity.playAudio();
                            mainActivity.bottomSheetPlayerFragment.updateQueueAdapter(shuffleIDList);
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
                removeItem(currentItem.getsId());
            }
        });

        holder.optBtn.setOnClickListener(v->mainActivity.openOptionMenu(currentItem,"favoriteList"));

    }

    public void removeItem(String item) {
        StorageUtil storageUtil = new StorageUtil(context);
        int position = musicIdList.indexOf(item);

        if (position != -1) {
            musicIdList.remove(position);
            storageUtil.removeFavorite(item);
        }

        notifyItemRangeChanged(position, musicIdList.size() - (position + 1));
        notifyItemRemoved(position);

    }

    @Override
    public int getItemCount() {
        return musicIdList.size();
    }

    public static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final ImageView optBtn;
        private final View cardView;
        private final TextView nameText, artistText, durationText;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.song_album_cover_favorite);
            optBtn = itemView.findViewById(R.id.favorite_itemOpt);
            cardView = itemView.findViewById(R.id.cv_song_play_favorite);
            nameText = itemView.findViewById(R.id.song_name_favorite);
            artistText = itemView.findViewById(R.id.song_artist_name_favorite);
            durationText = itemView.findViewById(R.id.song_length_favorite);
        }
    }
}
