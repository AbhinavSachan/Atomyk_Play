package com.atomykcoder.atomykplay.adapters.viewHolders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.adapters.generics.GenericViewHolder;

public class BeautifyListViewHolder extends GenericViewHolder<String> {
    public final ImageView imageView;
    public final TextView textView;

    public BeautifyListViewHolder(@NonNull View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.blacklist_directory_path);
        imageView = itemView.findViewById(R.id.delete_from_blacklist);

    }

    @Override
    public void onBind(String item) {
    }
}
