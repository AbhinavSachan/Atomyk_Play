package com.atomykcoder.atomykplay.function;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.R;

import java.util.ArrayList;

public class MusicLyricsAdapter extends RecyclerView.Adapter<MusicLyricsAdapter.LyricsViewAdapter> {
    private Context context;
    private ArrayList<String> arrayList;

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
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class LyricsViewAdapter extends RecyclerView.ViewHolder {
        private TextView textView;
        public LyricsViewAdapter(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.lyrics_text);
        }
    }
}
