package com.atomykcoder.atomykplay.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.adapters.MusicMainAdapter;
import com.atomykcoder.atomykplay.data.Music;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;

import java.util.ArrayList;

//Search Layout Fragment for Performing Searches and Presenting Results
public class SearchFragment extends Fragment {

    private RecyclerView recycler_view;
    private RadioGroup radioGroup;
    private ArrayList<String> idList;
    private ArrayList<Music> searchedMusicList;
    private MusicMainAdapter adapter;
    private RadioButton songButton, albumButton, artistButton, genreButton;
    private TextView textView;

    @SuppressLint("NotifyDataSetChanged")
    public void searchWithFilters(String query, ArrayList<Music> dataList) {

        //Check if any radio button is pressed
        radioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            //search if any radio button is pressed
            search(query, dataList);
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_results, container, false);

        recycler_view = view.findViewById(R.id.search_recycler_view);
        searchedMusicList = new ArrayList<>();

        // Filter Buttons Initialization
        songButton = view.findViewById(R.id.song_button);
        albumButton = view.findViewById(R.id.album_button);
        artistButton = view.findViewById(R.id.artist_button);
        genreButton = view.findViewById(R.id.genre_button);
        radioGroup = view.findViewById(R.id.radio_group);
        textView = view.findViewById(R.id.searched_song_num);
        ArrayList<Music> dataList = new StorageUtil(getContext()).loadInitialList();

        InputMethodManager manager = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);


        EditText searchView = view.findViewById(R.id.search_view_search);
        ImageView closeSearch = view.findViewById(R.id.close_search_btn);
        searchView.requestFocus();
        try {
            manager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence query, int start, int before, int count) {
                handleSearchEvent(query.toString().toLowerCase(), dataList);

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        closeSearch.setOnClickListener(v -> {
            searchView.clearFocus();
            try {
                manager.hideSoftInputFromWindow(searchView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            } catch (Exception e) {
                e.printStackTrace();
            }
            requireActivity().onBackPressed();
        });

        recycler_view.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recycler_view.setLayoutManager(layoutManager);
        idList = new ArrayList<>();
        for (Music music : searchedMusicList) {
            idList.add(music.getId());
        }
        adapter = new MusicMainAdapter(getContext(), searchedMusicList);
        recycler_view.setAdapter(adapter);

        return view;
    }

    //this will set the song selected every time you open search bar
    @Override
    public void onResume() {
        super.onResume();
        songButton.setChecked(true);
    }

    //Function that adds music to an arraylist which is being used to show music in recycler view
    private void addMusic(Music song) {
        searchedMusicList.add(song);
        idList.add(song.getId());
    }


    //Function that performs searches and if it finds a match we add that song to our arraylist
    //Function also do cleanup from previous search
    //Function also searches based on filters selected
    public void search(String query, ArrayList<Music> dataList) {
        cleanUp();

        //get id from selected button
        int id = getRadioID();

        // run a codebase based on selected id
        switch (id) {
            case 1:
                if (!query.isEmpty()) {
                    for (Music song : dataList) {
                        if (song.getName().toLowerCase().contains(query)) {
                            addMusic(song);
                        }
                    }
                }
                break;
            case 2:
                if (!query.isEmpty()) {
                    for (Music song : dataList) {
                        if (song.getAlbum().toLowerCase().contains(query)) {
                            addMusic(song);
                        }
                    }
                }
                break;
            case 3:
                if (!query.isEmpty()) {
                    for (Music song : dataList) {
                        if (song.getArtist().toLowerCase().contains(query)) {
                            addMusic(song);
                        }
                    }
                }
                break;
            case 4:
                if (!query.isEmpty()) {
                    for (Music song : dataList) {
                        if (song.getGenre() != null)
                            if (song.getGenre().toLowerCase().contains(query)) {
                                addMusic(song);
                            }
                    }
                }
                break;
            default:
                if (!query.isEmpty()) {
                    for (Music song : dataList) {
                        if (song.getName().toLowerCase().contains(query) || (song.getArtist().toLowerCase().contains(query))) {
                            addMusic(song);
                        }
                    }
                }
                break;
        }
        String num = searchedMusicList.size() + " Songs";
        textView.setText(num);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void handleSearchEvent(String query, ArrayList<Music> dataList) {
        if (dataList != null) {
            search(query, dataList);
            searchWithFilters(query, dataList);
            adapter.notifyDataSetChanged();
        }
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
        searchedMusicList.clear();
        idList.clear();
    }
}