package com.atomykcoder.atomykplay.adapters;

import android.content.Context;
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
import com.atomykcoder.atomykplay.enums.OptionSheetEnum;
import com.atomykcoder.atomykplay.helperFunctions.MusicDiffCallback;
import com.atomykcoder.atomykplay.utils.StorageUtil;
import com.atomykcoder.atomykplay.kotlin.ImageLoader;
import com.atomykcoder.atomykplay.utils.MusicUtils;

import java.util.ArrayList;

public class MusicMainAdapter extends MusicAdapter {
    Context context;
    MainActivity mainActivity;
    StorageUtil storage;
    StorageUtil.SettingsStorage settingsStorage;
    MusicUtils musicUtils;
    ImageLoader imageLoader;
    GlideBuilt glideBuilt;

    public MusicMainAdapter(Context _context, ArrayList<Music> musicList) {
        context = _context;
        mainActivity = (MainActivity) context;
        storage = new StorageUtil(context);
        settingsStorage = new StorageUtil.SettingsStorage(context);
        musicUtils = MusicUtils.getInstance();
        super.items = musicList;
        imageLoader = new ImageLoader(_context);
        glideBuilt = new GlideBuilt(context);
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_item_layout, parent, false);
        return new MusicMainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenericViewHolder<Music> _holder, int position) {
        super.onBindViewHolder(_holder, position);

        MusicMainViewHolder holder = (MusicMainViewHolder) _holder;
        Music currentItem = super.items.get(position);

        imageLoader.loadImage(R.drawable.ic_music, currentItem, holder.albumCoverIV, 128);

        holder.cardView.setOnClickListener(view -> {
            if (shouldIgnoreClick()) return;

            if (isMusicNotAvailable(currentItem)) {
                return;
            }

            if (canPlay()){
                if (settingsStorage.loadKeepShuffle()) {
                    handleShuffle(mainActivity,currentItem,storage, position, super.items);
                } else {
                    handleNoShuffle(mainActivity,currentItem,storage, position, super.items);
                }
            }
        });

        holder.optionButton.setOnClickListener(v -> {
            if (isMusicNotAvailable(currentItem)) {
                return;
            }
            mainActivity.openOptionMenu(currentItem, OptionSheetEnum.MAIN_LIST);
        });
    }

    private boolean isMusicNotAvailable(Music currentItem) {
        if (!doesMusicExists(currentItem)) {
            Toast.makeText(context, "Song is unavailable", Toast.LENGTH_SHORT).show();
            removeItem(currentItem);
            return true;
        }
        return false;
    }

    public void removeItem(Music item) {
        int position = super.items.indexOf(item);

        if (position != -1) {
            storage.removeFromInitialList(item);
            super.items.remove(position);
            musicUtils.removeFromList(item);
        }

        notifyItemRangeChanged(position, super.items.size() - (position + 1));
        notifyItemRemoved(position);

        mainActivity.bottomSheetPlayerFragment.queueAdapter.removeItem(item);
    }
}


