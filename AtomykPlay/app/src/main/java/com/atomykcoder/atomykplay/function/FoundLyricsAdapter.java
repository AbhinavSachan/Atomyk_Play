package com.atomykcoder.atomykplay.function;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.MainActivity;
import com.atomykcoder.atomykplay.R;

import java.util.ArrayList;


public class FoundLyricsAdapter extends RecyclerView.Adapter<FoundLyricsAdapter.FoundLyricsViewHolder> {
    private ArrayList<String> titles;
    private ArrayList<String> durations;
    private ArrayList<String> urls;
    private Context context;

    public FoundLyricsAdapter(ArrayList<String> _titles, ArrayList<String> _durations, ArrayList<String> _urls ,Context _context){
        titles = _titles;
        durations = _durations;
        urls = _urls;
        context = _context;
    }

    @NonNull
    @Override
    public FoundLyricsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.found_lyrics_item, parent, false);
        return new FoundLyricsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoundLyricsViewHolder holder, int position) {
        holder.song_title.setText(titles.get(position));
        holder.song_duration.setText(durations.get(position));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity mainActivity = (MainActivity) context;
                mainActivity.bottomSheetPlayerFragment.addLyricsFragment.loadSelectedLyrics(urls.get(holder.getBindingAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    public static class FoundLyricsViewHolder extends RecyclerView.ViewHolder {
        public TextView song_title;
        public TextView song_duration;

        public FoundLyricsViewHolder(@NonNull View itemView) {
            super(itemView);
            song_title = itemView.findViewById(R.id.found_song_title);
            song_duration = itemView.findViewById(R.id.found_song_lyrics_sample);
        }
    }
}

