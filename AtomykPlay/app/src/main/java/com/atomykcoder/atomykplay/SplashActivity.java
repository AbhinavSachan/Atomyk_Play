package com.atomykcoder.atomykplay;

import static com.atomykcoder.atomykplay.helperFunctions.StorageUtil.dark;
import static com.atomykcoder.atomykplay.helperFunctions.StorageUtil.no_dark;
import static com.atomykcoder.atomykplay.helperFunctions.StorageUtil.system_follow;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.atomykcoder.atomykplay.activities.MainActivity;
import com.atomykcoder.atomykplay.helperFunctions.StorageUtil;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String switch1 = new StorageUtil.SettingsStorage(this).loadTheme();
        switch (switch1) {
            case system_follow:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case no_dark:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case dark:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(i);
                // close this activity
                finish();
            }
        }, 100);
    }
}