package com.example.webrtcandroid.client

import android.util.Log
import org.webrtc.*

open class PeerConnectionObserver : PeerConnection.Observer{
    override fun onIceCandidate(p0: IceCandidate?) {
        Log.d("PeerConnectionObserver", "onIceCandidate : $p0")
    }

    override fun onDataChannel(p0: DataChannel?) {
        Log.d("PeerConnectionObserver", "onDataChannel : $p0")
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
        Log.d("PeerConnectionObserver", "onIceConnectionReceivingChange : $p0")
    }

    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
        Log.d("PeerConnectionObserver", "onIceConnectionChange : $p0")
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
        Log.d("PeerConnectionObserver", "onIceGatheringChange : $p0")
    }

    override fun onAddStream(p0: MediaStream?) {
        Log.d("PeerConnectionObserver", "onAddStream : $p0")
    }

    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
        Log.d("PeerConnectionObserver", "onSignalingChange : $p0")
    }

    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
        Log.d("PeerConnectionObserver", "onIceCandidatesRemoved : $p0")
    }

    override fun onRemoveStream(p0: MediaStream?) {
        Log.d("PeerConnectionObserver", "onRemoveStream : $p0")
    }

    override fun onRenegotiationNeeded() {
        Log.d("PeerConnectionObserver", "onRenegotiationNeeded")
    }

    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
        Log.d("PeerConnectionObserver", "onAddTrack")
    }

}