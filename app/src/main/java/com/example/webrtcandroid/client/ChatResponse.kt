package com.example.webrtcandroid.client

interface ChatResponse {

    enum class ChatState{
        CONNECTING, OPEN, CLOSING,CLOSED
    }

    fun onMessage(message:String, state : ChatState)
}