package com.atomykcoder.atomykplay.adapters.ViewHolders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.adapters.Generics.GenericViewHolder;
import com.atomykcoder.atomykplay.dataModels.Playlist;

public class PlayListDialogViewHolder extends GenericViewHolder<Playlist> {
    public final TextView textView;
    public final View view;

    public PlayListDialogViewHolder(@NonNull View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.playlist_name_dialog_tv);
        view = itemView.findViewById(R.id.playlist_name_dialog_ll);
    }
}
