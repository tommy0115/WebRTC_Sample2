package com.example.webrtcandroid.signaling

import org.webrtc.IceCandidate

data class IceData(
    var sdp: String = "",
    var sdpMLineIndex: String = "",
    var sdpMid: String = "",
    var serverUrl: String = ""
) {
    override fun toString(): String {
        return "IceData(sdp='$sdp', sdpMLineIndex='$sdpMLineIndex', sdpMid='$sdpMid', serverUrl='$serverUrl')"
    }

    fun toIceCandidate() : IceCandidate{
        return IceCandidate(sdpMid, sdpMLineIndex.toInt(), sdp)
    }
}