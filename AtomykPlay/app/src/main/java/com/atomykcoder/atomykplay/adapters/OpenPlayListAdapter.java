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
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;
import com.atomykcoder.atomykplay.interfaces.ItemTouchHelperAdapter;
import com.atomykcoder.atomykplay.interfaces.OnDragStartListener;

import java.util.ArrayList;

public class OpenPlayListAdapter extends MusicAdapter implements ItemTouchHelperAdapter {
    final Context context;
    final MainActivity mainActivity;
    String playlistName;
    StorageUtil storage;
    StorageUtil.SettingsStorage settingsStorage;
    OnDragStartListener onDragStartListener;

    public OpenPlayListAdapter(Context _context, String _playlistName, ArrayList<Music> items, OnDragStartListener onDragStartListener) {
        context = _context;
        playlistName = _playlistName;
        super.items = items;
        mainActivity = (MainActivity) context;
        this.onDragStartListener = onDragStartListener;
        storage = new StorageUtil(context);
        settingsStorage = new StorageUtil.SettingsStorage(context);
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
        View view = LayoutInflater.from(context).inflate(R.layout.open_playlist_music_item, parent, false);
        return new OpenPlayListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenericViewHolder<Music> _holder, int position) {
        super.onBindViewHolder(_holder, position);

        OpenPlayListViewHolder holder = (OpenPlayListViewHolder) _holder;
        Music currentItem = super.items.get(position);

        loadImage(context, currentItem, position, holder.albumCoverIV);

        holder.cardView.setOnClickListener(view -> {
            if (shouldIgnoreClick(context)) return;

            if (!isMusicAvailable(currentItem)){
                return;
            }

            //region timer to stop extra clicks
            if (settingsStorage.loadKeepShuffle())
                handleShuffle(storage, position, super.items);
            else
                handleNoShuffle(storage, position, super.items);

            handlePlayMusic(mainActivity, currentItem,super.items);
        });

        holder.optBtn.setOnClickListener(v -> {
            if (!isMusicAvailable(currentItem)){
                return;
            }
            mainActivity.openOptionMenu(currentItem, "openPlaylist");
        });
    }
    private boolean isMusicAvailable(Music currentItem){
        if (!doesMusicExists(currentItem)) {
            Toast.makeText(context, "Song is unavailable", Toast.LENGTH_SHORT).show();
            removeItem(currentItem);
            return false;
        }
        return true;
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

    @Override
    public int getItemCount() {
        return super.items.size();
    }
}

