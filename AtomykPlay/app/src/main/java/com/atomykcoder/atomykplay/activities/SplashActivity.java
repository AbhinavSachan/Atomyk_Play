package com.atomykcoder.atomykplay.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.atomykcoder.atomykplay.R;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StorageUtil.SettingsStorage settingsStorage = new StorageUtil.SettingsStorage(this);
        boolean switch1 = settingsStorage.loadIsThemeDark();
        if (!switch1) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
            setContentView(R.layout.activity_splash);
        }
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}