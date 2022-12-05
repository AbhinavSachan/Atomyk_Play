package com.atomykcoder.atomykplay.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.adapters.BlockFolderListAdapter;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;

import java.util.ArrayList;


public class SettingsFragment extends Fragment {

    private RadioGroup radioGroup;
    private RadioButton light_theme_btn, dark_theme_btn;
    private SwitchCompat songInfoSwi, artistSwi, extraSwi, autoPlaySwi, keepShuffleSwi, lowerVolSwi, selfStopSwi, keepScreenOnSwi, oneClickSkipSwi;
    private View songInfoLl, artistLl, extraLl, autoPlayLl, keepShuffleLl, lowerVolLl, blackListLl, filterDurLl, selfStopLl, keepScreenOnLl, oneClickSkipLl;


    private boolean dark;
    private boolean showInfo, showArtist, showOption, showExtra, autoPlay, keepShuffle, lowerVol, selfStop, keepScreenOn, oneClickSkip;
    private StorageUtil.SettingsStorage settingsStorage;
    private MainActivity mainActivity;
    private Dialog blacklistDialog, filterDurDialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        settingsStorage = new StorageUtil.SettingsStorage(requireContext());
        mainActivity = (MainActivity) requireContext();
        Toolbar toolbar = view.findViewById(R.id.toolbar_settings);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });
        //saved values
        dark = settingsStorage.loadTheme();
        showInfo = settingsStorage.loadShowInfo();
        showArtist = settingsStorage.loadShowArtist();
        showExtra = settingsStorage.loadExtraCon();
        showOption = settingsStorage.loadOptionMenu();
        autoPlay = settingsStorage.loadAutoPlay();
        keepShuffle = settingsStorage.loadKeepShuffle();
        lowerVol = settingsStorage.loadLowerVol();
        selfStop = settingsStorage.loadSelfStop();
        keepScreenOn = settingsStorage.loadKeepScreenOn();
        oneClickSkip = settingsStorage.loadOneClickSkip();


        //Player settings
        songInfoSwi = view.findViewById(R.id.show_file_info_swi);
        songInfoLl = view.findViewById(R.id.show_file_info_ll);
        artistSwi = view.findViewById(R.id.show_artist_swi);
        artistLl = view.findViewById(R.id.show_artist_ll);
        extraSwi = view.findViewById(R.id.show_extra_swi);
        extraLl = view.findViewById(R.id.show_extra_ll);
        keepScreenOnSwi = view.findViewById(R.id.keep_screen_swi);
        keepScreenOnLl = view.findViewById(R.id.keep_screen_ll);

        //audio settings
        autoPlaySwi = view.findViewById(R.id.autoPlay_swi);
        autoPlayLl = view.findViewById(R.id.autoPlay_ll);
        keepShuffleSwi = view.findViewById(R.id.keep_shuffle_swi);
        keepShuffleLl = view.findViewById(R.id.keep_shuffle_ll);
        lowerVolSwi = view.findViewById(R.id.lower_vol_swi);
        lowerVolLl = view.findViewById(R.id.lower_vol_ll);
        selfStopSwi = view.findViewById(R.id.self_stop_swi);
        selfStopLl = view.findViewById(R.id.self_stop_ll);
        oneClickSkipSwi = view.findViewById(R.id.one_click_skip_swi);
        oneClickSkipLl = view.findViewById(R.id.one_click_skip_ll);

        //filter settings
        blackListLl = view.findViewById(R.id.blacklist_ll);
        filterDurLl = view.findViewById(R.id.filter_dur_ll);

        //theme settings
        radioGroup = view.findViewById(R.id.radio_group_theme);
        light_theme_btn = view.findViewById(R.id.light_button);
        dark_theme_btn = view.findViewById(R.id.dark_button);

        setButtonState();

        //listeners
        songInfoLl.setOnClickListener(v -> songInfoSwi.setChecked(!songInfoSwi.isChecked()));
        artistLl.setOnClickListener(v -> artistSwi.setChecked(!artistSwi.isChecked()));
        extraLl.setOnClickListener(v -> extraSwi.setChecked(!extraSwi.isChecked()));
        autoPlayLl.setOnClickListener(v -> autoPlaySwi.setChecked(!autoPlaySwi.isChecked()));
        keepShuffleLl.setOnClickListener(v -> keepShuffleSwi.setChecked(!keepShuffleSwi.isChecked()));
        lowerVolLl.setOnClickListener(v -> lowerVolSwi.setChecked(!lowerVolSwi.isChecked()));
        selfStopLl.setOnClickListener(v -> selfStopSwi.setChecked(!selfStopSwi.isChecked()));
        keepScreenOnLl.setOnClickListener(v -> keepScreenOnSwi.setChecked(!keepScreenOnSwi.isChecked()));
        oneClickSkipLl.setOnClickListener(v -> oneClickSkipSwi.setChecked(!oneClickSkipSwi.isChecked()));
        blackListLl.setOnClickListener(v -> openBlackListDialogue());
        filterDurLl.setOnClickListener(v -> openFilterDurationDialog());

        songInfoSwi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            songInfoSwi.setChecked(isChecked);
            settingsStorage.showInfo(isChecked);
            hideInfo(isChecked);
        });
        artistSwi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            artistSwi.setChecked(isChecked);
            settingsStorage.showArtist(isChecked);
            hideArtist(isChecked);
        });
        extraSwi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            extraSwi.setChecked(isChecked);
            settingsStorage.showExtraCon(isChecked);
            hideExtra(isChecked);
        });
        autoPlaySwi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            autoPlaySwi.setChecked(isChecked);
            settingsStorage.autoPlay(isChecked);
        });
        keepShuffleSwi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            keepShuffleSwi.setChecked(isChecked);
            settingsStorage.keepShuffle(isChecked);
        });
        lowerVolSwi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            lowerVolSwi.setChecked(isChecked);
            settingsStorage.lowerVol(isChecked);
        });
        selfStopSwi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            selfStopSwi.setChecked(isChecked);
            settingsStorage.setSelfStop(isChecked);
        });
        keepScreenOnSwi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            keepScreenOnSwi.setChecked(isChecked);
            settingsStorage.keepScreenOn(isChecked);
        });
        oneClickSkipSwi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            oneClickSkipSwi.setChecked(isChecked);
            settingsStorage.oneClickSkip(isChecked);
        });

        //Check if any radio button is pressed
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            setDark(checkedId);
        });

        return view;
    }

    private void openFilterDurationDialog() {
        filterDurDialog = new Dialog(requireContext());
        filterDurDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        filterDurDialog.setContentView(R.layout.filter_duration_dialog);

        TextView filter_time_tv =  filterDurDialog.findViewById(R.id.filter_time_textview);
        SeekBar filter_dur_seekbar = filterDurDialog.findViewById(R.id.filter_dur_seekBar);
        Button filter_dur_ok_bt = filterDurDialog.findViewById(R.id.filter_dur_ok_bt);

        //set max in seconds
        filter_dur_seekbar.setMax(120);

        // settings previous data
        int previousDur = settingsStorage.loadFilterDur();
        filter_dur_seekbar.setProgress(previousDur);
        filter_time_tv.setText(String.valueOf(previousDur));

        // seekbar change listener
        filter_dur_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                filter_time_tv.setText(String.valueOf(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        filter_dur_ok_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingsStorage.saveFilterDur(Integer.parseInt(filter_time_tv.getText().toString()));
                mainActivity.checkForUpdateMusic();
                filterDurDialog.dismiss();
            }
        });
        filterDurDialog.show();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (blacklistDialog != null) {
            if (blacklistDialog.isShowing()) {
                blacklistDialog.dismiss();
            }
        }
        if (filterDurDialog != null) {
            if (filterDurDialog.isShowing()) {
                filterDurDialog.dismiss();
            }
        }
    }

    public ArrayList<String> blacklist;
    public BlockFolderListAdapter adapter;

    private void openBlackListDialogue() {
        //create blacklistDialog
        blacklistDialog = new Dialog(getContext());
        blacklistDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // collect references to ui components
        blacklistDialog.setContentView(R.layout.black_list_dialog);
        ImageView directory_icon = blacklistDialog.findViewById(R.id.blacklist_open_directory_icon);
        Button okay_bt = blacklistDialog.findViewById(R.id.blacklist_confirm_bt);
        RecyclerView recyclerView = blacklistDialog.findViewById(R.id.blacklist_recycler);

        blacklist = settingsStorage.loadBlackList();

        adapter = new BlockFolderListAdapter(blacklist, requireContext());

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        // adapter to load in spinner dropdown

        // start "SELECT FOLDER" activity when we click on directory icon
        directory_icon.setOnClickListener(view -> {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            i.addCategory(Intent.CATEGORY_DEFAULT);
            requireActivity().startActivityForResult(i, 2020);
        });

        // set listener on okay button
        okay_bt.setOnClickListener(v -> {
            blacklistDialog.dismiss();
        });
        //finally show the blacklistDialog
        blacklistDialog.show();
    }

    private void showToast(String s) {
        Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show();
    }

    private void hideExtra(boolean v) {
        if (v) {
            mainActivity.bottomSheetPlayerFragment.mini_next.setVisibility(View.VISIBLE);
        } else {
            mainActivity.bottomSheetPlayerFragment.mini_next.setVisibility(View.GONE);
        }
    }


    private void hideArtist(boolean v) {
        if (v) {
            mainActivity.bottomSheetPlayerFragment.mini_artist_text.setVisibility(View.VISIBLE);
        } else {
            mainActivity.bottomSheetPlayerFragment.mini_artist_text.setVisibility(View.GONE);
        }
    }

    private void hideInfo(boolean v) {
        if (v) {
            mainActivity.bottomSheetPlayerFragment.info_layout.setVisibility(View.VISIBLE);
        } else {
            mainActivity.bottomSheetPlayerFragment.info_layout.setVisibility(View.GONE);
        }
    }

    /**
     * set switches when the activity start
     */
    private void setButtonState() {

        songInfoSwi.setChecked(showInfo);
        artistSwi.setChecked(showArtist);
        extraSwi.setChecked(showExtra);
        autoPlaySwi.setChecked(autoPlay);
        keepShuffleSwi.setChecked(keepShuffle);
        lowerVolSwi.setChecked(lowerVol);
        selfStopSwi.setChecked(selfStop);
        keepScreenOnSwi.setChecked(keepScreenOn);
        oneClickSkipSwi.setChecked(oneClickSkip);

        if (!dark) {
            light_theme_btn.setChecked(true);
        } else {
            dark_theme_btn.setChecked(true);
        }
    }


    @SuppressLint("NonConstantResourceId")
    private void setDark(int checkedId) {
        switch (checkedId) {
            case R.id.light_button:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                dark = false;
                break;
            case R.id.dark_button:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                dark = true;
                break;

        }
        settingsStorage.saveTheme(dark);
    }

}