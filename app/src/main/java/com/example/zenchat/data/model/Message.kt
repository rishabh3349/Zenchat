package com.example.zenchat.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("message")
    @Expose
    var message: String? = null,
    
    @SerializedName("time")
    @Expose
    var time: String? = null,
    
    @SerializedName("senderId", alternate = ["sender_id"])
    @Expose
    var senderId: String? = null,
)