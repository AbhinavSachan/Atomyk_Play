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
import com.atomykcoder.atomykplay.adapters.ViewHolders.FavoriteViewHolder;
import com.atomykcoder.atomykplay.data.Music;
import com.atomykcoder.atomykplay.enums.OptionSheetEnum;
import com.atomykcoder.atomykplay.utils.StorageUtil;
import com.atomykcoder.atomykplay.interfaces.ItemTouchHelperAdapter;
import com.atomykcoder.atomykplay.interfaces.OnDragStartListener;
import com.atomykcoder.atomykplay.kotlin.ImageLoader;

import java.util.ArrayList;

public class FavoriteListAdapter extends MusicAdapter implements ItemTouchHelperAdapter {
    private final Context context;
    MainActivity mainActivity;
    OnDragStartListener onDragStartListener;
    StorageUtil storage;
    StorageUtil.SettingsStorage settingsStorage;
    ImageLoader imageLoader;

    public FavoriteListAdapter(Context context, ArrayList<Music> _musicList, OnDragStartListener onDragStartListener) {
        this.context = context;
        super.items = _musicList;
        this.onDragStartListener = onDragStartListener;
        mainActivity = (MainActivity) context;
        storage = new StorageUtil(context);
        settingsStorage = new StorageUtil.SettingsStorage(context);
        imageLoader = new ImageLoader(context);
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
            mainActivity.openOptionMenu(currentItem, OptionSheetEnum.FAVORITE_LIST);
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

    public void removeItem(Music item) {
        int position = super.items.indexOf(item);

        if (position != -1) {
            super.items.remove(position);
            storage.removeFavorite(item);
        }

        notifyItemRangeChanged(position, super.items.size() - (position + 1));
        notifyItemRemoved(position);

    }


}

