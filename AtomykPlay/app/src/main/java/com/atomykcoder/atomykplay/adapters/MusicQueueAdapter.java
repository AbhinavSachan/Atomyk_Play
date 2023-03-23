package com.atomykcoder.atomykplay.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.adapters.Generics.GenericViewHolder;
import com.atomykcoder.atomykplay.adapters.ViewHolders.MusicQueueViewHolder;
import com.atomykcoder.atomykplay.data.Music;
import com.atomykcoder.atomykplay.helperFunctions.MusicDiffCallback;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;
import com.atomykcoder.atomykplay.interfaces.ItemTouchHelperAdapter;
import com.atomykcoder.atomykplay.interfaces.OnDragStartListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.Collections;

public class MusicQueueAdapter extends MusicAdapter implements ItemTouchHelperAdapter {
    Context context;
    OnDragStartListener onDragStartListener;
    MainActivity mainActivity;
    StorageUtil storageUtil;

    public MusicQueueAdapter(Context _context, ArrayList<Music> items, OnDragStartListener onDragStartListener) {
        context = _context;
        super.items = items;
        this.onDragStartListener = onDragStartListener;
        mainActivity = (MainActivity) context;
        storageUtil = new StorageUtil(context);
    }

    public void updateMusicListItems(ArrayList<Music> newMusicArrayList) {
        final MusicDiffCallback diffCallback = new MusicDiffCallback(super.items, newMusicArrayList);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        super.items.clear();
        super.items.addAll(newMusicArrayList);
        diffResult.dispatchUpdatesTo(this);
    }


    //when item starts to move it will change positions of every item in real time
    @Override
    public void onItemMove(int fromPos, int toPos) {
        int savedIndex = storageUtil.loadMusicIndex();

        Collections.swap(super.items, fromPos, toPos);
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
        storageUtil.saveQueueList(super.items);

    }


    //removing item on swipe
    @Override
    public void onItemDismiss(int position) {
        //if any item has been removed this will save new list on temp list
        if (super.items != null) {
            if (position != -1 && position < super.items.size()) {
                removeItem(super.items.get(position));
            }
        }
    }

    @NonNull
    @Override
    public GenericViewHolder<Music> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.queue_music_item_layout, parent, false);
        return new MusicQueueViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull GenericViewHolder<Music> _holder, int position) {
        super.onBindViewHolder(_holder, position);

        MusicQueueViewHolder holder = (MusicQueueViewHolder) _holder;
        holder.musicIndex.setText(String.valueOf(position + 1));

        Music currentItem = super.items.get(position);

        loadImage(context, currentItem, position, holder.albumCoverIV);

        //playing song
        holder.cardView.setOnClickListener(v -> {
            if (shouldIgnoreClick(context)) return;

            if (!isMusicAvailable(currentItem)) {
                return;
            }

            storageUtil.saveMusicIndex(position);
            mainActivity.playAudio(currentItem);
        });

        holder.dragButton.setOnTouchListener((v, event) -> {
            if (!isMusicAvailable(currentItem)) {
                return false;
            }
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                onDragStartListener.onDragStart(holder);
            }
            return false;
        });

    }

    private boolean isMusicAvailable(Music currentItem) {
        if (!doesMusicExists(currentItem)) {
            Toast.makeText(context, "Song is unavailable", Toast.LENGTH_SHORT).show();
            removeItem(currentItem);
            return false;
        }
        return true;
    }

    public void removeItem(Music item) {
        int position = super.items.indexOf(item);
        int savedIndex = storageUtil.loadMusicIndex();
        if (!super.items.isEmpty()) {
            if (super.items.size() == 1) {
                mainActivity.bottomSheetPlayerFragment.queueSheetBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
                mainActivity.clearStorage();
                mainActivity.mainPlayerSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                mainActivity.bottomSheetPlayerFragment.resetMainPlayerLayout();
                mainActivity.resetDataInNavigation();
                mainActivity.stopMusic();
            }
            if (item != null) {
                super.items.remove(item);
            }
            if (!super.items.isEmpty()) {
                if (position == savedIndex) {
                    mainActivity.playAudio(super.items.get(savedIndex));
                } else if (position < savedIndex) {
                    storageUtil.saveMusicIndex(savedIndex - 1);
                }
            }
            notifyItemRangeChanged(position, super.items.size() - (position + 1));
            notifyItemRemoved(position);
            storageUtil.saveQueueList(super.items);
        }
    }

    @Override
    public int getItemCount() {
        return super.items.size();
    }

    public void updateItemInserted(Music music) {
        int pos = storageUtil.loadMusicIndex();
        super.items.add(pos + 1, music);
        notifyItemInserted(pos + 1);
        notifyItemRangeChanged(pos + 1, super.items.size() - (pos + 2));
        storageUtil.saveQueueList(super.items);
    }

    public void updateListInserted(ArrayList<Music> list) {
        int pos = storageUtil.loadMusicIndex();
        super.items.addAll(pos + 1, list);
        notifyItemRangeInserted(pos + 1, list.size());
        notifyItemRangeChanged(pos + list.size() + 1, super.items.size() - (pos + list.size() + 2));
        storageUtil.saveQueueList(super.items);
    }

    public void updateListInsertedLast(ArrayList<Music> list) {
        super.items.addAll(list);
        int pos = super.items.lastIndexOf(list.get(0));
        notifyItemRangeInserted(pos, list.size());
        storageUtil.saveQueueList(super.items);
    }

    public void updateItemInsertedLast(Music music) {
        super.items.add(music);
        int pos = super.items.lastIndexOf(music);
        notifyItemInserted(pos);
        storageUtil.saveQueueList(super.items);
    }
}

