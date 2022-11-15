package com.atomykcoder.atomykplay.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;
import com.atomykcoder.atomykplay.adapters.MusicMainAdapter;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

//Search Layout Fragment for Performing Searches and Presenting Results
public class SearchResultsFragment extends Fragment {

    private RecyclerView recycler_view;
    private RadioGroup radioGroup;
    private ArrayList<MusicDataCapsule> originalMusicList;
    private MusicMainAdapter adapter;
    private RadioButton songButton, albumButton, artistButton, genreButton;
    private TextView textView;

    public void SearchWithFilters(String query,ArrayList<MusicDataCapsule> dataList) {

        //Check if any radio button is pressed
        radioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            //search if any radio button is pressed
            search(query, dataList);
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_results, container, false);
        recycler_view = view.findViewById(R.id.search_recycler_view);
        originalMusicList = new ArrayList<>();

        // Filter Buttons Initialization
        songButton = view.findViewById(R.id.song_button);
        albumButton = view.findViewById(R.id.album_button);
        artistButton = view.findViewById(R.id.artist_button);
        genreButton = view.findViewById(R.id.genre_button);
        radioGroup = view.findViewById(R.id.radio_group);
        textView = view.findViewById(R.id.searched_song_num);


        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        recycler_view.setLayoutManager(layoutManager);
        recycler_view.setItemAnimator(new DefaultItemAnimator());
        setAdapter();

        return view;
    }

    //this will set the song selected every time you open search bar
    @Override
    public void onResume() {
        super.onResume();
        songButton.setChecked(true);
    }

    //Initializing And setting an adapter
    private void setAdapter() {
        adapter = new MusicMainAdapter(getContext(), originalMusicList);
        recycler_view.setAdapter(adapter);
    }

    //Function that adds music to an arraylist which is being used to show music in recycler view
    private void addMusic(MusicDataCapsule song) {
        originalMusicList.add(song);
    }

    //Function that performs searches and if it finds a match we add that song to our arraylist
    //Function also do cleanup from previous search
    //Function also searches based on filters selected
    public void search(String query, ArrayList<MusicDataCapsule> dataList) {
        cleanUp();

        //get id from selected button
        int id = getRadioID();

        // run a codebase based on selected id
        switch (id) {
            case 1:
                if (!query.isEmpty()) {
                    for (MusicDataCapsule song : dataList) {
                        if (song.getsName().toLowerCase().contains(query)) {
                            addMusic(song);
                        }
                    }
                }
                break;
            case 2:
                if (!query.isEmpty()) {
                    for (MusicDataCapsule song : dataList) {
                        if (song.getsAlbum().toLowerCase().contains(query)) {
                            addMusic(song);
                        }
                    }
                }
                break;
            case 3:
                if (!query.isEmpty()) {
                    for (MusicDataCapsule song : dataList) {
                        if (song.getsArtist().toLowerCase().contains(query)) {
                            addMusic(song);
                        }
                    }
                }
                break;
            case 4:
                if (!query.isEmpty()) {
                    for (MusicDataCapsule song : dataList) {
                        if (song.getsGenre() != null)
                            if (song.getsGenre().toLowerCase().contains(query)) {
                                addMusic(song);
                            }
                    }
                }
                break;
            default:
                if (!query.isEmpty()) {
                    for (MusicDataCapsule song : dataList) {
                        if (song.getsName().toLowerCase().contains(query) || (song.getsArtist().toLowerCase().contains(query))) {
                            addMusic(song);
                        }
                    }
                }
                break;
        }
        String num = originalMusicList.size() + " Songs";
        textView.setText(num);
    }

    //get radio get based on which radio button is selected
    private int getRadioID() {
        int radioId = 0;

        if (songButton.isChecked()) {
            radioId = 1;
        } else if (albumButton.isChecked()) {
            radioId = 2;
        } else if (artistButton.isChecked()) {
            radioId = 3;
        } else if (genreButton.isChecked()) {
            radioId = 4;
        }
        return radioId;
    }

    //Cleaning up any search results left from last search
    //Refreshing list
    private void cleanUp() {
        originalMusicList.clear();
        adapter.notifyItemRangeChanged(0,originalMusicList.size());
    }
}