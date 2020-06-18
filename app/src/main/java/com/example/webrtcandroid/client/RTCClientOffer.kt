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
import java.lang.Exception
import java.nio.ByteBuffer
import java.nio.charset.Charset

class RTCClientOffer(context: Context, peerConnectionObserver: PeerConnectionObserver) :
    RTCClient(context, peerConnectionObserver) {

    fun offer() {
        val constraints = MediaConstraints().apply {
            //mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }

        peerConnection?.createOffer(object : AppSdpObserver() {
            override fun onCreateSuccess(desc: SessionDescription?) {

                peerConnection?.setLocalDescription(object : SdpObserver {
                    override fun onSetFailure(p0: String?) {
                        Log.d("Offer", "onSetFailure")
                    }

                    override fun onSetSuccess() {
                        Log.d("Offer", "onSetSuccess")
                    }

                    override fun onCreateSuccess(p0: SessionDescription?) {
                        Log.d("Offer", "onCreateSuccess")
                    }

                    override fun onCreateFailure(p0: String?) {
                        Log.d("Offer", "onCreateFailure")
                    }

                }, desc)

                // send signaling
                signaling.standBy(
                    SignalingType.OFFER,
                    Gson().toJson(desc),
                    object : SignalingResponse {
                        override fun receive(type: SignalingType, desc: String) {
                            try {
                                if (type == SignalingType.ANSWER) {
                                    peerConnection?.setRemoteDescription(object : SdpObserver {
                                        override fun onSetFailure(p0: String?) {
                                            Log.d("Offer", "onSetFailure")
                                        }

                                        override fun onSetSuccess() {
                                            Log.d("Offer", "onSetSuccess")
                                        }

                                        override fun onCreateSuccess(p0: SessionDescription?) {
                                            Log.d("Offer", "onCreateSuccess")
                                        }

                                        override fun onCreateFailure(p0: String?) {
                                            Log.d("Offer", "onCreateFailure")
                                        }

                                    }, Gson().fromJson(desc, SessionDescription::class.java))
                                } else {
                                    throw Exception("Not Matched Signal Type ${type.typeName}")
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    })
            }

        }, constraints)

    }
}