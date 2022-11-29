package com.atomykcoder.atomykplay.adapters;

import static com.atomykcoder.atomykplay.helperFunctions.MusicHelper.convertDuration;
import static com.atomykcoder.atomykplay.helperFunctions.StorageUtil.no_shuffle;
import static com.atomykcoder.atomykplay.helperFunctions.StorageUtil.shuffle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
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
import com.atomykcoder.atomykplay.helperFunctions.MusicDiffCallback;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;
import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;
import com.turingtechnologies.materialscrollbar.INameableAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MusicMainAdapter extends RecyclerView.Adapter<MusicMainAdapter.MusicViewAdapter> implements INameableAdapter {
    Context context;
    ArrayList<MusicDataCapsule> musicArrayList;
    MainActivity mainActivity;

    public MusicMainAdapter(Context context, ArrayList<MusicDataCapsule> musicArrayList) {
        this.context = context;
        this.musicArrayList = musicArrayList;
        mainActivity = (MainActivity) context;
    }

    public void updateMusicListItems(ArrayList<MusicDataCapsule> musicArrayList) {
        final MusicDiffCallback diffCallback = new MusicDiffCallback(this.musicArrayList,musicArrayList);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.musicArrayList.clear();
        this.musicArrayList.addAll(musicArrayList);
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
        MusicDataCapsule currentItem = musicArrayList.get(pos);

        GlideBuilt.glide(context, currentItem.getsAlbumUri(), R.drawable.ic_music, holder.imageView, 128);
        //playing song
        int position = holder.getAbsoluteAdapterPosition();
        holder.cardView.setOnClickListener(v -> {
            File file = new File(currentItem.getsPath());
            if (file.exists()) {
                //check is service active

                StorageUtil storage = new StorageUtil(context);
                StorageUtil.SettingsStorage settingsStorage = new StorageUtil.SettingsStorage(context);
                storage.saveMusicList(musicArrayList);
                //if shuffle button is already on it will shuffle it from start
                if (settingsStorage.loadKeepShuffle()) {
                    MusicDataCapsule activeMusic;
                    ArrayList<MusicDataCapsule> shuffleList = new ArrayList<>(musicArrayList);
                    //saving list in temp for restore function in player fragment
                    storage.saveTempMusicList(musicArrayList);
                    storage.saveShuffle(shuffle);

                    if (musicArrayList != null) {
                        if (position != -1 && position < musicArrayList.size()) {
                            activeMusic = musicArrayList.get(position);
                        } else {
                            activeMusic = musicArrayList.get(0);
                        }

                        ExecutorService service = Executors.newSingleThreadExecutor();
                        Handler handler = new Handler(Looper.getMainLooper());
                        service.execute(() -> {
                            //removing current item from list
                            shuffleList.remove(position);
                            //shuffling list
                            Collections.shuffle(shuffleList);
                            //adding the removed item in shuffled list on 0th index
                            shuffleList.add(0, activeMusic);
                            //saving list
                            storage.saveMusicList(shuffleList);
                            storage.saveMusicIndex(0);
                            // post-execute code here
                            handler.post(() -> {
                                mainActivity.playAudio();
                                mainActivity.bottomSheetPlayerFragment.updateQueueAdapter(shuffleList);
                                mainActivity.openBottomPlayer();
                            });
                        });
                        service.shutdown();

                    }
                } else if (!settingsStorage.loadKeepShuffle()) {
                    //Store serializable music list to sharedPreference
                    ExecutorService service = Executors.newSingleThreadExecutor();
                    Handler handler = new Handler(Looper.getMainLooper());
                    service.execute(() -> {
                        storage.saveShuffle(no_shuffle);
                        storage.saveMusicList(musicArrayList);
                        storage.saveMusicIndex(position);
                        // post-execute code here
                        handler.post(() -> {
                            mainActivity.playAudio();
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
            File file = new File(currentItem.getsPath());
            if (file.exists()) {
                mainActivity.openOptionMenu(currentItem);
            } else {
                Toast.makeText(context, "Song is unavailable", Toast.LENGTH_SHORT).show();
                removeItem(currentItem);
            }
        });


        holder.nameText.setText(currentItem.getsName());
        holder.artistText.setText(currentItem.getsArtist());
        holder.durationText.setText(convertDuration(currentItem.getsLength()));
    }

    @Override
    public int getItemCount() {
        return musicArrayList.size();
    }

    @Override
    public Character getCharacterForElement(int element) {
        return musicArrayList.get(element).getsName().charAt(0);
    }

    /**
     * remove item from list after deleting it from device
     *
     * @param item selected delete item
     */
    public void removeItem(MusicDataCapsule item) {
        StorageUtil storageUtil = new StorageUtil(context);
        int position = musicArrayList.indexOf(item);
        int savedIndex = storageUtil.loadMusicIndex();

        if (position < savedIndex) {
            storageUtil.saveMusicIndex(savedIndex - 1);
        }
        if (position != -1) {
            musicArrayList.remove(position);
            storageUtil.saveInitialList(musicArrayList);
        }

        notifyItemRangeChanged(position, musicArrayList.size() - (position + 1));
        notifyItemRemoved(position);

        if (position == savedIndex) {
            mainActivity.playAudio();
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
            imageButton = itemView.findViewById(R.id.playlist_option);
            cardView = itemView.findViewById(R.id.cv_song_play);
            nameText = itemView.findViewById(R.id.song_name);
            artistText = itemView.findViewById(R.id.song_artist_name);
            durationText = itemView.findViewById(R.id.song_length);

        }
    }
}
