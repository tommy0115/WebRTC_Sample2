package com.example.webrtcandroid.signaling

interface Signaling {
    fun sendOffer(desc: String, signalingResponse: SignalingResponse)
    fun onAnswer(desc: String)
    fun standByOffer(desc: String, signalingResponse: SignalingResponse)
    fun close()
}