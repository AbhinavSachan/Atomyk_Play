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
    private final int secondOptionValue = 90;
    private final int thirdOptionValue = 180;
    private final int fourthOptionValue = 360;
    private RadioGroup radioGroup;
    private RadioButton firstRadio, secondRadio, thirdRadio, fourthRadio;
    private MusicMainAdapter adapter;
    private ArrayList<MusicDataCapsule> lastAddedMusicList;
    private Dialog filterDialog;
    private TextView textView;
    private StorageUtil.SettingsStorage settingsStorage;
    private ArrayList<MusicDataCapsule> initialMusicList;
    private RecyclerView recyclerView;
    private ProgressDialog progressDialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_last_added, container, false);

        //get references
        recyclerView = view.findViewById(R.id.last_added_recycle_view);
        settingsStorage = new StorageUtil.SettingsStorage(requireContext());
        View button = view.findViewById(R.id.filter_last_added_btn);
        ImageView imageView = view.findViewById(R.id.close_filter_btn);
        progressDialog = new ProgressDialog(requireContext());
        FragmentManager fragmentManager = ((MainActivity) requireContext()).getSupportFragmentManager();

        imageView.setOnClickListener(v -> {
            fragmentManager.popBackStackImmediate();
        });
        textView = view.findViewById(R.id.count_of_lastAdded);

        button.setOnClickListener(v -> openDialogFilter());

        initialMusicList = new StorageUtil(getContext()).loadInitialList();

        lastAddedMusicList = new ArrayList<>();
        //set Adapter
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MusicMainAdapter(getContext(), lastAddedMusicList);

        progressDialog.setMessage("Calculating...");
        progressDialog.setCancelable(true);
        progressDialog.show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            ExecutorService service = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            service.execute(() -> {
                int savedState = settingsStorage.loadLastAddedDur();
                loadLastAdded(initialMusicList, savedState);

                // post-execute code here
                handler.post(() -> {
                    recyclerView.setAdapter(adapter);
                    String num = lastAddedMusicList.size() + " Songs";
                    textView.setText(num);
                    progressDialog.dismiss();
                });
            });
            // stopping the background thread (crucial)
            service.shutdown();
        },200);

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

        // initialize/load music lists
        firstRadio.setText("Last one month");
        secondRadio.setText("Last three months");
        thirdRadio.setText("Last six months");
        fourthRadio.setText("Last year");

        //radio buttons check change listener
        radioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            progressDialog.setMessage("Calculating...");
            progressDialog.setCancelable(true);
            progressDialog.show();

            ExecutorService service = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            service.execute(() -> {
                int in = getRadioID();
                cleanUp();
                loadLastAdded(initialMusicList, in);
                settingsStorage.setLastAddedDur(in);
                // post-execute code here
                handler.post(() -> {
                    filterDialog.dismiss();
                    String num = lastAddedMusicList.size() + " Songs";
                    textView.setText(num);
                    notifyData();
                    progressDialog.dismiss();
                });
            });
            // stopping the background thread (crucial)
            service.shutdown();

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
     *
     * @param dataList dataList with all songs available on device
     */
    private void loadLastAdded(ArrayList<MusicDataCapsule> dataList, int i) {
        switch (i) {
            case 1:
                for (MusicDataCapsule music : dataList) {
                    boolean isWithinDays = isWithinDaysSelected(music.getsDateAdded(), firstOptionValue);
                    if (isWithinDays) {
                        lastAddedMusicList.add(music);
                    }
                }
                break;
            case 2:
                for (MusicDataCapsule music : dataList) {
                    boolean isWithinDays = isWithinDaysSelected(music.getsDateAdded(), secondOptionValue);
                    if (isWithinDays) {
                        lastAddedMusicList.add(music);
                    }
                }
                break;
            case 3:
                for (MusicDataCapsule music : dataList) {
                    boolean isWithinDays = isWithinDaysSelected(music.getsDateAdded(), thirdOptionValue);
                    if (isWithinDays) {
                        lastAddedMusicList.add(music);
                    }
                }
                break;
            case 4:
                for (MusicDataCapsule music : dataList) {
                    boolean isWithinDays = isWithinDaysSelected(music.getsDateAdded(), fourthOptionValue);
                    if (isWithinDays) {
                        lastAddedMusicList.add(music);
                    }
                }
                break;
        }
    }

    /**
     * takes the difference between current date and date_added and checks if difference is
     * less than given filter amount
     *
     * @param dateAdded  music date_added
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
            if (days <= daysFilter) {
                return true;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
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

    private void cleanUp() {
        lastAddedMusicList.clear();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void notifyData() {
        adapter.notifyDataSetChanged();
    }
}
