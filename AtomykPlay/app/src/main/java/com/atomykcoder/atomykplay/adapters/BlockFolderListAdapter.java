package com.atomykcoder.atomykplay.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;

import java.util.ArrayList;

public class BlockFolderListAdapter extends RecyclerView.Adapter<BlockFolderListAdapter.BlackListViewHolder> {
    private ArrayList<String> arrayList;
    private Context context;

    public BlockFolderListAdapter(ArrayList<String> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public BlackListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.blacklist_item_layout, parent, false);
        return new BlackListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlackListViewHolder holder, int position) {
        String curItem = arrayList.get(position);

        holder.textView.setText(curItem);
        holder.imageView.setOnClickListener(v -> removeFromList(curItem));
    }

    private void removeFromList(String name) {
        int pos = arrayList.indexOf(name);
        MainActivity mainActivity = (MainActivity) context;
        StorageUtil.SettingsStorage settingsStorage = new StorageUtil.SettingsStorage(context);
        settingsStorage.removeFromBlackList(name);
        arrayList.remove(name);
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, arrayList.size() - (pos + 1));
        mainActivity.checkForUpdateMusic();
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class BlackListViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final ImageView imageView;

        public BlackListViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.blacklist_directory_path);
            imageView = itemView.findViewById(R.id.delete_from_blacklist);

        }
    }
}
