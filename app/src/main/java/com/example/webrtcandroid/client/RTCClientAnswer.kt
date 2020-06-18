package com.example.webrtcandroid.client

import android.content.Context
import android.util.Log
import com.example.webrtcandroid.AppSdpObserver
import com.example.webrtcandroid.ChatResponse
import com.example.webrtcandroid.PeerConnectionObserver
import com.example.webrtcandroid.signaling.Signaling
import com.example.webrtcandroid.signaling.SignalingResponse
import com.example.webrtcandroid.signaling.SignalingType
import com.google.gson.Gson
import org.webrtc.*
import java.nio.ByteBuffer
import java.nio.charset.Charset

class RTCClientAnswer(context: Context, peerConnectionObserver: PeerConnectionObserver) :
    RTCClient(context, peerConnectionObserver) {

    fun answer() {
        val constraints = MediaConstraints().apply {
            //mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }


        peerConnection?.createAnswer(object : AppSdpObserver() {
            override fun onCreateSuccess(desc: SessionDescription?) {

                peerConnection?.setLocalDescription(object : SdpObserver {
                    override fun onSetFailure(p0: String?) {
                        Log.d("Answer", "onSetFailure")
                    }

                    override fun onSetSuccess() {
                        Log.d("Answer", "onSetSuccess")
                    }

                    override fun onCreateSuccess(p0: SessionDescription?) {
                        Log.d("Answer", "onCreateSuccess")
                    }

                    override fun onCreateFailure(p0: String?) {
                        Log.d("Answer", "onCreateFailure")
                    }

                }, desc)

                signaling.standBy(
                    SignalingType.ANSWER,
                    Gson().toJson(desc),
                    object : SignalingResponse {
                        override fun receive(type: SignalingType, desc: String) {

                            peerConnection?.setRemoteDescription(object : SdpObserver {
                                override fun onSetFailure(p0: String?) {
                                    Log.d("Answer", "onSetFailure")
                                }

                                override fun onSetSuccess() {
                                    Log.d("Answer", "onSetSuccess")
                                }

                                override fun onCreateSuccess(p0: SessionDescription?) {
                                    Log.d("Answer", "onCreateSuccess")
                                }

                                override fun onCreateFailure(p0: String?) {
                                    Log.d("Answer", "onCreateFailure")
                                }

                            }, Gson().fromJson(desc, SessionDescription::class.java))
                        }
                    })
            }

        }, constraints)
    }
}