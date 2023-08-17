package com.atomykcoder.atomykplay.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.ui.MainActivity;
import com.atomykcoder.atomykplay.adapters.generics.GenericRecyclerAdapter;
import com.atomykcoder.atomykplay.adapters.generics.GenericViewHolder;
import com.atomykcoder.atomykplay.adapters.viewHolders.BlackListViewHolder;
import com.atomykcoder.atomykplay.utils.StorageUtil;

import java.util.ArrayList;

public class BlockFolderListAdapter extends GenericRecyclerAdapter<String> {
    private final Context context;
    StorageUtil.SettingsStorage settingsStorage;

    public BlockFolderListAdapter(ArrayList<String> arrayList, Context context) {
        super.items = arrayList;
        this.context = context;
        settingsStorage = new StorageUtil.SettingsStorage(context);
    }

    @NonNull
    @Override
    public GenericViewHolder<String> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.blacklist_item_layout, parent, false);
        return new BlackListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenericViewHolder<String> _holder, int position) {
        super.onBindViewHolder(_holder, position);

        BlackListViewHolder holder = (BlackListViewHolder) _holder;

        holder.imageView.setOnClickListener(v -> removeFromList(super.items.get(position)));
    }

    @Override
    public int getItemCount() {
        return super.items.size();
    }

    private void removeFromList(String name) {
        int pos = super.items.indexOf(name);
        MainActivity mainActivity = ((MainActivity) context);
        settingsStorage.removeFromBlackList(name);
        super.items.remove(name);

        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, super.items.size() - (pos + 1));
        mainActivity.checkForUpdateList(true);
    }


}

