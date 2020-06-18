package com.example.webrtcandroid

import org.webrtc.CalledByNative
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

open class AbstractSdpObserver : SdpObserver{
    override fun onCreateSuccess(var1: SessionDescription?){

    }

    override fun onSetSuccess(){

    }

    override fun onCreateFailure(var1: String?){

    }

    override fun onSetFailure(var1: String?){

    }
}