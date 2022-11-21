package com.atomykcoder.atomykplay.fragments;

import static com.atomykcoder.atomykplay.helperFunctions.StorageUtil.dark;
import static com.atomykcoder.atomykplay.helperFunctions.StorageUtil.no_dark;
import static com.atomykcoder.atomykplay.helperFunctions.StorageUtil.system_follow;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;


public class SettingsFragment extends Fragment {

    private RadioGroup radioGroup;
    private RadioButton light_theme_btn, dark_theme_btn, default_theme_btn;
    private SwitchCompat songInfoSwi, artistSwi, optionSwi, extraSwi, autoPlaySwi, keepShuffleSwi, lowerVolSwi;
    private View songInfoLl, artistLl, optionLl, extraLl, autoPlayLl, keepShuffleLl, lowerVolLl, blackListLl, filterDurLl;


    private String theme;
    private boolean showInfo, showArtist, showOption, showExtra, autoPlay, keepShuffle, lowerVol;
    private StorageUtil.SettingsStorage settingsStorage;
    private MainActivity mainActivity;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        settingsStorage = new StorageUtil.SettingsStorage(requireContext());
        mainActivity = (MainActivity) requireContext();

        //saved values
        theme = settingsStorage.loadTheme();
        showInfo = settingsStorage.loadShowInfo();
        showArtist = settingsStorage.loadShowArtist();
        showExtra = settingsStorage.loadExtraCon();
        showOption = settingsStorage.loadOptionMenu();
        autoPlay = settingsStorage.loadAutoPlay();
        keepShuffle = settingsStorage.loadKeepShuffle();
        lowerVol = settingsStorage.loadLowerVol();


        //Player settings
        songInfoSwi = view.findViewById(R.id.show_file_info_swi);
        songInfoLl = view.findViewById(R.id.show_file_info_ll);
        artistSwi = view.findViewById(R.id.show_artist_swi);
        artistLl = view.findViewById(R.id.show_artist_ll);
        optionSwi = view.findViewById(R.id.show_option_swi);
        optionLl = view.findViewById(R.id.show_option_ll);
        extraSwi = view.findViewById(R.id.show_extra_swi);
        extraLl = view.findViewById(R.id.show_extra_ll);

        //audio settings
        autoPlaySwi = view.findViewById(R.id.autoPlay_swi);
        autoPlayLl = view.findViewById(R.id.autoPlay_ll);
        keepShuffleSwi = view.findViewById(R.id.keep_shuffle_swi);
        keepShuffleLl = view.findViewById(R.id.keep_shuffle_ll);
        lowerVolSwi = view.findViewById(R.id.lower_vol_swi);
        lowerVolLl = view.findViewById(R.id.lower_vol_ll);

        //filter settings
        blackListLl = view.findViewById(R.id.blacklist_ll);
        filterDurLl = view.findViewById(R.id.filter_dur_ll);

        //theme settings
        radioGroup = view.findViewById(R.id.radio_group_theme);
        light_theme_btn = view.findViewById(R.id.light_button);
        dark_theme_btn = view.findViewById(R.id.dark_button);
        default_theme_btn = view.findViewById(R.id.default_button);

        setButtonState();

        //listeners
        songInfoLl.setOnClickListener(v -> songInfoSwi.setChecked(!songInfoSwi.isChecked()));
        artistLl.setOnClickListener(v -> artistSwi.setChecked(!artistSwi.isChecked()));
        extraLl.setOnClickListener(v -> extraSwi.setChecked(!extraSwi.isChecked()));
        optionLl.setOnClickListener(v -> optionSwi.setChecked(!optionSwi.isChecked()));
        autoPlayLl.setOnClickListener(v -> autoPlaySwi.setChecked(!autoPlaySwi.isChecked()));
        keepShuffleLl.setOnClickListener(v -> keepShuffleSwi.setChecked(!keepShuffleSwi.isChecked()));
        lowerVolLl.setOnClickListener(v -> lowerVolSwi.setChecked(!lowerVolSwi.isChecked()));
        blackListLl.setOnClickListener(v -> showToast("coming soon!"));
        filterDurLl.setOnClickListener(v -> showToast("coming soon!"));

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
        optionSwi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            optionSwi.setChecked(isChecked);
            settingsStorage.showOptionMenu(isChecked);
            hideOptions(isChecked);
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

        //Check if any radio button is pressed
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            setTheme(checkedId);
        });

        return view;
    }

    private void showToast(String s) {
        Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show();
    }

    private void hideOptions(boolean v) {
        if (v) {
            mainActivity.bottomSheetPlayerFragment.optionImg.setVisibility(View.VISIBLE);
        } else {
            mainActivity.bottomSheetPlayerFragment.optionImg.setVisibility(View.GONE);
        }
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

    private void setButtonState() {

        songInfoSwi.setChecked(showInfo);
        artistSwi.setChecked(showArtist);
        extraSwi.setChecked(showExtra);
        optionSwi.setChecked(showOption);
        autoPlaySwi.setChecked(autoPlay);
        keepShuffleSwi.setChecked(keepShuffle);
        lowerVolSwi.setChecked(lowerVol);

        switch (theme) {
            case system_follow:
                default_theme_btn.setChecked(true);
                break;
            case no_dark:
                light_theme_btn.setChecked(true);
                break;
            case dark:
                dark_theme_btn.setChecked(true);
                break;
        }
    }


    @SuppressLint("NonConstantResourceId")
    private void setTheme(int checkedId) {
        switch (checkedId) {
            case R.id.default_button:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                theme = system_follow;
                break;
            case R.id.light_button:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                theme = no_dark;
                break;
            case R.id.dark_button:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                theme = dark;
                break;

        }
        settingsStorage.saveTheme(theme);
    }

}