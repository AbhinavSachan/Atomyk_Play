package com.atomykcoder.atomykplay.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.adapters.MusicMainAdapter;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;
import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class LastAddedFragment extends Fragment {
    private RadioGroup radioGroup;
    private RadioButton firstRadio, secondRadio, thirdRadio, fourthRadio;
    private RecyclerView recyclerView;
    private ArrayList<MusicDataCapsule> lastAddedMusicList;
    private final int firstOptionValue = 30;
    private final int secondOptionValue = 60;
    private final int thirdOptionValue = 90;
    private final int fourthOptionValue = 120;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_last_added, container, false);

        //get references
        radioGroup = view.findViewById(R.id.last_Added_radio_group);
        firstRadio = view.findViewById(R.id.last_added_rb_1);
        secondRadio = view.findViewById(R.id.last_added_rb_2);
        thirdRadio = view.findViewById(R.id.last_added_rb_3);
        fourthRadio = view.findViewById(R.id.last_added_rb_4);
        recyclerView = view.findViewById(R.id.last_added_recycle_view);

        firstRadio.setText(String.valueOf(firstOptionValue));
        secondRadio.setText(String.valueOf(secondOptionValue));
        thirdRadio.setText(String.valueOf(thirdOptionValue));
        fourthRadio.setText(String.valueOf(fourthOptionValue));

        // initialize/load music lists
        ArrayList<MusicDataCapsule> initialMusicList = new StorageUtil(getContext()).loadInitialList();
        lastAddedMusicList = new ArrayList<>();

        //radio buttons check change listener
        radioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            lastAddedMusicList.clear();
            loadLastAdded(initialMusicList);
        });

        //set Adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        MusicMainAdapter adapter = new MusicMainAdapter(view.getContext(), lastAddedMusicList);
        recyclerView.setAdapter(adapter);

        return view;
    }

    /**
     * load all last added songs based on selected radio buttons
     * @param datalist datalist with all songs available on device
     */
    private void loadLastAdded(ArrayList<MusicDataCapsule> datalist) {
        int i = getRadioID();
        switch (i) {
            case 1 :
                for(MusicDataCapsule music : datalist) {
                    boolean isWithinDays = isWithinDaysSelected(music.getsDateAdded(), firstOptionValue);
                    Log.i("info", music.getsName() + " : " + isWithinDays);
                    if(isWithinDays){
                        lastAddedMusicList.add(music);
                    }
                }
                break;
            case 2:
                for(MusicDataCapsule music : datalist) {
                    boolean isWithinDays = isWithinDaysSelected(music.getsDateAdded(), secondOptionValue);
                    Log.i("info", music.getsName() + " : " + isWithinDays);
                    if(isWithinDays){
                        lastAddedMusicList.add(music);
                    }
                }
                break;
            case 3:
                for(MusicDataCapsule music : datalist) {
                    boolean isWithinDays = isWithinDaysSelected(music.getsDateAdded(), thirdOptionValue);
                    Log.i("info", music.getsName() + " : " + isWithinDays);
                    if(isWithinDays){
                        lastAddedMusicList.add(music);
                    }
                }
                break;
            case 4:
                for(MusicDataCapsule music : datalist) {
                    boolean isWithinDays = isWithinDaysSelected(music.getsDateAdded(), fourthOptionValue);
                    Log.i("info", music.getsName() + " : " + isWithinDays);
                    if(isWithinDays){
                        lastAddedMusicList.add(music);
                    }
                }
                break;
        }
    }

    /**
     * takes the difference between current date and date_added and checks if difference is
     * less than given filter amount
     * @param dateAdded music date_added
     * @param daysFilter filter
     * @return returns true if difference between current date and date_added is less than or equal
     * to given days_filter
     */
    private boolean isWithinDaysSelected(String dateAdded, int daysFilter) {
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        try {
            // initialize variables
            Date _currentDate = dateFormat.parse(currentDate);
            Date _dateAdded = dateFormat.parse(dateAdded);
            long diff = 0;

            //gets Difference
            if (_currentDate != null && _dateAdded != null) {
                diff = _currentDate.getTime() - _dateAdded.getTime();
            }

            // get days between two days
            long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

            // finally if days is less or equal to filter then return true else false
            if(days <= daysFilter) {
                return true;
            }

        } catch(ParseException e) { e.printStackTrace(); }

        return false;
    }


    /**
     * radio id check
     * @return returns selected radio button id
     */
    private int getRadioID() {
        int radioId = 0;

        if (firstRadio.isChecked()) {
            radioId = 1;
        } else if (secondRadio.isChecked()) {
            radioId = 2;
        } else if (thirdRadio.isChecked()) {
            radioId = 3;
        } else if (fourthRadio.isChecked()) {
            radioId = 4;
        }
        return radioId;
    }
}
