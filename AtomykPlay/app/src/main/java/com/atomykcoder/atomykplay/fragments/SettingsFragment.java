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

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;


public class SettingsFragment extends Fragment {

    private RadioGroup radioGroup;
    private RadioButton light_theme, dark_theme, default_theme;


    private String theme;
    private StorageUtil storageUtil;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        radioGroup = view.findViewById(R.id.radio_group_theme);
        light_theme = view.findViewById(R.id.light_button);
        dark_theme = view.findViewById(R.id.dark_button);
        default_theme = view.findViewById(R.id.default_button);
        storageUtil = new StorageUtil(requireContext());

        theme = storageUtil.loadTheme();

        switch (theme) {
            case system_follow:
                default_theme.setChecked(true);
                break;
            case no_dark:
                light_theme.setChecked(true);
                break;
            case dark:
                dark_theme.setChecked(true);
                break;
        }

        //Check if any radio button is pressed
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
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
                storageUtil.saveTheme(theme);
            }
        });

        return view;
    }
}