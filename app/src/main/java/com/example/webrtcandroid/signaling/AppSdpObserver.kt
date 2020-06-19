package com.example.webrtcandroid.signaling

import android.util.Log
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

open class AppSdpObserver : SdpObserver {
    override fun onSetFailure(error: String?) {
        Log.e("AppSdpObserver", "onSetFailure : $error")
    }

    override fun onSetSuccess() {
        Log.d("AppSdpObserver", "onSetSuccess")
    }

    override fun onCreateSuccess(desc: SessionDescription?) {
        Log.d("AppSdpObserver", "onCreateSuccess ${desc?.type} / ${desc?.description}")
    }

    override fun onCreateFailure(error: String?) {
        Log.e("AppSdpObserver", "onCreateFailure : $error")
    }
}