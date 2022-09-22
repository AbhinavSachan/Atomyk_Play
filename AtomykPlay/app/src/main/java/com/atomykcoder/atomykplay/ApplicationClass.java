package com.atomykcoder.atomykplay;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class ApplicationClass extends Application {

    public static final String CHANNEL_ID = "MUSIC_NOTIFICATION";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,"Music Notification", NotificationManager.IMPORTANCE_NONE);
            notificationChannel.setDescription("Description");
            notificationChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setBypassDnd(false);
            notificationChannel.setLockscreenVisibility(1);

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);

        }

    }
}
