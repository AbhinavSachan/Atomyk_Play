package com.atomykcoder.atomykplay.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.function.StorageUtil;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;


public class SettingsFragment extends Fragment {

    public static final String SWITCH1 = "SWITCH";
    private TextView textView;
    private SwitchMaterial switchMaterial;

    private boolean SwitchOnOff;
    private ProgressDialog pd;
    private ArrayList<String> arrBatchName;
    private StorageUtil storageUtil;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_settings, container, false);

        pd = new ProgressDialog(requireContext());
        arrBatchName = new ArrayList<>();

        textView = view.findViewById(R.id.text_change_mode);
        switchMaterial = view.findViewById(R.id.switch_theme);
        storageUtil = new StorageUtil(requireContext());

        SwitchOnOff = storageUtil.loadTheme();

        switchMaterial.setChecked(SwitchOnOff);

        switchMaterial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    textView.setText(getString(R.string.light_mode_tv));
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    textView.setText(getString(R.string.dark_mode_tv));
                }
                storageUtil.saveTheme(isChecked);
            }
        });


        return view;
    }

}