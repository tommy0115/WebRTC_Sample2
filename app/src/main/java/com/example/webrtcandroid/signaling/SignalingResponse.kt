package com.example.webrtcandroid.signaling

interface SignalingResponse {
    fun onReceive(type : SignalingType, desc : String)
    fun onError(type : SignalingType, desc : String)
}