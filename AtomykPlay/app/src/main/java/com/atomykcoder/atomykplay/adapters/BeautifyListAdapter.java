package com.atomykcoder.atomykplay.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.adapters.Generics.GenericRecyclerAdapter;
import com.atomykcoder.atomykplay.adapters.Generics.GenericViewHolder;
import com.atomykcoder.atomykplay.adapters.ViewHolders.BeautifyListViewHolder;
import com.atomykcoder.atomykplay.utils.StorageUtil;

import java.util.ArrayList;

public class BeautifyListAdapter extends GenericRecyclerAdapter<String> {
    private final Context context;
    private final ArrayList<String> replacingTags;
    StorageUtil.SettingsStorage settingsStorage;

    public BeautifyListAdapter(ArrayList<String> tags, Context context, ArrayList<String> replacingTags) {
        this.replacingTags = replacingTags;
        super.items = tags;
        this.context = context;
        settingsStorage = new StorageUtil.SettingsStorage(context);
    }

    @NonNull
    @Override
    public GenericViewHolder<String> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.blacklist_item_layout, parent, false);
        return new BeautifyListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenericViewHolder<String> _holder, int position) {
        super.onBindViewHolder(_holder, position);

        BeautifyListViewHolder holder = (BeautifyListViewHolder) _holder;

        String s;
        s = super.items.get(position) + " = " + "(" + replacingTags.get(position) + ")";
        holder.textView.setText(s);
        holder.imageView.setOnClickListener(v -> removeFromList(position));
    }

    @Override
    public int getItemCount() {
        return super.items.size();
    }

    private void removeFromList(int pos) {
        MainActivity mainActivity = (MainActivity) context;
        settingsStorage.removeFromBeautifyList(String.valueOf(pos));
        super.items.remove(pos);

        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, super.items.size() - (pos + 1));
        mainActivity.checkForUpdateList(true);
    }


}

