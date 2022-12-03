package com.atomykcoder.atomykplay.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.activities.MainActivity;

import java.util.ArrayList;

public class MusicLyricsAdapter extends RecyclerView.Adapter<MusicLyricsAdapter.LyricsViewAdapter> {
    private final Context context;
    private final ArrayList<String> arrayList;

    public MusicLyricsAdapter(Context context, ArrayList<String> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public LyricsViewAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.lyrics_item, parent, false);
        return new LyricsViewAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LyricsViewAdapter holder, int position) {
        holder.textView.setText(arrayList.get(position));
        holder.textView.setOnClickListener(v -> ((MainActivity) context).bottomSheetPlayerFragment
                .skipToPosition(holder.getAbsoluteAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class LyricsViewAdapter extends RecyclerView.ViewHolder {
        private final TextView textView;

        public LyricsViewAdapter(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.lyrics_text);
        }

    }
}
