package com.atomykcoder.atomykplay.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.adapters.MusicMainAdapter;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;
import com.atomykcoder.atomykplay.viewModals.MusicDataCapsule;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class LastAddedFragment extends Fragment {
    private final int firstOptionValue = 30;
    private final int secondOptionValue = 60;
    private final int thirdOptionValue = 90;
    private final int fourthOptionValue = 120;
    private RadioGroup radioGroup;
    private RadioButton firstRadio, secondRadio, thirdRadio, fourthRadio;
    private MusicMainAdapter adapter;
    private ArrayList<MusicDataCapsule> lastAddedMusicList;
    private Dialog filterDialog;
    private TextView songCountTv;
    private StorageUtil.SettingsStorage settingsStorage;
    private ArrayList<MusicDataCapsule> initialMusicList;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_last_added, container, false);

        //get references
        RecyclerView recyclerView = view.findViewById(R.id.last_added_recycle_view);
        settingsStorage = new StorageUtil.SettingsStorage(requireContext());
        View filterButton = view.findViewById(R.id.filter_last_added_btn);
        ImageView backImageView = view.findViewById(R.id.close_filter_btn);
        progressBar = view.findViewById(R.id.progress_bar_filter);
        songCountTv = view.findViewById(R.id.count_of_lastAdded);
        FragmentManager fragmentManager = ((MainActivity) requireContext()).getSupportFragmentManager();

        // initialize/load music array lists
        initialMusicList = new StorageUtil(getContext()).loadInitialList();
        lastAddedMusicList = new ArrayList<>();

        // load previous set data
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable = () -> {
            int i = settingsStorage.loadLastAddedDur();
            loadLastAddedList(i);
        };
        handler.postDelayed(runnable, 200);

        // back button click listener
        backImageView.setOnClickListener(v -> fragmentManager.popBackStackImmediate());

        // filter click listener
        filterButton.setOnClickListener(v -> openDialogFilter());

        // set recyclerview and adapter
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MusicMainAdapter(getContext(), lastAddedMusicList);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @SuppressLint("SetTextI18n")
    private void openDialogFilter() {
        filterDialog = new Dialog(requireContext());
        filterDialog.setContentView(R.layout.last_added_filter_dialog);

        int savedState = settingsStorage.loadLastAddedDur();

        radioGroup = filterDialog.findViewById(R.id.last_Added_radio_group);
        firstRadio = filterDialog.findViewById(R.id.last_added_rb_1);
        secondRadio = filterDialog.findViewById(R.id.last_added_rb_2);
        thirdRadio = filterDialog.findViewById(R.id.last_added_rb_3);
        fourthRadio = filterDialog.findViewById(R.id.last_added_rb_4);

        switch (savedState) {
            case 1:
                firstRadio.setChecked(true);
                break;
            case 2:
                secondRadio.setChecked(true);
                break;
            case 3:
                thirdRadio.setChecked(true);
                break;
            case 4:
                fourthRadio.setChecked(true);
                break;
        }

        firstRadio.setText(firstOptionValue + " days");
        secondRadio.setText(secondOptionValue + " days");
        thirdRadio.setText(thirdOptionValue + " days");
        fourthRadio.setText(fourthOptionValue + " days");

        radioGroup.setOnCheckedChangeListener((radioGroup, in) -> {
            int i = getRadioID();
            loadLastAddedList(i);
            filterDialog.dismiss();
        });
        filterDialog.show();
    }



    @Override
    public void onStop() {
        super.onStop();
        if (filterDialog != null) {
            if (filterDialog.isShowing()) {
                filterDialog.dismiss();
            }
        }
    }



    /**
     * load all last added songs based on selected radio buttons
     * @param i radio id
     */
    private void loadLastAddedList(int i) {
        switch (i) {
            case 1:
                startThread(firstOptionValue);
                break;
            case 2:
                startThread(secondOptionValue);
                break;
            case 3:
                startThread(thirdOptionValue);
                break;
            case 4:
                startThread(fourthOptionValue);
                break;
        }
        settingsStorage.setLastAddedDur(i);
    }

    /**
     * start thread to handle loading music list within range
     * @param maxValue clamp at max value
     */
    private void startThread(int maxValue) {
        ExecutorService service = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        progressBar.setVisibility(View.VISIBLE);
        service.execute(() -> {
                lastAddedMusicList.clear();
                lastAddedMusicList.addAll(getMusicListWithinRange(maxValue));
            handler.post(() -> {
                progressBar.setVisibility(View.GONE);
                notifyAdapter();
                String num = lastAddedMusicList.size() + " Songs";
                songCountTv.setText(num);
                });
        });
        service.shutdown();
    }



    /**
     * takes the difference between current date and date_added and checks if difference is
     * less than given filter amount
     * @param dateAdded  music date_added
     * @param max maximum value
     * @return returns true if difference between current date and date_added is less than or equal
     * to given days_filter
     */
    private boolean isWithinDaysSelected(String dateAdded, int max) {
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        // initialize variables
        try {
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
            if (days <= max) {
                return true;
            }
        } catch (ParseException e) { e.printStackTrace(); }

        return false;
    }

    /**
     * get music list within specified range in days
     * @param max maximum value
     * @return returns arraylist<musicdatacapsule> within specified range
     */
    private ArrayList<MusicDataCapsule> getMusicListWithinRange(int max) {
        ArrayList<MusicDataCapsule> result = new ArrayList<>();
        for (MusicDataCapsule music : initialMusicList) {
            boolean isWithinRange = isWithinDaysSelected(music.getsDateAdded(), max);
            if(isWithinRange)
                result.add(music);
        }

        return result;
    }


    /**
     * radio id check
     *
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

    @SuppressLint("NotifyDataSetChanged")
    private void notifyAdapter() {
        adapter.notifyDataSetChanged();
    }

}
