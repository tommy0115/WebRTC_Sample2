package com.example.webrtcandroid.signaling

enum class SignalingType(val typeName : String){
    NONE("none"), OFFER("offer"), ANSWER("answer");

    fun getOperationName() : String = when(this){
        ANSWER -> {
            "Callee"
        }

        OFFER ->{
            "Caller"
        }

        NONE ->{
            ""
        }
    }
}
