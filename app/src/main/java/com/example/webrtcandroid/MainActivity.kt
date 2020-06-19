package com.example.webrtcandroid

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.webrtcandroid.client.ChatResponse
import com.example.webrtcandroid.client.PeerConnectionObserver
import com.example.webrtcandroid.client.RTCClient
import com.example.webrtcandroid.signaling.AppSdpObserver
import kotlinx.android.synthetic.main.activity_main2.*
import org.webrtc.*
import java.nio.ByteBuffer


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

    lateinit var rtcClient : RTCClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )*/

        setContentView(R.layout.activity_main2)

        rtcClient = RTCClient(this)


        rtcClient.createDataChannel("Chat", object :
            ChatResponse {
            override fun onMessage(message: String, state: ChatResponse.ChatState) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        })


        textMessage.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        //val data = ByteBuffer.wrap(textMessage.text.toString().toByteArray())
                        /*dataChannel?.send(DataChannel.Buffer(data, false))*/
                        rtcClient.sendMessage(textMessage.text.toString())
                        return true
                    }
                }
                return false
            }

        });


        waiting.setOnClickListener {
            rtcClient.offer(object : AppSdpObserver(){

            })
        }

        calling.setOnClickListener {
            rtcClient.answer(object : AppSdpObserver(){

            })

            /*val projectionManager =
                getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_CODE)*/
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        rtcClient.close()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

}