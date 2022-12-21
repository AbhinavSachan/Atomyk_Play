package com.atomykcoder.atomykplay.adapters;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.adapters.Generics.GenericViewHolder;
import com.atomykcoder.atomykplay.adapters.ViewHolders.MusicMainViewHolder;
import com.atomykcoder.atomykplay.classes.GlideBuilt;
import com.atomykcoder.atomykplay.data.Music;
import com.atomykcoder.atomykplay.helperFunctions.MusicDiffCallback;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MusicMainAdapter extends MusicAdapter {
    Context context;
    MainActivity mainActivity;
    StorageUtil storage;
    StorageUtil.SettingsStorage settingsStorage;

    public MusicMainAdapter(Context _context, ArrayList<Music> musicList) {
        context = _context;
        mainActivity = (MainActivity) context;
        storage = new StorageUtil(context);
        settingsStorage = new StorageUtil.SettingsStorage(context);
        super.items = musicList;
    }

    public void updateMusicListItems(ArrayList<Music> newMusicArrayList) {
        final MusicDiffCallback diffCallback = new MusicDiffCallback(super.items, newMusicArrayList);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        super.items.clear();
        super.items.addAll(newMusicArrayList);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public GenericViewHolder<Music> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.music_item_layout, parent, false);
        return new MusicMainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenericViewHolder<Music> _holder, int position) {
        super.onBindViewHolder(_holder, position);

        MusicMainViewHolder holder = (MusicMainViewHolder) _holder;
        Music currentItem = super.items.get(position);

        loadImage(context, currentItem, position, holder.albumCoverIV);

            if(shouldIgnoreClick()) return;

            if(!doesMusicExists(currentItem)) {
                Toast.makeText(context, "Song is unavailable", Toast.LENGTH_SHORT).show();
                removeItem(currentItem);
            }

            //region timer to stop extra clicks
            if(settingsStorage.loadKeepShuffle())
                handleShuffle(storage, position, super.items);
            else
                handleNoShuffle(storage, position, super.items);

            handlePlayMusic(mainActivity, currentItem);


        holder.optionButton.setOnClickListener(v -> {
            if(!doesMusicExists(currentItem)) {
                Toast.makeText(context, "Song is unavailable", Toast.LENGTH_SHORT).show();
                removeItem(currentItem);
                return;
            }
            mainActivity.openOptionMenu(currentItem, "mainList");
        });
    }

    @Override
    public void removeItem(Music item) {
        StorageUtil storageUtil = new StorageUtil(context);
        int position = super.items.indexOf(item);
        int savedIndex = storageUtil.loadMusicIndex();

        if (position < savedIndex) {
            storageUtil.saveMusicIndex(savedIndex - 1);
        }
        if (position != -1) {
            storageUtil.removeFromInitialList(item);
            super.items.remove(position);
        }

        notifyItemRangeChanged(position, super.items.size() - (position + 1));
        notifyItemRemoved(position);

        if (position == savedIndex) {
            mainActivity.playAudio(item);
        }
        mainActivity.bottomSheetPlayerFragment.queueAdapter.removeItem(item);
    }
}


