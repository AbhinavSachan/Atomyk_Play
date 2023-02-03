package com.atomykcoder.atomykplay.adapters.ViewHolders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.adapters.Generics.GenericViewHolder;

public class BlackListViewHolder extends GenericViewHolder<String> {
    public final ImageView imageView;
    private final TextView textView;

    public BlackListViewHolder(@NonNull View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.blacklist_directory_path);
        imageView = itemView.findViewById(R.id.delete_from_blacklist);

    }

    @Override
    public void onBind(String item) {
        textView.setText(item);
    }
}
