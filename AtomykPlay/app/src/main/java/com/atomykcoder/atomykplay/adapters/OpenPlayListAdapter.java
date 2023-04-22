package com.atomykcoder.atomykplay.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.adapters.Generics.GenericViewHolder;
import com.atomykcoder.atomykplay.adapters.ViewHolders.OpenPlayListViewHolder;
import com.atomykcoder.atomykplay.data.Music;
import com.atomykcoder.atomykplay.enums.OptionSheetEnum;
import com.atomykcoder.atomykplay.utils.StorageUtil;
import com.atomykcoder.atomykplay.interfaces.ItemTouchHelperAdapter;
import com.atomykcoder.atomykplay.interfaces.OnDragStartListener;
import com.atomykcoder.atomykplay.helperFunctions.ImageLoader;

import java.util.ArrayList;

public class OpenPlayListAdapter extends MusicAdapter implements ItemTouchHelperAdapter {
    final Context context;
    final MainActivity mainActivity;
    String playlistName;
    StorageUtil storage;
    StorageUtil.SettingsStorage settingsStorage;
    OnDragStartListener onDragStartListener;
    ImageLoader imageLoader;

    public OpenPlayListAdapter(Context _context, String _playlistName, ArrayList<Music> items, OnDragStartListener onDragStartListener) {
        context = _context;
        playlistName = _playlistName;
        super.items = items;
        mainActivity = (MainActivity) _context;
        this.onDragStartListener = onDragStartListener;
        storage = new StorageUtil(_context);
        settingsStorage = new StorageUtil.SettingsStorage(_context);
        imageLoader = new ImageLoader(_context);
    }

    //when item starts to move it will change positions of every item in real time
    @Override
    public void onItemMove(int fromPos, int toPos) {
    }

    //removing item on swipe
    @Override
    public void onItemDismiss(int position) {
        //if any item has been removed this will save new list on temp list
        if (super.items != null) {
            if (position != -1 && position < super.items.size()) {
                Music item = super.items.get(position);
                removeItem(item);
            }
        }
    }

    @NonNull
    @Override
    public GenericViewHolder<Music> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.open_playlist_music_item, parent, false);
        return new OpenPlayListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenericViewHolder<Music> _holder, int position) {
        super.onBindViewHolder(_holder, position);

        OpenPlayListViewHolder holder = (OpenPlayListViewHolder) _holder;
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

        holder.optBtn.setOnClickListener(v -> {
            if (isMusicNotAvailable(currentItem)) {
                return;
            }
            mainActivity.openOptionMenu(currentItem, OptionSheetEnum.OPEN_PLAYLIST);
        });
    }

    @Override
    public int getItemCount() {
        return super.items.size();
    }

    private boolean isMusicNotAvailable(Music currentItem) {
        if (!doesMusicExists(currentItem)) {
            Toast.makeText(context, "Song is unavailable", Toast.LENGTH_SHORT).show();
            removeItem(currentItem);
            return true;
        }
        return false;
    }

    public void removeItem(Music music) {
        int position = super.items.indexOf(music);

        if (position != -1) {
            super.items.remove(position);
            storage.removeItemInPlaylist(music, playlistName);
        }

        notifyItemRangeChanged(position, super.items.size() - (position + 1));
        notifyItemRemoved(position);
    }
}

