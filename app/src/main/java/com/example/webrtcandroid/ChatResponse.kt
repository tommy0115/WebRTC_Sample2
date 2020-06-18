package com.example.webrtcandroid

import org.webrtc.DataChannel

interface ChatResponse {

    enum class ChatState{
        CONNECTING, OPEN, CLOSING,CLOSED
    }

    fun onMessage(message:String, state : ChatState)
}