package com.example.webrtcandroid.signaling

import com.google.firebase.database.*
import org.webrtc.SdpObserver

class FirebaseRealTimeDbSignaling : Signaling {

    private val database = FirebaseDatabase.getInstance()
    private var standByOffer: DatabaseReference? = null

    override fun standBy(type: SignalingType, desc: String, signalingResponse: SignalingResponse) {

        when (type) {
            SignalingType.OFFER -> {
                database.getReference("rooms").child(type.typeName).setValue(desc)

                standByOffer = database.getReference("rooms").child(SignalingType.ANSWER.typeName)
                standByOffer?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (!p0.exists())
                            return

                        signalingResponse.receive(SignalingType.ANSWER, p0.value.toString())
                    }
                })
            }

            SignalingType.ANSWER -> {
                database.getReference("rooms").child(type.typeName).setValue(desc)
            }
        }
    }


    override fun close() {

    }

}