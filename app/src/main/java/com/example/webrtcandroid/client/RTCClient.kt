package com.example.webrtcandroid.client

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.webrtcandroid.signaling.AppSdpObserver
import com.example.webrtcandroid.signaling.FirebaseRealTimeDbSignaling
import com.example.webrtcandroid.signaling.Signaling
import com.example.webrtcandroid.signaling.SignalingResponse
import com.example.webrtcandroid.signaling.SignalingType
import com.google.gson.Gson
import org.webrtc.*
import java.nio.ByteBuffer
import java.nio.charset.Charset

class RTCClient(context: Context) {

    init {
        initializePeerConnectionFactory(context)
    }

    private val eglBase: EglBase by lazy { EglBase.create() }

    private val iceServer = listOf(
        PeerConnection.IceServer.builder(mutableListOf<String>().apply {
            add("stun:stun.l.google.com:19302")
            add("stun:stun1.l.google.com:19302")
            add("stun:stun2.l.google.com:19302")
            add("stun:stun3.l.google.com:19302")
            add("stun:stun4.l.google.com:19302")
        }).createIceServer()
    )

    private var signalingType : SignalingType = SignalingType.NONE

    private val peerConnectionFactory by lazy { buildPeerConnectionFactory() }
    private val peerConnection by lazy {
        buildPeerConnection(object : PeerConnectionObserver() {
                override fun onIceCandidate(p0: IceCandidate?) {
                    super.onIceCandidate(p0)
                    sendIceCandidate(p0)

                    val signalingResponse = object : SignalingResponse{
                        override fun onReceive(type: SignalingType, desc: String) {
                            addIceCandidate(Gson().fromJson(desc, IceCandidate::class.java))
                        }

                        override fun onError(type: SignalingType, desc: String) {
                            Log.e("iceCandidate","$desc")
                        }

                    }

                    if (signalingType == SignalingType.ANSWER){
                        signaling.waitIceCandidate(SignalingType.OFFER, signalingResponse)
                    } else {
                        signaling.waitIceCandidate(SignalingType.ANSWER, signalingResponse)
                    }

                }

                override fun onAddStream(p0: MediaStream?) {
                    super.onAddStream(p0)
                }

                override fun onDataChannel(p0: DataChannel?) {
                    super.onDataChannel(p0)
                    dataChannel = p0
                }

                override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
                    super.onIceConnectionChange(p0)
                    if (p0?.name?.toLowerCase() === "connected") {
                        // Peers connected!
                        Log.d("onIceConnectionChange", "connected")
                    }
                    //rtcClient.sendMessage("Connected")
                }
        })
    }
    private var dataChannel: DataChannel? = null
    private val signaling: Signaling by lazy { FirebaseRealTimeDbSignaling() }

    private fun initializePeerConnectionFactory(context: Context) {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    private fun buildPeerConnection(observer: PeerConnection.Observer) =
        peerConnectionFactory.createPeerConnection(
            iceServer,
            observer
        )

    private fun buildPeerConnectionFactory(): PeerConnectionFactory {
        return PeerConnectionFactory.builder().setOptions(PeerConnectionFactory.Options())
            /*.setVideoEncoderFactory(
                DefaultVideoEncoderFactory(
                    eglBase.eglBaseContext, true, true
                )
            )
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))*/
            .createPeerConnectionFactory()
    }

    fun addIceCandidate(iceCandidate: IceCandidate?) {
        peerConnection?.addIceCandidate(iceCandidate)
    }

    fun createDataChannel(label: String, chatResponse: ChatResponse) {
        dataChannel = peerConnection?.createDataChannel(label, DataChannel.Init())
        dataChannel?.registerObserver(object : DataChannel.Observer {
            override fun onMessage(p0: DataChannel.Buffer?) {
                if (p0 == null) {
                    return
                }

                dataChannel?.let {

                    val state = when (it.state()) {
                        DataChannel.State.CONNECTING -> {
                            ChatResponse.ChatState.CONNECTING
                        }
                        DataChannel.State.OPEN -> {
                            ChatResponse.ChatState.OPEN
                        }
                        DataChannel.State.CLOSING -> {
                            ChatResponse.ChatState.CLOSING
                        }
                        DataChannel.State.CLOSED -> {
                            ChatResponse.ChatState.CLOSED
                        }
                    }

                    chatResponse.onMessage(
                        byteBufferToString(p0.data, Charset.forName("UTF-8")) ?: "",
                        state
                    )
                }
            }

            override fun onBufferedAmountChange(p0: Long) {

            }

            override fun onStateChange() {
                dataChannel?.let {
                    val state = when (it.state()) {
                        DataChannel.State.CONNECTING -> {
                            ChatResponse.ChatState.CONNECTING
                        }
                        DataChannel.State.OPEN -> {
                            ChatResponse.ChatState.OPEN
                        }
                        DataChannel.State.CLOSING -> {
                            ChatResponse.ChatState.CLOSING
                        }
                        DataChannel.State.CLOSED -> {
                            ChatResponse.ChatState.CLOSED
                        }
                    }

                    Log.i("createDataChannel", state.name)
                }
            }
        })
    }

    fun sendMessage(message: String) {
        dataChannel?.send(DataChannel.Buffer(ByteBuffer.wrap(message.toByteArray()), true))
    }

    private fun byteBufferToString(
        buffer: ByteBuffer,
        charset: Charset
    ): String? {
        val bytes: ByteArray
        if (buffer.hasArray()) {
            bytes = buffer.array()
        } else {
            bytes = ByteArray(buffer.remaining())
            buffer[bytes]
        }
        return String(bytes, charset)
    }

    fun sendIceCandidate(p0: IceCandidate?) {
        signaling.sendIceCandidate(signalingType, p0)
    }

    fun offer(observer: AppSdpObserver) {
        val constraints = MediaConstraints().apply {
            /*mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))*/
        }

        signalingType = SignalingType.OFFER

        peerConnection?.createOffer(object : AppSdpObserver() {
            override fun onCreateSuccess(desc: SessionDescription?) {
                super.onCreateSuccess(desc)

                if (desc == null) {
                    return
                }

                // send signaling
                signaling.waitAnswer(object : SignalingResponse {
                    override fun onReceive(type: SignalingType, desc: String) {
                        peerConnection?.setRemoteDescription(
                            observer,
                            Gson().fromJson(desc, SessionDescription::class.java)
                        )
                    }

                    override fun onError(type: SignalingType, desc: String) {

                    }
                })

                /** set local description */
                peerConnection?.setLocalDescription(AppSdpObserver(), desc)

                signaling.sendOffer(Gson().toJson(desc))
            }

        }, constraints)

    }

    fun answer(observer: AppSdpObserver) {
        val constraints = MediaConstraints().apply {
            /*mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))*/
        }

        signalingType = SignalingType.ANSWER

        signaling.waitOffer(object : SignalingResponse {
            override fun onReceive(type: SignalingType, desc: String) {
                peerConnection?.setRemoteDescription(
                    observer,
                    Gson().fromJson(
                        desc,
                        SessionDescription::class.java
                    )
                )

                peerConnection?.createAnswer(object : AppSdpObserver() {
                    override fun onCreateSuccess(desc: SessionDescription?) {
                        super.onCreateSuccess(desc)

                        if (desc == null)
                            return

                        /** set local description */
                        peerConnection?.setLocalDescription(AppSdpObserver(), desc)

                        // send signaling
                        signaling.sendAnswer(Gson().toJson(desc))
                    }

                }, constraints)

            }

            override fun onError(type: SignalingType, desc: String) {

            }

        })
    }


    fun close() {
        peerConnection?.close()
        dataChannel?.close()
        signaling.close()
    }


}