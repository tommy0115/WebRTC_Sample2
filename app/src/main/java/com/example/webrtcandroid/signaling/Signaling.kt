package com.example.webrtcandroid.signaling

import org.webrtc.IceCandidate

interface Signaling {
    fun sendOffer(desc: String)
    fun sendAnswer(desc: String)
    fun sendIceCandidate(type: SignalingType, p0: IceCandidate?)
    fun waitIceCandidate(type: SignalingType, signalingResponse: SignalingResponse)
    fun waitOffer(response: SignalingResponse)
    fun waitAnswer(response: SignalingResponse)
    fun onError(error: String)
    fun close()
}