package com.example.webrtcandroid.signaling

import com.example.webrtcandroid.signaling.SignalingType.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import org.webrtc.IceCandidate

class FirebaseRealTimeDbSignaling : Signaling {

    private val database = FirebaseDatabase.getInstance()
    private val ref = database.getReference("rooms")

    override fun sendOffer(desc: String) {
        ref.child("offer").setValue(desc)
    }

    override fun sendAnswer(desc: String) {
        ref.child("answer").setValue(desc)
    }

    override fun sendIceCandidate(type: SignalingType, p0: String) {
        ref.child("${type.getOperationName()}_IceCandidate").setValue(p0)
    }

    override fun waitIceCandidate(type: SignalingType, signalingResponse: SignalingResponse){
        val wait = ref.child("${type.getOperationName()}_IceCandidate")
        wait.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                wait.removeEventListener(this)
                signalingResponse.onError(type, p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(!p0.exists()){
                    return
                }

                wait.removeEventListener(this)
                signalingResponse.onReceive(type, p0.value.toString())
            }
        })
    }

    override fun waitOffer(response: SignalingResponse) {
        val offer = ref.child("offer")
        offer.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                offer.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot)
            {
                if (!p0.exists()){
                    return
                }
                offer.removeEventListener(this)

                p0.value.toString().let {
                    if (it.isNullOrEmpty()){
                        response.onError(OFFER, "Null or Empty String")
                        return
                    }

                    response.onReceive(OFFER, it)
                }
            }
        })
    }

    override fun waitAnswer(response: SignalingResponse) {
        val offer = ref.child("answer")
        offer.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                offer.removeEventListener(this)
            }

            override fun onDataChange(p0: DataSnapshot)
            {
                if (!p0.exists()){
                    return
                }
                offer.removeEventListener(this)

                p0.value.toString().let {
                    if (it.isNullOrEmpty()){
                        response.onError(ANSWER, "Null or Empty String")
                        return
                    }

                    response.onReceive(ANSWER, it)
                }
            }
        })
    }

    override fun onError(desc: String) {

    }

    override fun close() {
        ref.removeValue()
    }
}