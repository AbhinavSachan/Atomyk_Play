package com.atomykcoder.atomykplay.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.events.LoadSelectedItemEvent;
import com.atomykcoder.atomykplay.events.SetLyricSheetStateEvent;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;


public class FoundLyricsAdapter extends RecyclerView.Adapter<FoundLyricsAdapter.FoundLyricsViewHolder> {
    private final ArrayList<String> titles;
    private final ArrayList<String> durations;
    private final ArrayList<String> urls;
    private final Context context;

    public FoundLyricsAdapter(ArrayList<String> _titles, ArrayList<String> _durations, ArrayList<String> _urls, Context _context) {
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

        holder.itemView.setOnClickListener(view -> {
            EventBus.getDefault().post(new SetLyricSheetStateEvent(BottomSheetBehavior.STATE_COLLAPSED));
            EventBus.getDefault().post(new LoadSelectedItemEvent(urls.get(holder.getBindingAdapterPosition())));
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

