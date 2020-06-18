package com.example.webrtcandroid

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.example.webrtcandroid.RTCApplication.Companion.RTC_NOTIFICATION_CHANNEL_ID

class CaptureService : Service() {

    companion object {
        const val FOREGROUND_ID = 100
    }


    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(FOREGROUND_ID, createNotification())
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotification(): Notification? {
        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, RTC_NOTIFICATION_CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }.apply {
            setContentTitle("Digital Clock Widget Updating...")
            setSmallIcon(R.mipmap.ic_launcher)
            setContentText("")
            setSubText("")
        }

        val notification = builder.build()
        notification.flags =
            Notification.FLAG_NO_CLEAR

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(RTC_NOTIFICATION_CHANNEL_ID)
        }
        return notification
    }
}