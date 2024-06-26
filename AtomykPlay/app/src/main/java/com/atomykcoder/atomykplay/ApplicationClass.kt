package com.atomykcoder.atomykplay

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class ApplicationClass : Application() {
    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "Music Notification",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.description = "Music playback notification are controlled from here"
            notificationChannel.importance = NotificationManager.IMPORTANCE_LOW
            notificationChannel.setBypassDnd(false)
            notificationChannel.enableLights(false)
            notificationChannel.enableVibration(false)
            notificationChannel.setShowBadge(false)
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(notificationChannel)
        }
    }

    companion object {
        const val CHANNEL_ID = "MUSIC_NOTIFICATION"
        lateinit var instance: ApplicationClass
            private set
    }
}