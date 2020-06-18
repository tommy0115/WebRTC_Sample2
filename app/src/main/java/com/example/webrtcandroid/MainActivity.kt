package com.example.webrtcandroid

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main2.*
import org.webrtc.*
import java.nio.ByteBuffer
import java.nio.charset.Charset


class MainActivity : AppCompatActivity() {

    /*stun.l.google.com:19302
        stun1.l.google.com:19302
        stun2.l.google.com:19302
        stun3.l.google.com:19302
        stun4.l.google.com:19302*/

    companion object {
        const val REQUEST_CODE = 120
    }

    private val iceServer = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
            .createIceServer()
    )

    private val eglBase: EglBase by lazy { EglBase.create() }
    private val database = FirebaseDatabase.getInstance()
    private val peerConnectionFactory by lazy { buildPeerConnectionFactory() }
    private var peerConnection: PeerConnection? = null
    private var dataChannel: DataChannel? = null

    private fun initializePeerConnectionFactory(context: Context) {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
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

    private fun buildPeerConnection(observer: PeerConnection.Observer) =
        peerConnectionFactory.createPeerConnection(
            iceServer,
            observer
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        setContentView(R.layout.activity_main2)
        initializePeerConnectionFactory(this)

        textMessage.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        val data = ByteBuffer.wrap(textMessage.text.toString().toByteArray())
                        dataChannel?.send(DataChannel.Buffer(data, false))
                        return true
                    }
                }
                return false
            }

        });

        // signaling server 는 firebase 로 구현해보자.

        waiting.setOnClickListener {
            val myRef = database.getReference("rooms")
            myRef.child("offer").addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    if (!dataSnapshot.exists())
                        return

                    peerConnection = buildPeerConnection(object : PeerConnectionObserver() {
                        override fun onIceCandidate(p0: IceCandidate?) {
                            super.onIceCandidate(p0)
                            val myRef = database.getReference("rooms")
                            myRef.child("Callee_IceCandidate").setValue(Gson().toJson(p0))
                            peerConnection?.addIceCandidate(p0)



                            dataChannel =
                                peerConnection?.createDataChannel("chatting", DataChannel.Init())
                                    ?.apply {
                                        registerObserver(object : DataChannel.Observer {
                                            override fun onMessage(p0: DataChannel.Buffer?) {
                                                if (p0 != null) {
                                                    val message = byteBufferToString(
                                                        p0.data,
                                                        Charset.defaultCharset()
                                                    )
                                                    runOnUiThread {
                                                        Toast.makeText(
                                                            this@MainActivity,
                                                            message,
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            }

                                            override fun onBufferedAmountChange(p0: Long) {

                                            }

                                            override fun onStateChange() {
                                                Log.d("datachannel", "onStateChange: " + dataChannel?.state().toString());
                                            }

                                        })
                                    }
                        }

                        override fun onAddStream(p0: MediaStream?) {
                            super.onAddStream(p0)
                            //p0?.videoTracks?.get(0)?.addSink(remote_view)
                        }
                    })

                    peerConnection?.setRemoteDescription(
                        object : SdpObserver {
                            override fun onSetFailure(p0: String?) {

                            }

                            override fun onSetSuccess() {

                            }

                            override fun onCreateSuccess(p0: SessionDescription?) {

                            }

                            override fun onCreateFailure(p0: String?) {

                            }

                        },
                        Gson().fromJson(
                            dataSnapshot.value.toString(),
                            SessionDescription::class.java
                        )
                    )

                    val constraints = MediaConstraints().apply {
                        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
                    }

                    peerConnection?.createAnswer(object : SdpObserver {
                        override fun onSetFailure(p0: String?) {

                        }

                        override fun onSetSuccess() {

                        }

                        override fun onCreateSuccess(p0: SessionDescription?) {
                            peerConnection?.setLocalDescription(object : SdpObserver {
                                override fun onSetFailure(p0: String?) {
                                }

                                override fun onSetSuccess() {
                                }

                                override fun onCreateSuccess(p0: SessionDescription?) {

                                }

                                override fun onCreateFailure(p0: String?) {
                                }
                            }, p0)

                            database.getReference("rooms").child("answer")
                                .setValue(Gson().toJson(p0))
                        }

                        override fun onCreateFailure(p0: String?) {

                        }
                    }, constraints)
                }
            })
        }

        calling.setOnClickListener {
            val projectionManager =
                getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_CODE)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                peerConnection = buildPeerConnection(object : PeerConnectionObserver() {

                    override fun onDataChannel(p0: DataChannel?) {
                        Log.e("DataChannel", "onDataChannel")
                    }

                    override fun onIceCandidate(p0: IceCandidate?) {
                        super.onIceCandidate(p0)
                        val myRef = database.getReference("rooms")
                        myRef.child("Caller_IceCandidate").setValue(Gson().toJson(p0))
                        peerConnection?.addIceCandidate(p0)

                        dataChannel =
                            peerConnection?.createDataChannel("chatting", DataChannel.Init())
                                ?.apply {
                                    registerObserver(object : DataChannel.Observer {
                                        override fun onMessage(p0: DataChannel.Buffer?) {
                                            if (p0 != null) {
                                                val message = byteBufferToString(
                                                    p0.data,
                                                    Charset.defaultCharset()
                                                )
                                                runOnUiThread {
                                                    Toast.makeText(
                                                        this@MainActivity,
                                                        message,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }

                                        override fun onBufferedAmountChange(p0: Long) {

                                        }

                                        override fun onStateChange() {
                                            Log.d("datachannel", "onStateChange: " + dataChannel?.state().toString());
                                        }

                                    })
                                }
                    }

                    override fun onAddStream(p0: MediaStream?) {
                        super.onAddStream(p0)
                        p0?.videoTracks?.get(0)?.addSink(remote_view)
                    }
                }
                )

                val constraints = MediaConstraints().apply {
                    mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
                }

                peerConnection?.createOffer(object : AppSdpObserver() {
                    override fun onCreateSuccess(desc: SessionDescription?) {

                        peerConnection?.setLocalDescription(object : SdpObserver {
                            override fun onSetFailure(p0: String?) {

                            }

                            override fun onSetSuccess() {

                            }

                            override fun onCreateSuccess(p0: SessionDescription?) {

                            }

                            override fun onCreateFailure(p0: String?) {

                            }
                        }, desc)

                        val myRef = database.getReference("rooms")
                        myRef.child("offer").setValue(Gson().toJson(desc))
                    }
                }, constraints)

                database.getReference("rooms").child("answer")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            if (!p0.exists())
                                return

                            peerConnection?.setRemoteDescription(object : SdpObserver {
                                override fun onSetFailure(p0: String?) {

                                }

                                override fun onSetSuccess() {

                                }

                                override fun onCreateSuccess(p0: SessionDescription?) {

                                }

                                override fun onCreateFailure(p0: String?) {

                                }

                            }, Gson().fromJson(p0.value.toString(), SessionDescription::class.java))


                        }
                    })

            } else {
                Toast.makeText(this, "취소되었습니다", Toast.LENGTH_SHORT).show()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
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
}