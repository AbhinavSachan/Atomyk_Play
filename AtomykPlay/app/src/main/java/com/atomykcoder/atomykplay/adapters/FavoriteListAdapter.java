package com.atomykcoder.atomykplay.adapters;

import static com.atomykcoder.atomykplay.helperFunctions.MusicHelper.convertDuration;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.adapters.Generics.GenericViewHolder;
import com.atomykcoder.atomykplay.adapters.ViewHolders.FavoriteViewHolder;
import com.atomykcoder.atomykplay.data.Music;
import com.atomykcoder.atomykplay.enums.OptionSheetEnum;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;
import com.atomykcoder.atomykplay.interfaces.ItemTouchHelperAdapter;
import com.atomykcoder.atomykplay.interfaces.OnDragStartListener;

import java.util.ArrayList;

public class FavoriteListAdapter extends MusicAdapter implements ItemTouchHelperAdapter {
    private final Context context;
    MainActivity mainActivity;
    OnDragStartListener onDragStartListener;
    StorageUtil storage;
    StorageUtil.SettingsStorage settingsStorage;

    public FavoriteListAdapter(Context context, ArrayList<Music> _musicList, OnDragStartListener onDragStartListener) {
        this.context = context;
        super.items = _musicList;
        this.onDragStartListener = onDragStartListener;
        mainActivity = (MainActivity) context;
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
                removeItem(super.items.get(position));
            }
        }
    }

    @NonNull
    @Override
    public GenericViewHolder<Music> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.favorite_item_layout, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenericViewHolder<Music> _holder, int position) {
        super.onBindViewHolder(_holder, position);

        FavoriteViewHolder holder = (FavoriteViewHolder) _holder;
        Music currentItem = super.items.get(position);

        loadImage(context, currentItem, position, holder.albumCoverIV);

        holder.cardView.setOnClickListener(view -> {
            if (shouldIgnoreClick(context)) return;

            if (!isMusicAvailable(currentItem)) {
                return;
            }

            if (settingsStorage.loadKeepShuffle())
                handleShuffle(storage, position, super.items);
            else
                handleNoShuffle(storage, position, super.items);

            handlePlayMusic(mainActivity, currentItem);
        });

        holder.optBtn.setOnClickListener(v -> {
            if (!isMusicAvailable(currentItem)) {
                return;
            }
            mainActivity.openOptionMenu(currentItem, OptionSheetEnum.FAVORITE_LIST);
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
    public void removeItem(Music item) {
        int position = super.items.indexOf(item);

        if (position != -1) {
            super.items.remove(position);
            storage.removeFavorite(item);
        }

        notifyItemRangeChanged(position, super.items.size() - (position + 1));
        notifyItemRemoved(position);

    }

    @Override
    public int getItemCount() {
        return super.items.size();
    }


}

