package com.example.uas_toko.chat

import android.os.Message
import com.google.firebase.firestore.Exclude

data class Chat( val username: String? = null,
                val message: String? = null,
                val time: String? = null) {

    @Exclude
    fun getMap(): Map<String, Any?> {
        return mapOf(
            "username" to username,
            "message" to message,
            "time" to time
        )
    }
}


