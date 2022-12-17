package com.atomykcoder.atomykplay.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class LastAddedFragment extends Fragment {
    private final int firstOptionValue = 30;
    private final int secondOptionValue = 90;
    private final int thirdOptionValue = 180;
    private final int fourthOptionValue = 360;
    private RadioButton firstRadio, secondRadio, thirdRadio, fourthRadio;
    private MusicMainAdapter adapter;
    private ArrayList<MusicDataCapsule> lastAddedMusicList;
    private Dialog filterDialog;
    private TextView songCountTv;
    private StorageUtil.SettingsStorage settingsStorage;
    private ArrayList<MusicDataCapsule> initialMusicList;
    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_last_added, container, false);

        //get references
        recyclerView = view.findViewById(R.id.last_added_recycle_view);
        settingsStorage = new StorageUtil.SettingsStorage(requireContext());
        View filterButton = view.findViewById(R.id.filter_last_added_btn);
        ImageView backImageView = view.findViewById(R.id.close_filter_btn);
        progressDialog = new ProgressDialog(requireContext());
        songCountTv = view.findViewById(R.id.count_of_lastAdded);
        // initialize/load music array lists
        initialMusicList = new StorageUtil(getContext()).loadInitialList();
        lastAddedMusicList = new ArrayList<>();



        //sort initial music list by date in reverse order
        Collections.sort(initialMusicList, new Comparator<MusicDataCapsule>() {
            final DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            @Override
            public int compare(MusicDataCapsule t1, MusicDataCapsule t2) {
                try {
                    Date d1 = dateFormat.parse(t1.getsDateAdded());
                    Date d2 = dateFormat.parse(t2.getsDateAdded());
                    int i = 0;
                    if (d1 != null) {
                        i = d1.compareTo(d2);
                    }
                    if(i != 0) return -i;
                } catch (ParseException e) {
                    throw new IllegalArgumentException();
                }
                return 0;
            }
        });

        loadLastAddedList(settingsStorage.loadLastAddedDur());

        // back button click listener
        backImageView.setOnClickListener(v -> requireActivity().onBackPressed());

        // filter click listener
        filterButton.setOnClickListener(v -> openDialogFilter());

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

        RadioGroup radioGroup1 = filterDialog.findViewById(R.id.last_Added_radio_group);
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

        firstRadio.setText("Last month");
        secondRadio.setText("Last three months");
        thirdRadio.setText("Last six months");
        fourthRadio.setText("Last year");

        radioGroup1.setOnCheckedChangeListener((radioGroup, in) -> {
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

        progressDialog.setMessage("Calculating...");
        progressDialog.show();

        service.execute(() -> {

                lastAddedMusicList.clear();
            try {
                lastAddedMusicList.addAll(getLastAddedMusicList(maxValue));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            handler.post(() -> {

                progressDialog.dismiss();
                String num = lastAddedMusicList.size() + " Songs";
                songCountTv.setText(num);

                // set recyclerview and adapter
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                ArrayList<String> idList = new ArrayList<>();
                for (MusicDataCapsule music : lastAddedMusicList) {
                    idList.add(music.getsId());
                }
                adapter = new MusicMainAdapter(getContext(), lastAddedMusicList,idList);
                recyclerView.setAdapter(adapter);
                });
        });
        service.shutdown();
    }


    private ArrayList<MusicDataCapsule> getLastAddedMusicList (long max) throws ParseException {
        ArrayList<MusicDataCapsule> result = new ArrayList<>();

        // dtf for parsing string to date
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());

        //get "current" date
        Date previousDate = dateFormat.parse(dateFormat.format(new Date()));

        // subtract days from current date and turn it into an older date
        if (previousDate != null) {
            previousDate.setTime(previousDate.getTime() - (max * 86400) * 1000);
        }

        //loop through music list and if a music date is older than given date, then break;
        for (MusicDataCapsule music : initialMusicList) {
            Date musicDate = dateFormat.parse(music.getsDateAdded());
            if(musicDate.compareTo(previousDate) < 0) {
                break;
            }
                result.add(music);
        }

        //return music ranging between current date and given older date
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
