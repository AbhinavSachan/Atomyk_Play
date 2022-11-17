package com.atomykcoder.atomykplay.adapters;

import static com.atomykcoder.atomykplay.function.MusicHelper.convertDuration;

import android.annotation.SuppressLint;
import android.content.Context;
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

import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.events.PlayAudioEvent;
import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;
import com.atomykcoder.atomykplay.function.StorageUtil;
import com.atomykcoder.atomykplay.interfaces.ItemTouchHelperAdapter;
import com.atomykcoder.atomykplay.interfaces.ItemTouchHelperViewfinder;
import com.atomykcoder.atomykplay.interfaces.OnDragStartListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class MusicQueueAdapter extends RecyclerView.Adapter<MusicQueueAdapter.MusicViewAdapter> implements ItemTouchHelperAdapter {
    Context context;
    ArrayList<MusicDataCapsule> musicArrayList;
    OnDragStartListener onDragStartListener;

    public MusicQueueAdapter(Context context, ArrayList<MusicDataCapsule> musicArrayList, OnDragStartListener onDragStartListener) {
        this.context = context;
        this.musicArrayList = musicArrayList;
        this.onDragStartListener = onDragStartListener;
    }

    //when item starts to move it will change positions of every item in real time
    @Override
    public void onItemMove(int fromPos, int toPos) {
        StorageUtil storageUtil = new StorageUtil(context.getApplicationContext());
        int savedIndex;
        savedIndex = storageUtil.loadMusicIndex();


        Collections.swap(musicArrayList, fromPos, toPos);
        notifyItemMoved(fromPos, toPos);
        notifyItemRangeChanged(fromPos, 1);
        notifyItemChanged(toPos, 1);

        storageUtil.saveMusicList(musicArrayList);
        // TODO: 11/16/2022 //FIX THIS POSITION ISSUE ☻

//        if (fromPos == savedIndex) {
//            storageUtil.saveMusicIndex(toPos);
//        } else if (fromPos < savedIndex && fromPos < toPos) {
//            storageUtil.saveMusicIndex(fromPos - 1);
//        } else if (fromPos > savedIndex && fromPos > toPos) {
//            storageUtil.saveMusicIndex(fromPos + 1);
//        }
    }

    //removing item on swipe
    @Override
    public void onItemDismiss(int position) {
        StorageUtil storageUtil = new StorageUtil(context);
        MusicDataCapsule activeMusic = null;
        int savedIndex;
        savedIndex = storageUtil.loadMusicIndex();
        if (musicArrayList != null)
            if (position != -1 && position < musicArrayList.size()) {
                activeMusic = musicArrayList.get(position);
            } else {
                activeMusic = musicArrayList.get(0);
            }
        //if any item has been removed this will save new list on tem list
        if (musicArrayList != null) {
            musicArrayList.remove(activeMusic);
            storageUtil.saveMusicList(musicArrayList);
            if (position != -1 && position < musicArrayList.size()) {
                musicArrayList.remove(position);
                notifyItemRemoved(position);
            }
        }

        if (position == savedIndex) {
            EventBus.getDefault().post(new PlayAudioEvent());
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

        try {
            Glide.with(context).load(currentItem.getsAlbumUri()).apply(new RequestOptions().placeholder(R.drawable.ic_no_album)
                    .override(75, 75)).into(holder.imageView);
        } catch (Exception ignored) {
        }
//playing song
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(currentItem.getsPath());
                if (file.exists()) {
                    //check is service active
                    StorageUtil storage = new StorageUtil(context);
                    //Store serializable music list to sharedPreference
                    storage.saveMusicIndex(position);
                    EventBus.getDefault().post(new PlayAudioEvent());
                } else {
                    Toast.makeText(context, "Audio file is unavailable", Toast.LENGTH_SHORT).show();
                    notifyItemRemoved(position);
                }

            }
        });

        //add bottom sheet functions in three dot click
        holder.imageButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    onDragStartListener.onDragStart(holder);
                }
                return false;
            }
        });

        holder.nameText.setText(currentItem.getsName());
        String index = String.valueOf(position + 1);
        holder.musicIndex.setText(index);
        holder.artistText.setText(currentItem.getsArtist());
        holder.durationText.setText(convertDuration(currentItem.getsLength()));
    }

    @Override
    public int getItemCount() {
        if(musicArrayList != null) return  musicArrayList.size();
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
