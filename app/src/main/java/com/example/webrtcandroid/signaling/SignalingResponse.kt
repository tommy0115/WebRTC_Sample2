package com.example.webrtcandroid.signaling

interface SignalingResponse {
    fun receive(type : SignalingType, desc : String)
}