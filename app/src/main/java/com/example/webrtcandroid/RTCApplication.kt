package com.example.webrtcandroid

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class RTCApplication : Application(){

    companion object {
        const val RTC_NOTIFICATION_CHANNEL_ID = "RTC_NOTIFICATION_ID"
        const val RTC_NOTIFICATION_CHANNEL_NAME = "RTC_NOTIFICATION"
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannel = NotificationChannel(
                RTC_NOTIFICATION_CHANNEL_ID,
                RTC_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.enableLights(true)
            notificationChannel.vibrationPattern = longArrayOf(0)
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}