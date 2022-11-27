package com.atomykcoder.atomykplay.adapters;

import static com.atomykcoder.atomykplay.helperFunctions.MusicHelper.convertDuration;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.MotionEventCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.classes.GlideBuilt;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;
import com.atomykcoder.atomykplay.interfaces.ItemTouchHelperAdapter;
import com.atomykcoder.atomykplay.interfaces.OnDragStartListener;
import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class MusicQueueAdapter extends RecyclerView.Adapter<MusicQueueAdapter.MusicViewAdapter> implements ItemTouchHelperAdapter {
    Context context;
    ArrayList<MusicDataCapsule> musicArrayList;
    OnDragStartListener onDragStartListener;
    MainActivity mainActivity;

    public MusicQueueAdapter(Context context, ArrayList<MusicDataCapsule> musicArrayList, OnDragStartListener onDragStartListener) {
        this.context = context;
        this.musicArrayList = musicArrayList;
        this.onDragStartListener = onDragStartListener;
        mainActivity = (MainActivity) context;
    }

    //when item starts to move it will change positions of every item in real time
    @Override
    public void onItemMove(int fromPos, int toPos) {
        StorageUtil storageUtil = new StorageUtil(context.getApplicationContext());
        int savedIndex = storageUtil.loadMusicIndex();

        Collections.swap(musicArrayList, fromPos, toPos);
        notifyItemMoved(fromPos, toPos);

        notifyItemRangeChanged(fromPos, 1, null);
        notifyItemChanged(toPos, null);

        if (fromPos < savedIndex) {
            if (toPos == savedIndex || toPos > savedIndex) {
                storageUtil.saveMusicIndex(savedIndex - 1);
            }
        } else if (fromPos > savedIndex) {
            if (toPos == savedIndex || toPos < savedIndex) {
                storageUtil.saveMusicIndex(savedIndex + 1);
            }
        } else {
            storageUtil.saveMusicIndex(toPos);
        }
        storageUtil.saveMusicList(musicArrayList);

    }

    //removing item on swipe
    @Override
    public void onItemDismiss(int position) {
        StorageUtil storageUtil = new StorageUtil(context);
        int savedIndex;
        savedIndex = storageUtil.loadMusicIndex();
        //if any item has been removed this will save new list on temp list
        if (musicArrayList != null) {
            if (position != -1 && position < musicArrayList.size()) {
                MusicDataCapsule currentItem = musicArrayList.get(position);
                removeItem(currentItem);
            }
        }
        if (position == savedIndex) {
            mainActivity.playAudio();
        } else if (position < savedIndex) {
            storageUtil.saveMusicIndex(savedIndex - 1);
        }
    }

    @NonNull
    @Override
    public MusicQueueAdapter.MusicViewAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.queue_music_item_layout, parent, false);
        return new MusicViewAdapter(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull MusicQueueAdapter.MusicViewAdapter holder, @SuppressLint("RecyclerView") int position) {
        MusicDataCapsule currentItem = musicArrayList.get(position);

        GlideBuilt.glide(context, currentItem.getsAlbumUri(), R.drawable.ic_music, holder.imageView, 128);
        //playing song
        holder.cardView.setOnClickListener(v -> {
            File file = new File(currentItem.getsPath());
            if (file.exists()) {
                //check is service active
                StorageUtil storage = new StorageUtil(context);
                //Store serializable music list to sharedPreference
                storage.saveMusicIndex(position);
                mainActivity.playAudio();
            } else {
                Toast.makeText(context, "Song is unavailable", Toast.LENGTH_SHORT).show();
                removeItem(currentItem);
            }

        });

        //add bottom sheet functions in three dot click
        holder.imageButton.setOnTouchListener((v, event) -> {
            File file = new File(currentItem.getsPath());
            if (file.exists()) {
                //noinspection deprecation
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    onDragStartListener.onDragStart(holder);
                }
            } else {
                Toast.makeText(context, "Song is unavailable", Toast.LENGTH_SHORT).show();
                removeItem(currentItem);
            }
            return false;
        });

        holder.nameText.setText(currentItem.getsName());
        String index = String.valueOf(position + 1);
        holder.musicIndex.setText(index);
        holder.artistText.setText(currentItem.getsArtist());
        holder.durationText.setText(convertDuration(currentItem.getsLength()));
    }

    public void removeItem(MusicDataCapsule item) {
        StorageUtil storageUtil = new StorageUtil(context);
        int position = musicArrayList.indexOf(item);
        if (musicArrayList.size() != 1 && !musicArrayList.isEmpty()) {
            if (item != null) {
                musicArrayList.remove(item);
            }
            notifyItemRangeChanged(position, musicArrayList.size() - (position + 1));
            notifyItemRemoved(position);
            storageUtil.saveMusicList(musicArrayList);
        } else if (musicArrayList.size() == 1) {
            mainActivity.bottomSheetPlayerFragment.queueSheetBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
            mainActivity.clearStorage();
            mainActivity.mainPlayerSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            mainActivity.bottomSheetPlayerFragment.resetMainPlayerLayout();
            mainActivity.resetDataInNavigation();
            mainActivity.stopMusic();
        }
    }

    @Override
    public int getItemCount() {
        if (musicArrayList != null) {
            return musicArrayList.size();
        } else {
            return 0;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateView() {
        notifyDataSetChanged();
    }

    public static class MusicViewAdapter extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final ImageView imageButton;
        private final View cardView;
        private final TextView nameText, artistText, durationText, musicIndex;

        public MusicViewAdapter(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.song_album_cover_queue);
            imageButton = itemView.findViewById(R.id.more_option_i_btn_queue);
            cardView = itemView.findViewById(R.id.cv_song_play_queue);
            nameText = itemView.findViewById(R.id.song_name_queue);
            musicIndex = itemView.findViewById(R.id.song_index_num_queue);
            artistText = itemView.findViewById(R.id.song_artist_name_queue);
            durationText = itemView.findViewById(R.id.song_length_queue);
        }
    }
}
