package com.atomykcoder.atomykplay.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.adapters.ViewHolders.FoundLyricsViewHolder;
import com.atomykcoder.atomykplay.fragments.AddLyricsFragment;

import java.util.ArrayList;

public class FoundLyricsAdapter extends RecyclerView.Adapter<FoundLyricsViewHolder> {
    private final ArrayList<String> titles;
    private final ArrayList<String> sampleLyrics;
    private final ArrayList<String> urls;
    private final MainActivity mainActivity;

    public FoundLyricsAdapter(ArrayList<String> _titles, ArrayList<String> _sampleLyrics, ArrayList<String> _urls, Context _context) {
        titles = _titles;
        sampleLyrics = _sampleLyrics;
        urls = _urls;
        mainActivity = (MainActivity) _context;
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
        holder.song_sampleLyrics.setText(sampleLyrics.get(position));

        holder.itemView.setOnClickListener(view -> {
            mainActivity.setBottomSheetState();
            AddLyricsFragment fragment = (AddLyricsFragment) mainActivity.getSupportFragmentManager().findFragmentByTag("AddLyricsFragment");
            if (fragment != null) {
                fragment.loadSelectedLyrics(urls.get(holder.getBindingAdapterPosition()));
            }
        });

    }

    @Override
    public int getItemCount() {
        return titles.size();
    }
}

