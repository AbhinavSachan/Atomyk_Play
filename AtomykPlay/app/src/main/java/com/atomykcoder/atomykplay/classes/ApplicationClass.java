package com.atomykcoder.atomykplay.classes;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.widget.Toast;

public class ApplicationClass extends Application {

    public static final String CHANNEL_ID = "MUSIC_NOTIFICATION";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Music Notification", NotificationManager.IMPORTANCE_NONE);
            notificationChannel.setDescription("Music playback notification are controlled from here");
            notificationChannel.setImportance(NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setBypassDnd(false);

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);

        }

    }

    public void showToast(String s){
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
    }
}
