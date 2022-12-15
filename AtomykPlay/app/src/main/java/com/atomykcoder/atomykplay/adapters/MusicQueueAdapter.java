package com.atomykcoder.atomykplay.adapters;

import static com.atomykcoder.atomykplay.helperFunctions.MusicHelper.convertDuration;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemClock;
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
    ArrayList<String> musicIDList;
    OnDragStartListener onDragStartListener;
    MainActivity mainActivity;
    StorageUtil storageUtil;
    long lastClickTime;
    // value in milliseconds
    int delay = 500;

    public MusicQueueAdapter(Context _context, ArrayList<String> _musicIDList, OnDragStartListener onDragStartListener) {
        context = _context;
        musicIDList = _musicIDList;

        this.onDragStartListener = onDragStartListener;
        mainActivity = (MainActivity) context;
        storageUtil = new StorageUtil(context);
    }

    //when item starts to move it will change positions of every item in real time
    @Override
    public void onItemMove(int fromPos, int toPos) {
        int savedIndex = storageUtil.loadMusicIndex();

        Collections.swap(musicIDList, fromPos, toPos);
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
        storageUtil.saveQueueList(musicIDList);

    }

    //removing item on swipe
    @Override
    public void onItemDismiss(int position) {
        int savedIndex;
        savedIndex = storageUtil.loadMusicIndex();
        String currentID = null;
        //if any item has been removed this will save new list on temp list
        if (musicIDList != null) {
            if (position != -1 && position < musicIDList.size()) {
                currentID = musicIDList.get(position);
                removeItem(currentID);
            }
        }
        if (position == savedIndex) {
            mainActivity.playAudio(currentID);
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
        MusicDataCapsule currentItem = storageUtil.getItemFromInitialList(musicIDList.get(position));

        GlideBuilt.glide(context, currentItem.getsAlbumUri(), R.drawable.ic_music, holder.imageView, 128);
        //playing song
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
                //Store serializable music list to sharedPreference
                storageUtil.saveMusicIndex(position);
                mainActivity.playAudio(currentItem.getsId());
            } else {
                Toast.makeText(context, "Song is unavailable", Toast.LENGTH_SHORT).show();
                removeItem(currentItem.getsId());
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
                removeItem(currentItem.getsId());
            }
            return false;
        });

        holder.nameText.setText(currentItem.getsName());
        String index = String.valueOf(position + 1);
        holder.musicIndex.setText(index);
        holder.artistText.setText(currentItem.getsArtist());
        holder.durationText.setText(convertDuration(currentItem.getsDuration()));
    }

    public void removeItem(String itemID) {
        int position = musicIDList.indexOf(itemID);
        if (musicIDList.size() != 1 && !musicIDList.isEmpty()) {
            if (itemID != null) {
                musicIDList.remove(itemID);
            }
            notifyItemRangeChanged(position, musicIDList.size() - (position + 1));
            notifyItemRemoved(position);
            storageUtil.saveQueueList(musicIDList);
        } else if (musicIDList.size() == 1) {
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
        return musicIDList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateView() {
        notifyDataSetChanged();
    }

    public void updateItemInserted(String musicID) {
        int pos = storageUtil.loadMusicIndex();
        musicIDList.add(pos + 1, musicID);
        notifyItemInserted(pos + 1);
        notifyItemRangeChanged(pos + 1, musicIDList.size() - (pos + 2));
        storageUtil.saveQueueList(musicIDList);
    }

    public void updateListInserted(ArrayList<String> list) {
        int pos = storageUtil.loadMusicIndex();
        musicIDList.addAll(pos + 1, list);
        notifyItemRangeInserted(pos + 1, list.size());
        notifyItemRangeChanged(pos + list.size() + 1, musicIDList.size() - (pos + list.size() + 2));
        storageUtil.saveQueueList(musicIDList);
    }

    public void updateListInsertedLast(ArrayList<String> list) {
        musicIDList.addAll(list);
        int pos = musicIDList.lastIndexOf(list.get(0));
        notifyItemRangeInserted(pos, list.size());
        storageUtil.saveQueueList(musicIDList);
    }

    public void updateItemInsertedLast(String musicID) {
        musicIDList.add(musicID);
        int pos = musicIDList.lastIndexOf(musicID);
        notifyItemInserted(pos);
        storageUtil.saveQueueList(musicIDList);
    }

    public static class MusicViewAdapter extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final ImageView imageButton;
        private final View cardView;
        private final TextView nameText, artistText, durationText, musicIndex;

        public MusicViewAdapter(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.song_album_cover_queue);
            imageButton = itemView.findViewById(R.id.drag_i_btn_queue);
            cardView = itemView.findViewById(R.id.cv_song_play_queue);
            nameText = itemView.findViewById(R.id.song_name_queue);
            musicIndex = itemView.findViewById(R.id.song_index_num_queue);
            artistText = itemView.findViewById(R.id.song_artist_name_queue);
            durationText = itemView.findViewById(R.id.song_length_queue);
        }
    }
}
