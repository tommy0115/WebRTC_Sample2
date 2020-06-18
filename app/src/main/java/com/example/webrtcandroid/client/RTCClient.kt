package com.example.webrtcandroid.client

import android.content.Context
import com.example.webrtcandroid.ChatResponse
import com.example.webrtcandroid.PeerConnectionObserver
import com.example.webrtcandroid.signaling.FirebaseRealTimeDbSignaling
import com.example.webrtcandroid.signaling.Signaling
import org.webrtc.*
import java.nio.ByteBuffer
import java.nio.charset.Charset

abstract class RTCClient(context: Context, peerConnectionObserver: PeerConnectionObserver) {

    init {
        initializePeerConnectionFactory(context)
    }

    private val eglBase: EglBase by lazy { EglBase.create() }

    private val iceServer = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
            .createIceServer()
    )

    protected val peerConnectionFactory by lazy { buildPeerConnectionFactory() }
    protected val peerConnection by lazy { buildPeerConnection(peerConnectionObserver) }
    protected var dataChannel: DataChannel? = null
    protected val signaling : Signaling by lazy { FirebaseRealTimeDbSignaling() }

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

            }
        })
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

    fun close() {
        peerConnection?.close()
        dataChannel?.close()
        signaling.close()
    }


}