package com.atomykcoder.atomykplay.adapters;

import static com.atomykcoder.atomykplay.helperFunctions.MusicHelper.convertDuration;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
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
import com.atomykcoder.atomykplay.data.Music;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;
import com.atomykcoder.atomykplay.interfaces.ItemTouchHelperAdapter;
import com.atomykcoder.atomykplay.interfaces.OnDragStartListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MusicQueueAdapter extends RecyclerView.Adapter<MusicQueueAdapter.MusicViewAdapter> implements ItemTouchHelperAdapter {
    Context context;
    ArrayList<Music> musicList;
    OnDragStartListener onDragStartListener;
    MainActivity mainActivity;
    StorageUtil storageUtil;
    long lastClickTime;
    // value in milliseconds
    int delay = 500;

    public MusicQueueAdapter(Context _context, ArrayList<Music> _musicList, OnDragStartListener onDragStartListener) {
        context = _context;
        musicList = _musicList;

        this.onDragStartListener = onDragStartListener;
        mainActivity = (MainActivity) context;
        storageUtil = new StorageUtil(context);
    }

    //when item starts to move it will change positions of every item in real time
    @Override
    public void onItemMove(int fromPos, int toPos) {
        int savedIndex = storageUtil.loadMusicIndex();

        Collections.swap(musicList, fromPos, toPos);
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
        storageUtil.saveQueueList(musicList);

    }

    //removing item on swipe
    @Override
    public void onItemDismiss(int position) {
        int savedIndex;
        savedIndex = storageUtil.loadMusicIndex();
        //if any item has been removed this will save new list on temp list
        if (musicList != null) {
            if (position != -1 && position < musicList.size()) {
                removeItem(musicList.get(position));
            }
            if (position == savedIndex) {
                mainActivity.playAudio(musicList.get(position));
            } else if (position < savedIndex) {
                storageUtil.saveMusicIndex(savedIndex - 1);
            }
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
        Music currentItem = musicList.get(position);
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
        //playing song
        holder.cardView.setOnClickListener(v -> {

            //region timer to stop extra clicks
            if(SystemClock.elapsedRealtime() < (lastClickTime + delay)) {
                return;
            }
            lastClickTime = SystemClock.elapsedRealtime();
            //endregion


            File file = new File(currentItem.getPath());
            if (file.exists()) {
                //check is service active
                //Store serializable music list to sharedPreference
                storageUtil.saveMusicIndex(position);
                mainActivity.playAudio(currentItem);
            } else {
                Toast.makeText(context, "Song is unavailable", Toast.LENGTH_SHORT).show();
                removeItem(currentItem);
            }

        });

        //add bottom sheet functions in three dot click
        holder.imageButton.setOnTouchListener((v, event) -> {
            File file = new File(currentItem.getPath());
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

        holder.nameText.setText(currentItem.getName());
        String index = String.valueOf(position + 1);
        holder.musicIndex.setText(index);
        holder.artistText.setText(currentItem.getArtist());
        holder.durationText.setText(convertDuration(currentItem.getDuration()));
    }

    public void removeItem(Music item) {
        int position = musicList.indexOf(item);
        if (musicList.size() != 1 && !musicList.isEmpty()) {
            if (item != null) {
                musicList.remove(item);
            }
            notifyItemRangeChanged(position, musicList.size() - (position + 1));
            notifyItemRemoved(position);
            storageUtil.saveQueueList(musicList);
        } else if (musicList.size() == 1) {
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
        return musicList.size();
    }

    public void updateItemInserted(Music music) {
        int pos = storageUtil.loadMusicIndex();
        musicList.add(pos + 1, music);
        notifyItemInserted(pos + 1);
        notifyItemRangeChanged(pos + 1, musicList.size() - (pos + 2));
        storageUtil.saveQueueList(musicList);
    }

    public void updateListInserted(ArrayList<Music> list) {
        int pos = storageUtil.loadMusicIndex();
        musicList.addAll(pos + 1, list);
        notifyItemRangeInserted(pos + 1, list.size());
        notifyItemRangeChanged(pos + list.size() + 1, musicList.size() - (pos + list.size() + 2));
        storageUtil.saveQueueList(musicList);
    }

    public void updateListInsertedLast(ArrayList<Music> list) {
        musicList.addAll(list);
        int pos = musicList.lastIndexOf(list.get(0));
        notifyItemRangeInserted(pos, list.size());
        storageUtil.saveQueueList(musicList);
    }

    public void updateItemInsertedLast(Music music) {
        musicList.add(music);
        int pos = musicList.lastIndexOf(music);
        notifyItemInserted(pos);
        storageUtil.saveQueueList(musicList);
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
