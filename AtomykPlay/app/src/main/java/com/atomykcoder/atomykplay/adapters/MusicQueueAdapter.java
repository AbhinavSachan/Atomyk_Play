package com.atomykcoder.atomykplay.adapters;

import static com.atomykcoder.atomykplay.helperFunctions.MusicHelper.convertDuration;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.atomykcoder.atomykplay.interfaces.ItemTouchHelperViewfinder;
import com.atomykcoder.atomykplay.interfaces.OnDragStartListener;
import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;

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
                musicArrayList.remove(position);
                notifyItemRemoved(position);
            }
        }
        if (position == savedIndex) {
            mainActivity.playAudio();
        } else if (position < savedIndex) {
            storageUtil.saveMusicIndex(savedIndex - 1);
        }
        storageUtil.saveMusicList(musicArrayList);
        notifyItemRangeChanged(position, musicArrayList.size());
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

        GlideBuilt.glide(context, currentItem.getsAlbumUri(), R.drawable.ic_music, holder.imageView, 75);
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
                Toast.makeText(context, "Audio file is unavailable", Toast.LENGTH_SHORT).show();
                notifyItemRemoved(position);
            }

        });

        //add bottom sheet functions in three dot click
        holder.imageButton.setOnTouchListener((v, event) -> {
            //noinspection deprecation
            if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                onDragStartListener.onDragStart(holder);
            }
            return false;
        });

        holder.nameText.setText(currentItem.getsName());
        String index = String.valueOf(position + 1);
        holder.musicIndex.setText(index);
        holder.artistText.setText(currentItem.getsArtist());
        holder.durationText.setText(convertDuration(currentItem.getsLength()));
    }

    @Override
    public int getItemCount() {
        if (musicArrayList != null) return musicArrayList.size();
        else return -1;
    }

    public static class MusicViewAdapter extends RecyclerView.ViewHolder implements ItemTouchHelperViewfinder {
        private final ImageView imageView;
        private final ImageView imageButton;
        private final RelativeLayout cardView;
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

        @Override
        public void onItemSelected() {
        }

        @Override
        public void onItemClear() {
        }
    }
}
