package com.example.webrtcandroid

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }


    data class IceData(
        var sdp: String = "",
        var sdpMLineIndex: String = "",
        var sdpMid: String = "",
        var serverUrl: String = ""
    ) {
        override fun toString(): String {
            return "IceData(sdp='$sdp', sdpMLineIndex='$sdpMLineIndex', sdpMid='$sdpMid', serverUrl='$serverUrl')"
        }
    }

    @Test
    fun getParsing() {

        val sdp =
            "\"{\"sdp\":\"candidate:3607764933 1 udp 2122197247 2001:e60:1023:5b6d:0:4:8ef3:1f01 53861 typ host generation 0 ufrag cz+5 network-id 4 network-cost 900\",\"sdpMLineIndex\":0,\"sdpMid\":\"data\",\"serverUrl\":\"\"}{\"sdp\":\"candidate:559267639 1 udp 2122071295 ::1 39373 typ host generation 0 ufrag cz+5 network-id 2\",\"sdpMLineIndex\":0,\"sdpMid\":\"data\",\"serverUrl\":\"\"}{\"sdp\":\"candidate:1510613869 1 udp 2121998079 127.0.0.1 50711 typ host generation 0 ufrag cz+5 network-id 1\",\"sdpMLineIndex\":0,\"sdpMid\":\"data\",\"serverUrl\":\"\"}{\"sdp\":\"candidate:3041853582 1 tcp 1518283007 2001:e60:6009:e422:0:3f:e22d:f501 9 typ host tcptype active generation 0 ufrag cz+5 network-id 3 network-cost 900\",\"sdpMLineIndex\":0,\"sdpMid\":\"data\",\"serverUrl\":\"\"}{\"sdp\":\"candidate:3865011119 1 tcp 1518149375 192.0.0.4 9 typ host tcptype active generation 0 ufrag cz+5 network-id 5 network-cost 900\",\"sdpMLineIndex\":0,\"sdpMid\":\"data\",\"serverUrl\":\"\"}{\"sdp\":\"candidate:1876313031 1 tcp 1518091519 ::1 41497 typ host tcptype passive generation 0 ufrag cz+5 network-id 2\",\"sdpMLineIndex\":0,\"sdpMid\":\"data\",\"serverUrl\":\"\"}{\"sdp\":\"candidate:344579997 1 tcp 1518018303 127.0.0.1 44061 typ host tcptype passive generation 0 ufrag cz+5 network-id 1\",\"sdpMLineIndex\":0,\"sdpMid\":\"data\",\"serverUrl\":\"\"}\""
        val conv = sdp.replace("\"", "")

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
            println(it)
        }


        println("stop")
        /*println(result!!.value)

        result!!.next()

        println(result!!.value)*/

        /* result?.groupValues?.forEach {
             println(it)
         }*/


        /* var match = result?.next()


        *//* while (match != null){
            println(match.value)
            match = result?.next()
        }*//*

        */
        /*val groupValues = result?.groupValues
        groupValues?.forEach {
            println(it)
        }*/


    }
}