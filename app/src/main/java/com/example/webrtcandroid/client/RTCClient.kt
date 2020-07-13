package com.example.webrtcandroid.client

import android.content.Context
import android.util.Log
import com.example.webrtcandroid.signaling.*
import com.google.gson.Gson
import org.webrtc.*
import java.nio.ByteBuffer
import java.nio.charset.Charset

class RTCClient(val context: Context, val localView : SurfaceViewRenderer,  val remoteView : SurfaceViewRenderer) {

    init {
        initializePeerConnectionFactory(context)
    }

    companion object {
        private const val LOCAL_TRACK_ID = "local_track"
        private const val LOCAL_STREAM_ID = "local_track"
    }

    private val eglBase: EglBase by lazy { EglBase.create() }
    private val videoCapturer by lazy { getVideoCapturer(context) }

    private var videoTrack: VideoTrack? = null
    private var audioTrack: AudioTrack? = null

    private val iceServer = listOf(
        PeerConnection.IceServer.builder(mutableListOf<String>().apply {
            add("stun:stun.l.google.com:19302")
            add("stun:stun1.l.google.com:19302")
            add("stun:stun2.l.google.com:19302")
            add("stun:stun3.l.google.com:19302")
            add("stun:stun4.l.google.com:19302")
            add("stun:numb.viagenie.ca:3478")
        }).createIceServer()
    )

    private var signalingType: SignalingType = SignalingType.NONE

    private val peerConnectionFactory by lazy { buildPeerConnectionFactory() }
    private val peerConnection by lazy {
        buildPeerConnection(object : PeerConnectionObserver() {
            var iceCandidate : String = ""
            override fun onIceCandidate(p0: IceCandidate?) {
                super.onIceCandidate(p0)

                iceCandidate += Gson().toJson(p0!!)

                val signalingResponse = object : SignalingResponse {
                    override fun onReceive(type: SignalingType, desc: String) {

                        val conv = desc.replace("\"", "")

                        //val reg = Regex(pattern = """[{].*?[}]""")
                        val reg = Regex(pattern = "[{].*?[}]")

                        val matchedResults = reg.findAll(conv)

                        val result = mutableListOf<IceData>()
                        for (matchedText in matchedResults) {
                            val base = matchedText.value.replace("{", "").replace("}", "")
                            val iceData = IceData()
                            base.split(",").forEachIndexed { index, s ->
                                val data = s.replaceBefore(":", "").drop(1)

                                when (index) {
                                    0 -> {
                                        iceData.sdp = data
                                    }
                                    1 -> {
                                        iceData.sdpMLineIndex = data
                                    }
                                    2 -> {
                                        iceData.sdpMid = data
                                    }
                                    3 -> {
                                        iceData.serverUrl = data
                                    }
                                }
                            }
                            result.add(iceData)
                        }

                        result.forEach {
                            addIceCandidate(it.toIceCandidate())
                        }

                    }

                    override fun onError(type: SignalingType, desc: String) {
                        Log.e("iceCandidate", "$desc")
                    }

                    //"{\"sdp\":\"candidate:842163049 1 udp 1685921535 39.7.230.82 47673 typ srflx raddr 192.0.0.2 rport 47673 generation 0 ufrag 2r/7 network-id 4 network-cost 900\",\"sdpMLineIndex\":0,\"sdpMid\":\"data\",\"serverUrl\":\"stun:74.125.23.127:19302\"}"

                }

                if (signalingType == SignalingType.ANSWER) {
                    signaling.waitIceCandidate(SignalingType.OFFER, signalingResponse)
                } else {
                    signaling.waitIceCandidate(SignalingType.ANSWER, signalingResponse)
                }

            }

            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
                super.onIceGatheringChange(p0)

                if (p0 == PeerConnection.IceGatheringState.COMPLETE){
                    sendIceCandidate(iceCandidate)
                }
            }

            override fun onAddStream(p0: MediaStream?) {
                super.onAddStream(p0)
                p0?.videoTracks?.get(0)?.addSink(remoteView)
            }

            override fun onDataChannel(p0: DataChannel?) {
                super.onDataChannel(p0)
                dataChannel = p0
            }

            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
                super.onIceConnectionChange(p0)
                if (p0 == PeerConnection.IceGatheringState.COMPLETE){

                }
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

    fun initSurfaceView(view: SurfaceViewRenderer) = view.run {
        setMirror(true)
        setEnableHardwareScaler(true)
        init(eglBase.eglBaseContext, null)
    }

    private fun buildPeerConnection(observer: PeerConnection.Observer) =
        peerConnectionFactory.createPeerConnection(
            iceServer,
            observer
        )?.apply {

            initSurfaceView(localView)
            initSurfaceView(remoteView)

            val localVideoSource: VideoSource = peerConnectionFactory.createVideoSource(false)
            val surfaceTextureHelper = SurfaceTextureHelper.create(Thread.currentThread().name, eglBase.eglBaseContext)
            (videoCapturer as VideoCapturer).initialize(surfaceTextureHelper, context, localVideoSource.capturerObserver)
            videoCapturer.startCapture(640, 240, 30)
            val localVideoTrack = peerConnectionFactory.createVideoTrack(LOCAL_TRACK_ID, localVideoSource)
            localVideoTrack.addSink(localView)
            val localStream = peerConnectionFactory.createLocalMediaStream(LOCAL_STREAM_ID)
            localStream.addTrack(localVideoTrack)
            addStream(localStream)
        }


    private fun buildPeerConnectionFactory(): PeerConnectionFactory {
        return PeerConnectionFactory.builder().setOptions(PeerConnectionFactory.Options())
            .setVideoEncoderFactory(
                DefaultVideoEncoderFactory(
                    eglBase.eglBaseContext, true, true
                )
            )
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))
            .createPeerConnectionFactory()
    }

    fun addIceCandidate(iceCandidate: IceCandidate) {
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

    fun sendIceCandidate(p0: String) {
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

    private fun getVideoCapturer(context: Context) =
        Camera2Enumerator(context).run {
            deviceNames.find {
                isFrontFacing(it)
            }?.let {
                createCapturer(it, null)
            } ?: throw IllegalStateException()
        }


}